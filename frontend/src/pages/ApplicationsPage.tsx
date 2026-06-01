import { useState } from 'react'
import { useAuthStore } from '../store/authStore'
import { useMyApplications, useApplicationsByJobPost, useUpdateApplicationStatus } from '../hooks/useApplications'
import { useMyJobPosts } from '../hooks/useJobPosts'
import ApplicationStatusBadge from '../components/applications/ApplicationStatusBadge'
import Spinner from '../components/shared/Spinner'
import toast from 'react-hot-toast'

const VALID_STATUSES = ['APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'ACCEPTED', 'REJECTED', 'WITHDRAWN']

export default function ApplicationsPage() {
  const user = useAuthStore((s) => s.user)
  const role = user?.role ?? 'STUDENT'

  if (role === 'STUDENT') return <StudentApplications />
  if (role === 'RECRUITER') return <RecruiterApplications />
  if (role === 'PO') return <POApplications />

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Applications</h1>
      <p className="text-gray-600">Applications management coming soon for admin.</p>
    </div>
  )
}

function StudentApplications() {
  const { data: applications, isLoading } = useMyApplications()

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">My Applications</h1>
      {isLoading && <div className="flex justify-center py-10"><Spinner /></div>}
      {applications && applications.length === 0 && (
        <p className="text-gray-500 text-sm">You haven't applied to any jobs yet.</p>
      )}
      <div className="space-y-3">
        {applications?.map((app) => (
          <div key={app.id} className="bg-white rounded-lg shadow p-4 flex items-center justify-between">
            <div className="flex-1">
              <h3 className="font-medium">{app.jobPost.title}</h3>
              <p className="text-xs text-gray-500">
                {app.jobPost.drive.title} · Applied {new Date(app.appliedAt).toLocaleDateString()}
              </p>
              {app.resumeSnapshotPath && (
                <p className="text-xs text-blue-600 mt-1">Resume attached</p>
              )}
            </div>
            <ApplicationStatusBadge status={app.status} />
          </div>
        ))}
      </div>
    </div>
  )
}

function RecruiterApplications() {
  const { data: jobPosts, isLoading: postsLoading } = useMyJobPosts()
  const [selectedPostId, setSelectedPostId] = useState<string | null>(null)
  const { data: applications, isLoading: appsLoading } = useApplicationsByJobPost(selectedPostId ?? '')
  const updateStatus = useUpdateApplicationStatus()

  const handleStatusChange = async (appId: string, status: string) => {
    await updateStatus.mutateAsync({ id: appId, status })
    toast.success('Status updated')
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Applications</h1>
      {postsLoading ? (
        <div className="flex justify-center py-10"><Spinner /></div>
      ) : (
        <div className="flex gap-6">
          <aside className="w-64 shrink-0">
            <h2 className="text-sm font-semibold text-gray-500 uppercase mb-2">Your Job Posts</h2>
            {jobPosts && jobPosts.length === 0 && <p className="text-sm text-gray-500">No job posts yet.</p>}
            <div className="space-y-1">
              {jobPosts?.map((jp) => (
                <button
                  key={jp.id}
                  onClick={() => setSelectedPostId(jp.id)}
                  className={`w-full text-left px-3 py-2 rounded text-sm transition ${
                    selectedPostId === jp.id ? 'bg-blue-100 text-blue-700 font-medium' : 'hover:bg-gray-100'
                  }`}
                >
                  {jp.title}
                </button>
              ))}
            </div>
          </aside>
          <div className="flex-1">
            {!selectedPostId && <p className="text-gray-500 text-sm">Select a job post to view applications.</p>}
            {selectedPostId && appsLoading && <div className="flex justify-center py-10"><Spinner /></div>}
            {selectedPostId && applications && applications.length === 0 && (
              <p className="text-gray-500 text-sm">No applications yet.</p>
            )}
            <div className="space-y-3">
              {applications?.map((app) => (
                <div key={app.id} className="bg-white rounded-lg shadow p-4">
                  <div className="flex items-center justify-between mb-2">
                    <div>
                      <h3 className="font-medium">{app.student.fullName}</h3>
                      <p className="text-xs text-gray-500">{app.student.email}</p>
                    </div>
                    <ApplicationStatusBadge status={app.status} />
                  </div>
                  <p className="text-xs text-gray-500 mb-2">
                    Applied {new Date(app.appliedAt).toLocaleDateString()}
                    {app.resumeSnapshotPath && (
                      <span className="ml-2 text-blue-600">Resume attached</span>
                    )}
                  </p>
                  <div className="flex gap-2">
                    {VALID_STATUSES.map((s) => (
                      <button
                        key={s}
                        onClick={() => handleStatusChange(app.id, s)}
                        disabled={updateStatus.isPending}
                        className={`text-xs px-2 py-1 rounded border transition ${
                          app.status === s
                            ? 'bg-blue-600 text-white border-blue-600'
                            : 'hover:bg-gray-50'
                        }`}
                      >
                        {s.replace('_', ' ')}
                      </button>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

function POApplications() {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Applications</h1>
      <p className="text-gray-500 text-sm mb-4">
        View applications for your drives from the drive detail page.
      </p>
    </div>
  )
}
