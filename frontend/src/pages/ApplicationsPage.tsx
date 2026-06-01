import { useAuthStore } from '../store/authStore'
import { useMyApplications } from '../hooks/useApplications'
import ApplicationStatusBadge from '../components/applications/ApplicationStatusBadge'

function ApplicationRow({ app }: { app: import('../types/application').ApplicationResponse }) {
  return (
    <div className="bg-white rounded-lg shadow p-4 flex items-center justify-between">
      <div className="flex-1">
        <h3 className="font-medium">{app.jobPost.title}</h3>
        <p className="text-xs text-gray-500">
          {app.jobPost.drive.title} · Applied {new Date(app.appliedAt).toLocaleDateString()}
        </p>
      </div>
      <ApplicationStatusBadge status={app.status} />
    </div>
  )
}

export default function ApplicationsPage() {
  const user = useAuthStore((s) => s.user)
  const role = user?.role ?? 'STUDENT'

  if (role === 'STUDENT') {
    return <StudentApplications />
  }

  if (role === 'RECRUITER') {
    return <RecruiterApplications />
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Applications</h1>
      <p className="text-gray-600">Applications management coming soon for PO.</p>
    </div>
  )
}

function StudentApplications() {
  const { data: applications, isLoading } = useMyApplications()

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">My Applications</h1>
      {isLoading && <p className="text-gray-500 text-sm">Loading...</p>}
      {applications && applications.length === 0 && (
        <p className="text-gray-500 text-sm">You haven't applied to any jobs yet.</p>
      )}
      <div className="space-y-3">
        {applications?.map((app) => (
          <ApplicationRow key={app.id} app={app} />
        ))}
      </div>
    </div>
  )
}

function RecruiterApplications() {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Applications</h1>
      <p className="text-gray-500 text-sm mb-4">
        View applications for your job posts from the job post detail page.
      </p>
    </div>
  )
}
