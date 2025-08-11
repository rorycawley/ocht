(ns ocht.config.core
  "Core configuration management implementation."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defonce ^:private config-atom (atom {}))

(def ^:private default-config
  {:ocht.connector.csv {:default-delimiter ","
                        :max-file-size-mb 100
                        :header? true}
   :ocht.connector.console {:default-format :table
                            :max-display-rows 1000}
   :ocht.executor {:timeout-ms 30000
                   :max-concurrent-pipelines 10}
   :ocht.pipeline.model {:max-transform-steps 50}
   :ocht.pipeline.transform {:max-data-size 1000000}})

(defn- load-config-file
  "Load configuration from EDN file."
  [file-path]
  (when (.exists (io/file file-path))
    (with-open [r (io/reader file-path)]
      (edn/read (java.io.PushbackReader. r)))))

(defn- merge-env-vars
  "Merge environment variables into config."
  [config]
  (let [env (System/getenv)]
    (reduce-kv
     (fn [cfg k v]
       (cond
         (.startsWith k "OCHT_")
         (let [config-key (->> (subs k 5)
                               (.toLowerCase)
                               (.replace "_" ".")
                               keyword)]
           (assoc cfg config-key v))
         :else cfg))
     config
     env)))

(defn load-config! 
  "Load configuration from multiple sources."
  [config-files]
  (let [base-config default-config
        file-configs (map load-config-file config-files)
        merged-config (apply merge base-config file-configs)
        final-config (merge-env-vars merged-config)]
    (reset! config-atom final-config)
    final-config))

(defn get-config
  "Get configuration value by key path."
  ([key-path] (get-config key-path nil))
  ([key-path default]
   (get-in @config-atom key-path default)))

(defn get-env
  "Get environment variable with coercion."
  ([env-var] (get-env env-var nil nil))
  ([env-var default] (get-env env-var default nil))
  ([env-var default coerce]
   (let [value (System/getenv env-var)]
     (cond
       (nil? value) default
       (nil? coerce) value
       (= coerce :int) (Integer/parseInt value)
       (= coerce :bool) (Boolean/parseBoolean value)
       (fn? coerce) (coerce value)
       :else value))))

(defn validate-config!
  "Validate configuration against schema."
  [schema]
  (doseq [[section-key section-schema] schema]
    (let [section-config (get @config-atom section-key)]
      (when (nil? section-config)
        (throw (ex-info "Missing configuration section" 
                       {:section section-key})))
      (doseq [[config-key expected-type] section-schema]
        (let [value (get section-config config-key)]
          (when (nil? value)
            (throw (ex-info "Missing configuration key" 
                           {:section section-key 
                            :key config-key})))
          (when (and (not= expected-type :any)
                     (case expected-type
                       :int (not (integer? value))
                       :str (not (string? value))
                       :bool (not (boolean? value))
                       :keyword (not (keyword? value))
                       false))
            (throw (ex-info "Configuration type mismatch" 
                           {:section section-key 
                            :key config-key 
                            :expected expected-type 
                            :actual (type value)})))))))
  true)