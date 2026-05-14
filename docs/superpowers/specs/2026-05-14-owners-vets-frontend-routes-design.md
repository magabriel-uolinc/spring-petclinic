# Frontend owners and vets routes design

## Problem

The frontend needs two main Next.js routes, `/owners` and `/vets`, that can be built in parallel. The backend REST API may not be ready while the frontend work is happening, so the routes must start from the existing `frontend/src/mock.json` data and keep a clean boundary for later API integration.

This spec covers only the frontend. Backend implementation, endpoint design, database changes, authentication, deployment, and removal of Thymeleaf are out of scope.

## Goals

- Implement `/owners` for the main owner workflows: search/list, detail, create/edit owner, create/edit pet, and add visit.
- Implement `/vets` for paginated vet listing with specialties.
- Use the existing `frontend/src/mock.json` as the initial data source.
- Let one person or sub-team work on owners while another works on vets after a small shared foundation is in place.
- Keep the switch from mock data to a real API localized to `src/lib`, not spread through route components.

## Current context

- The frontend is a Next.js `16.2.6` App Router application under `frontend/`.
- `frontend/src/app` currently contains the initial create-next-app page, layout, and global styles.
- `frontend/src/mock.json` already contains normalized mock records for `owners`, `pets`, `visits`, `types`, `vets`, `specialties`, and `vetSpecialties`.
- The mock data is not already shaped exactly like the UI needs. Owners need pets and visits joined by IDs; vets need specialties resolved through `vetSpecialties`.

## Recommended approach

Use a small shared foundation, then split the feature work into two independent route tracks.

1. Create shared frontend types and data access helpers in `src/lib`.
2. Read from `src/mock.json` by default.
3. Keep route-specific UI, components, and page logic under the owning route area.
4. Treat `NEXT_PUBLIC_API_BASE_URL` as the future integration boundary, but do not require a live API for the MVP.

This gives enough shared structure to prevent owners and vets from inventing incompatible models, while keeping the shared work small enough not to become a long sequential blocker.

## Route architecture

### Shared foundation

Create a small shared layer:

- `src/lib/types.ts`
  - `Owner`
  - `Pet`
  - `PetType`
  - `Visit`
  - `Vet`
  - `Specialty`
  - `Page<T>`
  - request/input types for owner, pet, and visit forms
- `src/lib/mock-data.ts`
  - imports `src/mock.json`
  - joins normalized records into UI-ready shapes
  - applies filtering and pagination in memory
- `src/lib/api.ts`
  - exports route-facing functions such as `getOwners`, `getOwner`, `createOwner`, `updateOwner`, `getVets`
  - initially delegates to `mock-data`
  - later becomes the only place that switches to `fetch` against `NEXT_PUBLIC_API_BASE_URL`

The shared layer should not contain route-specific React components.

### Owners route track

Route ownership:

- `src/app/owners/page.tsx`
- `src/app/owners/new/page.tsx`
- `src/app/owners/[ownerId]/page.tsx`
- `src/app/owners/[ownerId]/edit/page.tsx`
- `src/app/owners/[ownerId]/pets/new/page.tsx`
- `src/app/owners/[ownerId]/pets/[petId]/edit/page.tsx`
- `src/app/owners/[ownerId]/pets/[petId]/visits/new/page.tsx`
- owner-specific components under `src/app/owners/_components` or `src/components/owners`

Required behavior:

- `/owners`
  - shows a last-name search field
  - lists owners in a paginated table
  - links each owner to the detail route
  - supports empty results without crashing
- `/owners/[ownerId]`
  - shows owner contact details
  - shows that owner's pets
  - shows each pet's type and visits
  - links to edit owner, add/edit pet, and add visit flows
- owner form routes
  - validate required owner fields in the UI
  - preserve the same field names used by the mock/API model
  - return to the owner detail or owners list after a successful mock mutation
- pet and visit form routes
  - use pet types from `mock.json`
  - prevent future birth dates in the UI
  - keep duplicate pet-name validation visible in the owner track, matching the current PetClinic behavior

Mock mutations may be in-memory for the MVP. They do not need to persist across a full browser refresh unless a later implementation plan explicitly adds local storage.

### Vets route track

Route ownership:

- `src/app/vets/page.tsx`
- vet-specific components under `src/app/vets/_components` or `src/components/vets`

Required behavior:

- `/vets`
  - shows vets in a paginated table
  - resolves specialties using `vets`, `specialties`, and `vetSpecialties` from `mock.json`
  - displays a clear empty state if no vets are available
  - follows the same pagination model as `/owners`

The vets track must not depend on owners-specific components, forms, or route files.

## Data model expectations

The UI-facing model may be denormalized even though `mock.json` is normalized.

Expected owner detail shape:

```ts
type OwnerDetail = Owner & {
  pets: Array<Pet & {
    type: PetType;
    visits: Visit[];
  }>;
};
```

Expected vet shape:

```ts
type VetWithSpecialties = Vet & {
  specialties: Specialty[];
};
```

Expected page shape:

```ts
type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
```

Use zero-based page numbers in `src/lib` because that matches the expected backend contract. Route UI can display one-based page labels.

## Parallel work plan

### Shared foundation task

One developer creates only the shared contracts and adapters:

- types in `src/lib/types.ts`
- mock query helpers in `src/lib/mock-data.ts`
- public data functions in `src/lib/api.ts`
- route navigation links in the root page or shared nav, if needed

This task is intentionally small and should avoid building owners or vets UI.

### Owners track

Starts after the shared function signatures are available. The owners developer owns owners route files and owners components. They should not edit vets route files.

### Vets track

Starts after the shared function signatures are available. The vets developer owns vets route files and vets components. They should not edit owners route files.

### Integration rule

If either route track needs a shared API/type change, make the smallest compatible change in `src/lib` and keep it backwards-compatible for the other route whenever possible.

## Error, loading, and empty states

- Loading states should be route-local and simple.
- Empty owners search results should show an actionable message and keep the search form visible.
- Missing owner, pet, or vet records should render a not-found style state rather than throwing an unhandled UI error.
- Form validation errors should be displayed near the related fields.
- Backend/API failures are represented later through `src/lib/api.ts`; route components should not hard-code transport details.

## Testing and verification expectations

- Unit-test or component-test the data shaping helpers if the project test setup exists when implementation starts.
- Verify `/owners` search/list pagination against `mock.json`.
- Verify owner detail joins owners, pets, pet types, and visits correctly.
- Verify owner, pet, and visit forms enforce required fields and route back to the intended page after successful mock mutation.
- Verify `/vets` resolves specialties correctly and paginates consistently with owners.
- Run the existing frontend validation commands, especially `npm run lint` and `npm run build`, after implementation.

## Non-goals

- No backend REST implementation.
- No authentication or authorization.
- No database persistence.
- No major visual redesign.
- No advanced cache strategy.
- No internationalization pass.
- No requirement for mock mutations to survive a browser refresh.

## Acceptance criteria

- `/owners` and `/vets` exist as top-level frontend routes.
- Both routes run against `frontend/src/mock.json` without requiring a backend.
- Owners supports search/list, detail, owner create/edit, pet create/edit, and visit creation.
- Vets supports paginated listing with specialties.
- Owners and vets work can proceed in parallel after the shared `src/lib` foundation is created.
- Route-specific work is isolated enough that owners and vets developers do not normally edit the same files.
- The future API integration point is centralized in `src/lib/api.ts`.
