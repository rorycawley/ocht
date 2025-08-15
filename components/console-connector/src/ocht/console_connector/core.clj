(ns ocht.console-connector.core
  "Core console connector implementation."
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [ocht.connector :as conn]))

(defrecord ConsoleConnector []
  conn/Connector
  (pull [_ _ _]
    ;; Console connector doesn't support pull operations
    (throw (UnsupportedOperationException. "Console connector only supports push operations")))
  
  (push [_ config data _]
    (let [{:keys [format pretty?]} config
          format (or format :edn)
          pretty? (if (nil? pretty?) true pretty?)
          ;; Realize data once to avoid lazy sequence issues
          realized-data (vec data)]
      (case format
        :edn (if pretty?
               (pp/pprint realized-data)
               (prn realized-data))
        :json (throw (UnsupportedOperationException. "JSON format not yet supported"))
        :table (when (seq realized-data)
                 (let [sample (first realized-data)]
                   (when (map? sample)
                     (let [columns (keys sample)
                           {:keys [max-rows]} config
                           max-rows (or max-rows 1000)
                           limited-data (take max-rows realized-data)
                           actual-count (count limited-data)]
                       ;; Print header
                       (println (str/join "\t" (map name columns)))
                       ;; Print rows
                       (doseq [row limited-data]
                         (println (str/join "\t" (map #(str (get row %)) columns))))
                       ;; Show truncation warning if we hit the limit
                       (when (= actual-count max-rows)
                         (println (str "... showing first " max-rows " rows (may be more)")))))))
        ;; Default case
        (if pretty?
          (pp/pprint realized-data)
          (prn realized-data)))))
  
  (validate [_ config]
    (let [{:keys [format]} config
          errors (cond-> []
                   (and format (not (#{:edn :json :table} format)))
                   (conj {:error :invalid-format 
                          :message (str "Unsupported format: " format ". Supported: :edn, :json, :table")}))]
      {:valid? (empty? errors)
       :errors errors})))