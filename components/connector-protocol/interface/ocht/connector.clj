(ns ocht.connector
  "Shared connector protocol and utilities for data pipeline connectors.
  
  Defines the standard interface that all connectors must implement
  to participate in the Pull → Transform → Push pipeline pattern."
  (:require [ocht.connector.protocol]))

(def Connector
  "The connector protocol that all data source/sink connectors must implement."
  ocht.connector.protocol/Connector)

(defn connector?
  "Test whether an object implements the Connector protocol."
  [x]
  (ocht.connector.protocol/connector? x))

(defn validate-connector-result
  "Validate the structure of a connector operation result."
  [result operation]
  (ocht.connector.protocol/validate-connector-result result operation))

;; Protocol method delegates for convenience
(defn pull
  "Pull data from connector source."
  [connector config options]
  (ocht.connector.protocol/pull connector config options))

(defn push
  "Push data to connector destination."
  [connector config data options]
  (ocht.connector.protocol/push connector config data options))

(defn validate
  "Validate connector configuration."
  [connector config]
  (ocht.connector.protocol/validate connector config))