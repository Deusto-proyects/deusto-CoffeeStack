# Asistente IA — CoffeeStack

Pequeño chatbot integrado en CoffeeStack que ayuda a la persona que gestiona la cafetería (PROPIETARIO o ROOT) a tomar decisiones sobre el día a día: qué reponer, qué insumo está a punto de caducar, qué se vende menos, en qué se puede mejorar.

Funciona **100 % en local** — ningún dato sale del equipo donde corre la aplicación.

---

## Qué puedes preguntarle

Algunas preguntas reales que entiende:

- *"¿Qué tengo que reponer urgente?"*
- *"¿Tengo riesgo de mermas?"*
- *"¿Para cuántos días tengo leche?"*
- *"¿Qué producto se vende menos?"*
- *"¿En qué podemos mejorar esta semana?"*
- *"Si tuviera que pedir un único lote, ¿cuál sería?"*

Y un ejemplo de respuesta real con los datos demo del proyecto:

> *Sí, hay un riesgo de merma para la leche entera. El lote L-LECHE-001 tiene 22 litros y vence el 28 de mayo de 2026, con una merma probable de aproximadamente 19.33 litros debido al consumo previsto antes de su caducidad.*

---

## Cómo está hecho (sin tecnicismos)

Tres piezas:

1. **Un modelo de lenguaje corriendo en local — Ollama con `qwen2.5:7b`.**
   Ollama es una pequeña aplicación que descarga modelos abiertos y los sirve en una API HTTP. `qwen2.5:7b` es un modelo de 7 mil millones de parámetros, suficientemente capaz para razonar en español y suficientemente ligero para correr en un portátil.

2. **El backend Spring Boot prepara un "informe" del estado del negocio en cada pregunta.**
   Cuando llega una pregunta, el backend genera un texto en Markdown — el **snapshot** — con los datos relevantes de ese momento (stock, cobertura, caducidades, ventas). Ese snapshot va junto con la pregunta hasta el modelo. El modelo solo conoce lo que aparece en el snapshot; no se "inventa" datos.

3. **Una pantalla de chat en el frontend React.**
   Una página sencilla con burbujas estilo WhatsApp: tú escribes, el modelo responde. La conversación es independiente en cada turno (no recuerda lo anterior), así que cada pregunta se trata aislada y siempre se basa en el estado actual de los datos.

```
   ┌──────────────┐    pregunta     ┌──────────────┐    pregunta + snapshot    ┌──────────┐
   │  Frontend    │ ───────────────▶│  Backend     │ ─────────────────────────▶│  Ollama  │
   │  (React)     │                 │  Spring Boot │                           │  qwen2.5 │
   │              │◀─── respuesta ──│              │◀────── respuesta ─────────│          │
   └──────────────┘                 └──────┬───────┘                           └──────────┘
                                           │
                                           ▼
                                   ┌─────────────────┐
                                   │  PostgreSQL     │
                                   │  (stock, ventas,│
                                   │   lotes, etc.)  │
                                   └─────────────────┘
```

---

## Qué ve el modelo (el snapshot)

El snapshot que se le pasa al modelo tiene siete secciones. Las cuatro primeras casi siempre tienen contenido; las tres últimas son alertas que solo aparecen pobladas cuando hay algo que requiere acción.

| Sección                                | Cuándo aparece llena                                                      |
| -------------------------------------- | ------------------------------------------------------------------------- |
| Inventario actual                      | Siempre: lista los insumos con su stock, umbral, lead time y cobertura objetivo |
| Cobertura por insumo (30 días)         | Siempre: stock vs consumo medio diario → días de cobertura por insumo     |
| Riesgo de merma por caducidad          | Cuando hay lotes que vencen en ≤ 30 días con stock > consumo previsto     |
| Reposición urgente                     | Cuando un insumo está clasificado como URGENTE o ATENCION                 |
| Riesgo de faltantes                    | Cuando el stock cae por debajo del umbral de alerta del insumo            |
| Cobertura crítica (< 7 días)           | Cuando algún insumo tendría stock para menos de 7 días al ritmo actual    |
| Top productos (últimos 30 días)        | Cuando hay ventas formales registradas en la tabla `ventas`               |

Cada sección viene como tabla Markdown compacta, máx. 25 filas para no saturar la ventana de contexto del modelo.

El system prompt (las instrucciones que el modelo lee siempre antes de cada pregunta) le dice cosas como: "si te preguntan por mermas, mira la sección Riesgo de merma; si te preguntan por faltantes, mira Riesgo de faltantes; nunca inventes números; responde siempre en español".

---

## Por qué este diseño y no otro

Tres decisiones que vale la pena conocer:

- **LLM local, no en la nube.** Los datos de la cafetería (ventas, márgenes, proveedores) no se envían a OpenAI / Anthropic / etc. Trade-off: el modelo local es algo menos brillante que GPT-4 y más lento, pero es privado y gratis de operar.
- **"Contexto inyectado" en lugar de tool calling.** El backend mete TODOS los datos relevantes en un único prompt y deja que el modelo razone. La alternativa habría sido enseñarle al modelo a llamar a la API por sí mismo (function calling), pero eso requiere modelos más capaces y bastante más infraestructura. Para este caso de uso el snapshot basta.
- **Sin memoria conversacional.** Cada pregunta es independiente. Esto evita complicaciones (tabla de conversaciones, expiración, etc.) y hace que el sistema sea predecible: la respuesta solo depende del estado actual del negocio + la pregunta.

---

## Cómo arrancarlo

Necesitas tener Docker corriendo, Java 21, Node.js y Ollama instalado (o el contenedor del compose, ver más abajo).

### 1. Levantar Postgres

```bash
docker compose up -d postgres
```

### 2. Tener Ollama listo con el modelo

Si ya tienes Ollama nativo instalado en tu equipo:

```bash
ollama pull qwen2.5:7b
```

Si prefieres usarlo dentro de Docker, descomenta el servicio `ollama` en `docker-compose.yml` y levántalo (`docker compose up -d ollama`). Luego:

```bash
docker exec -it coffeestack-ollama ollama pull qwen2.5:7b
```

La primera descarga son unos 5 GB y va al disco de Ollama (queda persistido).

### 3. Backend

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 4. Frontend

```bash
cd frontend
npm install   # solo la primera vez
npm run dev
```

Abre http://localhost:5173, haz login (usuario por defecto `admin` / `admin123`) y pincha en **Asistente IA** en la barra de navegación.

---

## Cómo usarlo

### Desde la interfaz

Pantalla de chat con un cuadro de texto abajo. `Enter` envía, `Shift+Enter` salto de línea. El primer turno tarda más (30–60 s) porque el modelo se carga en memoria; los siguientes son mucho más rápidos.

### Desde la API

Dos endpoints, ambos requieren un usuario PROPIETARIO o ROOT (JWT).

```bash
# Preguntar
curl -X POST http://localhost:8080/api/chatbot/preguntar \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"pregunta":"¿Qué reponer urgente?"}'

# Ver el snapshot exacto que ve el modelo (útil para depurar)
curl http://localhost:8080/api/chatbot/snapshot \
  -H "Authorization: Bearer <TOKEN>"
```

El endpoint `/snapshot` está pensado precisamente para entender por qué el modelo responde lo que responde. Si una respuesta te sorprende, mira primero qué ve.

---

## Limitaciones y notas

- **Latencia.** En una CPU normal, una respuesta tarda 5–15 s. Con GPU NVIDIA va mucho más rápido (descomentar el bloque `deploy.resources` del servicio `ollama` en el compose).
- **No es determinista.** La temperatura está baja (`0.2`) para que las respuestas no varíen mucho, pero pueden cambiar entre ejecuciones. Para decisiones financieras concretas, verifica con la pantalla de Reposición y los reportes.
- **No tiene memoria.** Si haces una pregunta de seguimiento como *"¿y del anterior?"*, el modelo no sabe a qué te refieres. Reformula la pregunta de forma completa.
- **El snapshot tiene un tope.** Cada sección se trunca a 25 filas. Si tu cafetería tiene cientos de insumos, las menos urgentes pueden no aparecer; ajusta `MAX_FILAS_POR_SECCION` en `ContextoNegocioServiceImpl` si lo necesitas.
- **Sólo conoce lo que está en el snapshot.** Preguntas sobre opiniones de clientes, datos personales del personal o márgenes exactos que no estén en una sección, te responderán *"no dispongo de ese dato"*. Eso es por diseño — no queremos que invente.

---

## Ficheros clave

| Archivo                                                                     | Qué hace                                                  |
| --------------------------------------------------------------------------- | --------------------------------------------------------- |
| `src/main/java/.../service/ContextoNegocioServiceImpl.java`                 | Construye el snapshot en Markdown                         |
| `src/main/java/.../service/ChatbotServiceImpl.java`                         | Une snapshot + pregunta y llama al modelo                 |
| `src/main/java/.../controller/ChatbotController.java`                       | Endpoints `/preguntar` y `/snapshot`                      |
| `src/main/resources/prompts/system-chatbot.st`                              | Instrucciones permanentes del modelo (system prompt)      |
| `src/main/resources/application.yml`                                        | URL de Ollama, modelo, temperatura, ventana de contexto   |
| `frontend/src/pages/Chatbot.jsx`                                            | Pantalla de chat                                          |
| `docker-compose.yml`                                                        | Servicio Ollama (opcional, comentado por defecto)         |
