(ns ocht.validation.interface
  "Reusable validation functions for Ocht components.
  
  This component provides common validation logic that can be shared
  across components following POLYLITH.md principles. It implements
  validation at component boundaries as recommended.
  
  Key capabilities:
  - Schema-based validation for data structures
  - File and path validation
  - Network and URL validation
  - Pipeline configuration validation
  - Connector configuration validation
  
  Validation result format:
    {:valid? boolean
     :errors [{:type :error-type
               :message \"Human readable message\"
               :path [:field :path]
               :value invalid-value}]}
  
  Example usage:
    (validate-pipeline-config pipeline-data)
    => {:valid? false 
        :errors [{:type :missing-field
                  :message \"Required field :id is missing\"
                  :path [:id]}]}"
  (:require [ocht.validation.core]))

(defn validate-required-fields
  "Validate that required fields are present in data.
  
  Args:
    data - Map to validate
    required-fields - Vector of required field keywords
    
  Returns:
    Validation result map
    
  Example:
    (validate-required-fields {:name \"test\"} [:name :id])
    => {:valid? false :errors [{:type :missing-field :path [:id]}]}"
  [data required-fields]
  (ocht.validation.core/validate-required-fields data required-fields))

(defn validate-file-path
  "Validate file path exists and is accessible.
  
  Args:
    file-path - String path to validate
    
  Returns:
    Validation result map"
  [file-path]
  (ocht.validation.core/validate-file-path file-path))

(defn validate-connector-config
  "Validate connector configuration.
  
  Args:
    connector-type - Keyword connector type (:csv, :console, etc.)
    config - Configuration map
    
  Returns:
    Validation result map"
  [connector-type config]
  (ocht.validation.core/validate-connector-config connector-type config))

(defn validate-transform-step
  "Validate transform step specification.
  
  Args:
    transform-step - Transform step map
    
  Returns:
    Validation result map"
  [transform-step]
  (ocht.validation.core/validate-transform-step transform-step))

(defn validate-pipeline-structure
  "Validate complete pipeline structure.
  
  Args:
    pipeline - Pipeline data map
    
  Returns:
    Validation result map"
  [pipeline]
  (ocht.validation.core/validate-pipeline-structure pipeline))

(defn validation-result
  "Create validation result from errors collection.
  
  Standardized way to create validation results across components.
  
  Args:
    errors - Collection of error maps
    
  Returns:
    {:valid? boolean :errors [error-maps]}"
  [errors]
  (ocht.validation.core/validation-result errors))