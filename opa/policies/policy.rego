package billing.authz

import future.keywords.in
import future.keywords.if

default decision := {"allow": false, "reason": "Denied by default"}

allowed_callers := {"billing-service"}
allowed_purposes := {"BILLING_ANALYSIS", "FRAUD_PREVENTION"}

decision := {"allow": true, "reason": "Access granted"} if {
    input.caller.clientId in allowed_callers
    input.purpose in allowed_purposes
    input.dataSubjectId != null
    input.dataSubjectId != ""
    input.resource == "BILLING_RECORD"
    input.action == "READ"
}

decision := {"allow": false, "reason": "Caller not authorized"} if {
    not input.caller.clientId in allowed_callers
}

decision := {"allow": false, "reason": "Purpose not allowed"} if {
    input.caller.clientId in allowed_callers
    not input.purpose in allowed_purposes
}

decision := {"allow": false, "reason": "Missing dataSubjectId"} if {
    input.caller.clientId in allowed_callers
    input.purpose in allowed_purposes
    input.dataSubjectId == null
}

decision := {"allow": false, "reason": "Empty dataSubjectId"} if {
    input.caller.clientId in allowed_callers
    input.purpose in allowed_purposes
    input.dataSubjectId == ""
}
