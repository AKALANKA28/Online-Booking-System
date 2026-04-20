import { AdminEventStudio } from "@/components/admin-event-studio";
import { SectionHeading } from "@/components/section-heading";

export default function AdminEventsPage() {
  return (
    <div className="shell py-10 sm:py-14">
      <SectionHeading
        eyebrow="Organizer studio"
        title="Create and manage events."
        description="Create new events, set seat pricing and availability, and manage listings. Everything you need to run your event in one place."
      />
      <div className="mt-8">
        <AdminEventStudio />
      </div>
    </div>
  );
}
