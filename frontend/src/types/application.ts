import type { JobPostResponse } from './jobPost'
import type { UserResponse } from './user'

export interface CreateApplicationRequest {
  jobPostId: string
}

export interface ScoredApplicationResponse {
  applicationId: string
  studentName: string
  studentId: string
  status: string
  aiScore: number | null
  scoringFeedback: string | null
  rank: number | null
  scoringRationale: string | null
  scoringVersion: string | null
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
