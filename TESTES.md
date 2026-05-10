# Testes — billing-service

## Build

```bash
# Instalar dependência local primeiro
cd ~/Documents/TCC/PROJETO/tcc-security-starter
mvn clean install -DskipTests

# Build billing-service
cd ~/Documents/TCC/PROJETO/billing-service
mvn clean package -DskipTests
```

## Subir dependências

```bash
# Rede
docker network create consent-net

# Subir infra do Módulo 2 (postgres-user, postgres-billing, keycloak, OPA)
cd ~/Documents/TCC/PROJETO/infra
docker compose -f docker-compose.module2.yml up -d

# Verificar health
docker compose -f docker-compose.module2.yml ps
```

## Subir user-service

```bash
cd ~/Documents/TCC/PROJETO/user-service/user-service
mvn spring-boot:run
```

## Subir billing-service

```bash
cd ~/Documents/TCC/PROJETO/billing-service
mvn spring-boot:run
```

## Testar endpoints

### 1. Criar usuário no user-service

```bash
curl -s -X POST http://localhost:8083/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "João Silva",
    "email": "joao@example.com",
    "documentNumber": "12345678900",
    "birthDate": "1990-01-01"
  }'
```

### 2. Obter token do billing-service no Keycloak

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/tcc/protocol/openid-connect/token \
  -u "billing-service:billing-service-secret" \
  -d "grant_type=client_credentials" \
  | jq -r '.access_token')
echo $TOKEN
```

### 3. Criar registro de cobrança

```bash
curl -s -X POST http://localhost:8084/api/v1/billing \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "dataSubjectId": "1",
    "description": "Análise de crédito",
    "amount": 150.00
  }'
```

Esperado: `201 Created`

### 4. Buscar registro por id

```bash
curl -s http://localhost:8084/api/v1/billing/1 \
  -H "Authorization: Bearer $TOKEN"
```

Esperado: `200 OK`

### 5. Buscar registro + perfil do titular (fluxo M2M)

```bash
curl -s http://localhost:8084/api/v1/billing/1/with-profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Purpose: BILLING_ANALYSIS" \
  -H "X-Data-Subject-Id: 1" \
  -H "X-Correlation-Id: $(uuidgen)"
```

Esperado: `200 OK` com `billingRecord` + `userProfile`

### 6. Sem token (401)

```bash
curl -s -w "\nHTTP %{http_code}\n" http://localhost:8084/api/v1/billing/1
```

Esperado: `401 Unauthorized`

## Fluxo M2M (detalhado)

O fluxo do endpoint `GET /billing/{id}/with-profile`:

```
Cliente externo
  |  GET /api/v1/billing/1/with-profile
  |  Authorization: Bearer <token-externo>
  |  X-Purpose: BILLING_ANALYSIS
  |  X-Data-Subject-Id: 1
  v
BillingController
  |  @RequiresConsent (tcc-security-starter, opa desabilitado)
  v
BillingApplicationService.findWithProfile()
  |
  |  1. Busca BillingRecord local (id=1)
  |  2. Chama UserServiceClient.fetchUserProfile()
  |     |
  |     a. OAuth2AuthorizedClientManager → token client_credentials
  |     b. GET http://localhost:8083/api/v1/users/1
  |        Authorization: Bearer <token-billing>
  |        X-Purpose: BILLING_ANALYSIS
  |        X-Data-Subject-Id: 1
  |     c. User Service → ConsentAuthorizationAspect → OPA (se habilitado)
  v
Retorna { billingRecord, userProfile }
```
