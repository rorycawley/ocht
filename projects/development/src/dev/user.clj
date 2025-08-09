(ns user
  "Development REPL utilities for Ocht.
  
  Start here when working on the codebase - this namespace provides
  helpers for interactive development in the REPL."
  (:require [clojure.tools.namespace.repl :as repl]))

(defn refresh
  "Reload modified namespaces."
  []
  (repl/refresh))

(defn refresh-all
  "Reload all namespaces."
  []
  (repl/refresh-all))

(comment
  ;; Start development with:
  ;; clj -A:dev
  
  ;; Then use these helpers:
  (refresh)
  (refresh-all)
  
  ;; Future helpers will be added here for:
  ;; - Loading and testing pipelines
  ;; - Starting/stopping services
  ;; - Database connections
  ;; - Test data setup
  )