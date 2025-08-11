(ns ocht.config.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ocht.config.interface :as config]))

(deftest load-config-test
  (testing "loads default configuration"
    (config/load-config!)
    (let [cfg (config/get-config [])]
      (is (map? cfg))
      (is (contains? cfg :ocht.connector.csv))
      (is (contains? cfg :ocht.connector.console)))))

(deftest get-env-test
  (testing "gets environment variable with default"
    (is (string? (config/get-env "HOME" "default-home")))
    (is (= "default-test" (config/get-env "NONEXISTENT_VAR" "default-test")))))

(deftest validate-config-test
  (testing "validates configuration schema"
    (is (config/validate-config! {:test-key :keyword}))
    (is (not (config/validate-config! {:test-key "invalid-type"})))))