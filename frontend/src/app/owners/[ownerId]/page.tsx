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
