# **Ocht** – The Intelligent Data Workflow Platform

*Pronounced “OCK-t” (like “locked” without the ‘l’) — from the Irish word for “eight” — inspired by the octopus, a creature whose arms can act independently yet in harmony.*

---

## **Mission**

Replace data engineering tool sprawl with intelligent workflows that orchestrate, transform, and decide autonomously.

---

## **Vision**

A unified open-source platform where:

* **Workflows think** — every step can autonomously enrich, transform, and decide.
* **Batch and real-time are one** — no duplicate logic, no parallel stacks.
* **Developers are in control** — pipelines are declared as data, versioned, tested, and inherently auditable.
* **Insights arrive faster** — teams focus on business outcomes, not glue code.

---

## **The Octopus Advantage**

An octopus has a central brain but also neurons in each arm, enabling **local decision-making** without waiting for central control.
Ocht applies the same principle:

* Each step in a workflow can operate **independently** (deterministic or probabilistic).
* The orchestrator coordinates for **consistency, governance, and traceability**.
* Intelligence is **distributed**, enabling faster, more adaptive reactions to data events.

---

## **The Problem**

Data teams waste up to **60% of their time** managing tools instead of generating insights:

* **Multiple tools, multiple contexts** — SQL for transformations, Python for ML, YAML for orchestration — constant context switching slows delivery by 3–5×.
* **Batch/streaming divide** — duplicated logic across separate stacks doubles maintenance overhead.
* **Difficult ML integration** — models deployed separately from pipelines delay production by weeks.
* **Operational friction** — 5+ monitoring dashboards, inconsistent error handling, fragile integrations that break under scale.

**Impact:** New initiatives take months instead of weeks, with **40% higher operational costs** than necessary.

---

## **Strategy**

Ocht isn’t “just another orchestrator” — it’s the first platform built for **AI-native, intelligent data workflows**.

### **Core Innovation**

Treat ML inference as naturally as a SQL query:

* Define models alongside transformations in the **same pipeline**.
* Real-time and batch **share identical logic**.
* Autonomous decision-making at each step.
* Zero-deployment ML inference — models are **data**, not separate services.

### **Unlike existing tools**:

* **Airflow** — Orchestrates tasks, not intelligence.
* **dbt** — Transforms data but doesn’t integrate ML.
* **Kafka Streams** — Handles real-time but poorly integrates with batch.
* **MLflow** — Manages models but not pipelines.

---

## **Pipelines That Think About Data**

Traditional data pipelines move data.
**Ocht pipelines think about data**:

* **Adaptive thresholds** — ML models adjust anomaly detection in real-time.
* **Intelligent routing** — Data flows to different outputs based on ML predictions.
* **Self-healing** — Pipelines detect and compensate for data quality issues.
* **Contextual enrichment** — LLMs classify, extract, or enhance unstructured data.

---

## **Tactics**

* **Pipelines as Data** — declared in EDN, enabling programmatic manipulation, REPL-driven development, and composition.
* **Core Ingestion Support** — CSV, Parquet, JSON, Avro, plus JDBC for any database.
* **Transformation Engine** — pure Clojure functions for deterministic steps; embedded ML/AI for probabilistic ones.
* **Unified Orchestration** — internal scheduler with retries, backfills, and lineage tracking.
* **Security & Privacy by Design** — immutable, version-controlled definitions are inherently auditable; designed to integrate with encryption and access controls.
* **Interoperability First** — standards-based connectors ensure no vendor lock-in.
* **Long-Term Roadmap** — expand to unstructured data formats and enterprise-grade governance in a commercial edition.

---

## **Plan**

### **Roadmap Overview**

* **Q4 2025** — MVP (CSV → Transform → Console)
* **Q1 2026** — JDBC + ML Integration
* **Q2 2026** — Real-time Streaming
* **Q3 2026** — Enterprise Features

### **What You Can Do with the MVP (Available Now)**

* **Try the demo**: `clj -M:cli -p demo-simple.edn -v` (CSV → Console in seconds)
* **REPL development**: `clj -A:dev` for interactive component testing
* **Pure transformations**: Filter, map, take, group-by operations on data
* **Polylith architecture**: 6 working components demonstrating the full pattern
* **Production-ready code**: Linted, tested, following Clojure best practices

---

## **Why Clojure?**

* **Pipelines as Data** — EDN configuration is real data your code can manipulate.
* **REPL Development** — Test transformations interactively without restart cycles.
* **Immutability** — Reproducible, auditable pipeline execution by default.
* **Code Quality** — Automatic linting with clj-kondo ensures maintainable, idiomatic code.
* **JVM Ecosystem** — Access to all Java libraries and enterprise systems.

---

## **Technical Foundation**

* **Language:** Clojure (JVM) — functional purity, live REPL development, robust concurrency.
* **Pipeline Definition:** EDN — version-control friendly, programmatically manipulable.
* **Execution:** Unified batch/stream processing via internal scheduler.
* **ML Integration:** In-process model inference — no separate ML serving layer.
* **Connectors:** JDBC, Kafka, S3, HTTP APIs.
* **Deployment:** Single JAR, Docker container, or native binary (GraalVM).

---

## **Getting Started**

```bash
# Try the working demo
clj -M:cli -p demo-simple.edn -v

# Start REPL development  
clj -A:dev

# Run tests
clj -M:test

# Check code quality
clj-kondo --lint .
```

**See [CONTRIBUTING.md](CONTRIBUTING.md) for development workflow.**

**Priority Areas for Contributors:**

* 🔧 **More connectors** — Database, S3, HTTP, Kafka
* 🧠 **Transform functions** — Aggregations, joins, ML integration
* 📚 **Pipeline examples** — Real-world use cases
* 🧪 **Testing frameworks** — Property-based, contract testing
