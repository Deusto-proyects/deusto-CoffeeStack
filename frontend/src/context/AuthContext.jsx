import { createContext, useContext, useEffect, useState } from 'react'
import client from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const raw = localStorage.getItem('cs_user')
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  })
  const [token, setToken] = useState(() => localStorage.getItem('cs_token'))
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (token) localStorage.setItem('cs_token', token)
    else localStorage.removeItem('cs_token')
  }, [token])

  useEffect(() => {
    if (user) localStorage.setItem('cs_user', JSON.stringify(user))
    else localStorage.removeItem('cs_user')
  }, [user])

  async function login(username, password) {
    setLoading(true)
    try {
      const { data } = await client.post('/api/auth/login', { username, password })
      setToken(data.token)
      setUser({ username: data.username, rol: data.role })
      return data
    } finally {
      setLoading(false)
    }
  }

  async function register(username, password, rol) {
    setLoading(true)
    try {
      const payload = { username, password }
      if (rol) payload.rol = rol
      const { data } = await client.post('/api/auth/register', payload)
      setToken(data.token)
      setUser({ username: data.username, rol: data.role })
      return data
    } finally {
      setLoading(false)
    }
  }

  function logout() {
    setToken(null)
    setUser(null)
  }

  function hasRole(...roles) {
    if (!user) return false
    return roles.includes(user.rol)
  }

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return ctx
}
