(ns ocht.pipeline-transform.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ocht.pipeline-transform.core :as transform]))

(def sample-data
  [{:id 1 :amount 150 :type "sale"}
   {:id 2 :amount 50 :type "refund"}
   {:id 3 :amount 200 :type "sale"}
   {:id 4 :amount 75 :type "sale"}])

(deftest filter-transform-test
  (testing "filters data based on predicate"
    (let [step {:transform-fn :filter :args {:predicate #(> (:amount %) 100)}}
          xform (transform/create-transform step)
          result (sequence xform sample-data)]
      (is (= 2 (count result)))
      (is (every? #(> (:amount %) 100) result)))))

(deftest map-transform-test
  (testing "maps function over data"
    (let [step {:transform-fn :map :args {:f #(assoc % :category "processed")}}
          xform (transform/create-transform step)
          result (sequence xform (take 1 sample-data))]
      (is (= 1 (count result)))
      (is (= "processed" (:category (first result)))))))

(deftest take-transform-test
  (testing "takes first n items"
    (let [step {:transform-fn :take :args {:n 2}}
          xform (transform/create-transform step)
          result (sequence xform sample-data)]
      (is (= 2 (count result))))))

(deftest compose-transforms-test
  (testing "composes multiple transforms"
    (let [steps [{:transform-fn :filter :args {:predicate #(= "sale" (:type %))}}
                 {:transform-fn :map :args {:f #(assoc % :processed true)}}
                 {:transform-fn :take :args {:n 2}}]
          result (transform/apply-transforms sample-data steps)]
      (is (= 2 (count result)))
      (is (every? #(= "sale" (:type %)) result))
      (is (every? :processed result)))))

(deftest group-by-transform-test
  (testing "groups data by key function"
    (let [step {:transform-fn :group-by :args {:key-fn :type}}
          transform-fn (transform/create-transform step)
          result (transform-fn sample-data)]
      (is (map? result))
      (is (= #{"sale" "refund"} (set (keys result))))
      (is (= 3 (count (get result "sale"))))
      (is (= 1 (count (get result "refund"))))))
  
  (testing "handles empty data"
    (let [step {:transform-fn :group-by :args {:key-fn :type}}
          transform-fn (transform/create-transform step)
          result (transform-fn [])]
      (is (map? result))
      (is (empty? result)))))

(deftest empty-data-test
  (testing "handles empty data gracefully"
    (let [result (transform/apply-transforms [] [{:transform-fn :filter :args {:predicate pos?}}])]
      (is (empty? result))))
  
  (testing "handles nil transforms"
    (let [result (transform/apply-transforms sample-data [])]
      (is (= sample-data result)))))

(deftest unknown-transform-test
  (testing "throws on unknown transform"
    (let [step {:transform-fn :unknown :args {}}]
      (is (thrown-with-msg? Exception #"Unknown transform function"
                            (transform/create-transform step))))))