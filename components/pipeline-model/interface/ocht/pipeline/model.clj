(ns ocht.pipeline.model
  "Public interface for pipeline model operations.
  
  Provides functions to parse and validate EDN pipeline definitions."
  (:require [ocht.pipeline.model.core]))

(defn parse-pipeline
  "Parse an EDN pipeline definition into internal model.
  
  Args:
    pipeline-edn - EDN map containing pipeline definition
    
  Returns:
    Internal pipeline model with validated structure"
  [pipeline-edn]
  (ocht.pipeline.model.core/parse-pipeline pipeline-edn))

(defn validate-pipeline
  "Validate a parsed pipeline model.
  
  Args:
    pipeline - Internal pipeline model
    
  Returns:
    {:valid? boolean :errors [error-maps]} validation result"
  [pipeline]
  (ocht.pipeline.model.core/validate-pipeline pipeline))