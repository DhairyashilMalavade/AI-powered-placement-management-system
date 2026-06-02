import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { getDrives, getDrive, createDrive, updateDrive, deleteDrive, updateDriveStatus } from '../api/drives'
import toast from 'react-hot-toast'
import type { CreateDriveRequest } from '../types/drive'

export function useDrives(search?: string, status?: string, page = 0, size = 20) {
  return useQuery({
    queryKey: ['drives', search, status, page, size],
    queryFn: () => getDrives(search, status, page, size),
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
    onSuccess: () => {
      toast.success('Drive created')
      qc.invalidateQueries({ queryKey: ['drives'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to create drive')
    },
  })
}

export function useUpdateDrive() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<CreateDriveRequest> }) => updateDrive(id, data),
    onSuccess: () => {
      toast.success('Drive updated')
      qc.invalidateQueries({ queryKey: ['drives'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to update drive')
    },
  })
}

export function useDeleteDrive() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteDrive(id),
    onSuccess: () => {
      toast.success('Drive deleted')
      qc.invalidateQueries({ queryKey: ['drives'] })
      qc.invalidateQueries({ queryKey: ['job-posts'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to delete drive')
    },
  })
}

export function useUpdateDriveStatus() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => updateDriveStatus(id, status),
    onSuccess: () => {
      toast.success('Status updated')
      qc.invalidateQueries({ queryKey: ['drives'] })
      qc.invalidateQueries({ queryKey: ['job-posts'] })
    },
    onError: (err: Error) => {
      toast.error(err.message || 'Failed to update status')
    },
  })
}
