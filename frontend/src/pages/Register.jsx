import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { authAPI } from '../services/api'
import { Eye, EyeOff } from 'lucide-react'
import Button from '../components/Button'
import Input from '../components/Input'

export default function Register() {
  const [form, setForm] = useState({ username: '', email: '', password: '', role: 'DEVELOPER' })
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async e => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await authAPI.register(form)
      const { token, id, username, role } = res.data.data
      login({ id, username, role }, token)
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
      background: 'var(--color-bg)', position: 'relative', overflow: 'hidden',
    }}>
      <div style={{ position: 'absolute', width: 500, height: 500, borderRadius: '50%', background: 'radial-gradient(circle, rgba(124,58,237,0.12) 0%, transparent 70%)', top: -100, left: -100 }} />

      <div style={{ width: '100%', maxWidth: 420, padding: '0 24px', position: 'relative', zIndex: 1 }}>
        <div style={{ textAlign: 'center', marginBottom: 40 }}>
          <h1 style={{ color: 'var(--color-primary)', fontSize: '2rem', marginBottom: 8 }}>&#128640; Sprintly</h1>
          <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.95rem' }}>Create your account</p>
        </div>

        <div className="card" style={{ padding: 32 }}>
          <h2 style={{ textAlign: 'center', marginBottom: 24 }}>Sign Up</h2>

          {error && (
            <div style={{ background: '#FEE2E2', color: '#DC2626', padding: '12px 16px', borderRadius: 8, marginBottom: 16, fontSize: '0.875rem' }}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <Input label="Username" name="username" type="text" placeholder="Choose a username" value={form.username} onChange={handleChange} required />
            <Input label="Email" name="email" type="email" placeholder="Enter your email" value={form.email} onChange={handleChange} required />

            <div className="form-group">
              <label className="form-label">Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  className="input-field"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Create a password (min 6 chars)"
                  value={form.password}
                  onChange={handleChange}
                  required
                  minLength={6}
                  style={{ paddingRight: 44 }}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={{
                    position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)',
                    background: 'none', border: 'none', cursor: 'pointer', color: 'var(--color-text-secondary)',
                    padding: 4, display: 'flex', alignItems: 'center',
                  }}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Role</label>
              <select name="role" className="input-field" value={form.role} onChange={handleChange}>
                <option value="DEVELOPER">Developer</option>
                <option value="MANAGER">Manager</option>
              </select>
            </div>
            <Button type="submit" style={{ width: '100%', marginTop: 8, padding: '12px 24px' }} disabled={loading}>
              {loading ? 'Creating Account...' : 'Create Account'}
            </Button>
          </form>

          <p style={{ textAlign: 'center', marginTop: 20, fontSize: '0.875rem', color: 'var(--color-text-secondary)' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: 'var(--color-primary)', textDecoration: 'none', fontWeight: 600 }}>Sign In</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
