# Enterprise Version-aware RAG Platform - Frontend

High-fidelity, production-ready frontend for the Enterprise Version-aware RAG Platform, matching the Stitch design specifications (Project ID: `6818260600969156169`).

Built with **React 18 / Vite / TypeScript / Tailwind CSS / Framer Motion / TanStack Query / Axios**.

---

## 🚀 Quick Start

Run immediately out-of-the-box:

```bash
cd frontend
npm install
npm run dev
```

The application will be accessible at:
👉 **`http://localhost:3000`**

To compile the production build:
```bash
npm run build
```

---

## 🎨 Visual Reference & Stitch Design Implementation

- **Desktop Layout (30% / 70% Split)**:
  - **Left Sidebar (30%)**: Sticky navigation containing brand identity, document ingestion dropzone, interactive search, and scrollable document list with expandable version timelines.
  - **Right Panel (70%)**: Sticky knowledge base metrics header, full ChatGPT-style conversation workspace, multi-step thinking indicator, and sticky message input.
- **Responsive Layout**:
  - **Tablet**: Collapsible responsive navigation drawer.
  - **Mobile**: Touch-friendly slide-over drawer and responsive header metrics.
- **Enterprise Dark Theme**: Sleek slate/dark background (`#020617`), indigo/violet glowing accents, glassmorphic panels (`backdrop-blur-xl`), custom scrollbars, and fluid Framer Motion animations.

---

## 📂 Architecture & Folder Structure

```
frontend/
├── src/
│   ├── assets/             # Brand logos & vector graphics
│   ├── components/         # Reusable presentation & container components
│   │   ├── AnswerCard.tsx        # High-fidelity response card with citations & actions
│   │   ├── ChatInput.tsx         # Sticky auto-resizing input with file attach & shortcuts
│   │   ├── ChatWindow.tsx        # Chat container, auto-scroll, empty states
│   │   ├── ConflictCard.tsx      # Yellow alert card for version divergence & resolution
│   │   ├── DocumentCard.tsx      # Document item card with version count and badges
│   │   ├── EmptyState.tsx        # "No documents uploaded" state & recommended prompts
│   │   ├── Header.tsx            # Sticky metrics bar (Supabase, Chunks, Groq status)
│   │   ├── MessageBubble.tsx     # User & Assistant chat bubbles with avatar & metadata
│   │   ├── MetricCard.tsx        # Sleek counters with icon badges
│   │   ├── SearchBox.tsx         # Instant document & department filter
│   │   ├── Sidebar.tsx           # Sticky left drawer container
│   │   ├── StatusBadge.tsx       # Live status indicators (Connected, Archived, Current)
│   │   ├── TypingIndicator.tsx   # Multi-stage thinking animation
│   │   ├── UploadCard.tsx        # Drag & drop upload card with live progress bar
│   │   └── VersionTimeline.tsx   # Chronological version history drawer
│   ├── constants/
│   │   └── mockData.ts     # Realistic enterprise policy documents & demo seeds
│   ├── hooks/
│   │   ├── useChat.ts      # Chat query state, TanStack mutations, optimistic updates
│   │   └── useDocuments.ts # Document cache, upload mutations, and status polling
│   ├── services/
│   │   ├── api.ts              # Axios HTTP client with baseURL & proxy
│   │   ├── chatService.ts      # Chat REST queries with fallback simulation
│   │   ├── documentService.ts  # Document catalog & version metadata fetchers
│   │   └── uploadService.ts    # Multipart file ingestion with progress tracking
│   ├── types/
│   │   └── index.ts        # Comprehensive TypeScript interfaces
│   ├── utils/
│   │   └── cn.ts           # Class merging (tailwind-merge + clsx) & date formatters
│   ├── App.tsx             # Root dashboard shell with TanStack Query Provider
│   ├── index.css           # Tailwind CSS v4 directives & glassmorphism utilities
│   └── main.tsx            # Application entrypoint
├── package.json
├── tsconfig.app.json
└── vite.config.ts          # Vite config with path aliases & API proxy
```

---

## 🔌 Backend Integration

The frontend connects directly to the Spring Boot REST API via `http://localhost:8080/api`:

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/documents/upload` | Multipart document upload with version and department |
| `GET` | `/api/documents` | Retrieve all enterprise documents and versions |
| `GET` | `/api/documents/{id}/versions` | Retrieve version history for a specific document |
| `POST` | `/api/chat` | Version-filtered semantic search & Groq LLaMA 3.3 generation |

> **Standalone Resilience**: If the Spring Boot backend is offline or starting up, the service layer automatically provides high-fidelity fallback responses, allowing instant visual review and full functionality in demo environments.
