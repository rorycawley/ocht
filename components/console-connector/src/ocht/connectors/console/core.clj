(ns ocht.connectors.console.core
  "Core console connector implementation."
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]))

(defprotocol Connector
  "Basic connector protocol for Pull → Transform → Push pattern."
  (pull [this config options] "Pull data from source")
  (push [this config data options] "Push data to destination")
  (validate [this config] "Validate configuration"))

(defrecord ConsoleConnector []
  Connector
  (pull [_ config options]
    ;; Console connector doesn't support pull operations
    (throw (UnsupportedOperationException. "Console connector only supports push operations")))
  
  (push [_ config data options]
    (let [{:keys [format pretty?]} config
          format (or format :edn)
          pretty? (if (nil? pretty?) true pretty?)]
      (case format
        :edn (if pretty?
               (pp/pprint data)
               (prn data))
        :json (throw (UnsupportedOperationException. "JSON format not yet supported"))
        :table (when (seq data)
                 (let [sample (first data)]
                   (when (map? sample)
                     ;; Print header
                     (println (str/join "\t" (map name (keys sample))))
                     ;; Print rows
                     (doseq [row data]
                       (println (str/join "\t" (map #(str (get row %)) (keys sample))))))))
        ;; Default case
        (if pretty?
          (pp/pprint data)
          (prn data)))))
  
  (validate [_ config]
    (let [{:keys [format]} config
          errors (cond-> []
                   (and format (not (#{:edn :json :table} format)))
                   (conj {:error :invalid-format 
                          :message (str "Unsupported format: " format ". Supported: :edn, :json, :table")}))]
      {:valid? (empty? errors)
       :errors errors})))