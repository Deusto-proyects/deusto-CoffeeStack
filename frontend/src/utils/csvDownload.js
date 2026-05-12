const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

/**
 * Descarga un fichero CSV desde un endpoint autenticado.
 *
 * Hace la petición con el token JWT del localStorage, recibe el blob binario
 * y dispara la descarga nativa del navegador sin abrir una nueva pestaña.
 *
 * @param {string} path       - Ruta relativa del endpoint, ej: '/api/ventas/reporte/csv'
 * @param {string} filename   - Nombre del fichero a descargar, ej: 'reporte_ventas.csv'
 * @param {Object} params     - Query params opcionales (ej: { insumoId: 1, desde: '...', hasta: '...' })
 * @returns {Promise<void>}
 * @throws {Error} si la respuesta no es OK (incluye el mensaje del servidor si lo hay)
 */
export async function downloadCsv(path, filename, params = {}) {
  const token = localStorage.getItem('cs_token')

  const url = new URL(API_BASE + path)
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') {
      url.searchParams.set(k, v)
    }
  })

  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: {
      Authorization: token ? `Bearer ${token}` : '',
      Accept: 'text/csv',
    },
  })

  if (!response.ok) {
    let msg = `Error ${response.status}`
    try {
      const body = await response.json()
      msg = body.message || body.error || msg
    } catch {
      // cuerpo no es JSON, usar mensaje genérico
    }
    throw new Error(msg)
  }

  const blob = await response.blob()
  const objectUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = objectUrl
  anchor.download = filename
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(objectUrl)
}
