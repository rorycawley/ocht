(ns user
  "Development REPL utilities following POLYLITH.md patterns.
  
  This namespace provides tools for efficient REPL-driven development
  in the Ocht Polylith workspace."
  (:require [clojure.tools.namespace.repl :as repl]
            [clojure.test :as test]
            [ocht.pipeline-model.interface :as model]
            [ocht.executor.interface :as executor]
            [ocht.csv-connector.interface :as csv]
            [ocht.console-connector.interface :as console]))

;; Namespace reloading (core POLYLITH.md pattern)
(defn refresh
  "Reload changed namespaces."
  []
  (repl/refresh))

(defn refresh-all
  "Reload all namespaces."
  []
  (repl/refresh-all))

;; Testing utilities
(defn run-tests
  "Run all tests matching the ocht namespace pattern."
  []
  (test/run-all-tests #"ocht\..*-test"))

(defn run-component-tests
  "Run tests for a specific component."
  [component-name]
  (test/run-all-tests (re-pattern (str "ocht\\." component-name "\\..*-test"))))

;; Pipeline utilities for quick experimentation
(defn quick-pipeline
  "Create a simple test pipeline for experimentation."
  [& {:keys [input-file output-format]
      :or {input-file "demo-data.csv"
           output-format :table}}]
  {:id "quick-test"
   :pull {:connector :csv :config {:file input-file}}
   :transform []
   :push {:connector :console :config {:format output-format}}})

(defn run-pipeline
  "Parse and execute a pipeline definition."
  [pipeline-edn & {:keys [options] :or {options {}}}]
  (let [pipeline (model/parse-pipeline pipeline-edn)
        validation (model/validate-pipeline pipeline)]
    (if (:valid? validation)
      (executor/execute-pipeline pipeline options)
      (do (println "Pipeline validation failed:")
          (doseq [error (:errors validation)]
            (println "-" (:message error)))
          validation))))

(defn demo
  "Run a quick demo pipeline."
  []
  (run-pipeline (quick-pipeline)))

(comment
  ;; REPL workflow following POLYLITH.md recommendations
  
  ;; 1. Start development - reload code after changes
  (refresh)
  
  ;; 2. Run tests for specific components (boundary testing)
  (run-component-tests "csv-connector")
  (run-component-tests "pipeline-model")
  
  ;; 3. Run all tests
  (run-tests)
  
  ;; 4. Quick pipeline experimentation
  (demo)
  (run-pipeline (quick-pipeline :output-format :edn))
  
  ;; 5. Component interface exploration
  (def csv-conn (csv/create-connector))
  (def console-conn (console/create-connector))
  
  ;; Test connector functionality directly
  (require '[ocht.connector :as conn])
  (conn/validate csv-conn {:file "demo-data.csv"})
  
  )