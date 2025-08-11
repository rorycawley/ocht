(ns ocht.test-fixtures.core
  "Core test fixtures implementation."
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [ocht.connector :as conn]))

(def sample-csv-data
  "Sample CSV data for testing."
  [{:id "1" :name "Alice" :amount "100" :category "A"}
   {:id "2" :name "Bob" :amount "200" :category "B"}
   {:id "3" :name "Charlie" :amount "300" :category "A"}
   {:id "4" :name "Diana" :amount "400" :category "B"}])

(defn sample-pipeline
  "Generate sample pipeline definitions."
  [variant]
  (case variant
    :simple
    {:id "test-simple"
     :pull {:connector :csv :config {:file "test-data.csv"}}
     :transform []
     :push {:connector :console :config {:format :table}}}
    
    :complex
    {:id "test-complex"
     :pull {:connector :csv :config {:file "test-data.csv"}}
     :transform [{:transform-fn :filter
                  :args {:predicate #(> (Integer/parseInt (:amount %)) 150)}}
                 {:transform-fn :map
                  :args {:f #(assoc % :processed-at "2024-01-01")}}
                 {:transform-fn :take
                  :args {:n 2}}]
     :push {:connector :console :config {:format :edn}}}
    
    :invalid
    {:id "test-invalid"
     :pull {:connector :nonexistent :config {}}
     :push {:connector :console :config {:format :invalid}}}
    
    (sample-pipeline :simple)))

(defn with-temp-csv-file
  "Create temporary CSV file and execute function."
  [data f]
  (let [temp-file (java.io.File/createTempFile "test-" ".csv")]
    (try
      (with-open [writer (io/writer temp-file)]
        (csv/write-csv writer 
                      (cons (map name (keys (first data)))
                            (map vals data))))
      (f (.getAbsolutePath temp-file))
      (finally
        (.delete temp-file)))))

(defrecord MockConnector [type behavior]
  conn/Connector
  (validate [_ config]
    (get behavior :validate {:valid? true}))
  
  (pull [_ config options]
    (case type
      :pull-only (get behavior :pull-data sample-csv-data)
      :both (get behavior :pull-data sample-csv-data)
      (throw (UnsupportedOperationException. "Pull not supported"))))
  
  (push [_ config data options]
    (case type
      :push-only (get behavior :push-result data)
      :both (get behavior :push-result data)
      (throw (UnsupportedOperationException. "Push not supported")))))

(defn mock-connector
  "Create mock connector."
  [type behavior]
  (->MockConnector type behavior))

(defn with-test-system
  "Execute with test system setup."
  [config f]
  ;; Future: Set up mock databases, message queues, etc.
  ;; For now, just execute the function
  (f))