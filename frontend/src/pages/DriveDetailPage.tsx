import { Link, useParams, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { useDrive, useUpdateDrive, useDeleteDrive, useUpdateDriveStatus } from '../hooks/useDrives'
import { useJobPostsByDrive, useCreateJobPost } from '../hooks/useJobPosts'
import { useApplicationsByDrive, useUpdateApplicationStatus } from '../hooks/useApplications'
import { useDebounce } from '../hooks/useDebounce'
import DriveStatusBadge from '../components/drives/DriveStatusBadge'
import JobPostCard from '../components/jobposts/JobPostCard'
import JobPostForm from '../components/jobposts/JobPostForm'
import ApplicationStatusBadge from '../components/applications/ApplicationStatusBadge'
import Skeleton from '../components/shared/Skeleton'
import { useState } from 'react'
import DriveForm from '../components/drives/DriveForm'
import Spinner from '../components/shared/Spinner'
import Pagination from '../components/shared/Pagination'
import SearchInput from '../components/shared/SearchInput'
import EmptyState from '../components/shared/EmptyState'
import toast from 'react-hot-toast'
import type { CreateDriveRequest, DriveResponse } from '../types/drive'
import type { CreateJobPostRequest } from '../types/jobPost'

/* Must match ApplicationService.ALLOWED_TRANSITIONS */
const ALLOWED_TRANSITIONS: Record<string, string[]> = {
  APPLIED: ['UNDER_REVIEW', 'SHORTLISTED', 'ACCEPTED', 'REJECTED'],
  UNDER_REVIEW: ['SHORTLISTED', 'ACCEPTED', 'REJECTED'],
  SHORTLISTED: ['ACCEPTED', 'REJECTED'],
  ACCEPTED: ['REJECTED'],
  REJECTED: [],
  WITHDRAWN: [],
}

function toCreateRequest(drive: DriveResponse): Partial<CreateDriveRequest> {
  return {
    title: drive.title,
    description: drive.description ?? undefined,
    minGpa: drive.minGpa,
    allowedGraduationYears: drive.allowedGraduationYears ?? undefined,
    requiredSkills: drive.requiredSkills ?? undefined,
    additionalCriteria: drive.additionalCriteria ?? undefined,
    applicationDeadline: drive.applicationDeadline,
    driveDate: drive.driveDate,
  }
}

export default function DriveDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const { data: drive, isLoading: driveLoading, isError: driveError, refetch: refetchDrive } = useDrive(id!)
  const [jobPostPage, setJobPostPage] = useState(0)
  const [jobSearch, setJobSearch] = useState('')
  const [jobStatusFilter, setJobStatusFilter] = useState('')
  const debouncedJobSearch = useDebounce(jobSearch)
  const { data: jobPostsPage, isLoading: postsLoading } = useJobPostsByDrive(id!, debouncedJobSearch || undefined, jobStatusFilter || undefined, jobPostPage)
  const jobPosts = jobPostsPage?.content
  const updateDrive = useUpdateDrive()
  const deleteDrive = useDeleteDrive()
  const updateStatus = useUpdateDriveStatus()
  const createJobPost = useCreateJobPost()
  const [editing, setEditing] = useState(false)
  const [showJobForm, setShowJobForm] = useState(false)

  if (driveLoading) return <div className="p-4"><Skeleton lines={6} /></div>
  if (driveError) return (
    <div className="text-center py-10">
      <p className="text-red-600 mb-2">Failed to load drive.</p>
      <button onClick={() => refetchDrive()} className="text-sm px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">Try again</button>
    </div>
  )
  if (!drive) return <p>Drive not found.</p>

  const isOwner = drive.createdBy.id === user?.id
  const isPO = user?.role === 'PO'
  const isRecruiter = user?.role === 'RECRUITER'

  const handleDelete = async () => {
    if (!confirm('Delete this drive? This will also remove all associated job posts and applications.')) return
    await deleteDrive.mutateAsync(drive.id)
    navigate('/drives')
  }

  const handleStatusChange = (status: string) => {
    updateStatus.mutate({ id: drive.id, status })
  }

  const handleUpdate = async (data: CreateDriveRequest) => {
    await updateDrive.mutateAsync({ id: drive.id, data })
    setEditing(false)
  }

  const handleCreateJob = async (data: CreateJobPostRequest) => {
    await createJobPost.mutateAsync(data)
    setShowJobForm(false)
  }

  return (
    <div>
      <Link to="/drives" className="text-sm text-blue-600 hover:underline mb-4 inline-block">&larr; Back to Drives</Link>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold">{drive.title}</h1>
          <p className="text-gray-500 text-sm">
            Created by {drive.createdBy.fullName} · {new Date(drive.createdAt).toLocaleDateString()}
          </p>
        </div>
        <DriveStatusBadge status={drive.status} />
      </div>

      {isOwner && isPO && (
        <div className="flex gap-2 mb-4">
          <button onClick={() => setEditing(!editing)} className="px-3 py-1 text-sm border rounded hover:bg-gray-50">
            {editing ? 'Cancel' : 'Edit'}
          </button>
          {drive.status === 'DRAFT' && (
            <button onClick={() => handleStatusChange('ACTIVE')} className="px-3 py-1 text-sm bg-green-600 text-white rounded hover:bg-green-700">
              Activate
            </button>
          )}
          {drive.status === 'ACTIVE' && (
            <button onClick={() => handleStatusChange('CLOSED')} className="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700">
              Close
            </button>
          )}
          <button onClick={handleDelete} className="px-3 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200">
            Delete
          </button>
        </div>
      )}

      {editing && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="font-semibold mb-4">Edit Drive</h2>
          <DriveForm defaultValues={toCreateRequest(drive)} onSubmit={handleUpdate} loading={updateDrive.isPending} />
        </div>
      )}

      <div className="bg-white rounded-lg shadow p-6 mb-6 space-y-4">
        {drive.description && (
          <div>
            <h3 className="text-sm font-medium text-gray-500">Description</h3>
            <p>{drive.description}</p>
          </div>
        )}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <h3 className="text-sm font-medium text-gray-500">Application Deadline</h3>
            <p>{new Date(drive.applicationDeadline).toLocaleString()}</p>
          </div>
          {drive.driveDate && (
            <div>
              <h3 className="text-sm font-medium text-gray-500">Drive Date</h3>
              <p>{new Date(drive.driveDate).toLocaleString()}</p>
            </div>
          )}
          {drive.minGpa != null && (
            <div>
              <h3 className="text-sm font-medium text-gray-500">Min GPA</h3>
              <p>{drive.minGpa}</p>
            </div>
          )}
          {drive.allowedGraduationYears && (
            <div>
              <h3 className="text-sm font-medium text-gray-500">Graduation Years</h3>
              <p>{drive.allowedGraduationYears.join(', ')}</p>
            </div>
          )}
        </div>
        {drive.requiredSkills && drive.requiredSkills.length > 0 && (
          <div>
            <h3 className="text-sm font-medium text-gray-500">Required Skills</h3>
            <div className="flex gap-2 mt-1">
              {drive.requiredSkills.map((s) => (
                <span key={s} className="px-2 py-0.5 bg-blue-100 text-blue-700 rounded text-xs">{s}</span>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-xl font-semibold">Job Posts</h2>
        <div className="flex items-center gap-3">
          <SearchInput value={jobSearch} onChange={(v) => { setJobSearch(v); setJobPostPage(0) }} placeholder="Search job posts..." />
          <select
            value={jobStatusFilter}
            onChange={(e) => { setJobStatusFilter(e.target.value); setJobPostPage(0) }}
            className="text-xs border rounded px-2 py-1.5"
          >
            <option value="">All Statuses</option>
            <option value="OPEN">Open</option>
            <option value="FILLED">Filled</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
          {isRecruiter && drive.status === 'ACTIVE' && (
            <button onClick={() => setShowJobForm(!showJobForm)} className="px-3 py-1 text-sm bg-blue-600 text-white rounded hover:bg-blue-700">
              {showJobForm ? 'Cancel' : 'Post a Job'}
            </button>
          )}
        </div>
      </div>

      {showJobForm && (
        <div className="bg-white rounded-lg shadow p-4 mb-4">
          <h3 className="font-medium mb-3">New Job Post</h3>
          <JobPostForm driveId={drive.id} onSubmit={handleCreateJob} loading={createJobPost.isPending} />
        </div>
      )}

      {postsLoading && <div className="flex justify-center py-6"><Spinner /></div>}

      {jobPosts && jobPosts.length === 0 && (
        <EmptyState title="No job posts yet" description={jobSearch || jobStatusFilter ? 'Try adjusting your search or filters.' : undefined} />
      )}

      <div className="space-y-3">
        {jobPosts?.map((post) => (
          <JobPostCard key={post.id} post={post} />
        ))}
      </div>

      {jobPostsPage && (
        <Pagination page={jobPostPage} totalPages={jobPostsPage.totalPages} onPageChange={setJobPostPage} />
      )}

      {isOwner && isPO && <POApplicationReview driveId={drive.id} />}
    </div>
  )
}

function POApplicationReview({ driveId }: { driveId: string }) {
  const [appPage, setAppPage] = useState(0)
  const { data: appsPage, isLoading, isError, refetch } = useApplicationsByDrive(driveId, appPage)
  const updateStatus = useUpdateApplicationStatus()

  if (isLoading) return <div className="flex justify-center py-6"><Spinner /></div>
  if (isError) return (
    <div className="text-center py-10">
      <p className="text-red-600 mb-2">Failed to load applications.</p>
      <button onClick={() => refetch()} className="text-sm px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">Try again</button>
    </div>
  )

  const applications = appsPage?.content ?? []
  const grouped = applications.reduce<Record<string, typeof applications>>((acc, app) => {
    const key = app.jobPost.title
    ;(acc[key] ??= []).push(app)
    return acc
  }, {}) ?? {}

  const handleStatus = async (appId: string, status: string) => {
    await updateStatus.mutateAsync({ id: appId, status })
    toast.success('Status updated')
  }

  return (
    <div className="mt-8">
      <h2 className="text-xl font-semibold mb-4">Applications Overview</h2>
      {Object.keys(grouped).length === 0 && <EmptyState title="No applications yet" />}
      {Object.entries(grouped).map(([title, apps]) => (
        <div key={title} className="mb-6">
          <h3 className="font-medium text-lg mb-2">{title} ({apps.length})</h3>
          <div className="space-y-2">
            {apps.map((app) => (
              <div key={app.id} className="bg-white rounded-lg shadow p-4 flex items-center justify-between">
                <div>
                  <p className="font-medium">{app.student.fullName}</p>
                  <p className="text-xs text-gray-500">
                    {app.student.email} · Applied {new Date(app.appliedAt).toLocaleDateString()}
                  </p>
                  {app.resumeSnapshotPath && (
                    <a
                      href={`${(import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1').replace(/\/$/, '')}/applications/${app.id}/resume`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-xs text-blue-600 hover:underline mt-1 inline-block"
                    >
                      Download Resume
                    </a>
                  )}
                </div>
                <div className="flex items-center gap-2">
                  <ApplicationStatusBadge status={app.status} />
                    <select
                      value={app.status}
                      onChange={(e) => handleStatus(app.id, e.target.value)}
                      disabled={updateStatus.isPending}
                      className="text-xs border rounded px-2 py-1"
                    >
                      {(ALLOWED_TRANSITIONS[app.status] ?? []).length === 0 ? (
                        <option value={app.status}>{app.status.replace('_', ' ')}</option>
                      ) : (
                        (ALLOWED_TRANSITIONS[app.status] ?? []).map((s) => (
                          <option key={s} value={s}>{s.replace('_', ' ')}</option>
                        ))
                      )}
                    </select>
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}

      {appsPage && (
        <Pagination page={appPage} totalPages={appsPage.totalPages} onPageChange={setAppPage} />
      )}
    </div>
  )
}
