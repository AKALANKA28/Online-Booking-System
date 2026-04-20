import Link from "next/link";

export default function NotFound() {
  return (
    <div className="shell py-24">
      <div className="surface max-w-2xl p-10">
        <p className="eyebrow">Not found</p>
        <h1 className="mt-3 font-display text-4xl font-bold text-ink">That event could not be found.</h1>
        <p className="mt-4 text-sm leading-7 text-smoke">This event may no longer be available. Browse other events or contact support if you believe this is an error.</p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Link href="/events" className="rounded-full bg-ink px-5 py-3 text-sm font-semibold text-white">Browse events</Link>
          <Link href="/admin/events" className="rounded-full border border-line px-5 py-3 text-sm font-semibold text-ink">Open organizer studio</Link>
        </div>
      </div>
    </div>
  );
}
