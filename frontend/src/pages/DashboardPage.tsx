import { useAuthStore } from '../store/authStore'
import { useMyApplications } from '../hooks/useApplications'
import { useMyProfile } from '../hooks/useProfile'
import { useNotifications } from '../hooks/useNotifications'
import { useMyJobPosts } from '../hooks/useJobPosts'
import { useDrives } from '../hooks/useDrives'

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
  const { data: applications, isLoading: appsLoading } = useMyApplications()
  const { data: profile } = useMyProfile()
  const { data: notifications } = useNotifications()

  const unread = notifications?.filter((n) => !n.isRead).length ?? 0
  const activeApps = applications?.filter((a) => a.status !== 'WITHDRAWN').length ?? 0

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <StatCard title="Applications" value={appsLoading ? '...' : String(activeApps)} label="Active applications" />
      <StatCard title="Unread Notifications" value={String(unread)} label="Pending notifications" />
      <StatCard title="Resume" value={profile?.resumeFilePath ? 'Yes' : 'No'} label={profile?.resumeFilePath ? 'Uploaded' : 'Not uploaded'} />
    </div>
  )
}

function RecruiterDashboard() {
  const { data: jobPosts, isLoading } = useMyJobPosts()
  const { data: drives } = useDrives()

  const activePosts = jobPosts?.filter((jp) => jp.status === 'OPEN').length ?? 0

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <StatCard title="Your Job Posts" value={isLoading ? '...' : String(jobPosts?.length ?? 0)} label="Total posts" />
      <StatCard title="Open Positions" value={String(activePosts)} label="Currently accepting applications" />
      <StatCard title="Active Drives" value={String(drives?.filter((d) => d.status === 'ACTIVE').length ?? 0)} label="Drives you can post in" />
    </div>
  )
}

function PODashboard() {
  const { data: drives, isLoading } = useDrives()

  const active = drives?.filter((d) => d.status === 'ACTIVE').length ?? 0
  const draft = drives?.filter((d) => d.status === 'DRAFT').length ?? 0
  const closed = drives?.filter((d) => d.status === 'CLOSED' || d.status === 'COMPLETED').length ?? 0

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <StatCard title="Total Drives" value={isLoading ? '...' : String(drives?.length ?? 0)} label="All drives" />
      <StatCard title="Active" value={String(active)} label="Currently active" />
      <StatCard title="Draft / Closed" value={`${draft} / ${closed}`} label="Awaiting / Finished" />
    </div>
  )
}

function AdminDashboard() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <StatCard title="Users" value="—" label="User management" />
      <StatCard title="Drives" value="—" label="Drive overview" />
      <StatCard title="System" value="Healthy" label="All systems operational" />
    </div>
  )
}

function StatCard({ title, value, label }: { title: string; value: string; label: string }) {
  return (
    <div className="bg-white p-5 rounded-lg shadow">
      <h2 className="text-sm font-medium text-gray-500">{title}</h2>
      <p className="text-3xl font-bold mt-1">{value}</p>
      <p className="text-xs text-gray-400 mt-1">{label}</p>
    </div>
  )
}
