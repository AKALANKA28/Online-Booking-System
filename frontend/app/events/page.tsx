import { EventCatalogue } from "@/components/event-catalogue";
import { SectionHeading } from "@/components/section-heading";
import { sortEvents } from "@/lib/event-presentation";
import { getPublicEvents } from "@/lib/server-api";

export default async function EventsPage() {
  const events = sortEvents(await getPublicEvents());

  return (
    <div className="shell py-10 sm:py-14">
      <SectionHeading
        eyebrow="Browse events"
        title="Discovery page tuned for ticket shoppers."
        description="Search the real event catalog exposed by your Spring API gateway, then jump into a detail page built for seat selection and conversion."
      />
      <div className="mt-8">
        <EventCatalogue initialEvents={events} />
      </div>
    </div>
  );
}
