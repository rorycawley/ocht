(ns ocht.test-fixtures.interface
  "Shared test fixtures and utilities for Ocht components.
  
  This component provides common test data, mocks, and utilities
  following POLYLITH.md testing patterns. It centralizes test
  fixtures to ensure consistency across component tests.
  
  Key capabilities:
  - Sample pipeline definitions for testing
  - Mock connector implementations
  - Test data generators
  - Common test utilities and helpers
  - Temporary file management for tests
  
  Example usage:
    (with-temp-csv-file sample-csv-data
      (fn [file-path]
        ;; Test code using temporary CSV file
        ))"
  (:require [ocht.test-fixtures.core]))

(defn sample-pipeline
  "Generate sample pipeline for testing.
  
  Args:
    variant - Optional variant keyword (:simple, :complex, :invalid)
    
  Returns:
    Pipeline definition map
    
  Example:
    (sample-pipeline :simple)
    => {:id \"test-pipeline\" :pull {...} :push {...}}"
  ([] (ocht.test-fixtures.core/sample-pipeline :simple))
  ([variant] (ocht.test-fixtures.core/sample-pipeline variant)))

(defn sample-csv-data
  "Generate sample CSV data for testing.
  
  Returns:
    Vector of maps representing CSV rows"
  []
  (ocht.test-fixtures.core/sample-csv-data))

(defn with-temp-csv-file
  "Execute function with temporary CSV file.
  
  Args:
    data - Vector of maps to write to CSV
    f - Function that receives file path
    
  Returns:
    Result of calling f with temporary file path"
  [data f]
  (ocht.test-fixtures.core/with-temp-csv-file data f))

(defn mock-connector
  "Create a mock connector for testing.
  
  Args:
    type - Connector type keyword (:pull-only, :push-only, :both)
    behavior - Behavior map defining responses
    
  Returns:
    Mock connector instance"
  [type behavior]
  (ocht.test-fixtures.core/mock-connector type behavior))

(defn with-test-system
  "Execute tests with mock system components.
  
  Args:
    config - Test system configuration
    f - Function to execute with test system
    
  Returns:
    Result of calling f"
  [config f]
  (ocht.test-fixtures.core/with-test-system config f))