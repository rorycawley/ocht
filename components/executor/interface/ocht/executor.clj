(ns ocht.executor
  "Public interface for pipeline execution.
  
  Orchestrates the Pull → Transform → Push pipeline execution flow."
  (:require [ocht.executor.core]))

(defn execute-pipeline
  "Execute a parsed pipeline with the given options.
  
  Args:
    pipeline - Parsed pipeline model from ocht.pipeline.model
    options - Execution options map
    
  Returns:
    {:success true :result data} or {:success false :error error-map}"
  [pipeline options]
  (ocht.executor.core/execute-pipeline pipeline options))