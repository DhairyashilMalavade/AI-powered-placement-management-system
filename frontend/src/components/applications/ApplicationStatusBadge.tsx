export default function ApplicationStatusBadge({ status }: { status: string }) {
  const colors: Record<string, string> = {
    APPLIED: 'bg-gray-100 text-gray-700',
    UNDER_REVIEW: 'bg-yellow-100 text-yellow-700',
    SHORTLISTED: 'bg-blue-100 text-blue-700',
    ACCEPTED: 'bg-green-100 text-green-700',
    REJECTED: 'bg-red-100 text-red-700',
    WITHDRAWN: 'bg-gray-100 text-gray-500',
  }
  return (
    <span className={`inline-block px-2 py-0.5 rounded text-xs font-medium ${colors[status] ?? 'bg-gray-100 text-gray-700'}`}>
      {status}
    </span>
  )
}
