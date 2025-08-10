> **Navigation:**
> - [Start Here — CONTRIBUTING.md](CONTRIBUTING.md) for onboarding and repo workflow
> - [ARCHITECTURE.md](ARCHITECTURE.md) for Ocht’s design principles and structure

# CLAUDE.md — Ocht Development Guide for Claude Code

**Audience:** Claude Code (AI pair-programmer) working on the Ocht repository.
**Goal:** Produce high-quality, composable Clojure code and documentation that fits Ocht's **Polylith workspace**, **Pipelines-as-Data** model (EDN), and **functional-core/imperative-shell** philosophy. Optimize for clarity, testability, and evolvability.

---

## 1) Always Start Here

**Before touching code, read:**

1. **`ARCHITECTURE.md`** — especially:

   * *Architecture Overview* — workspace layout, brick types.
   * *Model–Interface–Environment* — core separation of responsibilities.
   * *Three Phases* — **Pull → Transform → Push**.
   * *Component Catalogue* — to find the brick you’ll work on.
2. **`README.md`** — mission, goals, quickstart.
3. **This `CLAUDE.md`** — coding rules & patterns.
4. **`components/*/README.md`** — target brick details.
5. **`projects/development/src/dev/user.clj`** — REPL helpers.

**If information is missing, ask**:

> **Clarify:** *I'm about to change `<area>` to achieve `<outcome>`. I'm unsure about `<specific ambiguity>`. Options: `<A/B>`. I recommend `<choice>` because `<reason>`.*

---

## 2) Ocht Mental Model

(See `ARCHITECTURE.md` — *Architecture Overview*, *Three Phases*, *Effects at Edges*.)

### Workspace (Polylith Bricks)

* **Components** = reusable libraries with public APIs in `interface` namespaces.
  - `connector-protocol` — shared Connector protocol and utilities
  - `pipeline-model` — parse and validate EDN pipeline definitions  
  - `pipeline-transform` — pure transformation functions (filter, map, take, group-by)
  - `csv-connector` — CSV file reader connector
  - `console-connector` — console output connector
  - `executor` — pipeline execution orchestrator
* **Bases** = runnable entry points.
  - `cli` — command line interface (implemented)
  - `worker` — background pipeline execution (planned)
  - `api` — HTTP API server (planned)
* **Projects** = build configs bundling bases + components for an environment.
  - `development` — development REPL and testing environment

### Core Process

* **Three phases:** **Pull → Transform → Push**

  * *Pull*/*Push* = side-effecting connector calls (edges only).
  * *Transform* = pure functions/transducers, no IO.
* **Pipelines as Data** — EDN is compiled into an internal model; all execution uses that model.
* **Unified batch + stream** — same executor logic in CLI and worker.
* **Effects at edges only** — connectors & bases perform IO; core is deterministic.
* **Explicit failure** — structured error maps at boundaries; invariants inside core.

---

## 3) Definition of Done

For each PR, confirm:

**Code Quality**

* Compiles and passes `clj -M:test -e "(clojure.test/run-all-tests)"`.
* No side effects in lazy sequences — see `ARCHITECTURE.md` (*Non-goals*).
* No premature realization of sequences — preserves streaming safety.
* Pure functions are truly pure — matches *pipeline-transform* guarantees.

**Docs**

* Public APIs documented in namespace docstrings.
* Component `README.md` updated with purpose, API, examples.
* Preconditions in docstrings — match invariants from `ARCHITECTURE.md` *Component Catalogue*.

**Testing**

* Protocol contract tests — all connectors must pass shared protocol tests.
* Unit tests for pure fns (transforms, model parsing).
* Integration tests per base (CLI implemented, worker/API planned).

**Architecture Compliance**

* Effects only at edges (connectors/bases).
* Option maps for optional params.
* Public fns accept `{:keys [...] :as opts}`.
* Naming matches brick purpose — see *Component Catalogue* in `ARCHITECTURE.md`.

**Ops**

* Logs/metrics/events via `components/observability` (planned).
* No `println` in production code (console-connector excepted for output).
* Secrets handled via config indirection.
* Least-privilege credentials.

---

## 4) First PR Quickstart

1. **Discover workspace bricks:**

   ```bash
   clj -Tpoly info
   ```
2. **Start REPL in dev project:**

   ```bash
   cd projects/development && clj -A:dev
   ```
3. Use helpers from `src/dev/user.clj` (e.g., `(refresh)`, `(refresh-all)`).
4. Make changes **only** inside a component unless wiring a base.
5. Run tests: `clj -M:test -e "(clojure.test/run-all-tests)"`.
6. Commit:

   ```
   feat(component): add <thing> to achieve <user outcome>
   ```
7. Open PR with intent, surface area, tests, risks, and evidence.

---

## 5) Code Style Rules

(See `ARCHITECTURE.md` — *Effects at Edges*, *Explicit Failure*, *Model–Interface–Environment*.)

**Names** (per *Elements of Clojure*):

* Narrow, descriptive — match domain (e.g., `deduplicate-rows`, not `process-data`).
* Data: `m`, `xs`, `f` for generics; domain-specific for concrete shapes.
* Maps of maps: `a->b`, nested `a->b->c`.

**Functions**

* Do *one* of: pull / transform / push — mirrors Three Phases.
* Option maps for optional params.
* Use narrow accessors (`get`, `keys`) over generic seq ops.

**Side effects & Laziness**

* Effects only at edges — see connectors in `ARCHITECTURE.md`.
* Explicit side effects in `let` bindings.
* No IO in lazy seqs; realize within resource scope.

**Interop & Macros**

* Java interop explicit; hide behind adapters if reused.
* Macros only for syntax sugar; document expansion.

---

## 6) Core Protocols & Contracts

(See `ARCHITECTURE.md` — *Contracts & Protocols*.)

**Connector**

Defined in `components/connector-protocol/`:

```clojure
(require '[ocht.connector :as conn])

;; Protocol methods available:
(conn/pull connector config options)
(conn/push connector config data options)
(conn/validate connector config)

;; Utility functions:
(conn/connector? obj)
(conn/validate-connector-result result operation)
```

* `pull` — bounded/streamable; no hidden effects.
* `push` — idempotent or documented otherwise.
* `validate` — cheap, side-effect free.
* All connectors implement `conn/Connector` protocol.

**Transform**

Available in `components/pipeline-transform/`:

```clojure
(require '[ocht.pipeline.transform :as transform])

;; Available transforms:
:filter  ;; {:transform-fn :filter :args {:predicate #(...)}}
:map     ;; {:transform-fn :map :args {:f #(...)}}
:take    ;; {:transform-fn :take :args {:n 10}}
:group-by ;; {:transform-fn :group-by :args {:key-fn :field}}

;; Functions:
(transform/apply-transforms data transform-steps)
(transform/create-transducer transform-steps)
```

**Executor**

Available in `components/executor/`:
* Accepts compiled pipeline + opts; returns `{:success true :result ...}` or `{:success false :error ...}`.
* Orchestrates Pull → Transform → Push flow with error handling.

---

## 7) Testing Standards

(See `ARCHITECTURE.md` — *Testing Strategy*.)

* **Unit tests** — pure fns, edge cases, property tests.
* **Protocol tests** — all connectors must implement and pass `conn/Connector` protocol tests.
* **Integration tests** — per base (CLI complete, worker/API planned).
* **End-to-end tests** — full pipeline execution via CLI.
* Fail if effects inside lazy transforms.

---

## 8) Error Handling & Observability

(See `ARCHITECTURE.md` — *Error Handling & Invariants*, *Observability & Operations*.)

**Error maps at boundaries:**

```clojure
{:error :file-too-large
 :details {:size-mb 1024 :limit-mb 512}
 :correlation-id cid}
```

**Logs & metrics via observability:**

```clojure
(log/info {:event :pipeline-start
           :pipeline-id pid
           :correlation-id cid})
(metrics/timing :executor/total-ms elapsed)
```

---

## 9) Security & Privacy

(See `ARCHITECTURE.md` — *Security & Config*.)

* No secrets in repo; env/keystore only.
* Least-privilege credentials for connectors.
* Redact sensitive data before logging.

---

## 10) Common Tasks

**A) Add Stateless Transform**

1. Create pure fn + transducer variant in `components/pipeline-transform/src/ocht/pipeline/transform/core.clj`.
2. Register in `transform-registry` map.
3. Add unit tests + update `README.md`.
4. Test integration via executor component.

**B) Add Connector**

1. Create new component in `components/<name>-connector/`.
2. Implement `ocht.connector/Connector` protocol in core.clj.
3. Provide interface namespace for clean API.
4. Pass connector protocol tests.
5. Register in executor's `connector-registry`.
6. Add to component README with examples.

**C) Working with the CLI**

Current demo command:
```bash
clj -M -m ocht.cli.core --pipeline demo-simple.edn --verbose
```

**D) Available Components & Status**

* ✅ `connector-protocol` — shared protocol (complete)
* ✅ `pipeline-model` — EDN parsing/validation (complete)  
* ✅ `pipeline-transform` — basic transforms (complete)
* ✅ `csv-connector` — CSV file reading (complete)
* ✅ `console-connector` — console output (complete)
* ✅ `executor` — pipeline orchestration (complete)
* ✅ `cli` base — command line interface (complete)
* 🔄 `worker` base — background execution (planned)
* 🔄 `api` base — HTTP REST API (planned)
* 🔄 `observability` component — logging/metrics (planned)
