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
