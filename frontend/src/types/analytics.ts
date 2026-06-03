export interface OverviewDTO {
  totalDrives: number
  totalJobPosts: number
  totalApplications: number
  totalPlacements: number
  averageScore: number | null
}

export interface DrivePerformanceDTO {
  driveId: string
  title: string
  totalPosts: number
  totalApplicants: number
  totalFilled: number
  averageScore: number | null
}

export interface FunnelDTO {
  statusCounts: Record<string, number>
  total: number
}
