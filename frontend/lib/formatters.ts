export function formatMoney(value: number) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "LKR",
    maximumFractionDigits: 0,
  }).format(value);
}

export function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(new Date(value));
}

export function formatTime(value: string) {
  return new Intl.DateTimeFormat("en-US", {
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));
}

export function formatDateRange(start: string, end: string) {
  return `${formatDate(start)} · ${formatTime(start)} - ${formatTime(end)}`;
}

export function initialsFromEmail(email: string) {
  const handle = email.split("@")[0] ?? "PT";
  return handle.slice(0, 2).toUpperCase();
}

export function seatSort(a: string, b: string) {
  const rowA = a.match(/[A-Z]+/)?.[0] ?? "";
  const rowB = b.match(/[A-Z]+/)?.[0] ?? "";
  const numA = Number(a.match(/\d+/)?.[0] ?? 0);
  const numB = Number(b.match(/\d+/)?.[0] ?? 0);
  if (rowA === rowB) return numA - numB;
  return rowA.localeCompare(rowB);
}
