const statusMap = {
  'To Do': 'badge-todo',
  'IN_PROGRESS': 'badge-progress',
  'In Progress': 'badge-progress',
  'DONE': 'badge-done',
  'Done': 'badge-done',
  'HIGH': 'badge-high',
  'MEDIUM': 'badge-medium',
  'LOW': 'badge-low',
}

export default function Badge({ children }) {
  const className = `badge ${statusMap[children] || 'badge-todo'}`
  return <span className={className}>{children}</span>
}
