(ns ocht.executor.core
  "Core pipeline executor implementation."
  (:require [ocht.csv-connector.interface :as csv]
            [ocht.console-connector.interface :as console]
            [ocht.pipeline-transform.interface :as transform]
            [ocht.connector :as conn]))

(def ^:private connector-registry
  "Registry of available connectors. Connectors are created lazily."
  (delay {:csv (csv/create-connector)
          :console (console/create-connector)}))

(defn get-connector
  "Get connector instance by type."
  [connector-type]
  {:pre [(keyword? connector-type)]}
  (let [registry @connector-registry]
    (if-let [connector (get registry connector-type)]
      connector
      (throw (ex-info "Unknown connector type" 
                     {:connector-type connector-type 
                      :available (keys registry)})))))

(defn execute-pull
  "Execute the pull phase of pipeline."
  [{:keys [connector config]} options]
  (let [conn (get-connector connector)]
    ;; Validate connector config
    (let [validation (conn/validate conn config)]
      (when-not (:valid? validation)
        (throw (ex-info "Pull connector validation failed" 
                       {:validation validation :config config}))))
    ;; Pull data with resource management
    (try
      (conn/pull conn config options)
      (catch Exception e
        (throw (ex-info "Pull operation failed" 
                       {:connector connector :config config 
                        :error (.getMessage e)} e))))))

(defn execute-transform
  "Execute the transform phase of pipeline."
  [transform-steps data _]
  {:pre [(sequential? transform-steps)]}
  (if (empty? transform-steps)
    data
    (transform/apply-transforms data transform-steps)))

(defn execute-push
  "Execute the push phase of pipeline."
  [{:keys [connector config]} data options]
  (let [conn (get-connector connector)]
    ;; Validate connector config
    (let [validation (conn/validate conn config)]
      (when-not (:valid? validation)
        (throw (ex-info "Push connector validation failed" 
                       {:validation validation :config config}))))
    ;; Push data with resource management  
    (try
      (conn/push conn config data options)
      (catch Exception e
        (throw (ex-info "Push operation failed"
                       {:connector connector :config config
                        :error (.getMessage e)} e))))
    data))

(defn execute-pipeline
  "Execute a complete pipeline: Pull → Transform → Push."
  [{:keys [id pull transform push]} options]
  {:pre [(map? pull) (sequential? transform) (map? push) (string? id)]}
  (try
    (let [start-time (System/nanoTime)
          raw-data (execute-pull pull options)
          transformed-data (execute-transform transform raw-data options)
          final-data (execute-push push transformed-data options)
          elapsed-ms (/ (- (System/nanoTime) start-time) 1000000.0)]
      {:success true
       :result final-data
       :pipeline-id id
       :execution-time-ms elapsed-ms})
    (catch Exception e
      {:success false
       :error {:message (.getMessage e)
               :type (class e)
               :pipeline-id id
               :data (ex-data e)}}))