(ns example.core-test
  (:require [clojure.test :refer :all]))

(deftest my-failure-test
  (testing "map"
    (is (= {:foo 1} {:foo 2}) "should fail"))
  (testing "vector"
    (is (= [1 2 3] []) "should fail")))
