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
  const owners = await getOwners({ lastName, page, size: PAGE_SIZE });
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
