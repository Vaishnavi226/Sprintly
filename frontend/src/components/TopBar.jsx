import { Search, Bell } from 'lucide-react'
import Avatar from './Avatar'

export default function TopBar({ user }) {
  return (
    <header className="topbar">
      <div style={{ display: 'flex', alignItems: 'center', gap: 16, flex: 1 }}>
        <div style={{ position: 'relative', width: 320 }}>
          <Search size={18} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--color-text-secondary)' }} />
          <input
            className="input-field"
            placeholder="Search tasks, sprints..."
            style={{ paddingLeft: 40, height: 40, fontSize: '0.875rem' }}
          />
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        <button style={{ background: 'none', border: 'none', cursor: 'pointer', position: 'relative', color: 'var(--color-text-secondary)' }}>
          <Bell size={20} />
          <span style={{ position: 'absolute', top: -2, right: -2, width: 8, height: 8, borderRadius: '50%', background: 'var(--color-danger)' }} />
        </button>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Avatar name={user?.username || 'User'} size={36} />
          <div>
            <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{user?.username || 'User'}</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--color-text-secondary)' }}>{user?.role || 'Developer'}</div>
          </div>
        </div>
      </div>
    </header>
  )
}
