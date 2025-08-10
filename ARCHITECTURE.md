> **Navigation:**
> - [Start Here — CONTRIBUTING.md](CONTRIBUTING.md) for onboarding and repo workflow
> - [CLAUDE.md](CLAUDE.md) for coding rules, style, and implementation patterns

# ARCHITECTURE.md

---

## Purpose

This document explains **how Ocht is structured using the Polylith architecture** and how that structure delivers the **"CSV → Transform → Console"** Q4 2025 MVP while providing a foundation for the full intelligent data workflow platform.

> **Contributor Note:** When implementing changes, see **CLAUDE.md → 2) Ocht Mental Model** for day-to-day coding rules that enforce these principles.

---

## Executive Summary

**Ocht implements a functional-core/imperative-shell architecture using Polylith** to deliver intelligent data workflows:

- ✅ **Stable core** - Pure transformation logic isolated from side effects
- ✅ **Composable components** - Each brick has a single responsibility  
- ✅ **Plugin-style growth** - New connectors/transforms add without breaking existing code
- ✅ **REPL-driven development** - All components testable in isolation
- ✅ **Pipelines as Data** - EDN configuration enables programmatic pipeline manipulation

> **Contributor Note:** See **CLAUDE.md → 5) Code Style Rules** for how to keep new bricks aligned with these architectural goals.

---

## Architecture Overview

**Ocht uses Polylith workspace architecture** with three brick types:

- **Components** = Reusable libraries with public APIs (e.g., `csv-connector`, `executor`)
- **Bases** = Runnable entry points that compose components (e.g., `cli`)
- **Projects** = Build configurations that bundle bases + components for deployment

**Key principle**: Components contain business logic; bases handle wiring and IO boundaries.

> **Contributor Note:** See **CLAUDE.md → 2) Ocht Mental Model** for the mental model to keep in mind while coding.

---

## Workspace Layout

```
ocht/
├── bases/                    # Runnable applications
│   └── cli/                  # Command-line interface
├── components/               # Reusable libraries
│   ├── connector-protocol/   # Shared Connector protocol
│   ├── pipeline-model/       # EDN pipeline parsing & validation
│   ├── pipeline-transform/   # Pure transformation functions
│   ├── csv-connector/        # CSV file I/O
│   ├── console-connector/    # Console output formatting
│   └── executor/             # Pipeline orchestration
├── projects/
│   └── development/          # Dev environment with REPL
├── deps.edn                  # Root workspace dependencies
└── workspace.edn             # Polylith configuration
```

**Top namespace**: `ocht` - All components use `ocht.*` namespaces.

> **Contributor Note:** See **CLAUDE.md → 5) Repository Conventions** for naming, namespace layout, and documentation requirements.

---

## Core Architectural Patterns

### Model–Interface–Environment

**Separation of concerns implemented as**:

- **Model**: EDN pipeline definitions, validation schemas, data shapes
- **Interface**: Protocols (`Connector`, public APIs in `interface` namespaces)
- **Environment**: File I/O, console output, external systems (connectors and bases only)

**Core is pure**: `pipeline-transform` and `pipeline-model` contain no side effects.

> **Contributor Note:** See **CLAUDE.md → 6) Core Protocols & Contracts** for connector/transform/executor contracts you must follow.

### Three Phases

**Every pipeline execution follows**: **Pull → Transform → Push**

1. **Pull**: Connector reads data (e.g., CSV file → lazy sequence)
2. **Transform**: Pure functions/transducers process data (filter, map, group)
3. **Push**: Connector outputs results (formatted to console)

**Implementation**: `executor` orchestrates; connectors handle I/O; transforms are pure.

> **Contributor Note:** See **CLAUDE.md → 5) Code Style Rules** and **10) Common Tasks** for patterns that respect this separation.

---

## Component Catalogue

### Core Components (Implemented)

**connector-protocol** (`ocht.connector.protocol`)
- **Purpose**: Shared protocol eliminating connector duplication
- **API**: `Connector` protocol with `pull`, `push`, `validate` methods
- **Used by**: All connectors implement this protocol

**pipeline-model** (`ocht.pipeline.model`)
- **Purpose**: Parse and validate EDN pipeline definitions
- **API**: `parse-pipeline`, `validate-pipeline` 
- **Invariants**: Validates connector existence, transform function validity

**pipeline-transform** (`ocht.pipeline.transform`)
- **Purpose**: Pure transformation functions as data
- **API**: `filter-transform`, `map-transform`, `take-transform`, `group-by-transform`
- **Key**: All return transducers, support both batch and streaming

**csv-connector** (`ocht.connectors.csv`)
- **Purpose**: Read/write CSV files with proper resource management
- **API**: Implements `Connector` protocol
- **Safety**: Uses `doall` to realize sequences within resource scope

**console-connector** (`ocht.connectors.console`)
- **Purpose**: Output data to console in multiple formats
- **API**: Implements `Connector` protocol
- **Formats**: `:table`, `:edn`, `:json` output modes

**executor** (`ocht.executor`)
- **Purpose**: Pipeline orchestration and execution
- **API**: `execute-pipeline` with options
- **Features**: Lazy connector registry, structured error handling

> **Contributor Note:** Before modifying a component here, check **CLAUDE.md → 3) Definition of Done** and **7) Testing Standards** for required tests and docs.

---

## Bases & Projects

### Implemented Bases

**cli** (`ocht.cli.core`)
- **Purpose**: Command-line interface for pipeline execution
- **Entry**: `java -jar` or `clj -M:cli` with `-p pipeline.edn`
- **Features**: EDN file parsing, validation, structured error output

### Projects

**development** (`projects/development/`)
- **Purpose**: REPL-driven development environment
- **Entry**: `clj -A:dev` loads all components + REPL helpers
- **Tools**: `user.clj` with `refresh`, `refresh-all` utilities

> **Contributor Note:** See **CLAUDE.md → 4) First PR Quickstart** for how to choose the right brick and test it in the dev REPL.

---

## Contracts & Protocols

### Connector Protocol

```clojure
(defprotocol Connector
  (pull [this config options])
  (push [this config data options]) 
  (validate [this config]))
```

**Contracts**:
- `pull`: Returns lazy sequence, bounded by config
- `push`: Idempotent where possible, clear docs where not
- `validate`: Returns `{:valid? boolean :errors []}`, no side effects

### Transform Functions

**Signature**: `(transform-fn config) → transducer`

**Contract**: Pure functions that return transducers for composability

### Executor Interface

**Signature**: `(execute-pipeline pipeline opts) → result-map`

**Returns**: `{:success boolean :result data}` or `{:success false :error error-map}`

> **Contributor Note:** See **CLAUDE.md → 6) Core Protocols & Contracts** for full signatures, return shapes, and idempotency rules.

---

## Error Handling & Invariants

### Structured Errors

**At boundaries** (connector validation, pipeline parsing):
```clojure
{:valid? false
 :errors [{:type :missing-field :field :source :message "..."}]}
```

**Internal failures** (execution errors):
```clojure
(throw (ex-info "Pipeline execution failed" 
               {:pipeline-id pid :step :transform}))
```

### Invariants

- **Pure core**: No side effects in `pipeline-model` or `pipeline-transform`
- **Resource safety**: Lazy sequences realized within resource scope
- **Validation first**: All inputs validated before processing
- **Pre-conditions**: Public functions use `{:pre [...]}` assertions

> **Contributor Note:** See **CLAUDE.md → 8) Error Handling & Observability** for log formats, metrics, and redaction rules.

---

## Testing Strategy

### Implemented Test Types

**Unit tests**: Each component has `test/` directory with pure function tests

**Protocol tests**: `connector-protocol-test` validates protocol implementations

**Contract validation**: Components test their own contract compliance

**Integration**: CLI base tested end-to-end with demo pipelines

### Test Commands

```bash
# Run all tests
clj -X:test

# REPL testing
clj -A:dev
user=> (refresh) ; reload and test
```

### Current Coverage

- ✅ All components have unit tests
- ✅ Protocol implementations validated
- ✅ End-to-end demo pipeline working
- ✅ Resource management (CSV reader) tested

> **Contributor Note:** See **CLAUDE.md → 7) Testing Standards** for the exact structure of each test type and failure conditions.

---

## Performance & Concurrency

### Current Implementation

**Streaming-ready**: All transforms return transducers for composability

**Resource management**: 
- CSV connector uses `doall` within `with-open` for safety
- Lazy sequences realized before resource cleanup

**Memory efficiency**:
- Large files processed as lazy sequences
- Transforms compose without intermediate collections
- Console output streams results progressively

### Future Considerations

**Chunking**: Transform pipeline supports chunked processing

**Parallelism**: Transducer model enables `pmap` and `core.async` integration

**Back-pressure**: Lazy sequences naturally provide back-pressure

> **Contributor Note:** See **CLAUDE.md → 3) Definition of Done (Why)** for why streaming safety and laziness rules matter.

---

## Observability & Operations

### Current Implementation

**Structured errors**: All components return structured error maps

**CLI feedback**: Verbose mode shows execution progress and metrics

**REPL observability**: All components testable in isolation

### Architecture for Future Observability

**Structured logging**: Ready for `components/observability` component

**Metrics collection**: Executor tracks timing, result counts

**Correlation IDs**: Error maps include context for tracing

**Example structure**:
```clojure
{:event :pipeline-start
 :pipeline-id "demo-simple" 
 :timestamp #inst "2025-01-01T12:00:00Z"}
```

> **Contributor Note:** See **CLAUDE.md → 8) Error Handling & Observability** for the full logging/metrics code pattern.

---

## Security & Config

### Current Implementation

**Configuration as data**: EDN pipeline files are pure data (no code execution)

**Resource boundaries**: File access controlled through connector validation

**Error sanitization**: Stack traces and sensitive paths not exposed to end users

### Architecture for Production

**Secrets management**: Config maps can reference environment variables

**Least privilege**: Connectors validate permissions before access

**Audit trail**: All pipeline executions traceable via structured logging

**Validation first**: All input validated before processing

> **Contributor Note:** See **CLAUDE.md → 9) Security & Privacy** for implementation guidance.

---

## Evolution Path

### Adding New Connectors

1. **Create component**: `components/my-connector/`
2. **Implement protocol**: `ocht.connector.protocol/Connector` 
3. **Register**: Add to `executor` connector registry
4. **Test**: Contract tests + integration tests

### Adding New Transforms

1. **Add function**: `components/pipeline-transform/src/.../transform.clj`
2. **Return transducer**: `(fn [config] (map (partial my-fn config)))`
3. **Register**: Add to transform registry
4. **Test**: Property tests for associativity, edge cases

### Adding New Bases

1. **Create base**: `bases/my-base/`
2. **Wire components**: Import and compose existing components
3. **Handle I/O**: Bases are the only place for system interaction
4. **Integration test**: End-to-end pipeline execution

### Current MVP → Production

**Next components**: `observability`, `config`, `database-connector`

**Next bases**: `worker` (background processing), `api` (HTTP endpoints)

**Next projects**: `production`, `staging` deployment configs

> **Contributor Note:** See **CLAUDE.md → 10) Common Tasks** for step-by-step implementation instructions.
