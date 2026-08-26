import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, MessageSquare, History } from 'lucide-react'
import Badge from '../components/Badge'
import Avatar from '../components/Avatar'
import Button from '../components/Button'
import { taskAPI } from '../services/api'

export default function TaskDetails() {
  const { taskId } = useParams()
  const navigate = useNavigate()
  const [task, setTask] = useState(null)
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [newComment, setNewComment] = useState('')

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [taskRes, commentsRes] = await Promise.all([
          taskAPI.getById(taskId),
          taskAPI.getComments(taskId),
        ])
        setTask(taskRes.data.data)
        setComments(commentsRes.data.data || [])
      } catch {
        setError('Failed to load task details')
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [taskId])

  const handleAddComment = async () => {
    if (!newComment.trim()) return
    try {
      await taskAPI.addComment(taskId, newComment)
      const res = await taskAPI.getComments(taskId)
      setComments(res.data.data || [])
      setNewComment('')
    } catch {
      setError('Failed to add comment')
    }
  }

  if (loading) return <div className="page-container"><p>Loading task...</p></div>
  if (!task) return <div className="page-container"><p>Task not found</p></div>

  return (
    <div className="page-container">
      <button onClick={() => navigate(-1)}
        style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8, color: 'var(--color-text-secondary)', marginBottom: 24, fontSize: '0.875rem' }}>
        <ArrowLeft size={18} /> Back
      </button>

      {error && (
        <div style={{ background: '#FEE2E2', color: '#DC2626', padding: '12px 16px', borderRadius: 8, marginBottom: 24, fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24 }}>
        <div>
          <div className="card" style={{ marginBottom: 24 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
              <h1 style={{ fontSize: '1.5rem' }}>{task.title}</h1>
              <Badge>{task.status?.replace('_', ' ')}</Badge>
            </div>
            <p style={{ color: 'var(--color-text-secondary)', marginBottom: 24 }}>{task.description || 'No description'}</p>
            <div style={{ display: 'flex', gap: 16 }}>
              <Badge>{task.priority}</Badge>
              <span style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)' }}>Created: {task.createdAt}</span>
            </div>
          </div>

          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 20 }}>
              <MessageSquare size={18} />
              <h3>Comments ({comments.length})</h3>
            </div>
            {comments.map(c => (
              <div key={c.id} style={{ display: 'flex', gap: 12, marginBottom: 16, paddingBottom: 16, borderBottom: '1px solid var(--color-border)' }}>
                <Avatar name={`User ${c.userId}`} size={32} />
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', gap: 8, marginBottom: 4 }}>
                    <span style={{ fontWeight: 600, fontSize: '0.875rem' }}>User {c.userId}</span>
                    <span style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)' }}>{c.createdAt ? new Date(c.createdAt).toLocaleDateString() : ''}</span>
                  </div>
                  <p style={{ fontSize: '0.875rem' }}>{c.content}</p>
                </div>
              </div>
            ))}
            <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
              <input className="input-field" placeholder="Write a comment..." value={newComment} onChange={e => setNewComment(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleAddComment()} style={{ flex: 1 }} />
              <Button onClick={handleAddComment}>Post</Button>
            </div>
          </div>
        </div>

        <div>
          <div className="card">
            <h3 style={{ marginBottom: 16 }}>Task Info</h3>
            <div style={{ display: 'grid', gap: 12 }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)', marginBottom: 4 }}>Assignee</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Avatar name={task.assigneeId ? `User ${task.assigneeId}` : 'Unassigned'} size={28} />
                  <span style={{ fontSize: '0.875rem' }}>{task.assigneeId ? `User ${task.assigneeId}` : 'Unassigned'}</span>
                </div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)', marginBottom: 4 }}>Sprint</div>
                <span style={{ fontSize: '0.875rem' }}>Sprint #{task.sprintId}</span>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)', marginBottom: 4 }}>Priority</div>
                <Badge>{task.priority}</Badge>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)', marginBottom: 4 }}>Status</div>
                <Badge>{task.status?.replace('_', ' ')}</Badge>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
