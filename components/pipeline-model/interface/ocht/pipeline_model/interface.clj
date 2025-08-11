(ns ocht.pipeline-model.interface
  "Pipeline model parsing and validation for EDN pipeline definitions.
  
  This component provides the core data model for Ocht pipelines,
  converting EDN pipeline definitions into validated internal models
  that can be executed by the executor component.
  
  Pipeline Structure:
    {:id \"unique-pipeline-id\"
     :pull {:connector :csv
            :config {:file \"input.csv\"}}
     :transform [{:transform-fn :filter
                  :args {:predicate #(> (:amount %) 100)}}
                 {:transform-fn :map
                  :args {:f #(assoc % :processed-at (java.time.Instant/now))}}]
     :push {:connector :console
            :config {:format :table}}}
  
  Key capabilities:
  - Parses EDN pipeline definitions into structured models
  - Validates pipeline syntax and semantics
  - Ensures required fields are present
  - Validates connector and transform configurations
  - Provides detailed error reporting for invalid pipelines
  
  Example usage:
    (def pipeline-edn (slurp \"pipeline.edn\"))
    (def pipeline (parse-pipeline pipeline-edn))
    (def validation (validate-pipeline pipeline))
    (when (:valid? validation)
      (executor/execute-pipeline pipeline {}))"
  (:require [ocht.pipeline-model.core]))

(defn parse-pipeline
  "Parse an EDN pipeline definition into internal model.
  
  Takes a raw EDN pipeline definition (typically loaded from a .edn file)
  and converts it into Ocht's internal pipeline model with normalized
  structure and default values applied.
  
  Args:
    pipeline-edn - EDN map containing pipeline definition with required keys:
                   :id, :pull, :push and optional :transform
                   
  Returns:
    Internal pipeline model map with normalized structure
    
  Throws:
    ExceptionInfo if pipeline-edn is malformed or missing required fields
    
  Example:
    (parse-pipeline {:id \"test\"
                     :pull {:connector :csv :config {:file \"data.csv\"}}
                     :transform []
                     :push {:connector :console :config {:format :table}}})
    => {:id \"test\" :pull {...} :transform [] :push {...}}"
  [pipeline-edn]
  (ocht.pipeline-model.core/parse-pipeline pipeline-edn))

(defn validate-pipeline
  "Validate a parsed pipeline model.
  
  Performs comprehensive validation of a pipeline model to ensure
  it can be safely executed. Checks connector configurations,
  transform specifications, and overall pipeline structure.
  
  Args:
    pipeline - Internal pipeline model (from parse-pipeline)
               
  Returns:
    Validation result map:
    {:valid? boolean
     :errors [error-maps]}
     
  Where error-maps contain:
    {:type :error-type
     :message \"Human readable error\"
     :context {...}}
     
  Example:
    (validate-pipeline parsed-pipeline)
    => {:valid? true :errors []}
    
    ; Invalid pipeline:
    => {:valid? false 
        :errors [{:type :missing-file
                  :message \"CSV file does not exist: missing.csv\"
                  :context {:file \"missing.csv\"}}]}"
  [pipeline]
  (ocht.pipeline-model.core/validate-pipeline pipeline))