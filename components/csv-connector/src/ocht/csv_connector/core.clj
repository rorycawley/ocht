(ns ocht.csv-connector.core
  "Core CSV connector implementation."
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [ocht.connector :as conn]))

(defrecord CsvConnector []
  conn/Connector
  (pull [_ config _]
    (let [{:keys [file headers? delimiter skip-lines]} config
          headers? (if (nil? headers?) true headers?)
          delimiter (or delimiter \,)
          skip-lines (or skip-lines 0)]
      (try
        (with-open [reader (io/reader file)]
          (let [csv-data (csv/read-csv reader :separator delimiter)
                csv-data (drop skip-lines csv-data)
                [headers & rows] csv-data]
            (if headers?
              (when headers
                (->> rows
                     (map #(zipmap (map keyword headers) %))
                     (remove empty?)))
              (->> csv-data
                   (map (fn [row] 
                          (zipmap (map #(keyword (str "col" %)) (range (count row))) row)))
                   (remove empty?)))))
        (catch java.io.FileNotFoundException e
          (throw (ex-info "CSV file not found" 
                         {:file file :error (.getMessage e)})))
        (catch Exception e
          (throw (ex-info "Failed to read CSV file" 
                         {:file file :error (.getMessage e)}))))))
  
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