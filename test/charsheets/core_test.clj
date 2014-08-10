(ns charsheets.core-test
  (:require [clojure.test :refer :all]
            [charsheets.core :refer :all :as charsheets]))

(deftest get-map-with-one-value-in-limit-test
  (testing "FIXME, I fail."
    (is (= 1 (count (keys (charsheets/get-map-with-values-in-limit {:k1 4 :k2 5})))))))

(deftest get-map-with-both-values-in-limit-test
  (testing "FIXME, I fail."
    (is (= 2 (count (keys (charsheets/get-map-with-values-in-limit {:k1 4 :k2 3})))))))

(deftest get-map-with-no-values-in-limit-test
  (testing "FIXME, I fail."
    (is (= 0 (count (keys (charsheets/get-map-with-values-in-limit {:k1 5 :k2 5})))))))