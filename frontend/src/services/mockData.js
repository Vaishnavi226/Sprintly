export const mockUsers = [
  { id: 1, username: 'alice', email: 'alice@sprintly.com', role: 'ADMIN' },
  { id: 2, username: 'bob', email: 'bob@sprintly.com', role: 'MANAGER' },
  { id: 3, username: 'charlie', email: 'charlie@sprintly.com', role: 'DEVELOPER' },
  { id: 4, username: 'diana', email: 'diana@sprintly.com', role: 'DEVELOPER' },
]

export const mockProjects = [
  { id: 1, name: 'Sprintly Frontend', description: 'React-based frontend for the Sprintly platform', managerId: 2, createdAt: '2026-07-01' },
  { id: 2, name: 'Sprintly Backend', description: 'Spring Boot REST API services', managerId: 2, createdAt: '2026-07-05' },
  { id: 3, name: 'Mobile App', description: 'React Native mobile application', managerId: 1, createdAt: '2026-08-01' },
]

export const mockSprints = [
  { id: 1, projectId: 1, name: 'Sprint 1 - Foundation', goal: 'Set up project structure', startDate: '2026-08-18', endDate: '2026-09-01', status: 'IN_PROGRESS' },
  { id: 2, projectId: 1, name: 'Sprint 2 - Core Features', goal: 'Implement core features', startDate: '2026-09-01', endDate: '2026-09-15', status: 'PLANNED' },
]

export const mockTasks = [
  { id: 1, sprintId: 1, title: 'Set up Vite project', description: 'Initialize React project with Vite', status: 'DONE', priority: 'HIGH', assigneeId: 3, createdAt: '2026-08-18' },
  { id: 2, sprintId: 1, title: 'Design system CSS', description: 'Create global CSS variables and base styles', status: 'DONE', priority: 'HIGH', assigneeId: 3, createdAt: '2026-08-19' },
  { id: 3, sprintId: 1, title: 'Build Sidebar component', description: 'Fixed left sidebar with navigation', status: 'IN_PROGRESS', priority: 'MEDIUM', assigneeId: 4, createdAt: '2026-08-20' },
  { id: 4, sprintId: 1, title: 'Implement Login page', description: 'Authentication UI with form validation', status: 'IN_PROGRESS', priority: 'HIGH', assigneeId: 3, createdAt: '2026-08-20' },
  { id: 5, sprintId: 1, title: 'Kanban board drag-and-drop', description: 'Implement drag-and-drop for task cards', status: 'TO_DO', priority: 'HIGH', assigneeId: 4, createdAt: '2026-08-21' },
  { id: 6, sprintId: 1, title: 'Dashboard analytics', description: 'Stat cards and charts', status: 'TO_DO', priority: 'MEDIUM', assigneeId: 3, createdAt: '2026-08-22' },
  { id: 7, sprintId: 1, title: 'API integration', description: 'Connect frontend with backend REST API', status: 'TO_DO', priority: 'LOW', assigneeId: 4, createdAt: '2026-08-22' },
]

export const mockComments = [
  { id: 1, taskId: 3, userId: 3, content: 'Started working on the sidebar layout', createdAt: '2026-08-20T10:30:00' },
  { id: 2, taskId: 3, userId: 4, content: 'Looks good! Maybe add hover effects?', createdAt: '2026-08-20T14:15:00' },
]

export const mockHistory = [
  { id: 1, taskId: 1, changedBy: 3, oldStatus: 'IN_PROGRESS', newStatus: 'DONE', changedAt: '2026-08-19T16:00:00' },
  { id: 2, taskId: 2, changedBy: 3, oldStatus: 'TO_DO', newStatus: 'IN_PROGRESS', changedAt: '2026-08-19T09:00:00' },
  { id: 3, taskId: 2, changedBy: 3, oldStatus: 'IN_PROGRESS', newStatus: 'DONE', changedAt: '2026-08-19T17:30:00' },
]
