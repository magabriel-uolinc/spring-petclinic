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