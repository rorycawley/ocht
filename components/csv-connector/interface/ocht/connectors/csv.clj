(ns ocht.connectors.csv
  "CSV connector for reading CSV files.
  
  Implements the Connector protocol for CSV file operations."
  (:require [ocht.connectors.csv.core]))

(defn create-connector
  "Create a CSV connector instance.
  
  Returns:
    Connector that can read CSV files"
  []
  (ocht.connectors.csv.core/->CsvConnector))