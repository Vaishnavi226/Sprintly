import { NavLink } from 'react-router-dom'
import { LayoutDashboard, FolderKanban, Timer, LogOut } from 'lucide-react'

export default function Sidebar() {
  return (
    <aside className="sidebar">
      <div style={{ marginBottom: 40 }}>
        <h2 style={{ color: '#FFF', fontSize: '1.5rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontSize: '1.5rem' }}>&#128640;</span> Sprintly
        </h2>
        <p style={{ color: 'rgba(255,255,255,0.5)', fontSize: '0.75rem', marginTop: 4 }}>Ship Sprints. Ship Code. Faster.</p>
      </div>

      <nav>
        <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <LayoutDashboard size={20} /> Dashboard
        </NavLink>
        <NavLink to="/projects" className={({ isActive }) => `nav-link ${isActive && !isActive.includes('/sprints') ? 'active' : ''}`}>
          <FolderKanban size={20} /> Projects
        </NavLink>
        <NavLink to="/sprints/1" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <Timer size={20} /> Sprints
        </NavLink>
      </nav>

      <div style={{ position: 'absolute', bottom: 24, left: 16, right: 16 }}>
        <NavLink to="/login" className="nav-link" onClick={() => localStorage.clear()}>
          <LogOut size={20} /> Logout
        </NavLink>
      </div>
    </aside>
  )
}
