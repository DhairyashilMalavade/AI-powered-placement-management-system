import apiClient from './client'
import type { ApiResponse, PagedResponse } from '../types/api'
import type { CreateDriveRequest, DriveResponse } from '../types/drive'

export async function getDrives(search?: string, status?: string, page = 0, size = 20): Promise<PagedResponse<DriveResponse>> {
  const params: Record<string, string | number> = { page, size }
  if (search) params.search = search
  if (status) params.status = status
  const res = await apiClient.get<ApiResponse<PagedResponse<DriveResponse>>>('/drives', { params })
  return res.data.data
}

export async function getDrive(id: string): Promise<DriveResponse> {
  const res = await apiClient.get<ApiResponse<DriveResponse>>(`/drives/${id}`)
  return res.data.data
}

export async function createDrive(data: CreateDriveRequest): Promise<DriveResponse> {
  const res = await apiClient.post<ApiResponse<DriveResponse>>('/drives', data)
  return res.data.data
}

export async function updateDrive(id: string, data: Partial<CreateDriveRequest>): Promise<DriveResponse> {
  const res = await apiClient.put<ApiResponse<DriveResponse>>(`/drives/${id}`, data)
  return res.data.data
}

export async function deleteDrive(id: string): Promise<void> {
  await apiClient.delete(`/drives/${id}`)
}

export async function updateDriveStatus(id: string, status: string): Promise<DriveResponse> {
  const res = await apiClient.patch<ApiResponse<DriveResponse>>(`/drives/${id}/status`, { status })
  return res.data.data
}
