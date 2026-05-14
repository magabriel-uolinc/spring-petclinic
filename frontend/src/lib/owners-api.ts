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

const API_BASE_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
  }
}

function apiUrl(path: string) {
  return new URL(path, API_BASE_URL);
}

async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(apiUrl(path), {
    ...init,
    cache: "no-store",
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
  });

  if (!response.ok) {
    throw new ApiError(`Backend request failed with status ${response.status}`, response.status);
  }

  return response.json() as Promise<T>;
}

async function apiFetchOptional<T>(path: string, init?: RequestInit): Promise<T | undefined> {
  try {
    return await apiFetch<T>(path, init);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return undefined;
    }

    throw error;
  }
}

function required(value: FormDataEntryValue | null) {
  return String(value ?? "").trim();
}

function petFromDetail(ownerId: number, pet: PetDetail): Pet {
  return {
    id: pet.id,
    name: pet.name,
    birthDate: pet.birthDate,
    typeId: pet.type.id,
    ownerId,
  };
}

export async function getOwners({
  lastName = "",
  page = 0,
  size = 5,
}: {
  lastName?: string;
  page?: number;
  size?: number;
} = {}) {
  const params = new URLSearchParams({
    lastName,
    page: String(Number.isFinite(page) ? page : 0),
    size: String(Number.isFinite(size) && size > 0 ? size : 5),
  });

  return apiFetch<Page<Owner>>(`/api/owners?${params}`);
}

export function getOwner(ownerId: number): Promise<OwnerDetail | undefined> {
  return apiFetchOptional<OwnerDetail>(`/api/owners/${ownerId}`);
}

export function getPetTypes() {
  return apiFetch<PetType[]>("/api/pet-types");
}

export async function getPet(ownerId: number, petId: number) {
  const owner = await getOwner(ownerId);
  const pet = owner?.pets.find((item) => item.id === petId);
  return pet ? petFromDetail(ownerId, pet) : undefined;
}

export function createOwner(input: OwnerInput) {
  return apiFetch<Owner>("/api/owners", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateOwner(ownerId: number, input: OwnerInput) {
  return apiFetchOptional<Owner>(`/api/owners/${ownerId}`, {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function createPet(ownerId: number, input: PetInput) {
  return apiFetchOptional<PetDetail>(`/api/owners/${ownerId}/pets`, {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updatePet(ownerId: number, petId: number, input: PetInput) {
  return apiFetchOptional<PetDetail>(`/api/owners/${ownerId}/pets/${petId}`, {
    method: "PUT",
    body: JSON.stringify(input),
  });
}

export function createVisit(ownerId: number, petId: number, input: VisitInput) {
  return apiFetchOptional<Visit>(`/api/owners/${ownerId}/pets/${petId}/visits`, {
    method: "POST",
    body: JSON.stringify(input),
  });
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

export async function hasDuplicatePetName(ownerId: number, name: string, currentPetId?: number) {
  const owner = await getOwner(ownerId);
  const normalizedName = name.trim().toLowerCase();

  return owner?.pets.some(
    (pet) =>
      pet.id !== currentPetId &&
      pet.name.toLowerCase() === normalizedName,
  ) ?? false;
}
