import { useNotifications, useMarkAsRead, useMarkAllAsRead } from '../hooks/useNotifications'

export default function NotificationsPage() {
  const { data: notifications, isLoading } = useNotifications()
  const markRead = useMarkAsRead()
  const markAll = useMarkAllAsRead()

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

      {isLoading && <p className="text-gray-500 text-sm">Loading...</p>}

      {notifications && notifications.length === 0 && (
        <p className="text-gray-500 text-sm">No notifications yet.</p>
      )}

      <div className="space-y-3">
        {notifications?.map((n) => (
          <div
            key={n.id}
            className={`bg-white rounded-lg shadow p-4 ${!n.isRead ? 'border-l-4 border-blue-500' : ''}`}
            onClick={() => { if (!n.isRead) markRead.mutate(n.id) }}
          >
            <div className="flex items-start justify-between">
              <h3 className="font-medium">{n.title}</h3>
              <span className="text-xs text-gray-400">
                {new Date(n.createdAt).toLocaleString()}
              </span>
            </div>
            <p className="text-sm text-gray-600 mt-1">{n.message}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
