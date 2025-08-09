# CLAUDE.md — Ocht Development Guide for Claude Code

**Audience:** Claude Code (AI pair-programmer) working on the Ocht repository.

**Goal:** Produce high-quality, composable Clojure code and documentation that fits Ocht's Polylith workspace, "pipelines-as-data" model (EDN), and functional-core/imperative-shell philosophy. Optimize for clarity, testability, and evolvability.

---

## 1) Always Start Here (What to Read / What to Ask)

### Required Reading Before Any Change:

1. **`ARCHITECTURE.md`** – structure, principles, and workspace layout.
2. **`README.md`** – mission, goals, quickstart.
3. **This `CLAUDE.md`** – coding rules and implementation patterns.
4. **`components/*/README.md`** – target brick(s) details.
5. **`projects/development/dev/user.clj`** – REPL helpers.

### When Information is Missing, Ask Precise Questions:

Template:

> **Clarify:** *I'm about to change `<area>` to achieve `<outcome>`. I'm unsure about `<specific ambiguity>`. Options: `<A/B>`. I recommend `<choice>` because `<reason>`.*

### When Proposing Code, Always State:

* **Intent:** what behavior changes.
* **Surface area:** added/changed namespaces, public functions, protocols.
* **Invariants & contracts:** pre/post conditions, effects.
* **Tests:** what will be added or updated.

---

## 2) Ocht Mental Model (Keep This In Your Head)

### Polylith Architecture

* **Components** = reusable libraries with public interfaces (`interface` ns).
* **Bases** = runnable entry points (`cli`, `worker`, later `api`).
* **Projects** = build configs assembling bases + components.

### Core Principles

* **Three phases per pipeline:** **Pull → Transform → Push**

  * *Pull*/*Push* are side-effecting connector calls (edges only).
  * *Transform* is **pure**, preferably via transducers for streaming.
* **Pipelines as data:** EDN compiled to an internal model; everything consumes the compiled form.
* **Unified batch + stream:** Same executor; no duplicate logic.
* **Effects at edges only:** connectors/bases do IO; core stays deterministic.
* **Explicit failure:** rich error maps at boundaries; clear invariants in the core.

### The Octopus Principle

Like an octopus with a central brain and independent arms:

* **Local decision-making** at each step.
* **Central coordination** for governance and traceability.
* **Distributed intelligence** for faster adaptation.

---

## 3) Definition of Done (What “Good” Looks Like)

Each item includes **why** it matters:

**Code Quality**

* [ ] Code compiles and `clj -X:test` (or `kaocha`) is green — ensures no regressions.
* [ ] No side effects in lazy sequences — prevents hidden runtime failures in streaming.
* [ ] No accidental realization outside scope — avoids resource leaks and memory blowups.
* [ ] Pure functions are truly pure — keeps transformations deterministic and testable.

**Documentation**

* [ ] Public APIs documented in namespace docstrings — improves discoverability.
* [ ] Component `README.md` updated with examples — shortens onboarding time.
* [ ] Docstrings include preconditions — makes contracts clear to callers.

**Testing**

* [ ] Property/contract tests for protocol implementations — guarantees behavior across connectors.
* [ ] Unit tests for pure fns with edge cases — ensures correctness in core logic.
* [ ] Integration tests updated if surface area changed — validates end-to-end flow.

**Architecture Compliance**

* [ ] Effects only at edges — preserves functional-core integrity.
* [ ] Option maps for optional params — makes APIs self-documenting.
* [ ] Public fns accept `{:keys [...] :as opts}` — supports future extensibility.
* [ ] Narrow, consistent naming — avoids semantic drift.

**Operations**

* [ ] Logs/metrics/events via `components/observability` — keeps monitoring consistent.
* [ ] No `println` — all logs structured for correlation.
* [ ] Secrets via config indirection — avoids credential leaks.
* [ ] Least-privilege credentials — reduces blast radius.

---

## 4) First PR Quickstart

1. **Clone & setup:**

   ```bash
   git clone https://github.com/yourorg/ocht
   cd ocht
   clj -Tpoly info
   ```
2. **Start REPL in dev project:**

   ```bash
   clj -A:dev
   ```
3. **Explore helpers in `dev/user.clj`** — e.g., `(reload-pipeline ...)`.
4. **Write or modify code** in a *component*, *never* directly in a base unless it’s wiring.
5. **Run tests:**

   ```bash
   clj -X:test
   ```
6. **Commit with message format:**

   ```
   feat(component): add <thing> to achieve <user outcome>
   ```
7. **Open PR** with intent, changes, risks, and test evidence.

---

## 5) Repository Conventions

* **Workspace:** matches Polylith layout (`bases/`, `components/`, `projects/`).
* **Namespace:**

  * Public API: `.../interface.clj`
  * Impl: sibling `impl/` ns.
  * Tests mirror source paths.
* **Docs:** each component has a `README.md` with purpose, API, guarantees, examples.
* **Config:** EDN overlays; secrets via env/keystore; profiles: `dev`, `test`, `prod`.

---

## 6) Code Style Rules

**Names** (per *Elements of Clojure*):

* Natural yet narrow — `parse-config`, `deduplicate-rows`, never `process-data` without context.
* Data: `m`, `xs`, `f` for generics; domain names for concrete shapes.
* Maps of maps: `a->b`, nested `a->b->c`.

**Functions**

* Do *one* of: pull / transform / push.
* Option maps for optional params; never Boolean flags.
* Use narrow accessors (`get`, `keys`) over generic seq ops.

**Side effects & Laziness**

* Effects only at edges.
* Call out side effects in `let`:

  ```clojure
  (let [data (fetch ...)
        _ (log/info ...)]
    ...)
  ```
* No IO in lazy seqs; realize sequences inside resource scope.

**Interop & Macros**

* Java interop explicit; hide behind adapters if reused.
* Macros only for syntax sugar; document expansion.

---

## 7) Core Protocols & Contracts

**Connector**

```clojure
(defprotocol Connector
  (pull [this config options])
  (push [this config data options])
  (validate [this config]))
```

* `pull` — bounded/streamable; no hidden effects.
* `push` — idempotent or documented otherwise.
* `validate` — cheap & side-effect free.

**Transform**

* Pure, total; prefer transducers; no IO.

**Executor**

* Takes compiled pipeline + opts; returns `{:result ...}` or `{:error ...}`.

---

## 8) Testing Standards

**Unit** — pure fns, edge cases, property tests.
**Contract** — connectors pass shared suite (`testkit`).
**Integration** — per base (CLI, worker, API).
Tests fail if effects occur in lazy transforms.

---

## 9) Error Handling & Observability

**At Boundaries:**

```clojure
{:error :file-too-large
 :details {:size-mb 1024 :limit-mb 512}
 :suggestions ["Increase limit" "Use streaming"]
 :correlation-id cid}
```

**Core:**

```clojure
(assert (valid? pipeline) "Invalid pipeline")
```

**Logging & Metrics:**

```clojure
(log/info {:event :pipeline-start
           :pipeline-id pid
           :correlation-id cid})
(metrics/timing :executor/total-ms elapsed)
```

* Lifecycle events: `started`, `step-begin/end`, `completed`, `failed`.
* Redact sensitive fields at edges.

---

## 10) Security & Privacy

* No secrets in repo; env/keystore only.
* Connectors use least-privilege credentials.
* Redact sensitive data before logging.

---

## 11) Common Tasks

**A) Add Stateless Transform**

```clojure
(defn deduplicate-rows
  "Remove duplicate rows based on key-fn"
  [key-fn]
  (comp (map (juxt key-fn identity))
        dedupe
        (map second)))

(register-transform
  :deduplicate
  {:fn deduplicate-rows
   :doc "Remove duplicate rows based on a key function"
   :examples [{:key-fn :id
               :input [{:id 1} {:id 1} {:id 2}]
               :expected [{:id 1} {:id 2}]}]})
```

**B) Add Connector**

```clojure
(ns connectors.csv
  (:require [clojure.data.csv :as csv]
            [ocht.connector.protocol :as proto]))

(defrecord CsvConnector []
  proto/Connector
  (pull [_ cfg _] {:data (read-csv (:path cfg))})
  (push [_ cfg data _] (write-csv (:path cfg) data))
  (validate [_ cfg] {:ok? (file-exists? (:path cfg))}))
```

Run connector contract tests from `testkit`.
