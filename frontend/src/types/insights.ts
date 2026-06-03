export interface SkillGapDTO {
  skill: string
  requiredCount: number
  matchedCount: number
  gapPercentage: number
}

export interface ScoreDistributionDTO {
  bucket: string
  count: number
}

export interface StatusCountDTO {
  status: string
  count: number
}

export interface JobPostOverviewDTO {
  scoreDistribution: ScoreDistributionDTO[]
  funnel: StatusCountDTO[]
}
