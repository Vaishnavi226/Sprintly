import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const authAPI = {
  login: (username, password) => api.post('/api/auth/login', { username, password }),
  register: (data) => api.post('/api/auth/register', data),
}

export const projectAPI = {
  getAll: () => api.get('/api/projects'),
  getById: (id) => api.get(`/api/projects/${id}`),
  create: (data) => api.post('/api/projects', data),
  update: (id, data) => api.put(`/api/projects/${id}`, data),
  delete: (id) => api.delete(`/api/projects/${id}`),
}

export const sprintAPI = {
  getByProject: (projectId) => api.get(`/api/projects/${projectId}/sprints`),
  getById: (id) => api.get(`/api/sprints/${id}`),
  create: (projectId, data) => api.post(`/api/projects/${projectId}/sprints`, data),
  update: (id, data) => api.put(`/api/sprints/${id}`, data),
  delete: (id) => api.delete(`/api/sprints/${id}`),
}

export const taskAPI = {
  getBySprint: (sprintId) => api.get(`/api/sprints/${sprintId}/tasks`),
  getById: (id) => api.get(`/api/tasks/${id}`),
  getByUser: (userId) => api.get(`/api/users/${userId}/tasks`),
  create: (sprintId, data) => api.post(`/api/sprints/${sprintId}/tasks`, data),
  update: (id, data) => api.put(`/api/tasks/${id}`, data),
  assign: (id, assigneeId) => api.put(`/api/tasks/${id}/assign?assigneeId=${assigneeId}`),
  updateStatus: (id, status) => api.put(`/api/tasks/${id}/status?status=${status}`),
  addComment: (id, content) => api.post(`/api/tasks/${id}/comments`, { content }),
  getComments: (id) => api.get(`/api/tasks/${id}/comments`),
  delete: (id) => api.delete(`/api/tasks/${id}`),
}

export const analyticsAPI = {
  getSprintProgress: (sprintId) => api.get(`/api/sprints/${sprintId}/progress`),
  getPriorityDistribution: (sprintId) => api.get(`/api/sprints/${sprintId}/priority-distribution`),
  getAssigneeDistribution: (sprintId) => api.get(`/api/sprints/${sprintId}/assignee-distribution`),
}

export default api
