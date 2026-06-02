import StatusBadge from '../shared/StatusBadge'

export default function ApplicationStatusBadge({ status }: { status: string }) {
  return <StatusBadge status={status} />
}
