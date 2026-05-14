---
description: Load these instructions when working on the backend REST API migration of the Spring PetClinic project (Java/Spring Boot code under backend/).
applyTo: "backend/**"
---

## Contexto do projeto

O novo backend é uma aplicação Spring Boot **independente**, localizada inteiramente na pasta `backend/` do repositório. Ela expõe uma API REST em `/api` para ser consumida por um frontend Next.js. O monólito original em `src/` não deve ser modificado.

- **Localização:** toda criação e edição de código deve ocorrer dentro de `backend/`.
- **Stack:** Spring Boot `4.0.3`, Java `17`, Spring Data JPA, Bean Validation, Actuator, Cache, SpringDoc OpenAPI (Swagger).
- **Banco:** H2 (único banco suportado nesta fase).
- **Domínio:** `Owner`, `Pet`, `Visit`, `PetType`, `Vet`, `Specialty`.
- **Repositório base do domínio:** recriar os repositories JPA necessários dentro de `backend/`; não importar nem depender de código de `src/`.

---

## Estrutura do projeto `backend/`

A aplicação é um projeto Maven/Gradle padrão Spring Boot dentro de `backend/`. O código fonte fica em `backend/src/main/java/org/springframework/samples/petclinic/`.

```
backend/
  src/
    main/
      java/org/springframework/samples/petclinic/
        PetClinicApiApplication.java        → classe main da app
        api/
          owner/   → OwnerController, OwnerService, OwnerRepository, DTOs de owner
          pet/     → PetController, PetService, DTOs de pet e pet-type
          visit/   → VisitController, VisitService, DTOs de visit
          vet/     → VetController, VetService, VetRepository, DTOs de vet
        infrastructure/
          exception/ → handler global de erros (@RestControllerAdvice), exceptions customizadas
          logging/   → configuração e utilitários de log
      resources/
        application.properties              → datasource H2, JPA, Swagger
        db/h2/schema.sql
        db/h2/data.sql
    test/
      java/org/springframework/samples/petclinic/
        ...
  pom.xml (ou build.gradle)
```

Cada módulo dentro de `api/` é autossuficiente: contém seu controller, service, repository e DTOs. Não criar pacotes horizontais (`controllers/`, `services/`, `dtos/`) separados do domínio.

---

## Documentação Swagger (obrigatória)

Todo endpoint criado deve ser documentado com SpringDoc OpenAPI. Sem documentação, o endpoint não está pronto.

- Adicionar `springdoc-openapi-starter-webmvc-ui` como dependência.
- A UI do Swagger deve estar disponível em `/swagger-ui.html`.
- Anotar cada controller com `@Tag(name = "...", description = "...")`.
- Anotar cada método com `@Operation(summary = "...")`.
- Anotar os parâmetros de request com `@Parameter` quando necessário.
- Anotar os possíveis retornos com `@ApiResponse` (ao menos 200, 404 e 400 onde aplicável).
- Anotar os DTOs de request com `@Schema` nos campos para descrever formato e restrições.

Exemplo mínimo:

```java
@Tag(name = "Owners", description = "Gerenciamento de tutores")
@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    @Operation(summary = "Busca tutores pelo sobrenome")
    @ApiResponse(responseCode = "200", description = "Lista paginada de tutores")
    @GetMapping
    public PageResponse<OwnerResponse> findAll(...) { ... }
}
```

---

## Contrato REST

Prefixo base: `/api`. Paginação **zero-based** (alinhada ao Spring Data).

### Owners

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `GET`  | `/api/owners?lastName=&page=0&size=5` | Lista paginada de owners |
| `GET`  | `/api/owners/{ownerId}` | Detalhe do owner com pets e visits |
| `POST` | `/api/owners` | Cria owner |
| `PUT`  | `/api/owners/{ownerId}` | Atualiza owner |

### Pet Types

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `GET`  | `/api/pet-types` | Tipos de pets ordenados por nome |

### Pets

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/api/owners/{ownerId}/pets` | Cria pet para um owner |
| `PUT`  | `/api/owners/{ownerId}/pets/{petId}` | Atualiza pet |

### Visits

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/api/owners/{ownerId}/pets/{petId}/visits` | Cria visit para um pet |

### Vets

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `GET`  | `/api/vets?page=0&size=5` | Lista paginada de veterinários |

---

## DTOs

Nunca expor entities JPA diretamente na API. Usar records ou classes de DTO explícitos.

### OwnerResponse

```json
{
  "id": 1,
  "firstName": "George",
  "lastName": "Franklin",
  "address": "110 W. Liberty St.",
  "city": "Madison",
  "telephone": "6085551023",
  "pets": []
}
```

### PetResponse

```json
{
  "id": 1,
  "name": "Leo",
  "birthDate": "2010-09-07",
  "type": { "id": 1, "name": "cat" },
  "visits": []
}
```

### VisitResponse

```json
{
  "id": 1,
  "date": "2026-05-14",
  "description": "rabies shot"
}
```

### VetResponse

```json
{
  "id": 1,
  "firstName": "James",
  "lastName": "Carter",
  "specialties": [{ "id": 1, "name": "radiology" }]
}
```

### PageResponse (wrapper genérico)

```json
{
  "content": [],
  "page": 0,
  "size": 5,
  "totalElements": 10,
  "totalPages": 2
}
```

### ErrorResponse (erros padronizados)

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": [
    { "field": "telephone", "message": "must match 10 digits" }
  ]
}
```

---

## Regras de negócio a extrair para a camada `service`

Estas regras estão hoje acopladas aos controllers MVC ou aos fluxos Thymeleaf e devem ser movidas para services:

1. Buscar owner por id ou lançar `404`.
2. Validar pet duplicado pelo mesmo nome dentro do mesmo owner.
3. Validar `birthDate` de pet não pode ser futura.
4. Adicionar visit a um pet existente.

---

## Tratamento de erros

- Criar um `@RestControllerAdvice` em `infrastructure/exception/` que produza `ErrorResponse` JSON.
- **Toda exception deve retornar uma mensagem amigável em português**, nunca a mensagem técnica interna da exception.
- As mensagens devem ser orientadas ao contexto da operação, por exemplo:
  - `"Tutor não encontrado"` — owner não existe.
  - `"Pet não encontrado"` — pet não existe.
  - `"Veterinário não encontrado"` — vet não existe.
  - `"Houve um erro ao criar o tutor"` — falha ao salvar owner.
  - `"Houve um erro ao criar o pet"` — falha ao salvar pet.
  - `"Houve um erro ao registrar a consulta"` — falha ao salvar visit.
  - `"Algum erro ocorreu, tente novamente em instantes"` — fallback genérico para qualquer exception não mapeada.
- Mapear `ConstraintViolationException` e `MethodArgumentNotValidException` para `VALIDATION_ERROR` (HTTP 400) com os `fieldErrors` populados. Cada `fieldError` deve indicar o nome exato do campo (`field`) e a mensagem amigável (`message`).
- Mapear exceptions de entidade não encontrada para HTTP 404.
- Mapear exceptions de regra de negócio (ex: nome de pet duplicado, data futura) para HTTP 400.
- Nunca retornar stack traces, mensagens de exception Java ou detalhes técnicos na resposta.
- Logar o erro completo (com stack trace) em `infrastructure/logging/` antes de devolver a resposta amigável.

---

## CORS

Configurar CORS para permitir o dev server do Next.js (por padrão `http://localhost:3000`) durante o desenvolvimento. Usar `@CrossOrigin` nos controllers REST ou uma configuração central via `WebMvcConfigurer`.

---

## Testes

- Todos os testes devem estar dentro de `backend/src/test/`.
- Testar controllers REST com `MockMvc` (não subir servidor completo).
- Cobrir ao menos: retorno 200 de listagem, retorno 404 para owner inexistente, retorno 400 para payload inválido.
- Nos testes de retorno 400, verificar que a resposta contém `fieldErrors` com o campo (`field`) e a mensagem (`message`) corretos.

---

## Restrições e fora de escopo

Não fazer dentro desta janela de trabalho:

- Modificar qualquer arquivo fora de `backend/` — o monólito em `src/` não deve ser tocado.
- Suportar bancos além do H2.
- Implementar autenticação ou autorização.
- Servir templates Thymeleaf ou assets estáticos no novo backend.
- Refatoração profunda do domínio além do necessário para a API REST.
- Deploy separado ou containerização.

---

## Prioridades de execução (backend)

1. Criar DTOs (records) para todas as respostas, com `@Schema` para documentação.
2. Criar controllers REST em `/api` por módulo de domínio, com anotações Swagger obrigatórias.
3. Extrair as regras críticas listadas acima para services dentro de cada módulo.
4. Criar o handler de erro global em `infrastructure/exception/` com mensagens amigáveis em português.
5. Configurar logging em `infrastructure/logging/`.
6. Configurar CORS para o dev server Next.js.
7. Escrever testes básicos de contrato com `MockMvc`.