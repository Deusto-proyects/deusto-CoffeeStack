import { useEffect, useRef, useState } from 'react'
import client, { apiErrorMessage } from '../api/client'
import PageHeader from '../components/PageHeader'

/**
 * Asistente IA conversacional. Envía cada pregunta al backend (que la
 * resuelve contra un LLM local en Ollama) y muestra la respuesta como
 * una burbuja en el historial. La conversación es stateless: el backend
 * trata cada pregunta como independiente, así que el historial solo vive
 * en el cliente y se pierde al recargar.
 */
export default function Chatbot() {
  const [mensajes, setMensajes] = useState([
    {
      rol: 'assistant',
      texto:
        'Hola, soy tu asistente de gestión. Puedo ayudarte con reposición de insumos, riesgo de faltantes, cobertura crítica y rotación de productos. La primera respuesta puede tardar 30-60 s mientras el modelo se carga en memoria.',
      ts: new Date(),
    },
  ])
  const [pregunta, setPregunta] = useState('')
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState(null)
  const historialRef = useRef(null)
  const textareaRef = useRef(null)

  useEffect(() => {
    if (historialRef.current) {
      historialRef.current.scrollTop = historialRef.current.scrollHeight
    }
  }, [mensajes, cargando])

  async function enviar() {
    const texto = pregunta.trim()
    if (!texto || cargando) return

    setError(null)
    const userMsg = { rol: 'user', texto, ts: new Date() }
    setMensajes((m) => [...m, userMsg])
    setPregunta('')
    setCargando(true)

    try {
      const { data } = await client.post('/api/chatbot/preguntar', { pregunta: texto })
      setMensajes((m) => [
        ...m,
        { rol: 'assistant', texto: data.respuesta, ts: new Date(), latenciaMs: data.latenciaMs },
      ])
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setCargando(false)
      setTimeout(() => textareaRef.current?.focus(), 0)
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      enviar()
    }
  }

  function formatHora(d) {
    const h = d.getHours().toString().padStart(2, '0')
    const m = d.getMinutes().toString().padStart(2, '0')
    return `${h}:${m}`
  }

  return (
    <div>
      <PageHeader
        icon="bi-robot"
        title="Asistente IA"
        subtitle="Pregunta sobre reposición, stock crítico y rotación de productos"
      />

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card">
        <div
          ref={historialRef}
          className="card-body"
          style={{ maxHeight: '60vh', overflowY: 'auto', backgroundColor: '#f8f9fa' }}
        >
          {mensajes.map((m, i) => (
            <div
              key={i}
              className={`d-flex mb-3 ${
                m.rol === 'user' ? 'justify-content-end' : 'justify-content-start'
              }`}
            >
              <div
                className={`px-3 py-2 rounded-3 shadow-sm ${
                  m.rol === 'user' ? 'bg-white border' : 'bg-coffee text-white'
                }`}
                style={{ maxWidth: '80%', whiteSpace: 'pre-wrap' }}
              >
                <div>{m.texto}</div>
                <div
                  className={`small mt-1 ${
                    m.rol === 'user' ? 'text-muted' : 'text-white-50'
                  }`}
                  style={{ fontSize: '0.7em' }}
                >
                  <i
                    className={`bi ${
                      m.rol === 'user' ? 'bi-person-fill' : 'bi-robot'
                    } me-1`}
                  ></i>
                  {formatHora(m.ts)}
                  {typeof m.latenciaMs === 'number' && (
                    <span className="ms-2">· {(m.latenciaMs / 1000).toFixed(1)} s</span>
                  )}
                </div>
              </div>
            </div>
          ))}

          {cargando && (
            <div className="d-flex justify-content-start mb-3">
              <div className="px-3 py-2 rounded-3 shadow-sm bg-coffee text-white">
                <span className="spinner-border spinner-border-sm me-2"></span>
                Pensando...
              </div>
            </div>
          )}
        </div>

        <div className="card-footer bg-white">
          <div className="d-flex gap-2 align-items-end">
            <textarea
              ref={textareaRef}
              className="form-control"
              rows="2"
              placeholder="Escribe tu pregunta — Enter para enviar, Shift+Enter para nueva línea"
              value={pregunta}
              onChange={(e) => setPregunta(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={cargando}
              maxLength={500}
            />
            <button
              className="btn btn-coffee"
              onClick={enviar}
              disabled={cargando || !pregunta.trim()}
            >
              {cargando ? (
                <span className="spinner-border spinner-border-sm"></span>
              ) : (
                <>
                  <i className="bi bi-send-fill me-1"></i>Enviar
                </>
              )}
            </button>
          </div>
          <div className="form-text mt-1">{pregunta.length}/500</div>
        </div>
      </div>
    </div>
  )
}
