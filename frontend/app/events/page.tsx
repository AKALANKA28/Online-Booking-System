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
         title="All upcoming events."
         description="Explore upcoming events. Filter by category, search by keyword, and find your next adventure."
       />
      <div className="mt-8">
        <EventCatalogue initialEvents={events} />
      </div>
    </div>
  );
}
