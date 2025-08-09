# executor

Pipeline executor component that orchestrates the Pull → Transform → Push execution flow.

## Purpose

Coordinates the execution of complete data pipelines by managing connectors, applying transformations, and handling errors in the three-phase pipeline model.

## Public API

```clojure
(require '[ocht.executor :as executor])

;; Execute a parsed pipeline
(executor/execute-pipeline pipeline options)
```

## Execution Flow

1. **Pull Phase**: Validate and execute source connector to retrieve data
2. **Transform Phase**: Apply transformation steps to the data
3. **Push Phase**: Validate and execute destination connector to output data

## Success Response

```clojure
{:success true
 :result transformed-data
 :pipeline-id "my-pipeline"}
```

## Error Response  

```clojure
{:success false
 :error {:message "Error description"
         :type ExceptionClass
         :pipeline-id "my-pipeline"
         :data {:validation {...}}}}
```

## Supported Connectors

- `:csv` - CSV file reader
- `:console` - Console output

## Example

```clojure
(require '[ocht.pipeline.model :as model]
         '[ocht.executor :as executor])

(let [pipeline-edn {:id "demo"
                    :pull {:connector :csv :config {:file "data.csv"}}
                    :transform [{:fn :filter :args {:predicate #(pos? (Integer/parseInt (:amount %)))}}]
                    :push {:connector :console :config {:format :table}}}
      pipeline (model/parse-pipeline pipeline-edn)
      result (executor/execute-pipeline pipeline {})]
  (when (:success result)
    (println "Pipeline executed successfully")))
```

## Error Handling

- Validates connector configurations before execution
- Catches and wraps all exceptions with structured error information
- Preserves pipeline context in error responses