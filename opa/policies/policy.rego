package billing.authz

import future.keywords.in
import future.keywords.if

default decision := {"allow": false, "reason": "Denied by default"}

allowed_purposes := {"BILLING", "ANALYTICS"}

token_uri := opa.runtime().env.OPA_KEYCLOAK_TOKEN_URI
client_id := opa.runtime().env.OPA_CLIENT_ID
client_secret := opa.runtime().env.OPA_CLIENT_SECRET

token_response := http.send({
    "method": "POST",
    "url": token_uri,
    "headers": {
        "Content-Type": "application/x-www-form-urlencoded"
    },
    "raw_body": concat("", [
        "grant_type=client_credentials",
        "&client_id=", client_id,
        "&client_secret=", client_secret
    ]),
    "timeout": "5s",
    "raise_error": false,
    "force_cache": true,
    "force_cache_duration_seconds": 240
})

access_token := token_response.body.access_token

# ──────────────────────────────────────
# BATCH REQUEST via PIP data
# ──────────────────────────────────────
is_batch_request if {
    count(input.dataSubjectIds) > 0
    input.pipData != null
    count(input.pipData) > 0
}

authorized_titulars := {r.titularId |
    r := input.pipData[_]
    r.authorized == true
}

decisions := [d |
    r := input.pipData[_]
    d := {
        "titularId": r.titularId,
        "allow": r.authorized,
        "reason": r.reason
    }
]

decision := {"allow": true, "reason": "Batch partial success", "decisions": decisions} if {
    is_batch_request
    count(authorized_titulars) > 0
    "BILLING_READ" in input.caller.roles
    input.purpose in allowed_purposes
}

decision := {"allow": false, "reason": "All titulars denied in batch", "decisions": decisions} if {
    is_batch_request
    count(authorized_titulars) == 0
    "BILLING_READ" in input.caller.roles
    input.purpose in allowed_purposes
}

# ──────────────────────────────────────
# SINGLE REQUEST via consent-query-service
# ──────────────────────────────────────
consent_response := http.send({
    "method": "GET",
    "url": concat("", [
        "http://consent-query-service:8080/api/v1/consent/",
        input.dataSubjectId
    ]),
    "headers": {
        "Authorization": concat(" ", ["Bearer", access_token]),
        "X-Purpose": input.purpose,
        "X-Data-Categories": concat(",", input.dataCategories),
        "X-Data-Subject-Id": input.dataSubjectId
    },
    "timeout": "3s",
    "raise_error": false
}) if { not is_batch_request }

consent_data := consent_response.body.authorizations if { not is_batch_request }

decision_has_active_consent if {
    not is_batch_request
    token_response.status_code == 200
    consent_response.status_code == 200
    some auth in consent_data
    auth.purpose == input.purpose
    auth.dataCategory == "FINANCIAL_DATA"
    auth.status == "GRANTED"
}

decision := {"allow": true, "reason": "Access granted"} if {
    not is_batch_request
    "BILLING_READ" in input.caller.roles
    input.purpose in allowed_purposes
    input.dataSubjectId != null
    input.dataSubjectId != ""
    input.resource == "BILLING_RECORD"
    input.action == "READ"
    "FINANCIAL_DATA" in input.dataCategories
    decision_has_active_consent
}

# ──────────────────────────────────────
# DENIAL REASONS (single request)
# ──────────────────────────────────────
decision := {"allow": false, "reason": "Caller does not have BILLING_READ role"} if {
    not is_batch_request
    not ("BILLING_READ" in input.caller.roles)
}

decision := {"allow": false, "reason": "Purpose not allowed"} if {
    not is_batch_request
    "BILLING_READ" in input.caller.roles
    not input.purpose in allowed_purposes
}

decision := {"allow": false, "reason": "Active consent not found for FINANCIAL_DATA"} if {
    not is_batch_request
    "BILLING_READ" in input.caller.roles
    input.purpose in allowed_purposes
    input.dataSubjectId != null
    input.dataSubjectId != ""
    input.resource == "BILLING_RECORD"
    input.action == "READ"
    "FINANCIAL_DATA" in input.dataCategories
    not decision_has_active_consent
}
