(ns ocht.validation.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ocht.validation.interface :as validation]))

(deftest validation-result-test
  (testing "creates valid result from empty errors"
    (let [result (validation/validation-result [])]
      (is (:valid? result))
      (is (empty? (:errors result)))))
  
  (testing "creates invalid result from errors"
    (let [errors [{:type :test-error :message "Test error"}]
          result (validation/validation-result errors)]
      (is (not (:valid? result)))
      (is (= errors (:errors result))))))

(deftest validate-required-fields-test
  (testing "validates all required fields present"
    (let [data {:name "test" :id 123}
          result (validation/validate-required-fields data [:name :id])]
      (is (:valid? result))
      (is (empty? (:errors result)))))
  
  (testing "identifies missing required fields"
    (let [data {:name "test"}
          result (validation/validate-required-fields data [:name :id])]
      (is (not (:valid? result)))
      (is (= 1 (count (:errors result))))
      (is (= :missing-field (-> result :errors first :type))))))

(deftest validate-file-path-test
  (testing "validates existing readable file"
    ;; Create a temporary test file
    (let [temp-file (java.io.File/createTempFile "validation-test" ".txt")]
      (try
        (spit (.getAbsolutePath temp-file) "test content")
        (let [result (validation/validate-file-path (.getAbsolutePath temp-file))]
          (is (:valid? result)))
        (finally
          (.delete temp-file)))))
  
  (testing "rejects non-existent file"
    (let [result (validation/validate-file-path "/non/existent/file.txt")]
      (is (not (:valid? result)))
      (is (some #(= :file-not-found (:type %)) (:errors result))))))