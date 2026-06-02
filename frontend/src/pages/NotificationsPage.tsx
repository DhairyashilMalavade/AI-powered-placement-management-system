import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useNotifications, useMarkAsRead, useMarkAllAsRead } from '../hooks/useNotifications'
import Pagination from '../components/shared/Pagination'
import EmptyState from '../components/shared/EmptyState'
import Skeleton from '../components/shared/Skeleton'

export default function NotificationsPage() {
  const navigate = useNavigate()
  const [page, setPage] = useState(0)
  const { data: notifsPage, isLoading, isError, refetch } = useNotifications(page)
  const notifications = notifsPage?.content
  const markRead = useMarkAsRead()
  const markAll = useMarkAllAsRead()

  const handleClick = (n: { id: string; isRead: boolean; linkUrl: string | null }) => {
    if (!n.isRead) markRead.mutate(n.id)
    if (n.linkUrl) navigate(n.linkUrl)
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-2xl font-bold">Notifications</h1>
        {notifications && notifications.some((n) => !n.isRead) && (
          <button
            onClick={() => markAll.mutate()}
            className="text-sm px-3 py-1 bg-blue-100 text-blue-700 rounded hover:bg-blue-200"
          >
            Mark all as read
          </button>
        )}
      </div>

      {isLoading && <Skeleton lines={4} className="mt-4" />}

      {isError && (
        <div className="text-center py-10">
          <p className="text-red-600 mb-2">Failed to load notifications.</p>
          <button onClick={() => refetch()} className="text-sm px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">
            Try again
          </button>
        </div>
      )}

      {!isError && notifications && notifications.length === 0 && (
        <EmptyState title="No notifications yet" />
      )}

      <div className="space-y-3">
        {notifications?.map((n) => (
          <button
            key={n.id}
            onClick={() => handleClick(n)}
            className={`w-full text-left bg-white rounded-lg shadow p-4 cursor-pointer ${!n.isRead ? 'border-l-4 border-blue-500' : ''}`}
          >
            <div className="flex items-start justify-between">
              <h3 className="font-medium">{n.title}</h3>
              <span className="text-xs text-gray-400">
                {new Date(n.createdAt).toLocaleString()}
              </span>
            </div>
            <p className="text-sm text-gray-600 mt-1">{n.message}</p>
          </button>
        ))}
      </div>

      {notifsPage && (
        <Pagination page={page} totalPages={notifsPage.totalPages} onPageChange={setPage} />
      )}
    </div>
  )
}
