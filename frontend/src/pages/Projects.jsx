import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus } from 'lucide-react'
import Card from '../components/Card'
import Button from '../components/Button'
import Modal from '../components/Modal'
import Input from '../components/Input'
import { projectAPI } from '../services/api'

export default function Projects() {
  const [projects, setProjects] = useState([])
  const [loading, setLoading] = useState(true)
  const [isOpen, setIsOpen] = useState(false)
  const [form, setForm] = useState({ name: '', description: '' })
  const [error, setError] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const res = await projectAPI.getAll()
        setProjects(res.data.data || [])
      } catch {
        setError('Failed to load projects')
      } finally {
        setLoading(false)
      }
    }
    fetchProjects()
  }, [])

  const handleCreate = async e => {
    e.preventDefault()
    try {
      const res = await projectAPI.create({ name: form.name, description: form.description, managerId: 2 })
      setProjects([...projects, res.data.data])
      setForm({ name: '', description: '' })
      setIsOpen(false)
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create project')
    }
  }

  if (loading) return <div className="page-container"><p>Loading projects...</p></div>

  return (
    <div className="page-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 32 }}>
        <h1>Projects</h1>
        <Button onClick={() => setIsOpen(true)}>
          <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}><Plus size={18} /> New Project</span>
        </Button>
      </div>

      {error && (
        <div style={{ background: '#FEE2E2', color: '#DC2626', padding: '12px 16px', borderRadius: 8, marginBottom: 24, fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 24 }}>
        {projects.map(p => (
          <Card key={p.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/projects/${p.id}/sprints`)}>
            <h3 style={{ marginBottom: 8 }}>{p.name}</h3>
            <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem', marginBottom: 16 }}>{p.description || 'No description'}</p>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <div className="avatar" style={{ width: 28, height: 28, fontSize: '0.65rem' }}>
                  {p.managerName?.[0]?.toUpperCase() || '?'}
                </div>
                <span style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)' }}>{p.managerName}</span>
              </div>
              <span style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)' }}>{p.createdAt}</span>
            </div>
          </Card>
        ))}
      </div>

      <Modal isOpen={isOpen} onClose={() => setIsOpen(false)} title="Create New Project">
        <form onSubmit={handleCreate}>
          <Input label="Project Name" placeholder="Enter project name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
          <Input label="Description" placeholder="Describe the project" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 8 }}>
            <button className="btn-secondary" type="button" onClick={() => setIsOpen(false)}>Cancel</button>
            <button className="btn-primary" type="submit">Create Project</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
