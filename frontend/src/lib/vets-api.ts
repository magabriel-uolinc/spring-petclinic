import type { Page, VetWithSpecialties } from "@/lib/types";

const API_BASE_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

function apiUrl(path: string) {
  return new URL(path, API_BASE_URL);
}

export async function getVets({
  page = 0,
  size = 5,
}: {
  page?: number;
  size?: number;
} = {}): Promise<Page<VetWithSpecialties>> {
  const params = new URLSearchParams({
    page: String(Number.isFinite(page) ? page : 0),
    size: String(Number.isFinite(size) && size > 0 ? size : 5),
  });

  const response = await fetch(apiUrl(`/api/vets?${params}`), {
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`Backend request failed with status ${response.status}`);
  }

  return response.json() as Promise<Page<VetWithSpecialties>>;
}
