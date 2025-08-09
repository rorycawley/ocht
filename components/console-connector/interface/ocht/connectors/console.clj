(ns ocht.connectors.console
  "Console connector for outputting data to stdout.
  
  Implements the Connector protocol for console output operations."
  (:require [ocht.connectors.console.core]))

(defn create-connector
  "Create a console connector instance.
  
  Returns:
    Connector that can output data to console"
  []
  (ocht.connectors.console.core/->ConsoleConnector))