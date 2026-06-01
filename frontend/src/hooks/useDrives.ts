import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getDrives, getDrive, createDrive, updateDrive, deleteDrive, updateDriveStatus } from '../api/drives'
import type { CreateDriveRequest } from '../types/drive'

export function useDrives() {
  return useQuery({
    queryKey: ['drives'],
    queryFn: getDrives,
  })
}

export function useDrive(id: string) {
  return useQuery({
    queryKey: ['drives', id],
    queryFn: () => getDrive(id),
    enabled: !!id,
  })
}

export function useCreateDrive() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateDriveRequest) => createDrive(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['drives'] }),
  })
}

export function useUpdateDrive() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<CreateDriveRequest> }) => updateDrive(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['drives'] }),
  })
}

export function useDeleteDrive() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteDrive(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['drives'] }),
  })
}

export function useUpdateDriveStatus() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => updateDriveStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['drives'] }),
  })
}
