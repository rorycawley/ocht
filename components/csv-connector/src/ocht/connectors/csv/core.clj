(ns ocht.connectors.csv.core
  "Core CSV connector implementation."
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [ocht.connector :as conn]))

(defrecord CsvConnector []
  conn/Connector
  (pull [_ config _]
    (let [{:keys [file headers?]} config
          headers? (if (nil? headers?) true headers?)]
      (with-open [reader (io/reader file)]
        (let [csv-data (doall (csv/read-csv reader))
              [headers & rows] csv-data]
          (if headers?
            (map #(zipmap (map keyword headers) %) rows)
            (map-indexed (fn [_ row] (zipmap (map #(keyword (str "col" %)) (range (count row))) row)) csv-data))))))
  
  (push [_ _ _ _]
    ;; CSV connector is read-only for MVP - no push operation
    (throw (UnsupportedOperationException. "CSV connector only supports pull operations")))
  
  (validate [_ config]
    (let [{:keys [file]} config
          errors (cond-> []
                   (not file)
                   (conj {:error :missing-file :message "CSV connector requires :file in config"})
                   
                   (and file (not (.exists (io/file file))))
                   (conj {:error :file-not-found :message (str "File not found: " file)}))]
      {:valid? (empty? errors)
       :errors errors})))