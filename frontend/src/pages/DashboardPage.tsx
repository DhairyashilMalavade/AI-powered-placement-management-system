import { useAuthStore } from '../store/authStore'
import Skeleton from '../components/shared/Skeleton'
import { useMyApplications } from '../hooks/useApplications'
import { useMyProfile } from '../hooks/useProfile'
import { useNotifications } from '../hooks/useNotifications'
import { useMyJobPosts } from '../hooks/useJobPosts'
import { useDrives } from '../hooks/useDrives'
import { useStats } from '../hooks/useAdmin'
import { useOverview } from '../hooks/useInsights'
import { useAnalyticsOverview } from '../hooks/useAnalytics'

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user)
  const role = user?.role ?? 'STUDENT'

  return (
    <div>
      <h1 className="text-2xl font-bold mb-1">Dashboard</h1>
      <p className="text-gray-600 mb-6">Welcome, {user?.fullName}!</p>

      {role === 'STUDENT' && <StudentDashboard />}
      {role === 'RECRUITER' && <RecruiterDashboard />}
      {role === 'PO' && <PODashboard />}
      {role === 'ADMIN' && <AdminDashboard />}
    </div>
  )
}

function StudentDashboard() {
  const { data: appsPage, isLoading: appsLoading, isError: appsError } = useMyApplications()
  const { data: profile, isError: profileError } = useMyProfile()
  const { data: notifsPage } = useNotifications()

  if (appsError || profileError) return <p className="text-red-600 text-sm">Failed to load dashboard data.</p>

  const notifications = notifsPage?.content ?? []
  const applications = appsPage?.content ?? []
  const unread = notifications.filter((n) => !n.isRead).length
  const activeApps = applications.filter((a) => a.status !== 'WITHDRAWN').length
  const scored = applications.filter((a) => a.aiScore !== null)
  const avgScore = scored.length > 0 ? (scored.reduce((s, a) => s + a.aiScore!, 0) / scored.length).toFixed(1) : null

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <StatCard title="Applications" value={appsLoading ? <Skeleton width="3rem" className="inline-block" /> : String(activeApps)} label="Active applications" />
      <StatCard title="Unread Notifications" value={String(unread)} label="Pending notifications" />
      <StatCard title="Resume" value={profile?.resumeFilePath ? 'Yes' : 'No'} label={profile?.resumeFilePath ? 'Uploaded' : 'Not uploaded'} />
      <StatCard title="Avg AI Score" value={avgScore !== null ? avgScore : '—'} label={`${scored.length} application${scored.length !== 1 ? 's' : ''} scored`} />
    </div>
  )
}

function RecruiterDashboard() {
  const { data: jobPostsPage, isLoading, isError } = useMyJobPosts()
  const { data: drivesPage, isError: drivesError } = useDrives()
  const { data: overview, isLoading: overviewLoading } = useOverview()

  if (isError || drivesError) return <p className="text-red-600 text-sm">Failed to load dashboard data.</p>

  const jobPosts = jobPostsPage?.content ?? []
  const drives = drivesPage?.content ?? []
  const activePosts = jobPosts.filter((jp) => jp.status === 'OPEN').length
  const scoredCount = overview?.scoreDistribution?.reduce((s, d) => s + d.count, 0) ?? 0

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <StatCard title="Your Job Posts" value={isLoading ? <Skeleton width="3rem" className="inline-block" /> : String(jobPosts.length)} label="Total posts" />
      <StatCard title="Open Positions" value={String(activePosts)} label="Currently accepting applications" />
      <StatCard title="Active Drives" value={String(drives.filter((d) => d.status === 'ACTIVE').length)} label="Drives you can post in" />
      <StatCard title="Applications Scored" value={overviewLoading ? <Skeleton width="3rem" className="inline-block" /> : String(scoredCount)} label="Total AI-scored applications" />
    </div>
  )
}

function PODashboard() {
  const { data: drivesPage, isLoading, isError } = useDrives()
  const { data: overview, isLoading: overviewLoading } = useAnalyticsOverview()

  if (isError) return <p className="text-red-600 text-sm">Failed to load dashboard data.</p>

  const drives = drivesPage?.content ?? []
  const active = drives.filter((d) => d.status === 'ACTIVE').length
  const draft = drives.filter((d) => d.status === 'DRAFT').length
  const closed = drives.filter((d) => d.status === 'CLOSED' || d.status === 'COMPLETED').length

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <StatCard title="Total Drives" value={isLoading ? <Skeleton width="3rem" className="inline-block" /> : String(drives.length)} label="All drives" />
      <StatCard title="Active" value={String(active)} label="Currently active" />
      <StatCard title="Draft / Closed" value={`${draft} / ${closed}`} label="Awaiting / Finished" />
      <StatCard title="Avg Score" value={overviewLoading ? <Skeleton width="3rem" className="inline-block" /> : overview?.averageScore?.toFixed(1) ?? '—'} label="Average AI score across all applications" />
    </div>
  )
}

function AdminDashboard() {
  const { data: stats, isLoading, isError } = useStats()

  if (isError) return <p className="text-red-600 text-sm">Failed to load dashboard data.</p>

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <StatCard title="Total Users" value={isLoading ? <Skeleton width="3rem" className="inline-block" /> : String(stats?.totalUsers ?? 0)} label={`${stats?.totalStudents ?? 0} students, ${stats?.totalRecruiters ?? 0} recruiters, ${stats?.totalPOs ?? 0} POs`} />
      <StatCard title="Active Drives" value={isLoading ? <Skeleton width="3rem" className="inline-block" /> : String(stats?.activeDrives ?? 0)} label={`${stats?.totalJobPosts ?? 0} total job posts`} />
      <StatCard title="Applications" value={isLoading ? <Skeleton width="3rem" className="inline-block" /> : String(stats?.totalApplications ?? 0)} label={`${stats?.appliedApplications ?? 0} pending · ${stats?.acceptedApplications ?? 0} accepted · ${stats?.rejectedApplications ?? 0} rejected`} />
    </div>
  )
}

function StatCard({ title, value, label }: { title: string; value: React.ReactNode; label: string }) {
  return (
    <div className="bg-white p-5 rounded-lg shadow">
      <h2 className="text-sm font-medium text-gray-500">{title}</h2>
      <p className="text-3xl font-bold mt-1">{value}</p>
      <p className="text-xs text-gray-400 mt-1">{label}</p>
    </div>
  )
}
