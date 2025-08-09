# ARCHITECTURE.md

---

## Purpose

This document explains **how Ocht is structured using the Polylith architecture** and how that structure delivers the goals set out in `README.md` and `CLAUDE.md`.

> **Contributor Note:** When implementing changes, see **CLAUDE.md → 2) Ocht Mental Model** for day-to-day coding rules that enforce these principles.

---

## Executive Summary

Polylith fits Ocht perfectly: stable, scalable, and allows plugin-style growth without disturbing the core.

> **Contributor Note:** See **CLAUDE.md → 5) Code Style Rules** for how to keep new bricks aligned with these architectural goals.

---

## Architecture Overview

**Polylith 101**: Components, Bases, Projects.

> **Contributor Note:** See **CLAUDE.md → 2) Ocht Mental Model** for the mental model to keep in mind while coding.

---

## Workspace Layout

Polylith layout for Ocht:
`bases/`, `components/`, `projects/`.

> **Contributor Note:** See **CLAUDE.md → 5) Repository Conventions** for naming, namespace layout, and documentation requirements.

---

## Core Architectural Patterns

### Model–Interface–Environment

Defines **Model** (data types/invariants), **Interface** (protocols), **Environment** (effects).

> **Contributor Note:** See **CLAUDE.md → 6) Core Protocols & Contracts** for connector/transform/executor contracts you must follow.

### Three Phases

**Pull → Transform → Push** separation.

> **Contributor Note:** See **CLAUDE.md → 5) Code Style Rules** and **10) Common Tasks** for patterns that respect this separation.

---

## Component Catalogue

List of core components (`pipeline.model`, `pipeline.transform`, etc.).

> **Contributor Note:** Before modifying a component here, check **CLAUDE.md → 3) Definition of Done** and **7) Testing Standards** for required tests and docs.

---

## Bases & Projects

Bases: `cli`, `worker`, `api`.
Projects: `development`, deployables.

> **Contributor Note:** See **CLAUDE.md → 4) First PR Quickstart** for how to choose the right brick and test it in the dev REPL.

---

## Contracts & Protocols

Connector, Transform, Executor interfaces.

> **Contributor Note:** See **CLAUDE.md → 6) Core Protocols & Contracts** for full signatures, return shapes, and idempotency rules.

---

## Error Handling & Invariants

Structured error maps, assertions, invariants.

> **Contributor Note:** See **CLAUDE.md → 8) Error Handling & Observability** for log formats, metrics, and redaction rules.

---

## Testing Strategy

Unit, property, contract, integration.

> **Contributor Note:** See **CLAUDE.md → 7) Testing Standards** for the exact structure of each test type and failure conditions.

---

## Performance & Concurrency

Chunking, back-pressure, parallelism.

> **Contributor Note:** See **CLAUDE.md → 3) Definition of Done (Why)** for why streaming safety and laziness rules matter.

---

## Observability & Operations

Metrics, structured logs, lifecycle events.

> **Contributor Note:** See **CLAUDE.md → 8) Error Handling & Observability** for the full logging/metrics code pattern.

---

## Security & Config

Secrets, least privilege, mTLS.

> **Contributor Note:** See **CLAUDE.md → 9) Security & Privacy** for implementation guidance.

---

## Evolution Path

How to add new connectors, transforms, and bases.

> **Contributor Note:** See **CLAUDE.md → 10) Common Tasks** for step-by-step implementation instructions.

Do you want me to make that next?
