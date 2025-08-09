# ARCHITECTURE

## Purpose

This document explains **how Ocht is structured using the Polylith architecture** and how that structure delivers the goals set out in `README.md` and `CLAUDE.md`:

* **Distributed intelligence** (autonomous steps)
* **Pipelines as data** (EDN-first)
* **REPL‑first development** (interactive, testable components)
* **Unified processing** (batch + streaming share logic)
* **Effects only at the edges** (pure functional core)
* **Explicit failure** (clear invariants and boundaries)

---

## Executive Summary

**Polylith is an outstanding fit for Ocht.** It gives us a stable, scalable way to grow a plugin-style data platform where new connectors, transforms, and bases can be added without disturbing the core. The workspace is subdivided into **reusable components**, **runnable bases**, and **buildable projects**, which directly supports our Model–Interface–Environment pattern and Three‑Phases (Pull → Transform → Push) process model.

---

## Architecture Overview

### Polylith 101 (in Ocht terms)

* **Component** = a reusable library with a clear public API. Examples: `pipeline.model`, `pipeline.executor`, `connector.csv`, `transform.registry`.
* **Base** = an entry point that wires components to run an application. Examples: `cli`, `worker`, `api`.
* **Project** = build configuration that bundles a base + selected components for a particular environment. Examples: `development` (REPL), `ocht-cli` (uberjar), `ocht-worker`.

### How this maps to our principles

| Goal                         | Polylith mechanism                                                                           |
| ---------------------------- | -------------------------------------------------------------------------------------------- |
| **Distributed intelligence** | Each “intelligent arm” is a component with a narrow API; bases orchestrate many arms.        |
| **Pipelines as data (EDN)**  | `pipeline.model` parses/validates EDN; other components consume the same in-memory model.    |
| **REPL-first**               | `projects/development` exposes all components for interactive exploration and fast feedback. |
| **Unified batch/stream**     | One `pipeline.executor` used by `cli` (batch) and `worker` (stream).                         |
| **Effects at edges**         | IO lives in connector components and bases; pure core remains deterministic and testable.    |
| **Explicit failure**         | Preconditions/postconditions at component boundaries; error mapping centralized in bases.    |

---

## Workspace Layout (proposed)

```
ocht/
├─ bases/
│  ├─ cli/            # command line entry point
│  ├─ worker/         # streaming/queue consumer
│  └─ api/            # REST/HTTP API (future)
│
├─ components/
│  ├─ pipeline/
│  │  ├─ model/       # EDN schema, validation, compilation, invariants
│  │  ├─ transform/   # pure transforms + transducers
│  │  └─ executor/    # Pull→Transform→Push orchestration via protocols
│  │
│  ├─ connectors/
│  │  ├─ csv/
│  │  ├─ jdbc/
│  │  ├─ kafka/       # future
│  │  └─ s3/          # future
│  │
│  ├─ registry/
│  │  ├─ transform/   # transform registry + discovery
│  │  └─ connector/   # connector registry + capability queries
│  │
│  ├─ ml/runtime/     # ML inference abstraction (optional)
│  ├─ observability/  # metrics, logging, events
│  ├─ config/         # EDN config loading/merging, secrets API
│  └─ testkit/        # generators, golden data, contract tests
│
├─ projects/
│  ├─ development/    # unified REPL (all components)
│  ├─ ocht-cli/       # deployable CLI artifact
│  └─ ocht-worker/    # deployable worker artifact
│
└─ deps.edn           # workspace deps
```

---

## Core Architectural Patterns

### Model–Interface–Environment

* **Model**: Domain data types and invariants are defined in `components/pipeline/model` (e.g., `Pipeline`, `Step`, validation spec/schema, compiler).
* **Interface**: Narrow protocols for interacting with the model and environment: `Connector` (pull/push/validate), `Transform` (apply), `Executor` (execute). Public functions are exposed from `interface` namespaces per component.
* **Environment**: Bases + connector components implement side effects (files, JDBC, Kafka, S3, HTTP). They are wired by projects into deployables.

### Three Phases (per process)

1. **Pull**: connectors acquire and validate input; executor materializes bounded chunks; options map controls limits/timeouts.
2. **Transform**: pure data-in/data-out transformations using transducers where possible; laziness contains **no** side effects.
3. **Push**: connectors persist, publish, or emit results; idempotency is enforced in connector implementations.

This separation keeps the **functional core** pure and moves all effects to the **edges**.

---

## Component Catalogue (initial set)

### `pipeline.model`

* **Responsibilities**: parse/validate EDN pipelines; enforce invariants; compile to an optimized internal form.
* **Guarantees**: if a pipeline compiles, downstream components can assume shape/semantics.
* **README alignment**: *Pipelines as data*; *Fail explicitly*; *Document assumptions*.

### `pipeline.transform`

* **Responsibilities**: canonical transform functions (filter/map/sort/aggregate), always pure; transducer variants for streaming.
* **Guarantees**: no IO; total functions within validated input ranges.
* **README alignment**: *Unified processing*; *Effects at edges only*.

### `registry.transform`

* **Responsibilities**: central registry for transform discovery and capability metadata; exposes `get-transform` and `list-transforms`.
* **Guarantees**: consistent lookup; helpful error data on missing keys.

### `pipeline.executor`

* **Responsibilities**: orchestrate Pull→Transform→Push; concurrency/back‑pressure; resource management; metrics hooks.
* **Guarantees**: bounded memory (chunking); timeouts; cancellation; structured error mapping.
* **README alignment**: *Correctness first → Clarity → Performance*; *Explicit failure*.

### `connectors.*`

* **Responsibilities**: implement `Connector` protocol for specific systems (CSV, JDBC, Kafka, S3...).
* **Guarantees**: idempotent `push` where possible; `validate` reports capabilities/limits; no leakage of vendor details upstream.
* **README alignment**: *Effects at edges only*; *Plugin architecture*.

### `ml.runtime` (optional, later)

* Abstracts inference endpoints (e.g., ONNX Runtime, TorchServe). Keeps ML effects at edges; deterministic contracts in the core.

### `observability`

* Common logging/metrics/events interfaces; provides noop + prod implementations; used by executor and connectors.

### `config`

* Deterministic config loading/merging; secrets indirection; supports profiles (dev/test/prod) without code changes.

### `testkit`

* Property generators for pipelines and data shapes; contract tests for connectors; golden files for reproducible runs.

---

## Bases & Projects

### Bases

* **`cli`**: reads a pipeline EDN and input path(s); executes once; prints/exports results and metadata.
* **`worker`**: subscribes to a stream/queue; executes pipelines continuously with back‑pressure.
* **`api`** (future): exposes HTTP endpoints for submission/inspection of pipelines and runs.

### Projects

* **`development`**: unified REPL—including all components/bases, `dev/user.clj` helpers (e.g., `(test-pipeline ...)`, `(reload-pipeline ...)`).
* **`ocht-cli` / `ocht-worker`**: minimal dependency graphs per deployable for tiny images and fast cold starts.

---

## Contracts & Protocols

### Connector Protocol (sketch)

```clojure
(pull config options)   ; => {:data seq|stream, :metadata {...}}
(push config data options) ; => {:success true, :rows-written n, :metadata {...}}
(validate config)       ; => {:ok? boolean, :capabilities #{...}, :errors [...]}
```

**Rules**

* `pull` returns bounded or streamable data; never performs hidden side effects.
* `push` is idempotent when target supports it (e.g., UPSERT semantics); otherwise documents idempotency keys.
* `validate` is cheap and side‑effect free.

### Transform API

* Functions are pure and total within compiled model constraints.
* Composition via transducers when dealing with large/streaming data.

### Executor API

* Accepts a compiled pipeline + runtime options; returns `{:result x :metadata m}` or `{:error k :details d}` at boundaries.

---

## Error Handling & Invariants

* **At boundaries** (bases/executor): try/catch with structured error maps (e.g., `:file-too-large`, `:out-of-memory`, `:invalid-pipeline`).
* **In the core** (model/transform): let failures surface; rely on pre-validated shapes; use assertions and specs with helpful messages.
* **Assumptions vs Invariants**: assumptions are documented in docstrings; invariants enforced via pre/post or spec.

---

## Testing Strategy

* **Unit & Property tests** per component (`components/*/test`).
* **Contract tests**: shared suites in `testkit` that every connector must pass (read/write/validate behaviors, idempotency, limits).
* **Integration tests** per project (e.g., CLI run against fixture data; worker against a local queue).
* **Non‑goals**: no effects hidden inside lazy sequences; tests fail if IO is observed mid‑transform.

---

## Performance & Concurrency

* **Chunked processing** for large files; **transducers** to minimize allocations.
* **Back‑pressure** in `worker` base; bounded queues.
* **Parallelism**: `pmap`/`core.async` (or an execution pool) for CPU‑bound transforms; never for IO unless explicitly safe.
* **Optimize last**: correctness → clarity → measured optimization.

---

## Observability & Operations

* **Metrics**: executor stages timing, rows processed, memory watermark, back‑pressure signals.
* **Logs**: structured, correlation IDs per run; redaction rules applied at the edge.
* **Events**: lifecycle events (started, step-begin/end, completed, failed) emitted from executor hooks.

---

## Security & Config

* **Config**: environment-agnostic EDN with overlays; secrets via indirection (env/keystore); no secrets in source.
* **Connectors**: least‑privilege credentials; optional mTLS/ACLs per target; audit hooks via `observability`.

---

## Evolution Path

1. **MVP (Q4 2025)**: `cli` base; `pipeline.model`, `pipeline.transform`, `pipeline.executor`; `connector.csv`; console output.
2. **Next**: add `connector.jdbc`; introduce `worker` base for streaming; basic observability.
3. **Later**: ML transforms via `ml.runtime`; `api` base; more connectors (Kafka/S3); scheduler and monitoring components.

Polylith makes each addition a **new brick**—existing bricks remain untouched.

---

## Trade‑offs & Risks

* **Learning curve**: components/bases/projects terminology; mitigated by examples and templates.
* **Granularity**: too fine → overhead; too coarse → coupling. Start coarse (`connectors`, `transforms`), split when needed.
* **Cross‑language edges**: keep them in connectors/bases to avoid leaking details into the core.

---

## Getting Started (practical steps)

1. Create workspace: `clj -Tpoly create :name ocht :top-ns ocht`.
2. Add initial components: `pipeline.model`, `pipeline.transform`, `pipeline.executor`, `connectors.csv`, `registry.transform`.
3. Create bases: `cli`; later `worker`.
4. Create projects: `development`, `ocht-cli`.
5. Wire `dev/user.clj` helpers for REPL: `(test-pipeline ...)`, `(reload-pipeline ...)`.
6. Add `testkit` with property and contract tests; make connector tests mandatory.

---

## Glossary (Ocht)

* **Pipeline**: EDN description of steps; compiled to an internal plan.
* **Step**: a typed unit of work (ingest/transform/ml-inference/output).
* **Connector**: effectful adapter to an external system implementing the `Connector` protocol.
* **Transform**: pure function/transducer transforming in‑memory data.
* **Base**: runnable application that composes components.
* **Project**: build definition for a base + selected components.

---

## Appendix: Alignment Checklist

* REPL‑first? **Yes** → `projects/development` + `dev/user.clj` utilities.
* Effects at edges? **Yes** → connectors & bases only.
* Pipelines as data? **Yes** → `pipeline.model` compiles EDN; everything consumes the compiled form.
* Unified batch/stream? **Yes** → same executor in `cli` and `worker`.
* Explicit failure? **Yes** → structured error maps at boundaries; invariants in the core.
* Evolvable plugins? **Yes** → new connectors/transforms as components, no core edits needed.
