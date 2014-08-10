(ns charsheets.core-test
  (:require [clojure.test :refer :all]
            [charsheets.core :refer :all :as charsheets]))

(deftest get-map-with-values-in-limit-test
  (testing "FIXME, I fail."
    (is (= 1 (count (keys (charsheets/get-map-with-values-in-limit {:k1 4 :k2 5})))))))
