interface SectionHeadingProps {
  eyebrow: string;
  title: string;
  description?: string;
  action?: React.ReactNode;
}

export function SectionHeading({ eyebrow, title, description, action }: SectionHeadingProps) {
  return (
    <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
      <div className="max-w-2xl">
        <p className="eyebrow">{eyebrow}</p>
        <h2 className="mt-2 font-display text-3xl font-bold leading-tight text-ink sm:text-4xl">{title}</h2>
        {description && <p className="mt-3 text-base leading-7 text-smoke">{description}</p>}
      </div>
      {action}
    </div>
  );
}
