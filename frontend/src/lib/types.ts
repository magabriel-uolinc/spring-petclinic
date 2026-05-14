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
