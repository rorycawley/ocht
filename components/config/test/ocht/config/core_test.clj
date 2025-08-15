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
    ;; Load config first
    (config/load-config!)
    ;; Test validation of existing sections
    (is (config/validate-config! {:ocht.connector.csv {:max-file-size-mb :pos-int}}))
    ;; Test validation fails for missing sections
    (is (thrown-with-msg? Exception #"Missing configuration section"
                          (config/validate-config! {:nonexistent.section {:key :type}})))))