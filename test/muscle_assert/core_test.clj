(ns muscle-assert.core-test
  (:require [clojure.test :refer :all]
            [muscle-assert.core :as sut]))

(deftest activate-test
  (is (nil? (sut/activate!))))
