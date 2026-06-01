import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getMyProfile, updateStudentProfile, updateRecruiterProfile, updatePlacementOfficerProfile } from '../api/profile'
import type { UpdateStudentProfileRequest, UpdateRecruiterProfileRequest, UpdatePlacementOfficerProfileRequest } from '../types/profile'

export function useMyProfile() {
  return useQuery({
    queryKey: ['profile'],
    queryFn: getMyProfile,
  })
}

export function useUpdateStudentProfile() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: UpdateStudentProfileRequest) => updateStudentProfile(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['profile'] })
    },
  })
}

export function useUpdateRecruiterProfile() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: UpdateRecruiterProfileRequest) => updateRecruiterProfile(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['profile'] })
    },
  })
}

export function useUpdatePlacementOfficerProfile() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: UpdatePlacementOfficerProfileRequest) => updatePlacementOfficerProfile(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['profile'] })
    },
  })
}
