import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '../api/notifications'
import toast from 'react-hot-toast'

export function useNotifications(page = 0, size = 50) {
  return useQuery({
    queryKey: ['notifications', page, size],
    queryFn: () => getNotifications(page, size),
  })
}

export function useUnreadCount() {
  return useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: getUnreadCount,
    refetchInterval: 60000,
  })
}

export function useMarkAsRead() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => markAsRead(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['notifications'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to mark as read')
    },
  })
}

export function useMarkAllAsRead() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: markAllAsRead,
    onSuccess: () => {
      toast.success('All marked as read')
      qc.invalidateQueries({ queryKey: ['notifications'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to mark all as read')
    },
  })
}
