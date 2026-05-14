import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { createVisit, getOwner, getPet, visitInputFromForm } from "@/lib/owners-api";

async function createVisitAction(ownerId: number, petId: number, formData: FormData) {
  "use server";

  const input = visitInputFromForm(formData);

  if (input.date > new Date().toISOString().slice(0, 10)) {
    redirect(`/owners/${ownerId}/pets/${petId}/visits/new?error=futureVisitDate`);
  }

  const visit = await createVisit(ownerId, petId, input);

  if (!visit) {
    notFound();
  }

  redirect(`/owners/${ownerId}`);
}

export default async function NewVisitPage({
  params,
  searchParams,
}: {
  params: Promise<{ ownerId: string; petId: string }>;
  searchParams: Promise<{ error?: string }>;
}) {
  const { ownerId, petId } = await params;
  const { error } = await searchParams;
  const owner = await getOwner(Number(ownerId));
  const pet = await getPet(Number(ownerId), Number(petId));

  if (!owner || !pet) {
    notFound();
  }

  const action = createVisitAction.bind(null, owner.id, pet.id);
  const today = new Date().toISOString().slice(0, 10);

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-8 px-6 py-10">
      <header>
        <Link className="text-sm font-medium text-emerald-700 hover:underline" href={`/owners/${owner.id}`}>
          {owner.firstName} {owner.lastName}
        </Link>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">New Visit for {pet.name}</h1>
      </header>

      <form action={action} className="grid gap-4 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        {error === "futureVisitDate" ? <p className="rounded-md bg-red-50 p-3 text-sm text-red-700">Visit date cannot be in the future.</p> : null}
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Date
          <input className="rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" max={today} name="date" required type="date" />
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-zinc-700">
          Description
          <textarea className="min-h-28 rounded-md border border-zinc-300 px-3 py-2 text-base text-zinc-950" name="description" required />
        </label>
        <button className="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800" type="submit">
          Save visit
        </button>
      </form>
    </main>
  );
}
