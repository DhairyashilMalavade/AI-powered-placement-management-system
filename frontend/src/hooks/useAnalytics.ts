import { useQuery } from '@tanstack/react-query'
import { getAnalyticsOverview, getDrivePerformance, getApplicationFunnel } from '../api/analytics'

export function useAnalyticsOverview() {
  return useQuery({
    queryKey: ['analytics', 'overview'],
    queryFn: getAnalyticsOverview,
  })
}

export function useDrivePerformance() {
  return useQuery({
    queryKey: ['analytics', 'drive-performance'],
    queryFn: getDrivePerformance,
  })
}

export function useApplicationFunnel() {
  return useQuery({
    queryKey: ['analytics', 'funnel'],
    queryFn: getApplicationFunnel,
  })
}
