import { useQuery } from '@tanstack/react-query'
import { getSkillGaps, getOverview } from '../api/insights'

export function useSkillGaps() {
  return useQuery({
    queryKey: ['insights', 'skill-gaps'],
    queryFn: getSkillGaps,
  })
}

export function useOverview() {
  return useQuery({
    queryKey: ['insights', 'overview'],
    queryFn: getOverview,
  })
}
