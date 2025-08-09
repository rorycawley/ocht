(ns ocht.connectors.console.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ocht.connectors.console.core :as console])
  (:import [java.io StringWriter]))

(def sample-data
  [{:id 1 :name "Alice" :amount 100}
   {:id 2 :name "Bob" :amount 200}])

(deftest console-push-test
  (testing "outputs EDN format"
    (let [connector (console/->ConsoleConnector)
          config {:format :edn :pretty? false}
          output (with-out-str
                   (console/push connector config sample-data {}))]
      (is (not (empty? output)))
      (is (re-find #"\{:id" output))))

  (testing "outputs table format"
    (let [connector (console/->ConsoleConnector)
          config {:format :table}
          output (with-out-str
                   (console/push connector config sample-data {}))]
      (is (not (empty? output)))
      (is (re-find #"id\tname\tamount" output))
      (is (re-find #"1\tAlice\t100" output))))

  (testing "outputs pretty EDN by default"
    (let [connector (console/->ConsoleConnector)
          config {}
          output (with-out-str
                   (console/push connector config sample-data {}))]
      (is (not (empty? output)))
      ;; Pretty printed should have newlines
      (is (re-find #"\n" output)))))

(deftest console-validate-test
  (testing "validates format"
    (let [connector (console/->ConsoleConnector)]
      (testing "valid formats"
        (is (:valid? (console/validate connector {:format :edn})))
        (is (:valid? (console/validate connector {:format :table})))
        (is (:valid? (console/validate connector {}))))
      
      (testing "invalid format"
        (let [result (console/validate connector {:format :xml})]
          (is (not (:valid? result)))
          (is (some #(= :invalid-format (:error %)) (:errors result)))))))

  (testing "pull operation not supported"
    (let [connector (console/->ConsoleConnector)]
      (is (thrown? UnsupportedOperationException
                   (console/pull connector {} {}))))))