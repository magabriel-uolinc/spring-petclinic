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