# pipeline-model

Parses and validates EDN pipeline definitions into internal model structures.

## Purpose

Converts declarative EDN pipeline definitions into validated internal models that can be executed by the pipeline engine.

## Public API

```clojure
(require '[ocht.pipeline.model :as model])

;; Parse EDN pipeline definition
(model/parse-pipeline pipeline-edn)

;; Validate parsed pipeline
(model/validate-pipeline pipeline)
```

## Pipeline EDN Format

```clojure
{:id "my-pipeline"
 :pull {:connector :csv :config {:file "data.csv"}}
 :transform [{:fn :filter :args {...}}
             {:fn :map :args {...}}]
 :push {:connector :console :config {}}}
```

## Example

```clojure
(let [pipeline-edn {:id "demo"
                    :pull {:connector :csv :config {:file "data.csv"}}
                    :transform [{:fn :filter :args {:predicate #(pos? (:amount %))}}]
                    :push {:connector :console :config {}}}
      parsed (model/parse-pipeline pipeline-edn)
      validation (model/validate-pipeline parsed)]
  (when (:valid? validation)
    ;; Pipeline is ready for execution
    parsed))
```