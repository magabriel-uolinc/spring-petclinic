# Relatório: migração Spring PetClinic para Backend REST Java + Frontend Next.js

## Contexto encontrado na codebase

O projeto atual é um monólito Spring Boot com renderização server-side via Spring MVC + Thymeleaf.

- **Stack backend atual:** Spring Boot `4.0.3`, Java `17`, Spring MVC, Spring Data JPA, Bean Validation, Actuator, Cache.
- **UI atual:** templates Thymeleaf em `src/main/resources/templates`, assets em `src/main/resources/static`, Bootstrap/Font Awesome via WebJars.
- **Banco:** H2 por padrão, com suporte existente para MySQL e PostgreSQL via profiles.
- **Domínio principal:** `Owner`, `Pet`, `Visit`, `PetType`, `Vet`, `Specialty`.
- **Persistência:** `OwnerRepository`, `PetTypeRepository`, `VetRepository`.
- **Controllers MVC atuais:**
  - `OwnerController`: busca, criação, edição e detalhe de owners.
  - `PetController`: criação/edição de pets dentro de owners.
  - `VisitController`: criação de visits dentro de pets.
  - `VetController`: lista de vets em HTML e um endpoint JSON legado em `/vets`.
  - `WelcomeController` e `CrashController`: home e página de erro/demo.

Hoje a regra de negócio está parcialmente acoplada aos controllers MVC e aos fluxos de formulário Thymeleaf, por exemplo:

- busca de owner por id;
- validação de pet duplicado por nome dentro do mesmo owner;
- validação de `birthDate` futura;
- inclusão de visit em um pet;
- paginação visual one-based na UI atual.

## Restrição principal

Front e back serão feitos por equipes diferentes em paralelo, com janela de cerca de 2 horas.

Isso muda a prioridade: **não vale começar por uma refatoração profunda**. O caminho mais seguro é criar rapidamente um contrato REST mínimo para desbloquear as duas equipes.

## Recomendação

Adotar uma abordagem **contract-first leve**:

1. Definir o contrato REST mínimo nos primeiros 20-30 minutos.
2. Backend implementa a API em cima do domínio e repositories existentes.
3. Frontend Next.js começa em paralelo usando fixtures/mocks baseados no contrato.
4. Integração final troca mocks pela API real usando uma variável como `NEXT_PUBLIC_API_BASE_URL`.

Essa abordagem maximiza paralelismo e reduz o risco de o frontend ficar bloqueado pelo backend.

## Arquitetura alvo

### Backend Java

Manter Spring Boot como uma API REST em `/api`.

Estrutura sugerida:

- `api`: controllers REST.
- `dto`: records/classes de request e response.
- `service`: regras hoje espalhadas nos controllers MVC.
- `exception`: handler JSON padronizado para erros.
- repositories e entities JPA atuais reaproveitados.

Não expor entities JPA diretamente na API. Usar DTOs para evitar ciclos, lazy/eager surprises e vazamento do modelo interno.

### Frontend Next.js

Criar um app Next.js separado, consumindo a API REST.

Estrutura sugerida:

- `app/owners`: busca, lista, detalhe, criação e edição.
- `app/vets`: lista de veterinários.
- `components`: tabelas, formulários e componentes compartilhados.
- `lib/api`: client HTTP tipado.
- `fixtures` ou `mocks`: dados locais baseados no contrato inicial.

O frontend deve conseguir rodar antes da API real estar pronta.

## Contrato REST mínimo sugerido

Usar paginação **zero-based** no contrato para alinhar com Spring Data. A UI Next.js pode converter isso para exibição one-based.

### Owners

```http
GET /api/owners?lastName=&page=0&size=5
```

Retorna lista paginada de owners.

```http
GET /api/owners/{ownerId}
```

Retorna detalhe do owner com pets e visits.

```http
POST /api/owners
```

Cria owner.

```http
PUT /api/owners/{ownerId}
```

Atualiza owner.

### Pet types

```http
GET /api/pet-types
```

Retorna tipos de pets ordenados por nome.

### Pets

```http
POST /api/owners/{ownerId}/pets
```

Cria pet para um owner.

```http
PUT /api/owners/{ownerId}/pets/{petId}
```

Atualiza pet.

### Visits

```http
POST /api/owners/{ownerId}/pets/{petId}/visits
```

Cria visit para um pet.

### Vets

```http
GET /api/vets?page=0&size=5
```

Retorna lista paginada de veterinários.

## DTOs mínimos

### Owner

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

### Pet

```json
{
  "id": 1,
  "name": "Leo",
  "birthDate": "2010-09-07",
  "type": {
    "id": 1,
    "name": "cat"
  },
  "visits": []
}
```

### Visit

```json
{
  "id": 1,
  "date": "2026-05-14",
  "description": "rabies shot"
}
```

### Vet

```json
{
  "id": 1,
  "firstName": "James",
  "lastName": "Carter",
  "specialties": [
    {
      "id": 1,
      "name": "radiology"
    }
  ]
}
```

### Página

```json
{
  "content": [],
  "page": 0,
  "size": 5,
  "totalElements": 10,
  "totalPages": 2
}
```

### Erro padrão

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": [
    {
      "field": "telephone",
      "message": "must match 10 digits"
    }
  ]
}
```

## Divisão de trabalho recomendada

### Time backend

Prioridades:

1. Criar DTOs e contrato base.
2. Criar controllers REST em `/api`.
3. Extrair regras essenciais para services:
   - buscar owner ou retornar 404;
   - validar pet duplicado por nome no owner;
   - validar pet com birth date futura;
   - adicionar visit a pet existente.
4. Criar handler de erro JSON.
5. Configurar CORS para o dev server do Next.js.
6. Testar contratos com `MockMvc`.

Não priorizar:

- reescrever entities;
- trocar banco;
- autenticação;
- deploy separado;
- remoção completa de Thymeleaf dentro da janela de 2 horas.

### Time frontend

Prioridades:

1. Criar app Next.js.
2. Criar types alinhados aos DTOs.
3. Criar client HTTP em `lib/api`.
4. Criar mock adapter/fixtures com os exemplos do contrato.
5. Implementar telas:
   - busca/lista de owners;
   - detalhe de owner com pets/visits;
   - criar/editar owner;
   - criar/editar pet;
   - adicionar visit;
   - listar vets.
6. Trocar mock por API real via `NEXT_PUBLIC_API_BASE_URL`.

Não priorizar:

- redesign visual grande;
- autenticação;
- SSR complexo;
- cache avançado;
- internacionalização completa.

## Plano de execução para 2 horas

### 0-20 min: contrato

- Fechar endpoints.
- Fechar DTOs.
- Fechar formato de erro.
- Criar fixtures JSON para frontend.
- Definir `NEXT_PUBLIC_API_BASE_URL`.

### 20-90 min: execução paralela

Backend:

- implementar endpoints principais;
- reaproveitar JPA/repositories;
- mover regras críticas para service;
- criar testes básicos de contrato.

Frontend:

- criar telas com mocks;
- montar navegação;
- implementar forms;
- preparar client para trocar base URL.

### 90-120 min: integração

- subir backend com H2;
- subir Next.js;
- ajustar CORS/proxy;
- validar fluxos principais;
- corrigir divergências de payload.

## Critério de sucesso

Ao final da janela, a equipe deve conseguir demonstrar:

- buscar owners;
- listar owners;
- ver detalhe de owner;
- criar owner;
- editar owner;
- adicionar/editar pet;
- adicionar visit;
- listar vets;
- rodar com H2 local.

Fora do escopo inicial:

- autenticação;
- autorização;
- deploy separado;
- troca de banco;
- remoção completa dos templates Thymeleaf;
- refatoração profunda do domínio.

## Riscos e mitigação

| Risco | Mitigação |
| --- | --- |
| Frontend bloquear esperando backend | Usar fixtures/mocks desde o início |
| Backend vazar entities JPA | Usar DTOs explícitos |
| Divergência de payload | Definir exemplos JSON antes de implementar |
| Validações diferentes entre front e back | Backend é fonte da verdade; frontend replica mensagens básicas |
| Paginação confusa | API zero-based; UI converte para one-based |
| Refatoração consumir tempo | Só extrair regras necessárias para API |

## Conclusão

A melhor abordagem para esse contexto é **contrato REST mínimo primeiro**. Ela permite que as equipes trabalhem em paralelo dentro da janela curta, preserva o domínio e banco atuais, e evita que a migração vire uma refatoração grande demais para o tempo disponível.

Depois da primeira entrega, a evolução natural é remover gradualmente Thymeleaf/WebJars, fortalecer OpenAPI/testes de contrato e organizar melhor a camada de service.
