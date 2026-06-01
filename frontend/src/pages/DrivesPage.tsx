import { useState } from 'react'
import { useAuthStore } from '../store/authStore'
import { useDrives, useCreateDrive } from '../hooks/useDrives'
import DriveCard from '../components/drives/DriveCard'
import DriveForm from '../components/drives/DriveForm'
import type { CreateDriveRequest } from '../types/drive'

export default function DrivesPage() {
  const user = useAuthStore((s) => s.user)
  const { data: drives, isLoading } = useDrives()
  const createDrive = useCreateDrive()
  const [showForm, setShowForm] = useState(false)

  const handleCreate = async (data: CreateDriveRequest) => {
    await createDrive.mutateAsync(data)
    setShowForm(false)
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Drives</h1>
        {user?.role === 'PO' && (
          <button
            onClick={() => setShowForm(!showForm)}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            {showForm ? 'Cancel' : 'Create Drive'}
          </button>
        )}
      </div>

      {showForm && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="font-semibold mb-4">New Drive</h2>
          <DriveForm onSubmit={handleCreate} loading={createDrive.isPending} />
        </div>
      )}

      {isLoading && <p className="text-gray-500">Loading drives...</p>}

      {drives && drives.length === 0 && (
        <p className="text-gray-500">No drives yet.</p>
      )}

      <div className="space-y-3">
        {drives?.map((drive) => (
          <DriveCard key={drive.id} drive={drive} />
        ))}
      </div>
    </div>
  )
}
