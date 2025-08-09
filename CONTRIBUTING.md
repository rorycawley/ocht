# CONTRIBUTING.md — Ocht

Welcome to **Ocht** — the Intelligent Data Workflow Platform.

This guide explains how to get started as a contributor and where to find **the two core documents** that define *what* to build and *how* to build it.

---

## 📜 Core Docs — Always Read Before You Code

1. **[ARCHITECTURE.md](ARCHITECTURE.md)**
   *The “Why” and “What”* — explains Ocht’s structure, principles, and patterns.

   * Read this to understand *why* the system is designed the way it is and *what* each part does.
   * Includes workspace layout, component catalogue, process phases, and contracts.

2. **[CLAUDE.md](CLAUDE.md)**
   *The “How”* — coding rules, style guide, and implementation patterns.

   * Read this to know *how* to write code that fits Ocht’s Polylith workspace.
   * Includes code style rules, testing standards, PR checklist, and common task recipes.

> **Tip:** Always keep both docs open in split view when working — **ARCHITECTURE.md** for context, **CLAUDE.md** for execution.

---

## 🚀 First-Time Setup

```bash
# Clone repo
git clone https://github.com/yourorg/ocht
cd ocht

# Install deps
clj -Tpoly info

# Start dev REPL
clj -A:dev
```

See **CLAUDE.md → 4) First PR Quickstart** for the full REPL workflow and commit guidelines.

---

## 🧠 Contribution Flow

1. **Pick a brick** — See *Component Catalogue* in **ARCHITECTURE.md**.
2. **Understand its role** — Cross-check with its `components/*/README.md`.
3. **Write tests first** — Contract tests for connectors, property/unit for core.
4. **Code to CLAUDE.md rules** — Respect “effects at edges” and “pure core” guidelines.
5. **Run all tests** — `clj -X:test` or `kaocha`.
6. **Open a PR** — Include intent, scope, risks, and test evidence.

---

## 🛡️ Contributor Principles

* **Follow the separation of concerns** — *Pull → Transform → Push*.
* **No IO in lazy sequences** — See **CLAUDE.md → Laziness Rules**.
* **Use option maps, not Boolean flags** — For API flexibility.
* **Document everything public** — In namespace docstrings + component README.
* **Test behaviours, not wiring** — Pure core functions must be fully tested in isolation.

---

## ⚠️ Most Common Mistakes to Avoid

These are the top issues caught in PR review — read and avoid them from the start:

1. **Hidden side effects in lazy sequences**

   * Side effects in `map`, `filter`, or other lazy ops can trigger at unpredictable times.
   * Realize sequences *inside* the scope that owns any resources.

2. **Skipping connector contract tests**

   * Every new connector must pass `testkit` shared contract tests — not just unit tests.

3. **Putting IO in the core**

   * Core (e.g., `pipeline.transform`) must stay pure.
   * All effects belong in connectors or bases — see **ARCHITECTURE.md → Effects at Edges**.

4. **Boolean flags in function signatures**

   * Replace with option maps:

     ```clojure
     ;; Bad
     (process-data data true false)
     ;; Good
     (process-data data {:validate? true :dry-run? false})
     ```

5. **Not updating component README.md**

   * Every public API change must be reflected in its brick’s README with updated examples.

6. **Logging sensitive data**

   * Use `observability` component for logs/metrics and redact fields at edges.
   * No direct `println`.

7. **Forgetting to run all tests before PR**

   * `clj -X:test` must be green — contract, unit, property, and integration tests.

---

## 📬 Getting Help

If you’re unsure about a change:

* Use the “Clarify” template from **CLAUDE.md → Always Start Here**.
* Tag your PR as `[WIP]` if you want early review.
