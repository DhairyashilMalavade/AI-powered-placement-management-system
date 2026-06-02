import apiClient from './client'
import type { ApiResponse, PagedResponse } from '../types/api'
import type { UserResponse } from '../types/user'

export interface SystemStats {
  totalUsers: number
  totalStudents: number
  totalRecruiters: number
  totalPOs: number
  totalAdmins: number
  activeDrives: number
  totalJobPosts: number
  totalApplications: number
  appliedApplications: number
  acceptedApplications: number
  rejectedApplications: number
  withdrawnApplications: number
}

export async function getUsers(role?: string, search?: string, page = 0, size = 20): Promise<PagedResponse<UserResponse>> {
  const params: Record<string, string | number> = { page, size }
  if (role) params.role = role
  if (search) params.search = search
  const res = await apiClient.get<ApiResponse<PagedResponse<UserResponse>>>('/admin/users', { params })
  return res.data.data
}

export async function getUser(id: string): Promise<UserResponse> {
  const res = await apiClient.get<ApiResponse<UserResponse>>(`/admin/users/${id}`)
  return res.data.data
}

export async function updateUserRole(id: string, role: string): Promise<UserResponse> {
  const res = await apiClient.patch<ApiResponse<UserResponse>>(`/admin/users/${id}/role`, { role })
  return res.data.data
}

export async function toggleUserActive(id: string): Promise<UserResponse> {
  const res = await apiClient.patch<ApiResponse<UserResponse>>(`/admin/users/${id}/active`)
  return res.data.data
}

export async function getStats(): Promise<SystemStats> {
  const res = await apiClient.get<ApiResponse<SystemStats>>('/admin/stats')
  return res.data.data
}
