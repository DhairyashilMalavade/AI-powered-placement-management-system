import { useState } from 'react'
import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '../../store/authStore'
import { useUnreadCount } from '../../hooks/useNotifications'
import apiClient from '../../api/client'

export default function AppLayout() {
  const user = useAuthStore((s) => s.user)
  const navigate = useNavigate()
  const store = useAuthStore()
  const queryClient = useQueryClient()
  const { data: unreadCount } = useUnreadCount()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const handleLogout = async () => {
    try {
      await apiClient.post('/auth/logout')
    } catch {
      // proceed even if backend logout fails
    }
    queryClient.clear()
    store.logout()
    navigate('/login')
  }

  const role = user?.role ?? 'STUDENT'

  const navLinks = [
    ...(role !== 'ADMIN' ? [{ to: '/dashboard', label: 'Dashboard' }] : []),
    { to: '/drives', label: 'Drives' },
    ...(role === 'STUDENT' ? [{ to: '/applications', label: 'My Applications' }] : []),
    ...(role === 'PO' || role === 'RECRUITER' ? [{ to: '/applications', label: 'Applications' }] : []),
    ...(role === 'ADMIN' ? [
      { to: '/admin', label: 'Dashboard' },
      { to: '/admin/users', label: 'Users' },
    ] : []),
    ...(role === 'RECRUITER' || role === 'PO' || role === 'ADMIN' ? [{ to: '/insights', label: 'Insights' }] : []),
    ...(role === 'PO' || role === 'ADMIN' ? [{ to: '/analytics', label: 'Analytics' }] : []),
    {
      to: '/notifications',
      label: 'Notifications' + (unreadCount && unreadCount > 0 ? ` (${unreadCount})` : ''),
    },
    { to: '/profile', label: 'Profile' },
  ]

  return (
    <div className="min-h-screen flex">
      <a href="#main-content" className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:px-4 focus:py-2 focus:bg-white focus:text-blue-600 focus:rounded focus:shadow-lg">
        Skip to content
      </a>

      <button
        onClick={() => setSidebarOpen(!sidebarOpen)}
        aria-label={sidebarOpen ? 'Close sidebar' : 'Open sidebar'}
        aria-expanded={sidebarOpen}
        className="md:hidden fixed top-2 left-2 z-50 p-2 bg-gray-900 text-white rounded"
      >
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          {sidebarOpen ? (
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          ) : (
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
          )}
        </svg>
      </button>

      {sidebarOpen && (
        <div
          className="md:hidden fixed inset-0 z-30 bg-black/50"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <aside
        className={`fixed md:static inset-y-0 left-0 z-40 w-64 bg-gray-900 text-white flex flex-col transition-transform md:translate-x-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="p-4 text-lg font-bold border-b border-gray-700">
          Placement System
        </div>
        <nav aria-label="Main navigation" className="flex-1 p-4 space-y-2">
          {navLinks.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              onClick={() => setSidebarOpen(false)}
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
      <main id="main-content" className="flex-1 bg-gray-50 p-6 pt-14 md:pt-6">
        <Outlet />
      </main>
    </div>
  )
}
