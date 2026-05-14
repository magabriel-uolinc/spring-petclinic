import Link from "next/link";

export default function NotFound() {
  return (
    <main className="mx-auto flex min-h-screen w-full max-w-3xl flex-col justify-center gap-6 px-6 py-10">
      <div className="rounded-2xl border border-amber-200 bg-amber-50 p-8 shadow-sm">
        <p className="text-sm font-semibold uppercase tracking-wide text-amber-700">
          Spring PetClinic
        </p>
        <h1 className="mt-3 text-4xl font-semibold tracking-tight text-zinc-950">
          Page not found
        </h1>
        <p className="mt-4 text-base leading-7 text-zinc-700">
          The page you requested does not exist in this PetClinic frontend.
        </p>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row">
        <Link
          className="rounded-md bg-emerald-700 px-4 py-2 text-center text-sm font-semibold text-white hover:bg-emerald-800"
          href="/"
        >
          Go home
        </Link>
        <Link
          className="rounded-md border border-zinc-300 px-4 py-2 text-center text-sm font-semibold text-zinc-800 hover:border-emerald-700"
          href="/owners"
        >
          Find owners
        </Link>
      </div>
    </main>
  );
}
