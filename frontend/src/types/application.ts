import type { JobPostResponse } from './jobPost'
import type { UserResponse } from './user'

export interface CreateApplicationRequest {
  jobPostId: string
}

export interface ApplicationResponse {
  id: string
  student: UserResponse
  jobPost: JobPostResponse
  status: string
  aiScore: number | null
  resumeSnapshotPath: string | null
  appliedAt: string
  updatedAt: string
}
