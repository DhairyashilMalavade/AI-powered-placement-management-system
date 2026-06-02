import StatusBadge from '../shared/StatusBadge'

export default function JobPostStatusBadge({ status }: { status: string }) {
  return <StatusBadge status={status} />
}
