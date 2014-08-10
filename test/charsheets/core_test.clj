(ns charsheets.core-test
  (:require [clojure.test :refer :all]
            [charsheets.core :refer :all :as charsheets]))

(deftest get-map-with-one-value-in-limit-test
  (testing "One value in the map is filtered out."
    (is (= 1 (count (keys (charsheets/get-map-with-values-in-limit {:k1 4 :k2 5})))))))

(deftest get-map-with-both-values-in-limit-test
  (testing "Both values in the map are not filtered out."
    (is (= 2 (count (keys (charsheets/get-map-with-values-in-limit {:k1 4 :k2 3})))))))

(deftest get-map-with-no-values-in-limit-test
  (testing "Both values in the map are filtered out."
    (is (= 0 (count (keys (charsheets/get-map-with-values-in-limit {:k1 5 :k2 5})))))))

(deftest get-points-accumulation-probability-gt-value-provider-test
  (testing "Accumulation probability is > what is produced by the value provider."
    (is (= 2 (charsheets/get-points 3 2 0 #(inc 0))))))

(deftest get-points-accumulation-probability-lt-value-provider-test
  (testing "Accumulation probability is < what is produced by the value provider."
    (is (= 1 (charsheets/get-points 1 2 0 #(inc 0))))))

(deftest get-points-accumulation-probability-eq-value-provider-test
  (testing "Accumulation probability is = what is produced by the value provider."
    (is (= 1 (charsheets/get-points 2 2 0 #(inc 0))))))

(deftest get-points-not-enough-remaining-points
  (testing "Not enough remaining points."
    (is (= 2 (charsheets/get-points 5 2 0 #(inc 0))))))

(deftest get-points-enough-remaining-points
  (testing "Enough remaining points."
    (is (= 5 (charsheets/get-points 5 5 0 #(inc -1))))))

(deftest get-points-current-points-plus-new-points-over-the-limit
  (testing "Current points plus new points is over the limit."
    (is (= 2 (charsheets/get-points 5 5 3 #(inc -1))))))

(deftest get-points-new-val-lt-1
  (testing "New value is less than 1."
    (is (= 1 (charsheets/get-points 1 5 3 #(inc 4))))))