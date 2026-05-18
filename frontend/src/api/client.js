import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

const client = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  timeout: 130000,
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('cs_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const path = window.location.pathname
      if (path !== '/login' && path !== '/register') {
        localStorage.removeItem('cs_token')
        localStorage.removeItem('cs_user')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default client

export function apiErrorMessage(err) {
  if (!err) return 'Error desconocido'
  if (err.code === 'ECONNABORTED' || err.message?.toLowerCase().includes('timeout')) {
    return 'El asistente está tardando demasiado en responder. Vuelve a intentarlo en unos segundos.'
  }
  const r = err.response
  if (!r) return 'No se pudo contactar con el servidor. Comprueba tu conexión.'
  const d = r.data
  if (typeof d === 'string') return d
  if (d?.message) return d.message
  if (d?.error) return d.error
  return `Error ${r.status}`
}
