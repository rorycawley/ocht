(ns ocht.pipeline-transform.interface
  "Pure data transformation functions for pipeline data processing.
  
  This component provides composable, pure transformation functions
  that form the 'Transform' phase of Ocht's Pull → Transform → Push
  pipeline architecture.
  
  Available transforms:
    :filter  - Filter data based on a predicate function
    :map     - Transform each data item with a mapping function  
    :take    - Take only the first N items from the data
    :group-by - Group data by a key function
  
  Transform specification format:
    {:transform-fn :filter
     :args {:predicate #(> (:amount %) 100)}}
     
    {:transform-fn :map
     :args {:f #(update % :name clojure.string/upper-case)}}
     
    {:transform-fn :take
     :args {:n 10}}
     
    {:transform-fn :group-by
     :args {:key-fn :category}}
  
  Key principles:
  - All transforms are pure functions (no side effects)
  - Transforms work with lazy sequences for memory efficiency
  - Transforms compose naturally through function composition
  - Each transform can be used standalone or in pipelines
  
  Example usage:
    (def transforms [{:transform-fn :filter :args {:predicate #(> (:amount %) 50)}}
                     {:transform-fn :map :args {:f #(assoc % :processed true)}}
                     {:transform-fn :take :args {:n 5}}])
    
    (apply-transforms input-data transforms)
    ; Returns transformed lazy sequence"
  (:require [ocht.pipeline-transform.core]))

(defn apply-transforms
  "Apply a sequence of transform steps to data.
  
  Applies transforms in order, with each transform receiving the output
  of the previous transform. Uses function composition for efficiency
  and maintains lazy evaluation throughout the pipeline.
  
  Args:
    data - Input data sequence (can be lazy)
    transform-steps - Vector of transform specification maps, each with:
                     {:transform-fn keyword, :args map}
                     
  Returns:
    Lazy sequence of transformed data
    
  Throws:
    ExceptionInfo if transform-steps contains invalid specifications
    
  Example:
    (apply-transforms [{:id 1 :amount 100} {:id 2 :amount 50}]
                      [{:transform-fn :filter 
                        :args {:predicate #(> (:amount %) 75)}}
                       {:transform-fn :map
                        :args {:f #(assoc % :status :high-value)}}])
    => ({:id 1 :amount 100 :status :high-value})"
  [data transform-steps]
  (ocht.pipeline-transform.core/apply-transforms data transform-steps))

(defn create-transducer
  "Create a transducer from transform steps.
  
  Builds a composable transducer from transform specifications,
  enabling efficient data processing with reduced intermediate
  collection creation.
  
  Args:
    transform-steps - Vector of transform specification maps
                     
  Returns:
    Transducer function that can be used with transduce, into, etc.
    
  Example:
    (def xform (create-transducer [{:transform-fn :filter 
                                   :args {:predicate #(> (:amount %) 50)}}
                                  {:transform-fn :map
                                   :args {:f #(update % :amount * 1.1)}}]))
    
    (into [] xform input-data)
    ; Returns vector of transformed data"
  [transform-steps]
  (ocht.pipeline-transform.core/create-transducer transform-steps))