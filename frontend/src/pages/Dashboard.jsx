import { useState, useEffect } from 'react'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { CheckCircle, Clock, AlertTriangle, ListTodo } from 'lucide-react'
import { analyticsAPI } from '../services/api'

const COLORS = { 'To Do': '#3B82F6', 'In Progress': '#F59E0B', 'Done': '#10B981' }

export default function Dashboard() {
  const [progress, setProgress] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const sprintId = 1

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await analyticsAPI.getSprintProgress(sprintId)
        setProgress(res.data.data)
      } catch {
        setError('Failed to load dashboard data')
        setProgress({ totalTasks: 0, todoCount: 0, inProgressCount: 0, doneCount: 0, progressPercentage: 0 })
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [sprintId])

  if (loading) return <div className="page-container"><p>Loading dashboard...</p></div>

  const stats = [
    { label: 'Total Tasks', value: progress?.totalTasks || 0, icon: ListTodo, color: 'var(--color-primary)' },
    { label: 'In Progress', value: progress?.inProgressCount || 0, icon: Clock, color: 'var(--color-warning)' },
    { label: 'Done', value: progress?.doneCount || 0, icon: CheckCircle, color: 'var(--color-success)' },
    { label: 'To Do', value: progress?.todoCount || 0, icon: AlertTriangle, color: 'var(--color-info)' },
  ]

  const chartData = [
    { name: 'To Do', count: progress?.todoCount || 0, fill: '#3B82F6' },
    { name: 'In Progress', count: progress?.inProgressCount || 0, fill: '#F59E0B' },
    { name: 'Done', count: progress?.doneCount || 0, fill: '#10B981' },
  ]

  const percent = progress?.progressPercentage || 0
  const circumference = 2 * Math.PI * 60
  const offset = circumference - (percent / 100) * circumference

  return (
    <div className="page-container">
      <h1 style={{ marginBottom: 32 }}>Dashboard</h1>

      {error && (
        <div style={{ background: '#FEE2E2', color: '#DC2626', padding: '12px 16px', borderRadius: 8, marginBottom: 24, fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 24, marginBottom: 32 }}>
        {stats.map(s => (
          <div key={s.label} className="stat-card">
            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              <div style={{ width: 48, height: 48, borderRadius: 12, background: `${s.color}15`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <s.icon size={24} color={s.color} />
              </div>
              <div>
                <div style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--color-text)' }}>{s.value}</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)' }}>{s.label}</div>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
        <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <h3 style={{ marginBottom: 24 }}>Sprint Progress</h3>
          <div style={{ position: 'relative', width: 160, height: 160 }}>
            <svg width="160" height="160" viewBox="0 0 160 160">
              <circle cx="80" cy="80" r="60" fill="none" stroke="var(--color-border)" strokeWidth="12" />
              <circle cx="80" cy="80" r="60" fill="none" stroke="var(--color-primary)" strokeWidth="12"
                strokeDasharray={circumference} strokeDashoffset={offset} strokeLinecap="round"
                transform="rotate(-90 80 80)" style={{ transition: 'stroke-dashoffset 0.5s ease' }} />
            </svg>
            <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', textAlign: 'center' }}>
              <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-primary)' }}>{percent}%</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)' }}>Complete</div>
            </div>
          </div>
          <p style={{ marginTop: 16, color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
            {progress?.doneCount || 0} of {progress?.totalTasks || 0} tasks completed
          </p>
        </div>

        <div className="card">
          <h3 style={{ marginBottom: 24 }}>Task Distribution</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border)" />
              <XAxis dataKey="name" tick={{ fontSize: 13, fill: 'var(--color-text-secondary)' }} />
              <YAxis tick={{ fontSize: 13, fill: 'var(--color-text-secondary)' }} allowDecimals={false} />
              <Tooltip contentStyle={{ borderRadius: 8, border: '1px solid var(--color-border)', boxShadow: 'var(--shadow-sm)' }} />
              <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                {chartData.map((entry, i) => (
                  <Cell key={i} fill={entry.fill} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
