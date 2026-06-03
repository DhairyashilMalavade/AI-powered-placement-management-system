import { useAnalyticsOverview, useDrivePerformance, useApplicationFunnel } from '../hooks/useAnalytics'
import OverviewCards from '../components/OverviewCards'
import FunnelChart from '../components/FunnelChart'
import Spinner from '../components/shared/Spinner'

export default function AnalyticsDashboard() {
  const { data: overview, isLoading: overviewLoading } = useAnalyticsOverview()
  const { data: drivePerf, isLoading: driveLoading } = useDrivePerformance()
  const { data: funnel, isLoading: funnelLoading } = useApplicationFunnel()

  const loading = overviewLoading || driveLoading || funnelLoading

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner size="lg" />
      </div>
    )
  }

  const funnelData = funnel?.statusCounts
    ? Object.entries(funnel.statusCounts).map(([status, count]) => ({ status, count }))
    : []

  return (
    <div>
      <h1 className="text-xl font-bold mb-6">Analytics Dashboard</h1>

      <div className="space-y-8">
        <section>
          <h2 className="text-lg font-semibold mb-3">Platform Overview</h2>
          <OverviewCards data={overview} loading={overviewLoading} />
        </section>

        <section>
          <h2 className="text-lg font-semibold mb-3">Application Funnel</h2>
          <div className="bg-white rounded-lg p-4 border">
            <FunnelChart data={funnelData} />
          </div>
        </section>

        <section>
          <h2 className="text-lg font-semibold mb-3">Drive Performance</h2>
          <div className="overflow-x-auto bg-white rounded-lg border">
            <table className="w-full text-sm border-collapse">
              <thead>
                <tr className="border-b bg-gray-100">
                  <th className="text-left px-3 py-2 font-medium">Drive</th>
                  <th className="text-left px-3 py-2 font-medium">Posts</th>
                  <th className="text-left px-3 py-2 font-medium">Applicants</th>
                  <th className="text-left px-3 py-2 font-medium">Filled</th>
                  <th className="text-left px-3 py-2 font-medium">Avg Score</th>
                </tr>
              </thead>
              <tbody>
                {drivePerf?.map((drive) => (
                  <tr key={drive.driveId} className="border-b hover:bg-gray-50 transition">
                    <td className="px-3 py-2 font-medium">{drive.title}</td>
                    <td className="px-3 py-2">{drive.totalPosts}</td>
                    <td className="px-3 py-2">{drive.totalApplicants}</td>
                    <td className="px-3 py-2">{drive.totalFilled}</td>
                    <td className="px-3 py-2">{drive.averageScore !== null ? drive.averageScore.toFixed(1) : '—'}</td>
                  </tr>
                ))}
                {(!drivePerf || drivePerf.length === 0) && (
                  <tr>
                    <td colSpan={5} className="px-3 py-4 text-center text-gray-500">No drive data yet.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  )
}
