# Vets Route Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the frontend-only `/vets` route with mock-backed paginated vet listing and specialty resolution.

**Architecture:** Use Next.js App Router under `frontend/src/app/vets`. Consume the shared types from `frontend/src/lib/types.ts`, then keep all vets-specific data access in `frontend/src/lib/vets-api.ts` so the vets worker does not touch owners route files. The route reads from `frontend/src/mock.json` and can run without a backend.

**Tech Stack:** Next.js `16.2.6`, React `19.2.4`, TypeScript, Tailwind CSS, existing `npm run lint` and `npm run build`.

---

## Parallelism contract

This plan owns:

- `frontend/src/lib/vets-api.ts`
- `frontend/src/app/vets/**`

The owners plan owns:

- `frontend/src/lib/types.ts`
- `frontend/src/lib/owners-api.ts`
- `frontend/src/app/owners/**`

This plan depends on `frontend/src/lib/types.ts` from the owners plan Task 1. After that shared type file exists, the vets worker can implement this plan without editing owners files.

Next.js 16 page props use promise-based `searchParams`, so the `/vets` page must `await` `searchParams`.

## File structure

- Read: `frontend/src/lib/types.ts`
  - Must already export `Page`, `Specialty`, `Vet`, `VetSpecialty`, and `VetWithSpecialties`.
- Create: `frontend/src/lib/vets-api.ts`
  - Vet-specific mock query functions.
- Create: `frontend/src/app/vets/page.tsx`
  - Paginated vets table with specialties.
- Modify: `frontend/src/app/page.tsx`
  - Add a link to `/vets` without changing owners route files.

## Task 1: Confirm shared type contract

**Files:**
- Read: `frontend/src/lib/types.ts`

- [ ] **Step 1: Verify `frontend/src/lib/types.ts` exists with vet types**

Confirm the file contains these exports:

```ts
export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type Specialty = {
  id: number;
  name: string;
};

export type Vet = {
  id: number;
  firstName: string;
  lastName: string;
};

export type VetSpecialty = {
  vetId: number;
  specialtyId: number;
};

export type VetWithSpecialties = Vet & {
  specialties: Specialty[];
};
```

- [ ] **Step 2: If the file is missing, stop and ask the integration lead to complete owners plan Task 1**

Do not create a duplicate type file under the vets route. The shared type file is the contract that keeps the two route plans compatible.

- [ ] **Step 3: Run frontend lint**

Run:

```bash
cd frontend && npm run lint
```

Expected: command exits with code `0`.

## Task 2: Vets mock data API

**Files:**
- Create: `frontend/src/lib/vets-api.ts`

- [ ] **Step 1: Create vets API module**

Create `frontend/src/lib/vets-api.ts` with this content:

```ts
import mockData from "@/mock.json";
import type { Page, Specialty, Vet, VetSpecialty, VetWithSpecialties } from "@/lib/types";

const vets: Vet[] = [...mockData.vets];
const specialties: Specialty[] = [...mockData.specialties];
const vetSpecialties: VetSpecialty[] = [...mockData.vetSpecialties];

const DEFAULT_PAGE_SIZE = 5;

function paginate<T>(items: T[], page: number, size: number): Page<T> {
  const safePage = Number.isFinite(page) && page > 0 ? page : 0;
  const safeSize = Number.isFinite(size) && size > 0 ? size : DEFAULT_PAGE_SIZE;
  const start = safePage * safeSize;
  const content = items.slice(start, start + safeSize);

  return {
    content,
    page: safePage,
    size: safeSize,
    totalElements: items.length,
    totalPages: Math.ceil(items.length / safeSize),
  };
}

function specialtiesForVet(vetId: number) {
  const specialtyIds = vetSpecialties
    .filter((relationship) => relationship.vetId === vetId)
    .map((relationship) => relationship.specialtyId);

  return specialties.filter((specialty) => specialtyIds.includes(specialty.id));
}

export function getVets({
  page = 0,
  size = DEFAULT_PAGE_SIZE,
}: {
  page?: number;
  size?: number;
} = {}): Page<VetWithSpecialties> {
  const resolvedVets = vets.map((vet) => ({
    ...vet,
    specialties: specialtiesForVet(vet.id),
  }));

  return paginate(resolvedVets, page, size);
}
```

- [ ] **Step 2: Run build to verify JSON import and aliases**

Run:

```bash
cd frontend && npm run build
```

Expected: build exits with code `0`.

- [ ] **Step 3: Commit vets API module**

Run:

```bash
git add frontend/src/lib/vets-api.ts
git commit -m "Add mock vets data API" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes only `frontend/src/lib/vets-api.ts`.

## Task 3: Vets list route

**Files:**
- Create: `frontend/src/app/vets/page.tsx`

- [ ] **Step 1: Create `/vets` page**

Create `frontend/src/app/vets/page.tsx` with this content:

```tsx
import Link from "next/link";
import { getVets } from "@/lib/vets-api";

const PAGE_SIZE = 5;

function singleValue(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

export default async function VetsPage({
  searchParams,
}: {
  searchParams: Promise<{ page?: string | string[] }>;
}) {
  const query = await searchParams;
  const page = Number(singleValue(query.page) ?? "0");
  const vets = getVets({ page, size: PAGE_SIZE });
  const previousPage = Math.max(0, vets.page - 1);
  const nextPage = Math.min(Math.max(vets.totalPages - 1, 0), vets.page + 1);

  return (
    <main className="mx-auto flex w-full max-w-5xl flex-col gap-8 px-6 py-10">
      <header>
        <Link className="text-sm font-medium text-emerald-700 hover:underline" href="/">
          Spring PetClinic
        </Link>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">Veterinarians</h1>
        <p className="mt-2 text-zinc-600">Browse veterinarians and their specialties from mock data.</p>
      </header>

      {vets.content.length === 0 ? (
        <p className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
          No veterinarians are available.
        </p>
      ) : (
        <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-50 text-zinc-700">
              <tr>
                <th className="px-4 py-3 font-semibold">Name</th>
                <th className="px-4 py-3 font-semibold">Specialties</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200">
              {vets.content.map((vet) => (
                <tr key={vet.id}>
                  <td className="px-4 py-3 font-medium">
                    {vet.firstName} {vet.lastName}
                  </td>
                  <td className="px-4 py-3 text-zinc-700">
                    {vet.specialties.length === 0
                      ? "none"
                      : vet.specialties.map((specialty) => specialty.name).join(", ")}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <nav className="flex items-center justify-between text-sm">
        <Link
          aria-disabled={vets.page === 0}
          className="rounded-md border border-zinc-300 px-3 py-2 aria-disabled:pointer-events-none aria-disabled:opacity-50"
          href={`/vets?page=${previousPage}`}
        >
          Previous
        </Link>
        <span>
          Page {vets.totalPages === 0 ? 0 : vets.page + 1} of {vets.totalPages}
        </span>
        <Link
          aria-disabled={vets.page >= vets.totalPages - 1}
          className="rounded-md border border-zinc-300 px-3 py-2 aria-disabled:pointer-events-none aria-disabled:opacity-50"
          href={`/vets?page=${nextPage}`}
        >
          Next
        </Link>
      </nav>
    </main>
  );
}
```

- [ ] **Step 2: Run lint and build**

Run:

```bash
cd frontend && npm run lint && npm run build
```

Expected: both commands exit with code `0`.

- [ ] **Step 3: Commit vets route**

Run:

```bash
git add frontend/src/app/vets/page.tsx
git commit -m "Add vets list route" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes only the vets page.

## Task 4: Add vets navigation link

**Files:**
- Modify: `frontend/src/app/page.tsx`

- [ ] **Step 1: Add `/vets` link to the root page**

If the owners plan already replaced `frontend/src/app/page.tsx`, update the grid to include the vets card exactly like this:

```tsx
import Link from "next/link";

export default function Home() {
  return (
    <main className="mx-auto flex min-h-screen w-full max-w-4xl flex-col justify-center gap-8 px-6 py-10">
      <div>
        <p className="text-sm font-medium text-emerald-700">Spring PetClinic</p>
        <h1 className="mt-2 text-4xl font-semibold tracking-tight">Frontend routes</h1>
        <p className="mt-4 max-w-2xl text-zinc-600">
          Use the mock-backed frontend routes while the backend REST API is being prepared.
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <Link className="rounded-xl border border-zinc-200 bg-white p-5 shadow-sm hover:border-emerald-600" href="/owners">
          <h2 className="text-xl font-semibold">Owners</h2>
          <p className="mt-2 text-sm text-zinc-600">Search owners and manage pets and visits.</p>
        </Link>
        <Link className="rounded-xl border border-zinc-200 bg-white p-5 shadow-sm hover:border-emerald-600" href="/vets">
          <h2 className="text-xl font-semibold">Veterinarians</h2>
          <p className="mt-2 text-sm text-zinc-600">Browse vets and their specialties.</p>
        </Link>
      </div>
    </main>
  );
}
```

- [ ] **Step 2: Run lint and build**

Run:

```bash
cd frontend && npm run lint && npm run build
```

Expected: both commands exit with code `0`.

- [ ] **Step 3: Commit vets navigation**

Run:

```bash
git add frontend/src/app/page.tsx
git commit -m "Add vets navigation link" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes only `frontend/src/app/page.tsx`.

## Task 5: Vets route verification

**Files:**
- Verify: `frontend/src/app/vets/page.tsx`
- Verify: `frontend/src/lib/vets-api.ts`

- [ ] **Step 1: Run static validation**

Run:

```bash
cd frontend && npm run lint && npm run build
```

Expected: both commands exit with code `0`.

- [ ] **Step 2: Start the frontend dev server**

Run:

```bash
cd frontend && npm run dev
```

Expected: the dev server reports a local URL, normally `http://localhost:3000`.

- [ ] **Step 3: Verify vets pages in a browser**

Open these URLs:

```text
http://localhost:3000/vets
http://localhost:3000/vets?page=1
```

Expected:

- `/vets` shows James Carter, Helen Leary, Linda Douglas, Rafael Ortega, and Henry Stevens.
- Helen Leary shows `radiology`.
- Linda Douglas shows `surgery, dentistry`.
- James Carter shows `none`.
- `/vets?page=1` shows Sharon Jenkins.

- [ ] **Step 4: Commit any verification fixes**

If files changed during verification, run:

```bash
git add frontend/src/app/vets/page.tsx frontend/src/lib/vets-api.ts frontend/src/app/page.tsx
git commit -m "Stabilize vets route flow" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes only vets-related fixes or the shared root navigation link.
