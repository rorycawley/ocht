# CONTRIBUTING.md

Welcome to **Ocht** — the Intelligent Data Workflow Platform.

This guide explains how to get started as a contributor and where to find **the two core documents** that define *what* to build and *how* to build it.

---

## 📜 Core Docs — Always Read Before You Code

1. **[ARCHITECTURE.md](ARCHITECTURE.md)**
   *The “Why” and “What”* — explains Ocht’s structure, principles, and patterns.

   * **Read this** to understand *why* the system is designed the way it is and *what* each part does.
   * Includes workspace layout, component catalogue, process phases, and contracts.

2. **[CLAUDE.md](CLAUDE.md)**
   *The “How”* — coding rules, style guide, and implementation patterns.

   * **Read this** to know *how* to write code that fits Ocht’s Polylith workspace.
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
6. **Open a PR** — Include intent, scope, risks, test evidence.

---

## 🛡️ Contributor Principles

* **Follow the separation of concerns** — *Pull → Transform → Push*.
* **No IO in lazy sequences** — See **CLAUDE.md → Laziness Rules**.
* **Use option maps, not Boolean flags** — For API flexibility.
* **Document everything public** — In namespace docstrings + component README.
* **Test behaviours, not wiring** — Pure core functions must be fully tested in isolation.

---

## 📬 Getting Help

If you’re unsure about a change:

* Use the “Clarify” template from **CLAUDE.md → Always Start Here**.
* Tag your PR as `[WIP]` if you want early review.

---

This CONTRIBUTING.md now works as a fast orientation layer — every path leads either to the *why* (**ARCHITECTURE.md**) or the *how* (**CLAUDE.md**).

Do you want me to add those cross-links too so all three docs form a navigable triangle?
