export interface NotificationResponse {
  id: string
  userId: string
  title: string
  message: string
  isRead: boolean
  linkUrl: string | null
  createdAt: string
}
