import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { createPet, getOwner, getPetTypes, hasDuplicatePetName, petInputFromForm } from "@/lib/owners-api";

async function createPetAction(ownerId: number, formData: FormData) {
  "use server";

  const input = petInputFromForm(formData);

  if (hasDuplicatePetName(ownerId, input.name)) {
    redirect(`/owners/${ownerId}/pets/new?error=duplicate`);
  }

  if (input.birthDate > new Date().toISOString().slice(0, 10)) {
    redirect(`/owners/${ownerId}/pets/new?error=futureBirthDate`);
  }

  const pet = createPet(ownerId, input);

  if (!pet) {
    notFound();
  }

  redirect(`/owners/${ownerId}`);
}

export default async function NewPetPage({
  params,
  searchParams,
}: {
  params: Promise<{ ownerId: string }>;
  searchParams: Promise<{ error?: string }>;
}) {
  const { ownerId } = await params;
  const { error } = await searchParams;
  const owner = getOwner(Number(ownerId));

  if (!owner) {
    notFound();
  }

  const action = createPetAction.bind(null, owner.id);
  const types = getPetTypes();

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 px-6 py-10">
      <header>
        <Link className="text-sm font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}`}>
          {owner.firstName} {owner.lastName}
        </Link>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">New Pet</h1>
      </header>

      {error === "duplicate" ? <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">A pet with that name already exists for this owner.</p> : null}
      {error === "futureBirthDate" ? <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">Birth date cannot be in the future.</p> : null}

      <form action={action} className="grid gap-4 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Name
          <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name="name" required />
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Birth date
          <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name="birthDate" required type="date" />
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Type
          <select className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name="typeId" required>
            {types.map((type) => (
              <option key={type.id} value={type.id}>
                {type.name}
              </option>
            ))}
          </select>
        </label>
        <button className="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800" type="submit">
          Save pet
        </button>
      </form>
    </main>
  );
}
