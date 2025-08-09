# pipeline-transform

Pure transformation functions for data pipeline processing.

## Purpose

Provides composable, pure transformation functions that can be combined using transducers for efficient data processing.

## Public API

```clojure
(require '[ocht.pipeline.transform :as transform])

;; Apply transforms to data
(transform/apply-transforms data transform-steps)

;; Create reusable transducer
(transform/create-transducer transform-steps)
```

## Available Transforms

- `:filter` - Filter items with predicate function
- `:map` - Transform items with mapping function  
- `:take` - Take first n items
- `:group-by` - Group items by key function

## Transform Step Format

```clojure
{:fn :filter :args {:predicate #(> (:amount %) 100)}}
{:fn :map :args {:f #(assoc % :category "high-value")}}
{:fn :take :args {:n 10}}
{:fn :group-by :args {:key-fn :type}}
```

## Example

```clojure
(let [data [{:amount 150 :type "sale"}
            {:amount 50 :type "refund"}
            {:amount 200 :type "sale"}]
      steps [{:fn :filter :args {:predicate #(= "sale" (:type %))}}
             {:fn :map :args {:f #(assoc % :processed true)}}]
      result (transform/apply-transforms data steps)]
  ;; Returns filtered and mapped sales records
  result)
```