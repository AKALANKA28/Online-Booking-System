import { BookingsDashboard } from "@/components/bookings-dashboard";
import { SectionHeading } from "@/components/section-heading";
import { sortEvents } from "@/lib/event-presentation";
import { getPublicEvents } from "@/lib/server-api";

export default async function BookingsPage() {
  const events = sortEvents(await getPublicEvents());

  return (
    <div className="shell py-10 sm:py-14">
       <SectionHeading
         eyebrow="My tickets"
         title="Your bookings, all in one place."
         description="View your tickets, track payment status, and stay updated on your event plans."
       />
      <div className="mt-8">
        <BookingsDashboard events={events} />
      </div>
    </div>
  );
}
