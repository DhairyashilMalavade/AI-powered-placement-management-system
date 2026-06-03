import { useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { useMyApplications, useApplicationsByJobPost, useWithdrawApplication, useUpdateApplicationStatus } from '../hooks/useApplications'
import { useMyJobPosts } from '../hooks/useJobPosts'
import { downloadResume } from '../hooks/useResume'
import ApplicationStatusBadge from '../components/applications/ApplicationStatusBadge'
import Spinner from '../components/shared/Spinner'
import Pagination from '../components/shared/Pagination'
import EmptyState from '../components/shared/EmptyState'
import toast from 'react-hot-toast'

/* Must match ApplicationService.ALLOWED_TRANSITIONS */
const ALLOWED_TRANSITIONS: Record<string, string[]> = {
  APPLIED: ['UNDER_REVIEW', 'SHORTLISTED', 'ACCEPTED', 'REJECTED'],
  UNDER_REVIEW: ['SHORTLISTED', 'ACCEPTED', 'REJECTED'],
  SHORTLISTED: ['ACCEPTED', 'REJECTED'],
  ACCEPTED: ['REJECTED'],
  REJECTED: [],
  WITHDRAWN: [],
}

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
  const [page, setPage] = useState(0)
  const { data: appsPage, isLoading } = useMyApplications(page)
  const applications = appsPage?.content
  const withdraw = useWithdrawApplication()

  const handleWithdraw = async (appId: string) => {
    await withdraw.mutateAsync(appId)
    toast.success('Application withdrawn')
  }

  const isTerminal = (status: string) => ['ACCEPTED', 'REJECTED', 'WITHDRAWN'].includes(status)

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">My Applications</h1>
      {isLoading && <div className="flex justify-center py-10"><Spinner /></div>}
      {applications && applications.length === 0 && (
        <EmptyState title="No applications yet" description="Browse drives and apply to job posts that interest you." />
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
                <button
                  onClick={() => downloadResume(app.id, `resume-${app.student.fullName.replace(/\s+/g, '-')}.pdf`)}
                  className="text-xs text-blue-600 hover:underline mt-1 inline-block"
                >
                  Download Resume
                </button>
              )}
            </div>
            <div className="flex items-center gap-2">
              <ApplicationStatusBadge status={app.status} />
              {!isTerminal(app.status) && (
                <button
                  onClick={() => handleWithdraw(app.id)}
                  disabled={withdraw.isPending}
                  className="text-xs px-2 py-1 rounded border border-red-300 text-red-600 hover:bg-red-50 transition disabled:opacity-50"
                >
                  {withdraw.isPending ? 'Withdrawing...' : 'Withdraw'}
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {appsPage && (
        <Pagination page={page} totalPages={appsPage.totalPages} onPageChange={setPage} />
      )}
    </div>
  )
}

function RecruiterApplications() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [postPage, setPostPage] = useState(0)
  const [appPage, setAppPage] = useState(0)
  const selectedPostId = searchParams.get('postId')
  const { data: jobPostsPage, isLoading: postsLoading, isError: postsError, refetch: refetchPosts } = useMyJobPosts(undefined, undefined, postPage)
  const jobPosts = jobPostsPage?.content
  const { data: appsPage, isLoading: appsLoading, isError: appsError, refetch: refetchApps } = useApplicationsByJobPost(selectedPostId ?? '', appPage)
  const applications = appsPage?.content
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
      ) : postsError ? (
        <div className="text-center py-10">
          <p className="text-red-600 mb-2">Failed to load job posts.</p>
          <button onClick={() => refetchPosts()} className="text-sm px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">Try again</button>
        </div>
      ) : (
        <div className="flex flex-col md:flex-row gap-6">
          <aside className="w-64 shrink-0">
            <h2 className="text-sm font-semibold text-gray-500 uppercase mb-2">Your Job Posts</h2>
            {jobPosts && jobPosts.length === 0 && <EmptyState title="No job posts yet" />}
            <div className="space-y-1">
              {jobPosts?.map((jp) => (
                <button
                  key={jp.id}
                  onClick={() => { setSearchParams({ postId: jp.id }); setAppPage(0) }}
                  className={`w-full text-left px-3 py-2 rounded text-sm transition ${
                    selectedPostId === jp.id ? 'bg-blue-100 text-blue-700 font-medium' : 'hover:bg-gray-100'
                  }`}
                >
                  {jp.title}
                </button>
              ))}
            </div>
            {jobPostsPage && (
              <Pagination page={postPage} totalPages={jobPostsPage.totalPages} onPageChange={setPostPage} />
            )}
          </aside>
          <div className="flex-1">
            {!selectedPostId && <p className="text-gray-500 text-sm">Select a job post to view applications.</p>}
            {selectedPostId && appsLoading && <div className="flex justify-center py-10"><Spinner /></div>}
            {selectedPostId && appsError && (
              <div className="text-center py-10">
                <p className="text-red-600 mb-2">Failed to load applications.</p>
                <button onClick={() => refetchApps()} className="text-sm px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">Try again</button>
              </div>
            )}
            {selectedPostId && !appsError && applications && applications.length === 0 && (
              <EmptyState title="No applications yet" />
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
                      <button
                        onClick={() => downloadResume(app.id, `resume-${app.student.fullName.replace(/\s+/g, '-')}.pdf`)}
                        className="ml-2 text-xs text-blue-600 hover:underline"
                      >
                        Download Resume
                      </button>
                    )}
                  </p>
                  <div className="flex gap-2">
                    {(ALLOWED_TRANSITIONS[app.status] ?? []).map((s) => (
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
                    {(ALLOWED_TRANSITIONS[app.status] ?? []).length === 0 && (
                      <span className="text-xs text-gray-400 italic">No actions available</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
            {appsPage && (
              <Pagination page={appPage} totalPages={appsPage.totalPages} onPageChange={setAppPage} />
            )}
          </div>
        </div>
      )}
    </div>
  )
}

function POApplications() {
  const navigate = useNavigate()
  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Applications</h1>
      <EmptyState
        title="View applications from your drives"
        description="Select a drive to review its applications."
        action={{ label: 'Go to Drives', onClick: () => navigate('/drives') }}
      />
    </div>
  )
}
