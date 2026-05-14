# Owners Route Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the frontend-only `/owners` route family with mock-backed owner search, detail, owner forms, pet forms, and visit creation.

**Architecture:** Use Next.js App Router pages under `frontend/src/app/owners`. Create the shared frontend types first, then keep owners data access in `frontend/src/lib/owners-api.ts` so the owners worker does not touch vets route files. The implementation reads from `frontend/src/mock.json` and uses in-memory mock mutations for the current runtime only.

**Tech Stack:** Next.js `16.2.6`, React `19.2.4`, TypeScript, Tailwind CSS, existing `npm run lint` and `npm run build`.

---

## Parallelism contract

This plan owns:

- `frontend/src/lib/types.ts`
- `frontend/src/lib/owners-api.ts`
- `frontend/src/app/owners/**`

The vets plan owns:

- `frontend/src/lib/vets-api.ts`
- `frontend/src/app/vets/**`

Both plans may read `frontend/src/mock.json`. Only this plan creates `frontend/src/lib/types.ts`; the vets worker should consume it without editing it unless both workers agree on a contract change.

Next.js 16 page props use promise-based `params` and `searchParams`, so every page that reads them must `await` those props.

## File structure

- Create: `frontend/src/lib/types.ts`
  - Shared domain and page types for owners and vets.
- Create: `frontend/src/lib/owners-api.ts`
  - Owner-specific mock query and mutation functions.
- Create: `frontend/src/app/owners/page.tsx`
  - Search and paginated owners list.
- Create: `frontend/src/app/owners/new/page.tsx`
  - Create owner form.
- Create: `frontend/src/app/owners/[ownerId]/page.tsx`
  - Owner detail with pets and visits.
- Create: `frontend/src/app/owners/[ownerId]/edit/page.tsx`
  - Edit owner form.
- Create: `frontend/src/app/owners/[ownerId]/pets/new/page.tsx`
  - Create pet form.
- Create: `frontend/src/app/owners/[ownerId]/pets/[petId]/edit/page.tsx`
  - Edit pet form.
- Create: `frontend/src/app/owners/[ownerId]/pets/[petId]/visits/new/page.tsx`
  - Add visit form.
- Create: `frontend/src/app/owners/not-found.tsx`
  - Owners-specific not-found UI.
- Modify: `frontend/src/app/page.tsx`
  - Add a link to `/owners` without depending on `/vets`.

## Task 1: Shared types for owners and vets

**Files:**
- Create: `frontend/src/lib/types.ts`

- [ ] **Step 1: Create shared domain types**

Create `frontend/src/lib/types.ts` with this content:

```ts
export type Page<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type Owner = {
  id: number;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  telephone: string;
};

export type PetType = {
  id: number;
  name: string;
};

export type Pet = {
  id: number;
  name: string;
  birthDate: string;
  typeId: number;
  ownerId: number;
};

export type Visit = {
  id: number;
  petId: number;
  date: string;
  description: string;
};

export type PetDetail = Pet & {
  type: PetType;
  visits: Visit[];
};

export type OwnerDetail = Owner & {
  pets: PetDetail[];
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

export type OwnerInput = Omit<Owner, "id">;

export type PetInput = {
  name: string;
  birthDate: string;
  typeId: number;
};

export type VisitInput = {
  date: string;
  description: string;
};
```

- [ ] **Step 2: Run frontend lint**

Run:

```bash
cd frontend && npm run lint
```

Expected: command exits with code `0`.

- [ ] **Step 3: Commit shared types**

Run:

```bash
git add frontend/src/lib/types.ts
git commit -m "Add shared frontend domain types" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit succeeds and only `frontend/src/lib/types.ts` is included.

## Task 2: Owners mock data API

**Files:**
- Create: `frontend/src/lib/owners-api.ts`

- [ ] **Step 1: Create owners API module**

Create `frontend/src/lib/owners-api.ts` with this content:

```ts
import mockData from "@/mock.json";
import type {
  Owner,
  OwnerDetail,
  OwnerInput,
  Page,
  Pet,
  PetDetail,
  PetInput,
  PetType,
  Visit,
  VisitInput,
} from "@/lib/types";

const owners: Owner[] = [...mockData.owners];
const pets: Pet[] = [...mockData.pets];
const visits: Visit[] = [...mockData.visits];
const petTypes: PetType[] = [...mockData.types];

const DEFAULT_PAGE_SIZE = 5;

function nextId(items: Array<{ id: number }>) {
  return Math.max(0, ...items.map((item) => item.id)) + 1;
}

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

function required(value: FormDataEntryValue | null) {
  return String(value ?? "").trim();
}

function findPetType(typeId: number) {
  return petTypes.find((type) => type.id === typeId);
}

function toPetDetail(pet: Pet): PetDetail {
  const type = findPetType(pet.typeId);

  if (!type) {
    throw new Error(`Pet type ${pet.typeId} was not found`);
  }

  return {
    ...pet,
    type,
    visits: visits.filter((visit) => visit.petId === pet.id),
  };
}

export function getOwners({
  lastName = "",
  page = 0,
  size = DEFAULT_PAGE_SIZE,
}: {
  lastName?: string;
  page?: number;
  size?: number;
} = {}) {
  const normalizedLastName = lastName.trim().toLowerCase();
  const filteredOwners = normalizedLastName
    ? owners.filter((owner) =>
        owner.lastName.toLowerCase().startsWith(normalizedLastName),
      )
    : owners;

  return paginate(filteredOwners, page, size);
}

export function getOwner(ownerId: number): OwnerDetail | undefined {
  const owner = owners.find((item) => item.id === ownerId);

  if (!owner) {
    return undefined;
  }

  return {
    ...owner,
    pets: pets
      .filter((pet) => pet.ownerId === owner.id)
      .map((pet) => toPetDetail(pet)),
  };
}

export function getPetTypes() {
  return [...petTypes].sort((left, right) => left.name.localeCompare(right.name));
}

export function getPet(ownerId: number, petId: number) {
  return pets.find((pet) => pet.ownerId === ownerId && pet.id === petId);
}

export function createOwner(input: OwnerInput) {
  const owner = {
    ...input,
    id: nextId(owners),
  };

  owners.push(owner);
  return owner;
}

export function updateOwner(ownerId: number, input: OwnerInput) {
  const index = owners.findIndex((owner) => owner.id === ownerId);

  if (index === -1) {
    return undefined;
  }

  owners[index] = {
    id: ownerId,
    ...input,
  };

  return owners[index];
}

export function createPet(ownerId: number, input: PetInput) {
  const owner = getOwner(ownerId);

  if (!owner) {
    return undefined;
  }

  const pet = {
    ...input,
    id: nextId(pets),
    ownerId,
  };

  pets.push(pet);
  return pet;
}

export function updatePet(ownerId: number, petId: number, input: PetInput) {
  const index = pets.findIndex((pet) => pet.ownerId === ownerId && pet.id === petId);

  if (index === -1) {
    return undefined;
  }

  pets[index] = {
    ...pets[index],
    ...input,
    id: petId,
    ownerId,
  };

  return pets[index];
}

export function createVisit(ownerId: number, petId: number, input: VisitInput) {
  const pet = getPet(ownerId, petId);

  if (!pet) {
    return undefined;
  }

  const visit = {
    ...input,
    id: nextId(visits),
    petId,
  };

  visits.push(visit);
  return visit;
}

export function ownerInputFromForm(formData: FormData): OwnerInput {
  return {
    firstName: required(formData.get("firstName")),
    lastName: required(formData.get("lastName")),
    address: required(formData.get("address")),
    city: required(formData.get("city")),
    telephone: required(formData.get("telephone")),
  };
}

export function petInputFromForm(formData: FormData): PetInput {
  return {
    name: required(formData.get("name")),
    birthDate: required(formData.get("birthDate")),
    typeId: Number(required(formData.get("typeId"))),
  };
}

export function visitInputFromForm(formData: FormData): VisitInput {
  return {
    date: required(formData.get("date")),
    description: required(formData.get("description")),
  };
}

export function hasDuplicatePetName(ownerId: number, name: string, currentPetId?: number) {
  const normalizedName = name.trim().toLowerCase();

  return pets.some(
    (pet) =>
      pet.ownerId === ownerId &&
      pet.id !== currentPetId &&
      pet.name.toLowerCase() === normalizedName,
  );
}
```

- [ ] **Step 2: Run build to verify JSON import and aliases**

Run:

```bash
cd frontend && npm run build
```

Expected: build exits with code `0`.

- [ ] **Step 3: Commit owners API module**

Run:

```bash
git add frontend/src/lib/owners-api.ts
git commit -m "Add mock owners data API" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes only `frontend/src/lib/owners-api.ts`.

## Task 3: Owners list route

**Files:**
- Create: `frontend/src/app/owners/page.tsx`
- Modify: `frontend/src/app/page.tsx`

- [ ] **Step 1: Create `/owners` list page**

Create `frontend/src/app/owners/page.tsx` with this content:

```tsx
import Link from "next/link";
import { getOwners } from "@/lib/owners-api";

const PAGE_SIZE = 5;

function singleValue(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value;
}

export default async function OwnersPage({
  searchParams,
}: {
  searchParams: Promise<{ lastName?: string | string[]; page?: string | string[] }>;
}) {
  const query = await searchParams;
  const lastName = singleValue(query.lastName) ?? "";
  const page = Number(singleValue(query.page) ?? "0");
  const owners = getOwners({ lastName, page, size: PAGE_SIZE });
  const previousPage = Math.max(0, owners.page - 1);
  const nextPage = Math.min(Math.max(owners.totalPages - 1, 0), owners.page + 1);

  return (
    <main className="mx-auto flex w-full max-w-5xl flex-col gap-8 px-6 py-10">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm font-medium text-emerald-700">Owners</p>
          <h1 className="text-3xl font-semibold tracking-tight">Find Owners</h1>
        </div>
        <Link
          className="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800"
          href="/owners/new"
        >
          Add owner
        </Link>
      </header>

      <form className="rounded-lg border border-zinc-200 bg-white p-4 shadow-sm">
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Last name
          <input
            className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950"
            defaultValue={lastName}
            name="lastName"
            placeholder="Search by last name"
          />
        </label>
        <button
          className="mt-4 rounded-md bg-zinc-950 px-4 py-2 text-sm font-semibold text-white hover:bg-zinc-800"
          type="submit"
        >
          Search
        </button>
      </form>

      {owners.content.length === 0 ? (
        <p className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
          No owners found. Try a different last name or add a new owner.
        </p>
      ) : (
        <div className="overflow-hidden rounded-lg border border-zinc-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="bg-zinc-50 text-zinc-700">
              <tr>
                <th className="px-4 py-3 font-semibold">Name</th>
                <th className="px-4 py-3 font-semibold">Address</th>
                <th className="px-4 py-3 font-semibold">City</th>
                <th className="px-4 py-3 font-semibold">Telephone</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200">
              {owners.content.map((owner) => (
                <tr key={owner.id}>
                  <td className="px-4 py-3">
                    <Link className="font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}`}>
                      {owner.firstName} {owner.lastName}
                    </Link>
                  </td>
                  <td className="px-4 py-3 text-zinc-700">{owner.address}</td>
                  <td className="px-4 py-3 text-zinc-700">{owner.city}</td>
                  <td className="px-4 py-3 text-zinc-700">{owner.telephone}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <nav className="flex items-center justify-between text-sm">
        <Link
          aria-disabled={owners.page === 0}
          className="rounded-md border border-zinc-300 px-3 py-2 aria-disabled:pointer-events-none aria-disabled:opacity-50"
          href={`/owners?lastName=${encodeURIComponent(lastName)}&page=${previousPage}`}
        >
          Previous
        </Link>
        <span>
          Page {owners.totalPages === 0 ? 0 : owners.page + 1} of {owners.totalPages}
        </span>
        <Link
          aria-disabled={owners.page >= owners.totalPages - 1}
          className="rounded-md border border-zinc-300 px-3 py-2 aria-disabled:pointer-events-none aria-disabled:opacity-50"
          href={`/owners?lastName=${encodeURIComponent(lastName)}&page=${nextPage}`}
        >
          Next
        </Link>
      </nav>
    </main>
  );
}
```

- [ ] **Step 2: Replace the root page with a simple navigation page**

Replace `frontend/src/app/page.tsx` with this content:

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
      </div>
    </main>
  );
}
```

- [ ] **Step 3: Run lint and build**

Run:

```bash
cd frontend && npm run lint && npm run build
```

Expected: both commands exit with code `0`.

- [ ] **Step 4: Commit owners list route**

Run:

```bash
git add frontend/src/app/page.tsx frontend/src/app/owners/page.tsx
git commit -m "Add owners list route" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes only the root page and owners list page.

## Task 4: Owner detail route

**Files:**
- Create: `frontend/src/app/owners/[ownerId]/page.tsx`
- Create: `frontend/src/app/owners/not-found.tsx`

- [ ] **Step 1: Create owners not-found UI**

Create `frontend/src/app/owners/not-found.tsx` with this content:

```tsx
import Link from "next/link";

export default function OwnersNotFound() {
  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-4 px-6 py-10">
      <p className="text-sm font-medium text-emerald-700">Owners</p>
      <h1 className="text-3xl font-semibold tracking-tight">Owner not found</h1>
      <p className="text-zinc-600">The requested owner, pet, or visit target does not exist in the mock data.</p>
      <Link className="font-medium text-emerald-700 hover:underline" href="/owners">
        Back to owners
      </Link>
    </main>
  );
}
```

- [ ] **Step 2: Create owner detail page**

Create `frontend/src/app/owners/[ownerId]/page.tsx` with this content:

```tsx
import Link from "next/link";
import { notFound } from "next/navigation";
import { getOwner } from "@/lib/owners-api";

export default async function OwnerDetailPage({
  params,
}: {
  params: Promise<{ ownerId: string }>;
}) {
  const { ownerId } = await params;
  const owner = getOwner(Number(ownerId));

  if (!owner) {
    notFound();
  }

  return (
    <main className="mx-auto flex w-full max-w-5xl flex-col gap-8 px-6 py-10">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <Link className="text-sm font-medium text-emerald-700 hover:underline" href="/owners">
            Owners
          </Link>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight">
            {owner.firstName} {owner.lastName}
          </h1>
        </div>
        <Link
          className="rounded-md border border-zinc-300 px-4 py-2 text-sm font-semibold hover:border-emerald-700"
          href={`/owners/${owner.id}/edit`}
        >
          Edit owner
        </Link>
      </header>

      <section className="rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <h2 className="text-lg font-semibold">Owner Information</h2>
        <dl className="mt-4 grid gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-sm text-zinc-500">Address</dt>
            <dd className="font-medium">{owner.address}</dd>
          </div>
          <div>
            <dt className="text-sm text-zinc-500">City</dt>
            <dd className="font-medium">{owner.city}</dd>
          </div>
          <div>
            <dt className="text-sm text-zinc-500">Telephone</dt>
            <dd className="font-medium">{owner.telephone}</dd>
          </div>
        </dl>
      </section>

      <section className="flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">Pets and Visits</h2>
          <Link className="text-sm font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}/pets/new`}>
            Add pet
          </Link>
        </div>

        {owner.pets.length === 0 ? (
          <p className="rounded-lg border border-zinc-200 bg-white p-4 text-sm text-zinc-600">No pets yet.</p>
        ) : (
          <div className="grid gap-4">
            {owner.pets.map((pet) => (
              <article className="rounded-lg border border-zinc-200 bg-white p-5 shadow-sm" key={pet.id}>
                <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <h3 className="text-lg font-semibold">{pet.name}</h3>
                    <p className="text-sm text-zinc-600">
                      {pet.type.name} · born {pet.birthDate}
                    </p>
                  </div>
                  <div className="flex gap-3 text-sm">
                    <Link className="font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}/pets/${pet.id}/edit`}>
                      Edit pet
                    </Link>
                    <Link className="font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}/pets/${pet.id}/visits/new`}>
                      Add visit
                    </Link>
                  </div>
                </div>

                <div className="mt-4">
                  <h4 className="text-sm font-semibold text-zinc-700">Visits</h4>
                  {pet.visits.length === 0 ? (
                    <p className="mt-2 text-sm text-zinc-500">No visits recorded.</p>
                  ) : (
                    <ul className="mt-2 divide-y divide-zinc-200 text-sm">
                      {pet.visits.map((visit) => (
                        <li className="py-2" key={visit.id}>
                          <span className="font-medium">{visit.date}</span> · {visit.description}
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
```

- [ ] **Step 3: Run lint and build**

Run:

```bash
cd frontend && npm run lint && npm run build
```

Expected: both commands exit with code `0`.

- [ ] **Step 4: Commit owner detail route**

Run:

```bash
git add frontend/src/app/owners/not-found.tsx frontend/src/app/owners/[ownerId]/page.tsx
git commit -m "Add owner detail route" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes the owner detail page and owners not-found page.

## Task 5: Owner create and edit forms

**Files:**
- Create: `frontend/src/app/owners/new/page.tsx`
- Create: `frontend/src/app/owners/[ownerId]/edit/page.tsx`

- [ ] **Step 1: Create new owner page**

Create `frontend/src/app/owners/new/page.tsx` with this content:

```tsx
import Link from "next/link";
import { redirect } from "next/navigation";
import { createOwner, ownerInputFromForm } from "@/lib/owners-api";

async function createOwnerAction(formData: FormData) {
  "use server";

  const owner = createOwner(ownerInputFromForm(formData));
  redirect(`/owners/${owner.id}`);
}

export default function NewOwnerPage() {
  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 px-6 py-10">
      <header>
        <Link className="text-sm font-medium text-emerald-700 hover:underline" href="/owners">
          Owners
        </Link>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">New Owner</h1>
      </header>

      <form action={createOwnerAction} className="grid gap-4 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        {["firstName", "lastName", "address", "city", "telephone"].map((name) => (
          <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700" key={name}>
            {name}
            <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name={name} required />
          </label>
        ))}
        <button className="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800" type="submit">
          Save owner
        </button>
      </form>
    </main>
  );
}
```

- [ ] **Step 2: Create edit owner page**

Create `frontend/src/app/owners/[ownerId]/edit/page.tsx` with this content:

```tsx
import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { getOwner, ownerInputFromForm, updateOwner } from "@/lib/owners-api";

async function updateOwnerAction(ownerId: number, formData: FormData) {
  "use server";

  const owner = updateOwner(ownerId, ownerInputFromForm(formData));

  if (!owner) {
    notFound();
  }

  redirect(`/owners/${owner.id}`);
}

export default async function EditOwnerPage({
  params,
}: {
  params: Promise<{ ownerId: string }>;
}) {
  const { ownerId } = await params;
  const owner = getOwner(Number(ownerId));

  if (!owner) {
    notFound();
  }

  const action = updateOwnerAction.bind(null, owner.id);

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 px-6 py-10">
      <header>
        <Link className="text-sm font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}`}>
          {owner.firstName} {owner.lastName}
        </Link>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">Edit Owner</h1>
      </header>

      <form action={action} className="grid gap-4 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        {[
          ["firstName", owner.firstName],
          ["lastName", owner.lastName],
          ["address", owner.address],
          ["city", owner.city],
          ["telephone", owner.telephone],
        ].map(([name, value]) => (
          <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700" key={name}>
            {name}
            <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" defaultValue={value} name={name} required />
          </label>
        ))}
        <button className="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800" type="submit">
          Save owner
        </button>
      </form>
    </main>
  );
}
```

- [ ] **Step 3: Run lint and build**

Run:

```bash
cd frontend && npm run lint && npm run build
```

Expected: both commands exit with code `0`.

- [ ] **Step 4: Commit owner forms**

Run:

```bash
git add frontend/src/app/owners/new/page.tsx frontend/src/app/owners/[ownerId]/edit/page.tsx
git commit -m "Add owner form routes" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes the two owner form pages.

## Task 6: Pet create and edit forms

**Files:**
- Create: `frontend/src/app/owners/[ownerId]/pets/new/page.tsx`
- Create: `frontend/src/app/owners/[ownerId]/pets/[petId]/edit/page.tsx`

- [ ] **Step 1: Create new pet page**

Create `frontend/src/app/owners/[ownerId]/pets/new/page.tsx` with this content:

```tsx
import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { createPet, getOwner, getPetTypes, hasDuplicatePetName, petInputFromForm } from "@/lib/owners-api";

async function createPetAction(ownerId: number, formData: FormData) {
  "use server";

  const input = petInputFromForm(formData);

  if (hasDuplicatePetName(ownerId, input.name)) {
    redirect(`/owners/${ownerId}/pets/new?error=duplicate`);
  }

  if (input.birthDate > new Date().toISOString().slice(0, 10)) {
    redirect(`/owners/${ownerId}/pets/new?error=futureBirthDate`);
  }

  const pet = createPet(ownerId, input);

  if (!pet) {
    notFound();
  }

  redirect(`/owners/${ownerId}`);
}

export default async function NewPetPage({
  params,
  searchParams,
}: {
  params: Promise<{ ownerId: string }>;
  searchParams: Promise<{ error?: string }>;
}) {
  const { ownerId } = await params;
  const { error } = await searchParams;
  const owner = getOwner(Number(ownerId));

  if (!owner) {
    notFound();
  }

  const action = createPetAction.bind(null, owner.id);
  const types = getPetTypes();

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 px-6 py-10">
      <header>
        <Link className="text-sm font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}`}>
          {owner.firstName} {owner.lastName}
        </Link>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">New Pet</h1>
      </header>

      {error === "duplicate" ? <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">A pet with that name already exists for this owner.</p> : null}
      {error === "futureBirthDate" ? <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">Birth date cannot be in the future.</p> : null}

      <form action={action} className="grid gap-4 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Name
          <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name="name" required />
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Birth date
          <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name="birthDate" required type="date" />
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Type
          <select className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name="typeId" required>
            {types.map((type) => (
              <option key={type.id} value={type.id}>
                {type.name}
              </option>
            ))}
          </select>
        </label>
        <button className="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800" type="submit">
          Save pet
        </button>
      </form>
    </main>
  );
}
```

- [ ] **Step 2: Create edit pet page**

Create `frontend/src/app/owners/[ownerId]/pets/[petId]/edit/page.tsx` with this content:

```tsx
import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { getOwner, getPet, getPetTypes, hasDuplicatePetName, petInputFromForm, updatePet } from "@/lib/owners-api";

async function updatePetAction(ownerId: number, petId: number, formData: FormData) {
  "use server";

  const input = petInputFromForm(formData);

  if (hasDuplicatePetName(ownerId, input.name, petId)) {
    redirect(`/owners/${ownerId}/pets/${petId}/edit?error=duplicate`);
  }

  if (input.birthDate > new Date().toISOString().slice(0, 10)) {
    redirect(`/owners/${ownerId}/pets/${petId}/edit?error=futureBirthDate`);
  }

  const pet = updatePet(ownerId, petId, input);

  if (!pet) {
    notFound();
  }

  redirect(`/owners/${ownerId}`);
}

export default async function EditPetPage({
  params,
  searchParams,
}: {
  params: Promise<{ ownerId: string; petId: string }>;
  searchParams: Promise<{ error?: string }>;
}) {
  const { ownerId, petId } = await params;
  const { error } = await searchParams;
  const owner = getOwner(Number(ownerId));
  const pet = getPet(Number(ownerId), Number(petId));

  if (!owner || !pet) {
    notFound();
  }

  const action = updatePetAction.bind(null, owner.id, pet.id);
  const types = getPetTypes();

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 px-6 py-10">
      <header>
        <Link className="text-sm font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}`}>
          {owner.firstName} {owner.lastName}
        </Link>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">Edit Pet</h1>
      </header>

      {error === "duplicate" ? <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">A pet with that name already exists for this owner.</p> : null}
      {error === "futureBirthDate" ? <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">Birth date cannot be in the future.</p> : null}

      <form action={action} className="grid gap-4 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Name
          <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" defaultValue={pet.name} name="name" required />
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Birth date
          <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" defaultValue={pet.birthDate} name="birthDate" required type="date" />
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Type
          <select className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" defaultValue={pet.typeId} name="typeId" required>
            {types.map((type) => (
              <option key={type.id} value={type.id}>
                {type.name}
              </option>
            ))}
          </select>
        </label>
        <button className="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800" type="submit">
          Save pet
        </button>
      </form>
    </main>
  );
}
```

- [ ] **Step 3: Run lint and build**

Run:

```bash
cd frontend && npm run lint && npm run build
```

Expected: both commands exit with code `0`.

- [ ] **Step 4: Commit pet forms**

Run:

```bash
git add frontend/src/app/owners/[ownerId]/pets/new/page.tsx frontend/src/app/owners/[ownerId]/pets/[petId]/edit/page.tsx
git commit -m "Add pet form routes" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes the two pet form pages.

## Task 7: Visit creation route

**Files:**
- Create: `frontend/src/app/owners/[ownerId]/pets/[petId]/visits/new/page.tsx`

- [ ] **Step 1: Create new visit page**

Create `frontend/src/app/owners/[ownerId]/pets/[petId]/visits/new/page.tsx` with this content:

```tsx
import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { createVisit, getOwner, getPet, visitInputFromForm } from "@/lib/owners-api";

async function createVisitAction(ownerId: number, petId: number, formData: FormData) {
  "use server";

  const visit = createVisit(ownerId, petId, visitInputFromForm(formData));

  if (!visit) {
    notFound();
  }

  redirect(`/owners/${ownerId}`);
}

export default async function NewVisitPage({
  params,
}: {
  params: Promise<{ ownerId: string; petId: string }>;
}) {
  const { ownerId, petId } = await params;
  const owner = getOwner(Number(ownerId));
  const pet = getPet(Number(ownerId), Number(petId));

  if (!owner || !pet) {
    notFound();
  }

  const action = createVisitAction.bind(null, owner.id, pet.id);

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 px-6 py-10">
      <header>
        <Link className="text-sm font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}`}>
          {owner.firstName} {owner.lastName}
        </Link>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">New Visit for {pet.name}</h1>
      </header>

      <form action={action} className="grid gap-4 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Date
          <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name="date" required type="date" />
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Description
          <textarea className="min-h-28 rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name="description" required />
        </label>
        <button className="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800" type="submit">
          Save visit
        </button>
      </form>
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

- [ ] **Step 3: Commit visit route**

Run:

```bash
git add frontend/src/app/owners/[ownerId]/pets/[petId]/visits/new/page.tsx
git commit -m "Add visit creation route" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes only the visit creation page.

## Task 8: Owners route verification

**Files:**
- Verify: `frontend/src/app/owners/**`
- Verify: `frontend/src/lib/owners-api.ts`

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

- [ ] **Step 3: Verify owners pages in a browser**

Open these URLs:

```text
http://localhost:3000/owners
http://localhost:3000/owners?lastName=Davis
http://localhost:3000/owners/1
http://localhost:3000/owners/new
http://localhost:3000/owners/1/edit
http://localhost:3000/owners/1/pets/new
http://localhost:3000/owners/1/pets/1/edit
http://localhost:3000/owners/6/pets/7/visits/new
```

Expected:

- `/owners` shows the owner table.
- `/owners?lastName=Davis` shows Betty Davis and Harold Davis.
- `/owners/1` shows George Franklin and pet Leo.
- form pages render required fields and submit back to the intended owner page or list.

- [ ] **Step 4: Commit any verification fixes**

If files changed during verification, run:

```bash
git add frontend/src/app/owners frontend/src/lib/owners-api.ts frontend/src/lib/types.ts frontend/src/app/page.tsx
git commit -m "Stabilize owners route flow" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: commit includes only owners-related fixes.
