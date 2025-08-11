(ns ocht.console-connector.interface
  "Console connector for outputting data to standard output (stdout).
  
  This component implements the Connector protocol defined in ocht.connector
  to provide console output capabilities for data pipeline destinations.
  
  Key capabilities:
  - Outputs data to stdout in various formats (table, JSON, EDN, raw)
  - Supports pretty-printing and formatting options
  - Handles large datasets with streaming output
  - Provides configurable output formatting
  
  Configuration format:
    {:format :table              ; :table, :json, :edn, or :raw
     :pretty? true               ; optional, pretty-print output
     :columns [:field1 :field2]} ; optional, specific columns to show
  
  Example usage:
    (def connector (create-connector))
    (conn/push connector {:format :table} data {})"
  (:require [ocht.console-connector.core]))

(defn create-connector
  "Create a console connector instance.
  
  Creates a connector that implements the ocht.connector/Connector protocol
  for outputting data to the console (stdout).
  
  The returned connector supports:
  - push: Outputs data to stdout in the specified format
  - validate: Checks configuration validity
  - pull: Not supported (throws UnsupportedOperationException)
  
  Returns:
    A connector instance that can output data to console
  
  Example:
    (def console-conn (create-connector))
    (conn/validate console-conn {:format :table})
    => {:valid? true}
    
    (conn/push console-conn {:format :table} 
               [{:name \"Alice\" :age 25} {:name \"Bob\" :age 30}] {})
    ; Outputs formatted table to stdout"
  []
  (ocht.console-connector.core/->ConsoleConnector))