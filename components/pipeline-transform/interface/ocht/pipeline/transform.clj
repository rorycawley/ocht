(ns ocht.pipeline.transform
  "Public interface for pipeline transformations.
  
  Provides pure transformation functions that can be composed in pipelines."
  (:require [ocht.pipeline.transform.core]))

(defn apply-transforms
  "Apply a sequence of transform steps to data.
  
  Args:
    data - Input data sequence
    transform-steps - Vector of transform step maps
    
  Returns:
    Transformed data sequence"
  [data transform-steps]
  (ocht.pipeline.transform.core/apply-transforms data transform-steps))

(defn create-transducer
  "Create a transducer from transform steps.
  
  Args:
    transform-steps - Vector of transform step maps
    
  Returns:
    Transducer function"
  [transform-steps]
  (ocht.pipeline.transform.core/create-transducer transform-steps))