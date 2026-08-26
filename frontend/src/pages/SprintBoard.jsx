import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd'
import { Flag } from 'lucide-react'
import Badge from '../components/Badge'
import Avatar from '../components/Avatar'
import { taskAPI } from '../services/api'

const columns = {
  'TO_DO': { title: 'To Do', color: '#3B82F6' },
  'IN_PROGRESS': { title: 'In Progress', color: '#F59E0B' },
  'DONE': { title: 'Done', color: '#10B981' },
}

export default function SprintBoard() {
  const { sprintId } = useParams()
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        const res = await taskAPI.getBySprint(sprintId)
        setTasks(res.data.data || [])
      } catch {
        setError('Failed to load tasks')
      } finally {
        setLoading(false)
      }
    }
    fetchTasks()
  }, [sprintId])

  const onDragEnd = async (result) => {
    if (!result.destination) return
    const { source, destination, draggableId } = result
    if (source.droppableId === destination.droppableId && source.index === destination.index) return

    const taskId = Number(draggableId)
    const newStatus = destination.droppableId
    const prevTasks = [...tasks]

    const updated = [...tasks]
    const taskIdx = updated.findIndex(t => t.id === taskId)
    if (taskIdx !== -1) {
      updated[taskIdx] = { ...updated[taskIdx], status: newStatus }
      const [moved] = updated.splice(taskIdx, 1)
      const destTasks = updated.filter(t => t.status === newStatus)
      const otherTasks = updated.filter(t => t.status !== newStatus)
      const insertIdx = destination.index
      destTasks.splice(insertIdx, 0, moved)
      setTasks([...otherTasks, ...destTasks])
    }

    try {
      await taskAPI.updateStatus(taskId, newStatus)
    } catch {
      setTasks(prevTasks)
      setError('Failed to update task status. Reverted changes.')
      setTimeout(() => setError(''), 3000)
    }
  }

  if (loading) return <div className="page-container"><p>Loading sprint board...</p></div>

  return (
    <div className="page-container">
      <div style={{ marginBottom: 32 }}>
        <h1>Sprint Board</h1>
        <p style={{ color: 'var(--color-text-secondary)', marginTop: 4 }}>Sprint #{sprintId}</p>
      </div>

      {error && (
        <div style={{ background: '#FEE2E2', color: '#DC2626', padding: '12px 16px', borderRadius: 8, marginBottom: 24, fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <DragDropContext onDragEnd={onDragEnd}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 24 }}>
          {Object.entries(columns).map(([status, { title, color }]) => (
            <div key={status}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
                <div style={{ width: 10, height: 10, borderRadius: '50%', background: color }} />
                <h3>{title}</h3>
                <span style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)', marginLeft: 'auto' }}>
                  {tasks.filter(t => t.status === status).length}
                </span>
              </div>

              <Droppable droppableId={status}>
                {(provided, snapshot) => (
                  <div ref={provided.innerRef} {...provided.droppableProps} className="kanban-column"
                    style={{ background: snapshot.isDraggingOver ? 'var(--color-primary-light)' : 'var(--color-bg)', minHeight: 500 }}>
                    {tasks.filter(t => t.status === status).map((task, index) => (
                      <Draggable key={task.id} draggableId={String(task.id)} index={index}>
                        {(provided, snapshot) => (
                          <div ref={provided.innerRef} {...provided.draggableProps} {...provided.dragHandleProps}
                            className={`task-card ${snapshot.isDragging ? 'dragging' : ''}`}
                            style={{ borderLeft: `4px solid ${color}`, ...provided.draggableProps.style }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                              <h4 style={{ fontSize: '0.9rem', fontWeight: 600, flex: 1 }}>{task.title}</h4>
                              <Badge>{task.priority}</Badge>
                            </div>
                            {task.description && (
                              <p style={{ fontSize: '0.8rem', color: 'var(--color-text-secondary)', marginBottom: 12 }}>
                                {task.description.length > 80 ? task.description.slice(0, 80) + '...' : task.description}
                              </p>
                            )}
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                              <Avatar name={task.assigneeId ? `User ${task.assigneeId}` : 'Unassigned'} size={28} />
                              <Flag size={14}
                                color={task.priority === 'HIGH' ? '#EF4444' : task.priority === 'MEDIUM' ? '#F59E0B' : '#3B82F6'}
                                fill={task.priority === 'HIGH' ? '#EF4444' : task.priority === 'MEDIUM' ? '#F59E0B' : '#3B82F6'} />
                            </div>
                          </div>
                        )}
                      </Draggable>
                    ))}
                    {provided.placeholder}
                  </div>
                )}
              </Droppable>
            </div>
          ))}
        </div>
      </DragDropContext>
    </div>
  )
}
