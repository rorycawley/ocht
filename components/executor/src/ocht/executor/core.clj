(ns ocht.executor.core
  "Core pipeline executor implementation."
  (:require [ocht.connectors.csv :as csv]
            [ocht.connectors.console :as console]
            [ocht.pipeline.transform :as transform]))

(def connector-registry
  "Registry of available connectors."
  {:csv (csv/create-connector)
   :console (console/create-connector)})

(defn get-connector
  "Get connector instance by type."
  [connector-type]
  (if-let [connector (get connector-registry connector-type)]
    connector
    (throw (ex-info "Unknown connector type" 
                   {:connector-type connector-type 
                    :available (keys connector-registry)}))))

(defn execute-pull
  "Execute the pull phase of pipeline."
  [{:keys [connector config]} options]
  (let [conn (get-connector connector)]
    ;; Validate connector config
    (let [validation (.validate conn config)]
      (when-not (:valid? validation)
        (throw (ex-info "Pull connector validation failed" 
                       {:validation validation :config config}))))
    ;; Pull data
    (.pull conn config options)))

(defn execute-transform
  "Execute the transform phase of pipeline."
  [transform-steps data options]
  (if (empty? transform-steps)
    data
    (transform/apply-transforms data transform-steps)))

(defn execute-push
  "Execute the push phase of pipeline."
  [{:keys [connector config]} data options]
  (let [conn (get-connector connector)]
    ;; Validate connector config
    (let [validation (.validate conn config)]
      (when-not (:valid? validation)
        (throw (ex-info "Push connector validation failed" 
                       {:validation validation :config config}))))
    ;; Push data
    (.push conn config data options)
    data))

(defn execute-pipeline
  "Execute a complete pipeline: Pull → Transform → Push."
  [{:keys [id pull transform push]} options]
  (try
    ;; Execute Pull phase
    (let [raw-data (execute-pull pull options)
          ;; Execute Transform phase
          transformed-data (execute-transform transform raw-data options)
          ;; Execute Push phase  
          final-data (execute-push push transformed-data options)]
      {:success true 
       :result final-data
       :pipeline-id id})
    (catch Exception e
      {:success false
       :error {:message (.getMessage e)
               :type (class e)
               :pipeline-id id
               :data (ex-data e)}})))