import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import { useUnreadCount } from '../../hooks/useNotifications'

export default function AppLayout() {
  const user = useAuthStore((s) => s.user)
  const navigate = useNavigate()
  const store = useAuthStore()
  const { data: unreadCount } = useUnreadCount()

  const handleLogout = () => {
    store.logout()
    navigate('/login')
  }

  const role = user?.role ?? 'STUDENT'

  const navLinks = [
    { to: '/dashboard', label: 'Dashboard' },
    { to: '/drives', label: 'Drives' },
    ...(role === 'STUDENT' ? [{ to: '/applications', label: 'My Applications' }] : []),
    ...(role === 'PO' || role === 'RECRUITER' ? [{ to: '/applications', label: 'Applications' }] : []),
    {
      to: '/notifications',
      label: 'Notifications' + (unreadCount && unreadCount > 0 ? ` (${unreadCount})` : ''),
    },
    { to: '/profile', label: 'Profile' },
  ]

  return (
    <div className="min-h-screen flex">
      <aside className="w-64 bg-gray-900 text-white flex flex-col">
        <div className="p-4 text-lg font-bold border-b border-gray-700">
          Placement System
        </div>
        <nav className="flex-1 p-4 space-y-2">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className="block px-3 py-2 rounded hover:bg-gray-700 transition"
            >
              {link.label}
            </Link>
          ))}
        </nav>
        <div className="p-4 border-t border-gray-700">
          <div className="text-sm mb-2">{user?.fullName}</div>
          <button
            onClick={handleLogout}
            className="w-full px-3 py-2 text-sm bg-red-600 rounded hover:bg-red-700 transition"
          >
            Logout
          </button>
        </div>
      </aside>
      <main className="flex-1 bg-gray-50 p-6">
        <Outlet />
      </main>
    </div>
  )
}
