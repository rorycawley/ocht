# CLAUDE.md - Ocht Project Development Guide

## Project Context

You are helping build **Ocht**, an intelligent data workflow platform inspired by the octopus's distributed intelligence. This guide synthesizes principles from "Elements of Clojure" by Zachary Tellman to guide AI-assisted development.

### Core Philosophy
- **Distributed Intelligence**: Like an octopus with neurons in each arm, each workflow step can make autonomous decisions
- **Pipelines as Data**: All pipelines are declared in EDN, making them programmatically manipulable
- **REPL-First Development**: Every component should be testable interactively
- **Unified Processing**: Batch and streaming share the same logic

## Naming Principles

### General Rules
1. **Names should be narrow and consistent**
   - Narrow: clearly excludes what it cannot represent
   - Consistent: easily understood by someone familiar with the codebase

2. **Prefer natural names over synthetic ones**
   - Natural: `pipeline`, `transform`, `connector`
   - Synthetic: `monad`, `functor` (avoid unless necessary)

### Data Naming Conventions

```clojure
;; Single values
moon         ; a single moon entity
student      ; a single student record

;; Collections
moons        ; sequence of moon entities
students     ; sequence of students

;; Maps
key->value   ; explicit mapping
class->students  ; departments to their students
dept->class->students ; nested mappings

;; Tuples
tutor+student  ; 2-vector of different types
```

### Function Naming

```clojure
;; Functions that cross data scopes (I/O) use verbs
get-pipeline    ; pulls from storage
save-results    ; pushes to storage
delete-job      ; affects external state

;; Pure transformations avoid verbs
->csv           ; transforms to CSV
pipeline-md5    ; calculates hash
validate        ; checks validity (returns boolean)

;; In pipeline namespace, names can be shorter
(ns ocht.pipeline)
get            ; instead of get-pipeline
parse          ; instead of parse-pipeline
```

## Code Structure Idioms

### Prefer < and <= for inequalities
```clojure
;; GOOD - consistent ascending order
(cond
  (< n 10) :small
  (< n 100) :medium
  :else :large)

;; AVOID - mixing operators
(cond
  (< n 10) :small
  (> n 9) :medium  ; confusing
  :else :large)
```

### Use option maps, not named parameters
```clojure
;; GOOD - option map
(defn execute-pipeline
  [pipeline-def {:keys [timeout batch-size] 
                 :or {timeout 30000 
                      batch-size 100}
                 :as options}]
  ...)

;; AVOID - named parameters
(defn execute-pipeline
  [pipeline-def & {:keys [timeout batch-size]}]
  ...)
```

### Handle nil explicitly
```clojure
;; GOOD - explicit nil handling
(defn process-data [data]
  (if (nil? data)
    (throw (IllegalArgumentException. "Data cannot be nil"))
    (transform data)))

;; AVOID - implicit nil propagation
(defn process-data [data]
  (when data  ; just passes the buck
    (transform data)))
```

## Module Design (Indirection)

### Model-Interface-Environment Pattern

Each module in Ocht should follow this pattern:

```clojure
;; Model - internal representation
(defrecord Pipeline [steps metadata state])

;; Interface - how others interact
(defprotocol IPipeline
  (add-step [this step])
  (execute [this input])
  (status [this]))

;; Environment - everything else (users, databases, other modules)
```

### Assumptions vs Invariants

```clojure
;; Invariant - enforced by the module
(defn add-step [pipeline step]
  {:pre [(valid-step? step)]  ; enforced
   :post [(valid-pipeline? %)]}
  ...)

;; Assumption - must be documented
(defn load-csv
  "Assumes file exists and is < 1GB.
   Throws IOException if file not found.
   May throw OutOfMemoryError for large files."
  [filename]
  ...)
```

## Process Composition

### The Three Phases Pattern

Every process in Ocht should separate:

1. **Pull Phase** - Acquire and validate input
2. **Transform Phase** - Pure functional core
3. **Push Phase** - Execute effects

```clojure
(defn process-pipeline [config]
  ;; PULL - acquire and validate
  (let [input (pull-input config)
        validated (validate-input input)]
    
    ;; TRANSFORM - pure functions only
    (let [result (-> validated
                     parse-edn
                     optimize-pipeline
                     compile-steps)]
      
      ;; PUSH - perform effects
      (push-results result config))))
```

### Error Handling Strategy

```clojure
;; At process boundaries - be explicit
(defn robust-file-reader [filename options]
  (try
    (let [content (slurp filename)]
      (if (> (count content) (:max-size options))
        {:error :file-too-large :size (count content)}
        {:success true :content content}))
    (catch IOException e
      {:error :file-not-found :message (.getMessage e)})
    (catch OutOfMemoryError e
      {:error :out-of-memory :message "File too large"})))

;; In functional core - let it fail
(defn transform-data [data]
  (-> data
      parse-edn  ; assumes valid EDN
      normalize  ; assumes expected structure
      optimize)) ; assumes normalized input
```

## Pipeline-Specific Patterns

### EDN Pipeline Definition

```clojure
;; Example pipeline structure
{:pipeline/id "customer-analysis"
 :pipeline/version 1
 :pipeline/steps
 [{:step/type :ingest
   :step/source {:type :csv
                 :path "data/customers.csv"
                 :options {:header true}}}
  
  {:step/type :transform
   :step/function :filter
   :step/params {:age {:gt 18}}}
  
  {:step/type :ml-inference
   :step/model "sentiment-analyzer"
   :step/input-field :comment
   :step/output-field :sentiment}
  
  {:step/type :output
   :step/destination {:type :jdbc
                      :table "results"}}]}
```

### Connector Pattern

```clojure
(ns ocht.connectors.csv)

(defprotocol Connector
  (pull [this options])
  (push [this data options])
  (validate [this]))

(defrecord CSVConnector [config]
  Connector
  (pull [this options]
    ;; Return data + metadata
    {:data (read-csv (:path config))
     :metadata {:rows-read 100
                :source (:path config)}})
  
  (push [this data options]
    ;; Write and return confirmation
    (write-csv (:path config) data)
    {:success true
     :rows-written (count data)})
  
  (validate [this]
    ;; Check configuration validity
    (and (string? (:path config))
         (.exists (io/file (:path config))))))
```

### Transform Registry

```clojure
(ns ocht.transform.registry)

;; Register available transformations
(def transforms
  {:filter (fn [data pred]
             (filter (compile-predicate pred) data))
   
   :map (fn [data f]
          (map (compile-function f) data))
   
   :aggregate (fn [data group-by agg-fn]
                (aggregate-by data group-by agg-fn))})

(defn get-transform [key]
  (or (get transforms key)
      (throw (ex-info "Unknown transform" {:key key}))))
```

## REPL Development Workflow

### Interactive Pipeline Development

```clojure
;; dev/user.clj - REPL utilities
(ns user
  (:require [ocht.core :as ocht]
            [ocht.pipeline :as p]))

(def sample-data
  "Sample data for REPL testing"
  [{:id 1 :age 25 :name "Alice"}
   {:id 2 :age 17 :name "Bob"}
   {:id 3 :age 30 :name "Charlie"}])

(defn test-pipeline
  "Test a pipeline definition with sample data"
  [pipeline-edn]
  (-> pipeline-edn
      p/parse
      (p/execute sample-data)))

(defn reload-pipeline
  "Reload a pipeline from file"
  [filename]
  (-> filename
      slurp
      edn/read-string
      test-pipeline))

;; Usage in REPL:
;; (test-pipeline {:pipeline/steps [...]})
;; (reload-pipeline "pipelines/example.edn")
```

## Testing Patterns

### Property-Based Testing

```clojure
(defn pipeline-properties
  "Properties that should hold for all pipelines"
  [pipeline]
  {:idempotent? (= (execute pipeline data)
                   (execute pipeline data))
   :preserves-count? (= (count input)
                        (count (execute pipeline input)))
   :type-consistent? (uniform-type? (execute pipeline input))})
```

### Test Data Generators

```clojure
(defn generate-test-pipeline
  "Generate random but valid pipeline for testing"
  []
  {:pipeline/id (str (random-uuid))
   :pipeline/steps
   (repeatedly (inc (rand-int 5))
               #(rand-nth [{:step/type :filter
                           :step/pred {:age {:gt (rand-int 100)}}}
                          {:step/type :map
                           :step/fn :uppercase}
                          {:step/type :sort
                           :step/by :age}]))})
```

## Common Pitfalls to Avoid

### 1. Lazy Sequence Effects
```clojure
;; BAD - effects in lazy seq
(defn process-files [filenames]
  (map slurp filenames))  ; IO happens lazily!

;; GOOD - explicit realization
(defn process-files [filenames]
  (doall (map slurp filenames)))  ; forces immediate IO
```

### 2. Unbounded Data
```clojure
;; BAD - no size limits
(defn load-all [source]
  (slurp source))

;; GOOD - bounded operations
(defn load-bounded [source max-size]
  (let [content (slurp source)]
    (if (> (count content) max-size)
      (throw (ex-info "Content too large" 
                      {:size (count content) :max max-size}))
      content)))
```

### 3. Mixed Concerns
```clojure
;; BAD - IO mixed with logic
(defn analyze-and-save [data]
  (let [result (analyze data)]
    (spit "output.txt" result)  ; effect in middle!
    (further-process result)))

;; GOOD - separated concerns
(defn analyze [data]
  (-> data
      analyze
      further-process))

(defn save-analysis [data filename]
  (spit filename (analyze data)))
```

## Claude Code Prompting Guide

When requesting code from Claude Code, use these patterns:

### For New Components
```
"Create src/ocht/connectors/jdbc.clj that implements the Connector protocol for JDBC databases. Include connection pooling, prepared statements, and transaction support. Follow the same pattern as CSVConnector."
```

### For Pipeline Definitions
```
"Create resources/pipelines/etl-example.edn that demonstrates a complete ETL pipeline: CSV input -> filter age > 18 -> enrich with ML sentiment -> aggregate by region -> JDBC output"
```

### For Tests
```
"Create test/ocht/pipeline/executor_test.clj with property-based tests for pipeline execution. Test idempotency, error handling, and resource cleanup."
```

### For REPL Utilities
```
"Add to dev/user.clj a function that visualizes pipeline execution as a flow diagram, showing data shapes at each step"
```

## Performance Considerations

### Memory Management
- Use transducers for large data sets
- Implement chunked processing for files > 100MB
- Provide streaming alternatives for all connectors

### Concurrency
- Use `pmap` for CPU-bound parallel transforms
- Implement back-pressure for streaming pipelines
- Default to atoms for state, only use STM when necessary

### Optimization Priorities
1. Correctness first
2. Clarity second  
3. Performance third
4. Only optimize measured bottlenecks

## Documentation Standards

### Function Documentation
```clojure
(defn execute-pipeline
  "Executes a pipeline definition against input data.
  
  Pipeline must be a valid EDN pipeline definition.
  Options:
    :timeout - max execution time in ms (default: 30000)
    :batch-size - records to process at once (default: 100)
    :async? - return channel instead of blocking (default: false)
  
  Returns a map with :result and :metadata keys.
  Throws ExceptionInfo on validation failure."
  [pipeline input options]
  ...)
```

### Namespace Documentation
```clojure
(ns ocht.pipeline.executor
  "Pipeline execution engine.
  
  This namespace provides the core execution logic for Ocht pipelines.
  Pipelines are executed in stages with automatic error recovery and
  resource management.
  
  Key functions:
  - execute: Run a complete pipeline
  - validate: Check pipeline definition
  - optimize: Pre-execution optimization"
  (:require ...))
```

## Evolution Path

### MVP (Q4 2025)
Focus on:
- CSV ingestion
- Basic transformations (filter, map, sort)
- Console output
- REPL-driven development

### Next Iterations
1. Add JDBC support
2. Integrate ML models
3. Add streaming support
4. Build web UI
5. Add monitoring/observability

## Remember

1. **Every pipeline is data** - Keep definitions in EDN, not code
2. **Effects at edges only** - Pure functional core, I/O at boundaries
3. **REPL-first** - If it's not REPL-testable, redesign it
4. **Fail explicitly** - Better to crash than corrupt
5. **Document assumptions** - What you ignore is as important as what you handle
