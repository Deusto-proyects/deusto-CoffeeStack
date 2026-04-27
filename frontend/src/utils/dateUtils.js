/**
 * Calcula la diferencia en días entre la fecha actual y la fecha proporcionada.
 * @param {string|Date} dateStr - Fecha a evaluar (formato 'YYYY-MM-DD' u objeto Date).
 * @returns {number|null} Diferencia en días. Negativo si la fecha ya pasó. Null si no hay fecha.
 */
export function getDaysUntil(dateStr) {
  if (!dateStr) return null
  
  // Establecemos ambas fechas a las 00:00:00 para comparar solo días enteros
  const targetDate = new Date(dateStr)
  targetDate.setHours(0, 0, 0, 0)
  
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const diffTime = targetDate - today
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

/**
 * Devuelve el estado de caducidad y el nivel de severidad basado en los días restantes.
 * Umbrales:
 * - <= 7 días (o negativo): 'danger' (Rojo)
 * - <= 30 días: 'warning' (Amarillo)
 * - > 30 días: 'success' (Gris/Verde)
 * 
 * @param {string|Date} dateStr - Fecha de vencimiento a evaluar.
 * @returns {object} Objeto con { status: 'danger'|'warning'|'success', message: string, days: number }
 */
export function getExpiryStatus(dateStr) {
  const days = getDaysUntil(dateStr)
  
  if (days === null) {
    return { status: 'none', message: 'Sin fecha', days: null }
  }

  if (days < 0) {
    return { status: 'danger', message: `Caducado hace ${Math.abs(days)} días`, days }
  }
  
  if (days === 0) {
    return { status: 'danger', message: 'Caduca hoy', days }
  }

  if (days <= 7) {
    return { status: 'danger', message: `Caduca en ${days} días`, days }
  }

  if (days <= 30) {
    return { status: 'warning', message: `Caduca en ${days} días`, days }
  }

  return { status: 'success', message: `Caduca en ${days} días`, days }
}
