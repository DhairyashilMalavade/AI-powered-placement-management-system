import apiClient from './client'
import type { ApiResponse, PagedResponse } from '../types/api'
import type { NotificationResponse } from '../types/notification'

export async function getNotifications(page = 0, size = 50): Promise<PagedResponse<NotificationResponse>> {
  const res = await apiClient.get<ApiResponse<PagedResponse<NotificationResponse>>>('/notifications', { params: { page, size } })
  return res.data.data
}

export async function getUnreadCount(): Promise<number> {
  const res = await apiClient.get<ApiResponse<{ count: number }>>('/notifications/unread-count')
  return res.data.data.count
}

export async function markAsRead(id: string): Promise<void> {
  await apiClient.patch(`/notifications/${id}/read`)
}

export async function markAllAsRead(): Promise<void> {
  await apiClient.patch('/notifications/read-all')
}
