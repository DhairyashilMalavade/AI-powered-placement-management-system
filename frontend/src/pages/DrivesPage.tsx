import { useAuthStore } from '../store/authStore'

export default function DrivesPage() {
  const user = useAuthStore((s) => s.user)

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Drives</h1>
      <p className="text-gray-600">Placement drives will be listed here.</p>
      {user?.role === 'PO' && (
        <button className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
          Create Drive
        </button>
      )}
    </div>
  )
}
