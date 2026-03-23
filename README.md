# Template API 🚀

Projeto template (boilerplate) construído com **Spring Boot 3.3** focado em segurança, boas práticas de arquitetura,
rastreabilidade e escalabilidade. Pronto para ser usado como base de novos projetos.

---

## 🛠 Tecnologias Utilizadas

- **Java 17** — records para DTOs imutáveis, pattern matching, sealed classes
- **Spring Boot 3.3**
- **Spring Security** — autenticação e autorização com JWT
- **JJWT 0.12.6** — geração e validação de tokens
- **Spring Data JPA** — persistência com suporte a soft delete e auditoria automática
- **Flyway** — migrations versionadas para produção
- **MapStruct + Lombok** — mapeamento DTO/entidade e redução de boilerplate
- **SpringDoc OpenAPI** — documentação interativa via Swagger UI (apenas dev)
- **Spring AMQP (RabbitMQ)** — mensageria opcional para eventos assíncronos
- **Spring Scheduling** — cron jobs para tarefas periódicas
- **H2 Database** — banco em memória para desenvolvimento e testes
- **Logback** — logging estruturado com correlationId via MDC

---

## 📋 Funcionalidades Implementadas

### Autenticação e Autorização

- Login e logout com JWT via cookies `HttpOnly`
- Refresh token via cookie `HttpOnly` (query parameters rejeitados por segurança)
- Blacklist de tokens em memória com limpeza agendada — logout invalida tokens imediatamente
- Rate limiting de login: 20 tentativas por minuto por IP
- Perfis de segurança distintos para `dev`, `test` e `prod`

### Gerenciamento de Usuários

- CRUD completo de usuários (somente admins)
- Usuários comuns só visualizam e editam o próprio perfil
- Resposta segmentada por papel: admins recebem `UserResponse` completo, usuários comuns recebem `UserPublicResponse` (
  userId, name, username apenas)
- Validação de unicidade de username e email
- Política de senhas forte: mínimo 10 caracteres, maiúscula, minúscula, dígito, caractere especial e bloqueio de padrões
  comuns
- Sistema de roles: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUPERVISOR`
- Soft delete com `@SQLRestriction("deleted_at IS NULL")`

### Auditoria

- Log assíncrono de ações (criação, atualização, exclusão de usuários e webhooks recebidos)
- Executor dedicado com política de rejeição logada (sem descarte silencioso)
- Endpoint paginado `/api/audit` para administradores
- Campos `createdAt`, `updatedAt`, `createdBy`, `updatedBy` em todas as entidades via JPA Auditing

### Rastreabilidade

- `CorrelationIdFilter` com prioridade máxima injeta `X-Correlation-Id` em todas as requisições via MDC
- ID gerado automaticamente se não fornecido pelo cliente
- Logback configurado por perfil: texto legível em `dev`, JSON estruturado em `prod`

### Crescimento de Usuários

- Cron diário à meia-noite captura snapshot de total de usuários
- Partial unique index garante compatibilidade com soft delete
- Endpoint `/api/users/growth` para admins

### Webhook

- Validação de assinatura HMAC-SHA256 via header `X-Signature`
- Proteção anti-replay via header `X-Timestamp` (janela de 5 minutos)
- Reencaminhamento opcional para fila RabbitMQ

### Mensageria (RabbitMQ — opcional)

- Habilitado via `app.rabbitmq.enabled=true`
- Evento `UserCreatedEvent` publicado ao criar usuário
- Evento `WebhookEvent` publicado ao receber webhook
- Listeners para processar eventos

### Abstração de Blacklist

- Interface `TokenBlacklistPort` permite trocar implementação em memória por Redis sem alterar `JwtBlacklistService`
- Implementação padrão `InMemoryTokenBlacklist` ativa via `@ConditionalOnMissingBean`

---

## 🏗 Arquitetura

```
src/main/java/.../
├── config/          # Segurança, filtros, async, CORS, rate limiting, AMQP
├── controller/      # Endpoints REST
├── domain/          # Entidades JPA
├── dto/             # Records Java 17 (imutáveis)
├── exception/       # Exceções de domínio e handler global
├── mapper/          # Interfaces MapStruct
├── repository/      # Spring Data JPA
├── security/        # Constantes de roles
└── service/         # Lógica de negócio
```

### Padrões Aplicados

- **DTO Pattern** com Java Records para imutabilidade
- **Repository Pattern** com `JpaSpecificationExecutor` para consultas dinâmicas
- **Port & Adapter** para blacklist de tokens (substituível por Redis)
- **Soft Delete** com `@SQLRestriction` em `BaseEntity`
- **JPA Auditing** automático via `AuditingEntityListener`
- **Event-Driven** com RabbitMQ (opcional e condicional)
- **Async Processing** para auditoria com `@Async`

---

## 🔐 Segurança

### JWT

- Algoritmo HMAC-SHA256, segredo mínimo de 32 caracteres
- Access token: 15 minutos
- Refresh token: 7 dias
- Armazenamento em cookies `HttpOnly`, `SameSite=Lax` (dev) / `SameSite=Strict` (prod)
- Revogação imediata via blacklist no logout

### Proteções por Ambiente

| Proteção          | Dev       | Prod               |
|-------------------|-----------|--------------------|
| HTTPS obrigatório | ✗         | ✓                  |
| CSRF              | ✗         | ✓                  |
| HSTS              | ✗         | ✓                  |
| CSP               | ✗         | ✓                  |
| Swagger UI        | ✓         | ✗                  |
| H2 Console        | ✓         | ✗                  |
| CORS              | localhost | domínio específico |

### Política de Senhas

- Mínimo 10 caracteres, máximo 128
- Deve conter: maiúscula, minúscula, dígito e caractere especial
- Bloqueia padrões comuns: `password`, `senha`, `123456`, `qwerty`
- Aplicada em criação e atualização de usuários

---

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.8+

### Variáveis de Ambiente Obrigatórias

| Variável         | Descrição                                 | Obrigatória                      |
|------------------|-------------------------------------------|----------------------------------|
| `JWT_SECRET`     | Chave de assinatura JWT (mínimo 32 chars) | ✓ sempre                         |
| `ADMIN_PASSWORD` | Senha do admin inicial                    | ✓ sempre                         |
| `WEBHOOK_SECRET` | Chave HMAC para webhooks                  | ✗ (webhook desabilitado sem ela) |

### Executando em Desenvolvimento

**PowerShell:**

```powershell
$env:JWT_SECRET="sua-chave-secreta-com-pelo-menos-32-chars"
$env:ADMIN_PASSWORD="SuaSenha1@Forte"
mvn spring-boot:run
```

**Linux/Mac:**

```bash
export JWT_SECRET="sua-chave-secreta-com-pelo-menos-32-chars"
export ADMIN_PASSWORD="SuaSenha1@Forte"
mvn spring-boot:run
```

**IntelliJ IDEA:**

1. Run → Edit Configurations → TemplateApiApplication
2. Environment variables:
    - `JWT_SECRET=sua-chave-secreta-com-pelo-menos-32-chars`
    - `ADMIN_PASSWORD=SuaSenha1@Forte`
    - `WEBHOOK_SECRET=sua-chave-webhook` (opcional)

### Acesso após subir

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **H2 Console:** http://localhost:8080/h2-console
    - JDBC URL: `jdbc:h2:mem:bolsao-db`
    - Usuário: `sa` / Senha: *(vazio)*
- **Admin criado automaticamente:** `admin` / senha definida em `ADMIN_PASSWORD`

---

## 📡 Endpoints

### Autenticação

| Método | Endpoint            | Acesso      | Descrição                                  |
|--------|---------------------|-------------|--------------------------------------------|
| POST   | `/api/auth/login`   | Público     | Login — retorna tokens via cookie HttpOnly |
| POST   | `/api/auth/refresh` | Público     | Renova access token via cookie HttpOnly    |
| GET    | `/api/auth/me`      | Autenticado | Dados do usuário logado                    |
| POST   | `/api/auth/logout`  | Autenticado | Invalida tokens e limpa cookies            |

### Usuários

| Método | Endpoint               | Acesso                  | Descrição                           |
|--------|------------------------|-------------------------|-------------------------------------|
| GET    | `/api/users`           | Admin                   | Lista paginada de usuários          |
| POST   | `/api/users`           | Admin                   | Cria usuário                        |
| GET    | `/api/users/{id}`      | Admin / próprio usuário | Detalhes do usuário                 |
| PUT    | `/api/users/{id}`      | Admin / próprio usuário | Atualiza usuário                    |
| DELETE | `/api/users/{id}`      | Admin                   | Soft delete de usuário              |
| GET    | `/api/users/{id}/full` | Admin                   | Detalhes completos                  |
| PUT    | `/api/users/{id}/full` | Admin                   | Atualização completa (inclui roles) |
| GET    | `/api/users/growth`    | Admin                   | Histórico de crescimento da base    |

> Usuários não-admin que acessam `GET /api/users/{id}` e `PUT /api/users/{id}` recebem `UserPublicResponse` (userId,
> name, username) em vez do objeto completo.

### Auditoria

| Método | Endpoint     | Acesso | Descrição                   |
|--------|--------------|--------|-----------------------------|
| GET    | `/api/audit` | Admin  | Logs de auditoria paginados |

### Health

| Método | Endpoint              | Acesso  | Descrição                 |
|--------|-----------------------|---------|---------------------------|
| GET    | `/api/health/live`    | Público | Liveness probe            |
| GET    | `/api/health/ready`   | Público | Readiness probe           |
| GET    | `/api/health/startup` | Público | Startup probe             |
| GET    | `/api/health/metrics` | Admin   | Métricas de memória e CPU |

### Webhook

| Método | Endpoint   | Acesso | Descrição               |
|--------|------------|--------|-------------------------|
| POST   | `/webhook` | HMAC   | Recebe eventos externos |

---

## 🧪 Testes

```bash
mvn test
```

### Cobertura

| Tipo                        | Classes                                                                                      |
|-----------------------------|----------------------------------------------------------------------------------------------|
| Unitários (Mockito)         | `UserServiceTest`, `AuthServiceTest`, `PasswordPolicyServiceTest`, `JwtBlacklistServiceTest` |
| Integração (SpringBootTest) | `AuthControllerIntegrationTest`, `UserControllerIntegrationTest`, `WebhookControllerTest`    |
| JPA (DataJpaTest)           | `UserGrowthServiceTest`                                                                      |

Os testes de integração usam `@ActiveProfiles("test")` que carrega `application-test.properties` com H2, Flyway
desabilitado, rate limiting desabilitado e segredos fixos.

---

## 🔧 Personalização

### Adicionando um novo módulo

1. Crie o record DTO em `dto/`
2. Crie a entidade JPA em `domain/` estendendo `BaseEntity`
3. Crie o repository em `repository/`
4. Crie o service em `service/`
5. Crie a interface MapStruct em `mapper/`
6. Crie o controller em `controller/`
7. Adicione a migration correspondente em `resources/db/migration/`

### Substituindo a blacklist por Redis

1. Adicione a dependência `spring-boot-starter-data-redis`
2. Crie um bean `redisTokenBlacklist` implementando `TokenBlacklistPort`
3. Anote com `@Bean("redisTokenBlacklist")` e `@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")`
4. O `InMemoryTokenBlacklist` será desativado automaticamente via `@ConditionalOnMissingBean`

### Adicionando Roles

1. Declare a constante em `SecurityRoles.java`
2. Crie a role no `DataLoader` se precisar que exista ao subir
3. Use `@PreAuthorize` nos endpoints

### Configurando banco de dados em produção

Em `application-prod.properties`, adicione as propriedades do seu banco (PostgreSQL recomendado):

```properties
spring.datasource.url=jdbc:postgresql://host:5432/dbname
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

---

## ⚠️ Notas Importantes

- **Blacklist em memória:** os tokens revogados são perdidos no restart da aplicação. Para múltiplas instâncias ou
  tolerância a restart, implemente `TokenBlacklistPort` com Redis.
- **Admin inicial:** criado automaticamente via `DataLoader` na primeira execução se não existir. A senha é definida
  pela variável `ADMIN_PASSWORD` — obrigatória sem fallback.
- **Webhook sem segredo:** se `WEBHOOK_SECRET` não estiver definido, o endpoint `/webhook` responde 503. Isso é
  intencional.
- **CorrelationId:** todas as requisições recebem um `X-Correlation-Id` na resposta. Passe o mesmo header na requisição
  para manter o ID em chamadas encadeadas.
- **Soft delete:** registros deletados permanecem no banco com `deleted_at` preenchido e são invisíveis nas queries via
  `@SQLRestriction`. O partial unique index em `user_growth_snapshot` garante que novas inserções para a mesma data
  funcionem mesmo com registros soft-deletados.

---

## 📄 Licença

Projeto template open-source. Use, adapte e distribua conforme necessário.