import { BookingsDashboard } from "@/components/bookings-dashboard";
import { SectionHeading } from "@/components/section-heading";
import { sortEvents } from "@/lib/event-presentation";
import { getPublicEvents } from "@/lib/server-api";

export default async function BookingsPage() {
  const events = sortEvents(await getPublicEvents());

  return (
    <div className="shell py-10 sm:py-14">
      <SectionHeading
        eyebrow="Account center"
        title="Tickets, payment records, and notification logs in one place."
        description="This page exists to showcase the full microservice flow after checkout: booking creation, payment reference storage, and notification delivery history."
      />
      <div className="mt-8">
        <BookingsDashboard events={events} />
      </div>
    </div>
  );
}
