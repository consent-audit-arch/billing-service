# billing-service

Cenário de cobrança que consulta dados pessoais mediante autorização contextual.

Parte do **Consent & Audit Architecture**, projeto pessoal de Renan Rohers iniciado no TCC sobre controle de acesso e auditabilidade em microsserviços.

[Visão geral e execução local](https://github.com/consent-audit-arch/infra#readme) · [Diagramas e decisões](https://github.com/consent-audit-arch/infra/blob/main/docs/architecture.md) · [Console](https://github.com/consent-audit-arch/consent-frontend)

## Papel na arquitetura

Obtém tokens M2M no Keycloak e propaga o contexto de finalidade e correlação para consultar o user-service. A negativa de consentimento recebida do provedor é propagada como HTTP 403. Cobrança é um cenário técnico do protótipo, sem pressupor que consentimento seja a base aplicável a toda cobrança real.

## Tecnologias

Java 21 · Spring Boot · Maven. Integrações deste componente: PostgreSQL, Keycloak, OPA, Kafka, user-service.

## Executar e desenvolver

O [guia central](https://github.com/consent-audit-arch/infra#executar-localmente) configura bancos, mensageria, identidade, imagens e sidecars OPA. No Compose, este serviço fica em `http://localhost:8084`.

Para compilar e executar os testes a partir da raiz deste repositório, instale primeiro o starter no Maven local (os dois repositórios devem estar lado a lado):

```bash
mvn -f ../tcc-security-starter/pom.xml install
mvn verify
```

Os testes de integração podem exigir Docker e dependências externas; consulte as configurações em `src/main/resources`. Para executar o conjunto integrado, use o Compose central.

## API principal

```text
POST /api/v1/billing
GET /api/v1/billing/{id}
GET /api/v1/billing/{id}/with-profile
GET /api/v1/billing/users/{userId}/billing/{billingId}/with-profile
POST /api/v1/billing/batch
```

A autenticação usa JWT do realm `tcc`. Nos endpoints protegidos pelo PEP, o contexto pode incluir `X-Purpose`, `X-Data-Subject-Id`, `X-Data-Subject-Ids`, `X-Data-Categories` e `X-Correlation-Id`, conforme a operação e a policy. A [coleção Postman](https://github.com/consent-audit-arch/infra/blob/main/postman_collection.json) reúne exemplos.

## Escopo

Protótipo para estudo de autorização contextual e rastreabilidade com dados de demonstração. A cobertura de políticas varia por serviço. Os [limites e próximos passos](https://github.com/consent-audit-arch/infra/blob/main/docs/architecture.md#limites-e-evolução) descrevem as garantias e os pontos ainda em evolução.
