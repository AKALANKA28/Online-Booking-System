import { AdminEventStudio } from "@/components/admin-event-studio";
import { SectionHeading } from "@/components/section-heading";

export default function AdminEventsPage() {
  return (
    <div className="shell py-10 sm:py-14">
      <SectionHeading
        eyebrow="Organizer view"
        title="Publish events without switching to Swagger during the demo."
        description="A purpose-built admin screen makes the full product story stronger: publish an event, wait for seat generation, then immediately book that event from the customer-facing UI."
      />
      <div className="mt-8">
        <AdminEventStudio />
      </div>
    </div>
  );
}
