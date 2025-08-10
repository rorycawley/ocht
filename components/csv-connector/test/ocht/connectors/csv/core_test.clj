(ns ocht.connectors.csv.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ocht.connectors.csv.core :as csv]
            [ocht.connector :as conn]
            [clojure.java.io :as io])
)

(def sample-csv-content "id,name,amount\n1,Alice,100\n2,Bob,200")
(def sample-csv-file "/tmp/test-data.csv")

(defn create-test-csv-file []
  (spit sample-csv-file sample-csv-content))

(defn cleanup-test-file []
  (when (.exists (io/file sample-csv-file))
    (.delete (io/file sample-csv-file))))

(deftest csv-pull-test
  (testing "reads CSV with headers"
    (create-test-csv-file)
    (try
      (let [connector (csv/->CsvConnector)
            config {:file sample-csv-file :headers? true}
            result (conn/pull connector config {})]
        (is (= 2 (count result)))
        (is (= "Alice" (:name (first result))))
        (is (= "100" (:amount (first result)))))
      (finally
        (cleanup-test-file))))

  (testing "reads CSV without headers"
    (create-test-csv-file)
    (try
      (let [connector (csv/->CsvConnector)
            config {:file sample-csv-file :headers? false}
            result (conn/pull connector config {})]
        (is (= 3 (count result)))  ; includes header row
        (is (= "id" (:col0 (first result)))))
      (finally
        (cleanup-test-file)))))

(deftest csv-validate-test
  (testing "validates file exists"
    (let [connector (csv/->CsvConnector)]
      (testing "missing file config"
        (let [result (conn/validate connector {})]
          (is (not (:valid? result)))
          (is (= 1 (count (:errors result))))))
      
      (testing "file not found"
        (let [result (conn/validate connector {:file "/nonexistent/file.csv"})]
          (is (not (:valid? result)))
          (is (some #(= :file-not-found (:error %)) (:errors result)))))
      
      (testing "valid file"
        (create-test-csv-file)
        (try
          (let [result (conn/validate connector {:file sample-csv-file})]
            (is (:valid? result))
            (is (empty? (:errors result))))
          (finally
            (cleanup-test-file)))))))