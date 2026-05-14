import Link from "next/link";

export default function OwnersNotFound() {
  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-4 px-6 py-10">
      <p className="text-sm font-medium text-emerald-700">Owners</p>
      <h1 className="text-3xl font-semibold tracking-tight">Owner not found</h1>
      <p className="text-zinc-600">The requested owner, pet, or visit target does not exist in the mock data.</p>
      <Link className="font-medium text-emerald-700 hover:underline" href="/owners">
        Back to owners
      </Link>
    </main>
  );
}
