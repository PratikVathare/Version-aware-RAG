# Version-Aware Enterprise RAG Application

An enterprise-grade Retrieval Augmented Generation (RAG) platform designed to handle multiple versions of enterprise documents, temporal applicability, and department-specific access.

## Tech Stack Overview

- **Backend**: Java 21, Spring Boot 3.3.4, Spring Data JPA, Spring AI (OpenAI API spec for Groq)
- **Database**: Supabase PostgreSQL with `pgvector` extension
- **LLM**: Groq API (`llama-3.3-70b-versatile` / OpenAI-compatible API)
- **Embedding Model**: `sentence-transformers/all-MiniLM-L6-v2` (384-dimensional dense vectors)
- **Frontend** *(Upcoming)*: React + Vite

---

## Clean Architecture Backend Structure

The backend is structured under `backend/` following Clean Architecture and separation of concerns:

```
backend/
├── pom.xml
├── .env.example
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
└── src/
    ├── main/
    │   ├── java/com/enterprise/rag/
    │   │   ├── VersionAwareRagApplication.java   # Spring Boot Entry Point
    │   │   ├── config/                           # Configuration Layer
    │   │   │   ├── OpenApiConfig.java            # Swagger / OpenAPI documentation
    │   │   │   ├── PgVectorConfig.java           # Supabase pgvector configuration
    │   │   │   └── CorsConfig.java               # CORS for React + Vite frontend
    │   │   ├── controller/                       # Presentation Layer (REST API)
    │   │   │   ├── DocumentController.java       # Document ingestion & version routes
    │   │   │   └── RagQueryController.java       # Version-aware RAG query routes
    │   │   ├── dto/                              # Data Transfer Objects
    │   │   │   ├── ApiResponse.java              # Standardized API envelope
    │   │   │   ├── DocumentUploadRequest.java    # Document payload with version metadata
    │   │   │   ├── DocumentChunkDto.java         # Document chunk representation
    │   │   │   ├── RagQueryRequest.java          # Query request with filters
    │   │   │   └── RagQueryResponse.java         # LLM response with citations
    │   │   ├── entity/                           # Domain / Persistence Entities
    │   │   │   └── DocumentChunk.java            # Document chunk entity with pgvector
    │   │   ├── repository/                       # Data Access Layer
    │   │   │   └── DocumentChunkRepository.java  # JPA + pgvector similarity queries
    │   │   ├── service/                          # Application & Business Interfaces
    │   │   │   ├── DocumentService.java
    │   │   │   ├── RagService.java
    │   │   │   ├── EmbeddingService.java
    │   │   │   └── impl/                         # Service Implementations (Skeletons)
    │   │   │       ├── DocumentServiceImpl.java
    │   │   │       ├── RagServiceImpl.java
    │   │   │       └── EmbeddingServiceImpl.java
    │   │   ├── exception/                        # Exception Handling Layer
    │   │   │   ├── ErrorResponse.java            # RFC 7807-style error DTO
    │   │   │   ├── GlobalExceptionHandler.java   # Centralized @RestControllerAdvice
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   └── RagProcessingException.java
    │   │   └── util/                             # Utility Layer
    │   │       ├── VectorUtils.java              # Vector math and conversion utils
    │   │       └── DateUtils.java                # Temporal parsing utilities
    │   └── resources/
    │       └── application.yml                   # Spring Boot application configuration
    └── test/
        ├── java/com/enterprise/rag/
        │   └── VersionAwareRagApplicationTests.java
        └── resources/
            └── application-test.yml
```

---

## Document Chunk Schema

Each document chunk contains:
| Field | Type | Description |
|---|---|---|
| `id` | `UUID` | Unique chunk identifier |
| `documentName` | `String` | Logical document name |
| `versionNumber` | `String` | Version identifier (e.g., `v1.0`, `v2.1`) |
| `effectiveDate` | `LocalDate` | Date from which the version takes effect |
| `department` | `String` | Target organization unit |
| `chunkText` | `String` (TEXT) | Segmented text content |
| `embedding` | `vector(384)` | Dense vector (`all-MiniLM-L6-v2`) |
| `createdAt` | `OffsetDateTime`| Ingestion timestamp |
