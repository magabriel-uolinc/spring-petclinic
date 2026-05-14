import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { getOwner, ownerInputFromForm, updateOwner } from "@/lib/owners-api";

async function updateOwnerAction(ownerId: number, formData: FormData) {
  "use server";

  const owner = await updateOwner(ownerId, ownerInputFromForm(formData));

  if (!owner) {
    notFound();
  }

  redirect(`/owners/${owner.id}`);
}

export default async function EditOwnerPage({
  params,
}: {
  params: Promise<{ ownerId: string }>;
}) {
  const { ownerId } = await params;
  const owner = await getOwner(Number(ownerId));

  if (!owner) {
    notFound();
  }

  const action = updateOwnerAction.bind(null, owner.id);

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 px-6 py-10">
      <header>
        <Link className="text-sm font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}`}>
          {owner.firstName} {owner.lastName}
        </Link>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">Edit Owner</h1>
      </header>

      <form action={action} className="grid gap-4 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        {[
          ["firstName", owner.firstName],
          ["lastName", owner.lastName],
          ["address", owner.address],
          ["city", owner.city],
          ["telephone", owner.telephone],
        ].map(([name, value]) => (
          <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700" key={name}>
            {name}
            <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" defaultValue={value} name={name} required />
          </label>
        ))}
        <button className="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800" type="submit">
          Save owner
        </button>
      </form>
    </main>
  );
}
