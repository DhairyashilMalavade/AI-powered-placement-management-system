import { Link } from 'react-router-dom'
import type { DriveResponse } from '../../types/drive'
import DriveStatusBadge from './DriveStatusBadge'

export default function DriveCard({ drive }: { drive: DriveResponse }) {
  return (
    <Link
      to={`/drives/${drive.id}`}
      className="block bg-white rounded-lg shadow p-4 hover:shadow-md transition"
    >
      <div className="flex items-start justify-between mb-2">
        <h3 className="font-semibold text-lg">{drive.title}</h3>
        <DriveStatusBadge status={drive.status} />
      </div>
      {drive.description && (
        <p className="text-gray-600 text-sm mb-2 line-clamp-2">{drive.description}</p>
      )}
      <div className="text-xs text-gray-400">
        Deadline: {new Date(drive.applicationDeadline).toLocaleDateString()}
        {drive.minGpa && ` · Min GPA: ${drive.minGpa}`}
      </div>
    </Link>
  )
}
