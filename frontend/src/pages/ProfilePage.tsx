import { useState } from 'react'
import { useAuthStore } from '../store/authStore'
import { useMyProfile, useUpdateStudentProfile, useUpdateRecruiterProfile, useUpdatePlacementOfficerProfile } from '../hooks/useProfile'
import { useUploadResume } from '../hooks/useResume'
import Spinner from '../components/shared/Spinner'
import toast from 'react-hot-toast'

export default function ProfilePage() {
  const user = useAuthStore((s) => s.user)
  const role = user?.role ?? 'STUDENT'
  const { data: profile, isLoading, isError, refetch } = useMyProfile()
  const uploadResume = useUploadResume()

  if (isLoading) return <div className="flex justify-center py-10"><Spinner size="lg" /></div>
  if (isError) return (
    <div className="text-center py-10">
      <p className="text-red-600 mb-2">Failed to load profile.</p>
      <button onClick={() => refetch()} className="text-sm px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">Try again</button>
    </div>
  )
  if (!profile) return <p className="text-gray-500">Profile not found.</p>

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Profile</h1>

      <div className="bg-white rounded-lg shadow p-6 max-w-xl space-y-6">
        <section>
          <h2 className="text-lg font-semibold mb-2">Account</h2>
          <div className="space-y-1 text-sm">
            <p><span className="text-gray-500">Name:</span> {user?.fullName}</p>
            <p><span className="text-gray-500">Email:</span> {user?.email}</p>
            <p><span className="text-gray-500">Role:</span> {user?.role}</p>
          </div>
        </section>

        {role === 'STUDENT' && <StudentProfileSection profile={profile} />}
        {role === 'RECRUITER' && <RecruiterProfileSection profile={profile} />}
        {role === 'PO' && <POProfileSection profile={profile} />}

        {role === 'STUDENT' && (
          <section>
            <h2 className="text-lg font-semibold mb-2">Resume</h2>
            {profile.resumeFilePath ? (
              <div className="flex items-center gap-3">
                <span className="text-sm text-green-700">Resume uploaded</span>
                <a
                  href={`${(import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1').replace(/\/$/, '')}/resumes/${profile.resumeFilePath}`}
                  className="text-sm text-blue-600 hover:underline"
                >
                  Download
                </a>
              </div>
            ) : (
              <p className="text-sm text-gray-500 mb-2">No resume uploaded.</p>
            )}
            <label className="inline-block mt-2 px-4 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 cursor-pointer">
              {uploadResume.isPending ? 'Uploading...' : 'Upload Resume'}
              <input
                type="file"
                accept=".pdf"
                className="hidden"
                disabled={uploadResume.isPending}
                onChange={(e) => {
                  const file = e.target.files?.[0]
                  if (file) uploadResume.mutate(file)
                }}
              />
            </label>
          </section>
        )}
      </div>
    </div>
  )
}

function StudentProfileSection({ profile }: { profile: import('../types/profile').ProfileResponse }) {
  const updateMutation = useUpdateStudentProfile()
  const [editing, setEditing] = useState(false)
  const [collegeName, setCollegeName] = useState(profile.collegeName ?? '')
  const [graduationYear, setGraduationYear] = useState(profile.graduationYear ?? new Date().getFullYear())
  const [major, setMajor] = useState(profile.major ?? '')
  const [gpa, setGpa] = useState(profile.gpa?.toString() ?? '')
  const [skills, setSkills] = useState(profile.skills?.join(', ') ?? '')
  const [phone, setPhone] = useState(profile.phone ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})

  const validate = () => {
    const errs: Record<string, string> = {}
    if (!collegeName.trim()) errs.collegeName = 'College name is required'
    const year = parseInt(String(graduationYear))
    if (isNaN(year) || year < 1900 || year > 2100) errs.graduationYear = 'Enter a valid year (1900–2100)'
    if (gpa) {
      const g = parseFloat(gpa)
      if (isNaN(g) || g < 0 || g > 10) errs.gpa = 'GPA must be between 0 and 10'
    }
    if (phone && !/^\+?[\d\s\-()]{7,20}$/.test(phone)) errs.phone = 'Enter a valid phone number'
    setErrors(errs)
    return Object.keys(errs).length === 0
  }

  const handleSave = async () => {
    if (!validate()) return
    await updateMutation.mutateAsync({
      collegeName,
      graduationYear,
      major,
      gpa: gpa ? parseFloat(gpa) : null,
      skills: skills ? skills.split(',').map((s) => s.trim()) : [],
      phone,
    })
    toast.success('Profile updated')
    setEditing(false)
  }

  if (!editing) {
    return (
      <section>
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-lg font-semibold">Student Details</h2>
          <button onClick={() => setEditing(true)} className="text-sm text-blue-600 hover:underline">Edit</button>
        </div>
        <div className="space-y-1 text-sm">
          <p><span className="text-gray-500">College:</span> {profile.collegeName}</p>
          <p><span className="text-gray-500">Graduation Year:</span> {profile.graduationYear}</p>
          <p><span className="text-gray-500">Major:</span> {profile.major}</p>
          {profile.gpa != null && <p><span className="text-gray-500">GPA:</span> {profile.gpa}</p>}
          {profile.skills && profile.skills.length > 0 && (
            <p><span className="text-gray-500">Skills:</span> {profile.skills.join(', ')}</p>
          )}
          {profile.phone && <p><span className="text-gray-500">Phone:</span> {profile.phone}</p>}
        </div>
      </section>
    )
  }

  return (
    <section>
      <h2 className="text-lg font-semibold mb-3">Edit Student Details</h2>
      <div className="space-y-3">
        <Input label="College" value={collegeName} onChange={setCollegeName} error={errors.collegeName} />
        <Input label="Graduation Year" type="number" value={String(graduationYear)} onChange={(v) => setGraduationYear(parseInt(v) || new Date().getFullYear())} error={errors.graduationYear} />
        <Input label="Major" value={major} onChange={setMajor} />
        <Input label="GPA" value={gpa} onChange={setGpa} error={errors.gpa} />
        <Input label="Skills (comma-separated)" value={skills} onChange={setSkills} />
        <Input label="Phone" value={phone} onChange={setPhone} error={errors.phone} />
        <div className="flex gap-2">
          <button onClick={handleSave} disabled={updateMutation.isPending} className="px-4 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50">
            {updateMutation.isPending ? 'Saving...' : 'Save'}
          </button>
          <button onClick={() => setEditing(false)} className="px-4 py-2 text-sm border rounded hover:bg-gray-50">Cancel</button>
        </div>
      </div>
    </section>
  )
}

function RecruiterProfileSection({ profile }: { profile: import('../types/profile').ProfileResponse }) {
  const updateMutation = useUpdateRecruiterProfile()
  const [editing, setEditing] = useState(false)
  const [companyName, setCompanyName] = useState(profile.companyName ?? '')
  const [companyWebsite, setCompanyWebsite] = useState(profile.companyWebsite ?? '')
  const [companyDescription, setCompanyDescription] = useState(profile.companyDescription ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})

  const validate = () => {
    const errs: Record<string, string> = {}
    if (!companyName.trim()) errs.companyName = 'Company name is required'
    if (companyWebsite && !/^https?:\/\/.+/.test(companyWebsite)) errs.companyWebsite = 'Enter a valid URL (e.g. https://example.com)'
    setErrors(errs)
    return Object.keys(errs).length === 0
  }

  const handleSave = async () => {
    if (!validate()) return
    await updateMutation.mutateAsync({ companyName, companyWebsite, companyDescription })
    toast.success('Profile updated')
    setEditing(false)
  }

  if (!editing) {
    return (
      <section>
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-lg font-semibold">Company Details</h2>
          <button onClick={() => setEditing(true)} className="text-sm text-blue-600 hover:underline">Edit</button>
        </div>
        <div className="space-y-1 text-sm">
          <p><span className="text-gray-500">Company:</span> {profile.companyName}</p>
          {profile.companyWebsite && <p><span className="text-gray-500">Website:</span> {profile.companyWebsite}</p>}
          {profile.companyDescription && <p><span className="text-gray-500">Description:</span> {profile.companyDescription}</p>}
        </div>
      </section>
    )
  }

  return (
    <section>
      <h2 className="text-lg font-semibold mb-3">Edit Company Details</h2>
      <div className="space-y-3">
        <Input label="Company Name" value={companyName} onChange={setCompanyName} error={errors.companyName} />
        <Input label="Website" value={companyWebsite} onChange={setCompanyWebsite} error={errors.companyWebsite} />
        <Input label="Description" value={companyDescription} onChange={setCompanyDescription} />
        <div className="flex gap-2">
          <button onClick={handleSave} disabled={updateMutation.isPending} className="px-4 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50">
            {updateMutation.isPending ? 'Saving...' : 'Save'}
          </button>
          <button onClick={() => setEditing(false)} className="px-4 py-2 text-sm border rounded hover:bg-gray-50">Cancel</button>
        </div>
      </div>
    </section>
  )
}

function POProfileSection({ profile }: { profile: import('../types/profile').ProfileResponse }) {
  const updateMutation = useUpdatePlacementOfficerProfile()
  const [editing, setEditing] = useState(false)
  const [collegeName, setCollegeName] = useState(profile.collegeName ?? '')
  const [department, setDepartment] = useState(profile.department ?? '')
  const [errors, setErrors] = useState<Record<string, string>>({})

  const validate = () => {
    const errs: Record<string, string> = {}
    if (!collegeName.trim()) errs.collegeName = 'College name is required'
    setErrors(errs)
    return Object.keys(errs).length === 0
  }

  const handleSave = async () => {
    if (!validate()) return
    await updateMutation.mutateAsync({ collegeName, department })
    toast.success('Profile updated')
    setEditing(false)
  }

  if (!editing) {
    return (
      <section>
        <div className="flex items-center justify-between mb-2">
          <h2 className="text-lg font-semibold">PO Details</h2>
          <button onClick={() => setEditing(true)} className="text-sm text-blue-600 hover:underline">Edit</button>
        </div>
        <div className="space-y-1 text-sm">
          <p><span className="text-gray-500">College:</span> {profile.collegeName}</p>
          {profile.department && <p><span className="text-gray-500">Department:</span> {profile.department}</p>}
        </div>
      </section>
    )
  }

  return (
    <section>
      <h2 className="text-lg font-semibold mb-3">Edit PO Details</h2>
      <div className="space-y-3">
        <Input label="College Name" value={collegeName} onChange={setCollegeName} error={errors.collegeName} />
        <Input label="Department" value={department} onChange={setDepartment} />
        <div className="flex gap-2">
          <button onClick={handleSave} disabled={updateMutation.isPending} className="px-4 py-2 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50">
            {updateMutation.isPending ? 'Saving...' : 'Save'}
          </button>
          <button onClick={() => setEditing(false)} className="px-4 py-2 text-sm border rounded hover:bg-gray-50">Cancel</button>
        </div>
      </div>
    </section>
  )
}

function Input({ label, value, onChange, type = 'text', error }: { label: string; value: string; onChange: (v: string) => void; type?: string; error?: string }) {
  return (
    <div>
      <label className="block text-sm font-medium mb-1">{label}</label>
      {type === 'textarea' ? (
        <textarea value={value} onChange={(e) => onChange(e.target.value)} className="w-full px-3 py-2 border rounded-lg text-sm" rows={3} />
      ) : (
        <input type={type} value={value} onChange={(e) => onChange(e.target.value)} className={`w-full px-3 py-2 border rounded-lg text-sm ${error ? 'border-red-500' : ''}`} />
      )}
      {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
    </div>
  )
}
