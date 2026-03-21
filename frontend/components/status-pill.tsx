interface StatusPillProps {
  value: string;
}

const map: Record<string, string> = {
  PUBLISHED: "bg-pineSoft text-pine border-pine/20",
  DRAFT: "bg-goldSoft text-gold border-gold/20",
  CANCELLED: "bg-emberSoft text-ember border-ember/20",
  AVAILABLE: "bg-pineSoft text-pine border-pine/20",
  RESERVED: "bg-goldSoft text-gold border-gold/20",
  BOOKED: "bg-ink/10 text-ink border-ink/10",
  CONFIRMED: "bg-pineSoft text-pine border-pine/20",
  FAILED: "bg-emberSoft text-ember border-ember/20",
  PENDING_PAYMENT: "bg-goldSoft text-gold border-gold/20",
  SUCCESS: "bg-pineSoft text-pine border-pine/20",
  SENT: "bg-cobaltSoft text-cobalt border-cobalt/20",
};

export function StatusPill({ value }: StatusPillProps) {
  return (
    <span className={`inline-flex rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-[0.12em] ${map[value] ?? "bg-white text-ink border-line"}`}>
      {value.replaceAll("_", " ")}
    </span>
  );
}
