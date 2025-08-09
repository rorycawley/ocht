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
5. **`projects/development/dev/user.clj`** — REPL helpers.

**If information is missing, ask**:

> **Clarify:** *I'm about to change `<area>` to achieve `<outcome>`. I'm unsure about `<specific ambiguity>`. Options: `<A/B>`. I recommend `<choice>` because `<reason>`.*

---

## 2) Ocht Mental Model

(See `ARCHITECTURE.md` — *Architecture Overview*, *Three Phases*, *Effects at Edges*.)

### Workspace (Polylith Bricks)

* **Components** = reusable libraries with public APIs in `interface` namespaces.
* **Bases** = runnable entry points (`cli`, `worker`, `api`).
* **Projects** = build configs bundling bases + components for an environment.

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

* Compiles and passes `clj -X:test`.
* No side effects in lazy sequences — see `ARCHITECTURE.md` (*Non-goals*).
* No premature realization of sequences — preserves streaming safety.
* Pure functions are truly pure — matches *pipeline.transform* guarantees.

**Docs**

* Public APIs documented in namespace docstrings.
* Component `README.md` updated with purpose, API, examples.
* Preconditions in docstrings — match invariants from `ARCHITECTURE.md` *Component Catalogue*.

**Testing**

* Property tests or contract tests for protocols — align with `testkit` design in `ARCHITECTURE.md`.
* Unit tests for pure fns.
* Integration tests per base (CLI, worker, API).

**Architecture Compliance**

* Effects only at edges (connectors/bases).
* Option maps for optional params.
* Public fns accept `{:keys [...] :as opts}`.
* Naming matches brick purpose — see *Component Catalogue* in `ARCHITECTURE.md`.

**Ops**

* Logs/metrics/events via `components/observability`.
* No `println`.
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
   clj -A:dev
   ```
3. Use helpers from `dev/user.clj` (e.g., `(reload-pipeline ...)`).
4. Make changes **only** inside a component unless wiring a base.
5. Run tests: `clj -X:test`.
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

```clojure
(defprotocol Connector
  (pull [this config options])
  (push [this config data options])
  (validate [this config]))
```

* `pull` — bounded/streamable; no hidden effects.
* `push` — idempotent or documented otherwise.
* `validate` — cheap, side-effect free.

**Transform**

* Pure, total within validated model ranges; prefer transducers.

**Executor**

* Accepts compiled pipeline + opts; returns `{:result ...}` or `{:error ...}`.

---

## 7) Testing Standards

(See `ARCHITECTURE.md` — *Testing Strategy*.)

* **Unit tests** — pure fns, edge cases, property tests.
* **Contract tests** — connectors pass shared suite from `testkit`.
* **Integration tests** — per base (CLI, worker, API).
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

1. Create pure fn + transducer variant in `components/pipeline/transform/...`.
2. Register in `registry.transform`.
3. Add property tests + update `README.md`.

**B) Add Connector**

1. Implement Connector protocol in `components/connectors/...`.
2. Ensure idempotent push semantics.
3. Pass connector contract tests from `testkit`.
