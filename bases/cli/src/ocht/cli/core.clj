(ns ocht.cli.core
  "Command line interface for Ocht pipeline execution."
  (:require [clojure.tools.cli :as cli]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [ocht.pipeline-model.interface :as model]
            [ocht.executor :as executor])
  (:gen-class))

(def cli-options
  [["-p" "--pipeline PIPELINE" "Pipeline EDN file path"
    :required true]
   ["-v" "--verbose" "Verbose output"]
   ["-h" "--help" "Show help"]])

(defn read-pipeline-file
  "Read and parse pipeline EDN file."
  [file-path]
  {:pre [(string? file-path)]}
  (try
    (with-open [reader (io/reader file-path)
                pushback-reader (java.io.PushbackReader. reader)]
      (edn/read pushback-reader))
    (catch Exception e
      (throw (ex-info "Failed to read pipeline file" 
                     {:file file-path :error (.getMessage e)})))))

(defn execute-pipeline-from-file
  "Execute pipeline from EDN file."
  [file-path options]
  (let [pipeline-edn (read-pipeline-file file-path)
        pipeline (model/parse-pipeline pipeline-edn)
        validation (model/validate-pipeline pipeline)]
    
    (when-not (:valid? validation)
      (throw (ex-info "Pipeline validation failed" 
                     {:validation validation})))
    
    (when (:verbose options)
      (println "Executing pipeline:" (:id pipeline)))
    
    (executor/execute-pipeline pipeline options)))

(defn print-result
  "Print execution result."
  [result options]
  (if (:success result)
    (when (:verbose options)
      (println "✓ Pipeline executed successfully")
      (println "Pipeline ID:" (:pipeline-id result))
      (println "Result count:" (count (:result result))))
    (do
      (println "✗ Pipeline execution failed")
      (println "Error:" (get-in result [:error :message]))
      (when (:verbose options)
        (println "Pipeline ID:" (get-in result [:error :pipeline-id]))
        (when-let [data (get-in result [:error :data])]
          (println "Error data:" data))))))

(defn -main
  "CLI main entry point."
  [& args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)]
    
    (cond
      (:help options)
      (do
        (println "Ocht - Intelligent Data Workflow Platform")
        (println)
        (println "Usage: ocht [options]")
        (println)
        (println summary)
        (System/exit 0))
      
      errors
      (do
        (println "Errors:")
        (doseq [error errors]
          (println " " error))
        (System/exit 1))
      
      :else
      (try
        (let [result (execute-pipeline-from-file (:pipeline options) options)]
          (print-result result options)
          (System/exit (if (:success result) 0 1)))
        (catch Exception e
          (println "Fatal error:" (.getMessage e))
          (when (:verbose options)
            (println "Details:" (ex-data e)))
          (System/exit 1))))))