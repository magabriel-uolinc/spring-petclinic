import Link from "next/link";
import { redirect } from "next/navigation";
import { createOwner, ownerInputFromForm } from "@/lib/owners-api";

async function createOwnerAction(formData: FormData) {
  "use server";

  const owner = await createOwner(ownerInputFromForm(formData));
  redirect(`/owners/${owner.id}`);
}

export default function NewOwnerPage() {
  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 px-6 py-10">
      <header>
        <Link className="text-sm font-medium text-emerald-700 hover:underline" href="/owners">
          Owners
        </Link>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">New Owner</h1>
      </header>

      <form action={createOwnerAction} className="grid gap-4 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        {["firstName", "lastName", "address", "city", "telephone"].map((name) => (
          <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700" key={name}>
            {name}
            <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name={name} required />
          </label>
        ))}
        <button className="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800" type="submit">
          Save owner
        </button>
      </form>
    </main>
  );
}
