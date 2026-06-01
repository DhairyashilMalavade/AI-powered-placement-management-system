import apiClient from './client'
import type { ApiResponse } from '../types/api'
import type { CreateDriveRequest, DriveResponse } from '../types/drive'

export async function getDrives(): Promise<DriveResponse[]> {
  const res = await apiClient.get<ApiResponse<DriveResponse[]>>('/drives')
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
