import Link from "next/link";

export default function Home() {
  return (
    <main className="mx-auto flex min-h-screen w-full max-w-5xl flex-col justify-center gap-10 px-6 py-10">
      <section className="rounded-2xl border border-emerald-100 bg-emerald-50 p-8 shadow-sm">
        <p className="text-sm font-semibold uppercase tracking-wide text-emerald-700">
          Spring PetClinic
        </p>
        <h1 className="mt-3 text-4xl font-semibold tracking-tight text-zinc-950">
          Welcome to PetClinic
        </h1>
        <p className="mt-4 max-w-2xl text-base leading-7 text-zinc-700">
          A simple frontend for managing clinic owners, pets, and visits while the
          REST API migration is in progress.
        </p>
      </section>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Link
          className="rounded-xl border border-zinc-200 bg-white p-5 shadow-sm hover:border-emerald-600"
          href="/owners"
        >
          <h2 className="text-xl font-semibold text-zinc-950">Find owners</h2>
          <p className="mt-2 text-sm leading-6 text-zinc-600">
            Search owners and manage their pets and visits.
          </p>
        </Link>

        <Link
          className="rounded-xl border border-zinc-200 bg-white p-5 shadow-sm hover:border-emerald-600"
          href="/vets"
        >
          <h2 className="text-xl font-semibold text-zinc-950">Veterinarians</h2>
          <p className="mt-2 text-sm leading-6 text-zinc-600">
            Browse vets and their specialties.
          </p>
        </Link>

        <div className="rounded-xl border border-dashed border-zinc-300 bg-white p-5 text-zinc-500">
          <h2 className="text-xl font-semibold text-zinc-700">Clinic info</h2>
          <p className="mt-2 text-sm leading-6">
            Use the owners route to explore the current mock-backed workflow.
          </p>
        </div>
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white p-5 text-sm leading-6 text-zinc-600 shadow-sm">
        <p>
          This page mirrors the base PetClinic landing experience with a small
          set of entry points for the routes available in this frontend.
        </p>
      </div>
    </main>
  );
}
