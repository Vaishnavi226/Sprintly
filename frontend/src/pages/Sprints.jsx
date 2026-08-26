import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Plus, ArrowLeft, Timer } from 'lucide-react'
import Card from '../components/Card'
import Button from '../components/Button'
import Badge from '../components/Badge'
import Modal from '../components/Modal'
import Input from '../components/Input'
import { sprintAPI } from '../services/api'

export default function Sprints() {
  const { projectId } = useParams()
  const navigate = useNavigate()
  const [sprints, setSprints] = useState([])
  const [loading, setLoading] = useState(true)
  const [isOpen, setIsOpen] = useState(false)
  const [form, setForm] = useState({ name: '', startDate: '', endDate: '' })
  const [error, setError] = useState('')

  useEffect(() => {
    const fetchSprints = async () => {
      try {
        const res = await sprintAPI.getByProject(projectId)
        setSprints(res.data.data || [])
      } catch {
        setError('Failed to load sprints')
      } finally {
        setLoading(false)
      }
    }
    fetchSprints()
  }, [projectId])

  const handleCreate = async e => {
    e.preventDefault()
    try {
      const res = await sprintAPI.create(projectId, {
        name: form.name,
        startDate: form.startDate,
        endDate: form.endDate,
      })
      setSprints([...sprints, res.data.data])
      setForm({ name: '', startDate: '', endDate: '' })
      setIsOpen(false)
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create sprint')
    }
  }

  if (loading) return <div className="page-container"><p>Loading sprints...</p></div>

  return (
    <div className="page-container">
      <button onClick={() => navigate('/projects')}
        style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8, color: 'var(--color-text-secondary)', marginBottom: 24, fontSize: '0.875rem' }}>
        <ArrowLeft size={18} /> Back to Projects
      </button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 32 }}>
        <h1>Sprints</h1>
        <Button onClick={() => setIsOpen(true)}>
          <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}><Plus size={18} /> New Sprint</span>
        </Button>
      </div>

      {error && (
        <div style={{ background: '#FEE2E2', color: '#DC2626', padding: '12px 16px', borderRadius: 8, marginBottom: 24, fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 24 }}>
        {sprints.map(s => (
          <Card key={s.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/sprints/${s.id}`)}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <Timer size={20} color="var(--color-primary)" />
                <h3>{s.name}</h3>
              </div>
              <Badge>{s.status}</Badge>
            </div>
            <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem', marginBottom: 16 }}>
              {s.goal || 'No goal set'}
            </p>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: 'var(--color-text-secondary)' }}>
              <span>{s.startDate} → {s.endDate}</span>
            </div>
          </Card>
        ))}
      </div>

      <Modal isOpen={isOpen} onClose={() => setIsOpen(false)} title="Create New Sprint">
        <form onSubmit={handleCreate}>
          <Input label="Sprint Name" placeholder="e.g. Sprint 1" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
          <Input label="Start Date" type="date" value={form.startDate} onChange={e => setForm({ ...form, startDate: e.target.value })} required />
          <Input label="End Date" type="date" value={form.endDate} onChange={e => setForm({ ...form, endDate: e.target.value })} required />
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 8 }}>
            <button className="btn-secondary" type="button" onClick={() => setIsOpen(false)}>Cancel</button>
            <button className="btn-primary" type="submit">Create Sprint</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
