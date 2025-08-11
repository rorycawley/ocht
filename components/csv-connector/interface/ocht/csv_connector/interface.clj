(ns ocht.csv-connector.interface
  "CSV connector for reading CSV files from the local filesystem.
  
  This component implements the Connector protocol defined in ocht.connector
  to provide CSV file reading capabilities for data pipeline sources.
  
  Key capabilities:
  - Reads CSV files with customizable delimiters and parsing options
  - Supports header row detection and field name mapping
  - Validates file existence and accessibility
  - Handles common CSV parsing edge cases (quotes, escapes, empty fields)
  
  Configuration format:
    {:file \"path/to/file.csv\"
     :delimiter \",\"           ; optional, defaults to comma
     :header? true             ; optional, defaults to true
     :skip-lines 0}            ; optional, defaults to 0
  
  Example usage:
    (def connector (create-connector))
    (conn/pull connector {:file \"data.csv\"} {})"
  (:require [ocht.csv-connector.core]))

(defn create-connector
  "Create a CSV connector instance.
  
  Creates a connector that implements the ocht.connector/Connector protocol
  for reading CSV files from the local filesystem.
  
  The returned connector supports:
  - pull: Reads CSV data and returns a lazy sequence of maps
  - validate: Checks file existence and configuration validity
  - push: Not supported (throws UnsupportedOperationException)
  
  Returns:
    A connector instance that can read CSV files
  
  Example:
    (def csv-conn (create-connector))
    (conn/validate csv-conn {:file \"data.csv\"})
    => {:valid? true}
    
    (conn/pull csv-conn {:file \"data.csv\"} {})
    => ({:name \"Alice\" :age \"25\"} {:name \"Bob\" :age \"30\"})"
  []
  (ocht.csv-connector.core/->CsvConnector))