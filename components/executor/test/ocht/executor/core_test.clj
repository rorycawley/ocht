(ns ocht.executor.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ocht.executor.core :as executor]
            [ocht.pipeline.model :as model]
            [clojure.java.io :as io]))

(def test-csv-file "/tmp/executor-test.csv")
(def test-csv-content "id,name,amount\n1,Alice,150\n2,Bob,75\n3,Carol,200")

(defn create-test-csv []
  (spit test-csv-file test-csv-content))

(defn cleanup-test-csv []
  (when (.exists (io/file test-csv-file))
    (.delete (io/file test-csv-file))))

(def sample-pipeline-edn
  {:id "test-pipeline"
   :pull {:connector :csv :config {:file test-csv-file}}
   :transform [{:fn :filter :args {:predicate #(> (Integer/parseInt (:amount %)) 100)}}
               {:fn :map :args {:f #(assoc % :category "high-value")}}]
   :push {:connector :console :config {:format :table}}})

(deftest execute-pipeline-test
  (testing "executes complete pipeline successfully"
    (create-test-csv)
    (try
      (let [pipeline (model/parse-pipeline sample-pipeline-edn)
            result (executor/execute-pipeline pipeline {})]
        (is (:success result))
        (is (= "test-pipeline" (:pipeline-id result)))
        (is (sequential? (:result result)))
        ;; Should have filtered to 2 records (Alice=150, Carol=200)
        (is (= 2 (count (:result result))))
        ;; Should have added :category "high-value"
        (is (every? #(= "high-value" (:category %)) (:result result))))
      (finally
        (cleanup-test-csv))))

  (testing "handles pipeline execution errors"
    (let [bad-pipeline {:id "bad-pipeline"
                        :pull {:connector :csv :config {:file "/nonexistent.csv"}}
                        :transform []
                        :push {:connector :console :config {}}}
          result (executor/execute-pipeline bad-pipeline {})]
      (is (not (:success result)))
      (is (:error result))
      (is (= "bad-pipeline" (get-in result [:error :pipeline-id]))))))

(deftest connector-registry-test
  (testing "gets known connectors"
    (is (executor/get-connector :csv))
    (is (executor/get-connector :console)))

  (testing "throws on unknown connector"
    (is (thrown-with-msg? Exception #"Unknown connector type"
                          (executor/get-connector :unknown)))))