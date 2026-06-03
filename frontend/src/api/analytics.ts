import apiClient from './client'
import type { ApiResponse } from '../types/api'
import type { OverviewDTO, DrivePerformanceDTO, FunnelDTO } from '../types/analytics'

export async function getAnalyticsOverview(): Promise<OverviewDTO> {
  const res = await apiClient.get<ApiResponse<OverviewDTO>>('/analytics/overview')
  return res.data.data
}

export async function getDrivePerformance(): Promise<DrivePerformanceDTO[]> {
  const res = await apiClient.get<ApiResponse<DrivePerformanceDTO[]>>('/analytics/drive-performance')
  return res.data.data
}

export async function getApplicationFunnel(): Promise<FunnelDTO> {
  const res = await apiClient.get<ApiResponse<FunnelDTO>>('/analytics/application-funnel')
  return res.data.data
}
