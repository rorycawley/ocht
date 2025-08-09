(ns ocht.pipeline.model.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ocht.pipeline.model.core :as model]))

(def sample-pipeline-edn
  {:id "demo-pipeline"
   :pull {:connector :csv :config {:file "data.csv"}}
   :transform [{:fn :filter :args {:predicate #(> (:amount %) 100)}}
               {:fn :map :args {:f #(assoc % :category "high-value")}}]
   :push {:connector :console :config {}}})

(deftest parse-pipeline-test
  (testing "parses valid pipeline EDN"
    (let [result (model/parse-pipeline sample-pipeline-edn)]
      (is (= "demo-pipeline" (:id result)))
      (is (= :csv (get-in result [:pull :connector])))
      (is (= :console (get-in result [:push :connector])))
      (is (= 2 (count (:transform result)))))))

(deftest validate-pipeline-test
  (testing "validates complete pipeline"
    (let [pipeline (model/parse-pipeline sample-pipeline-edn)
          result (model/validate-pipeline pipeline)]
      (is (:valid? result))
      (is (empty? (:errors result)))))
  
  (testing "catches missing required fields"
    (let [invalid-pipeline {}
          result (model/validate-pipeline invalid-pipeline)]
      (is (not (:valid? result)))
      (is (= 4 (count (:errors result)))))))