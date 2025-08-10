(ns ocht.connector.protocol-test
  (:require [clojure.test :refer [deftest is testing]]
            [ocht.connector.protocol :as protocol]))

;; Test connector implementation
(defrecord TestConnector [data]
  protocol/Connector
  (pull [_ _ _]
    (:data data))
  (push [_ _ data _]
    data)
  (validate [_ _]
    {:valid? true :errors []}))

(defrecord InvalidConnector []
  protocol/Connector
  (pull [_ _ _] [])
  (push [_ _ data _] data)
  (validate [_ _]
    ;; Invalid structure - missing :errors
    {:valid? true}))

(deftest connector-protocol-test
  (testing "connector? predicate"
    (let [valid-connector (->TestConnector {:data [1 2 3]})
          invalid-object "not a connector"]
      (is (protocol/connector? valid-connector))
      (is (not (protocol/connector? invalid-object)))))

  (testing "protocol methods work"
    (let [connector (->TestConnector {:data [1 2 3]})]
      (is (= [1 2 3] (protocol/pull connector {} {})))
      (is (= [4 5 6] (protocol/push connector {} [4 5 6] {})))
      (is (= {:valid? true :errors []} (protocol/validate connector {})))))

  (testing "validate-connector-result validates structure"
    (testing "valid result"
      (let [valid-result {:valid? true :errors []}]
        (is (= valid-result 
               (protocol/validate-connector-result valid-result :test)))))
    
    (testing "invalid result throws"
      (let [invalid-result {:valid? true}] ; missing :errors
        (is (thrown-with-msg? Exception #"Invalid connector validation result format"
                              (protocol/validate-connector-result invalid-result :test)))))))