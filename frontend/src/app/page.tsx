import Link from "next/link";

export default function Home() {
  return (
    <main className="mx-auto flex min-h-screen w-full max-w-4xl flex-col justify-center gap-8 px-6 py-10">
      <div>
        <p className="text-sm font-medium text-emerald-700">Spring PetClinic</p>
        <h1 className="mt-2 text-4xl font-semibold tracking-tight">Frontend routes</h1>
        <p className="mt-4 max-w-2xl text-zinc-600">
          Use the mock-backed frontend routes while the backend REST API is being prepared.
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <Link className="rounded-xl border border-zinc-200 bg-white p-5 shadow-sm hover:border-emerald-600" href="/owners">
          <h2 className="text-xl font-semibold">Owners</h2>
          <p className="mt-2 text-sm text-zinc-600">Search owners and manage pets and visits.</p>
        </Link>
      </div>
    </main>
  );
}
