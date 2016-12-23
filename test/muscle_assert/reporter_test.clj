(ns muscle-assert.reporter-test
  (:require [muscle-assert.reporter :as r]
            [clojure.test :refer :all]
            [muscle-assert.test-helper :refer :all]
            [clojure.string :as str]))

(defn join-with-newline [sv]
  (str/join \newline sv))

(defn remove-first-two-lines [s]
  (->> (str/split-lines s)
       (drop 2)
       join-with-newline))

(defmacro with-fail-msg [[fail-msg cause] & body]
  `(let [~'fail-msg (ignore-report-counter
                     (with-test-out-str
                       ~cause))
         ~'fail-msg (remove-first-two-lines ~'fail-msg)]
     ~@body))

(deftest equals-fail-report-test-for-atom
  (with-fail-msg [fail-msg (is (= 1 2))]
    (is (= fail-msg
           (join-with-newline
            [" left: 1"
             "right: 2"]))))

  (with-fail-msg [fail-msg (is (= 1 nil))]
    (is (= fail-msg
           (join-with-newline
            [" left: 1"
             "right: nil"]))))

  (with-fail-msg [fail-msg (let [x 10 y 20]
                             (is (= x y)))]
    (is (= fail-msg
           (join-with-newline
            [" left: 10"
             "right: 20"]))))

  (with-fail-msg [fail-msg (is (= 1 #{1}))]
    (is (= fail-msg
           (join-with-newline
            [" left: 1"
             "right: #{1}"])))))

(deftest equals-fail-report-test-for-map
  (with-fail-msg [fail-msg (is (= {:foo 1} {}))]
    (is (= fail-msg
           (join-with-newline
            [" left: {:foo 1}"
             "right: {}"
             "   in [:foo]"
             "      left 1"
             "     right "]))))

  (with-fail-msg [fail-msg (is (= {:foo 1} {:foo nil}))]
    (is (= fail-msg
           (join-with-newline
            [" left: {:foo 1}"
             "right: {:foo nil}"
             "   in [:foo]"
             "      left 1"
             "     right nil"]))))

  (with-fail-msg [fail-msg (is (= {:foo 1} {:foo 2}))]
    (is (= fail-msg
           (join-with-newline
            [" left: {:foo 1}"
             "right: {:foo 2}"
             "   in [:foo]"
             "      left 1"
             "     right 2"]))))

  (with-fail-msg [fail-msg (is (= {:foo {:bar 1}} {:foo {:bar 2}}))]
    (is (= fail-msg
           (join-with-newline
            [" left: {:foo {:bar 1}}"
             "right: {:foo {:bar 2}}"
             "   in [:foo :bar]"
             "      left 1"
             "     right 2"])))))


(deftest equals-fail-report-test-for-vec
  (with-fail-msg [fail-msg (is (= [1] []))]
    (is (= fail-msg
           (join-with-newline
            [" left: [1]"
             "right: []"
             "   in [0]"
             "      left 1"
             "     right "]))))

  (with-fail-msg [fail-msg (is (= [1] [nil]))]
    (is (= fail-msg
           (join-with-newline
            [" left: [1]"
             "right: [nil]"
             "   in [0]"
             "      left 1"
             "     right nil"]))))

  (with-fail-msg [fail-msg (is (= [1] [2]))]
    (is (= fail-msg
           (join-with-newline
            [" left: [1]"
             "right: [2]"
             "   in [0]"
             "      left 1"
             "     right 2"]))))

  (with-fail-msg [fail-msg (is (= [[1]] [[2]]))]
    (is (= fail-msg
           (join-with-newline
            [" left: [[1]]"
             "right: [[2]]"
             "   in [0 0]"
             "      left 1"
             "     right 2"])))))

(deftest equals-fail-report-test-for-set
  (with-fail-msg [fail-msg (is (= #{1} #{}))]
    (is (= fail-msg
           (join-with-newline
            [" left: #{1}"
             "right: #{}"
             "   ----- diff details -----"
             "   only in left  #{1}"
             "   only in right #{}"
             "   in both       #{}"]))))

  (with-fail-msg [fail-msg (is (= #{1} #{nil}))]
    (is (= fail-msg
           (join-with-newline
            [" left: #{1}"
             "right: #{nil}"
             "   ----- diff details -----"
             "   only in left  #{1}"
             "   only in right #{nil}"
             "   in both       #{}"]))))

  (with-fail-msg [fail-msg (is (= #{1} #{2}))]
    (is (= fail-msg
           (join-with-newline
            [" left: #{1}"
             "right: #{2}"
             "   ----- diff details -----"
             "   only in left  #{1}"
             "   only in right #{2}"
             "   in both       #{}"]))))

  (with-fail-msg [fail-msg (is (= #{1 2} #{2 3}))]
    (is (= fail-msg
           (join-with-newline
            [" left: #{1 2}"
             "right: #{3 2}"
             "   ----- diff details -----"
             "   only in left  #{1}"
             "   only in right #{3}"
             "   in both       #{2}"])))))

(deftest equals-fail-report-test-for-str
  (with-fail-msg [fail-msg (is (= "Hello" "Hi"))]
    (is (= fail-msg
           (join-with-newline
            [" left: \"Hello\""
             "right: \"Hi\""
             "   ----- diff details -----"
             "   @@ -1,5 +1,2 @@"
             "    H"
             "   -ello"
             "   +i"]))))

  (with-fail-msg [fail-msg (is (= "This is a pen" "This is an apple"))]
    (is (= fail-msg
           (join-with-newline
            [" left: \"This is a pen\""
             "right: \"This is an apple\""
             "   ----- diff details -----"
             "   @@ -6,8 +6,11 @@"
             "    is a"
             "   - pen"
             "   +n apple"])))))

(deftest equals-fail-report-test-for-complex-structure
  (with-fail-msg [fail-msg (is (= {:foo {:bar [1 2 3]}}
                                  {:foo {:bar [1 2 4]}}))]
    (is (= fail-msg
           (join-with-newline
            [" left: {:foo {:bar [1 2 3]}}"
             "right: {:foo {:bar [1 2 4]}}"
             "   in [:foo :bar 2]"
             "      left 3"
             "     right 4"]))))

  (with-fail-msg [fail-msg (is (= {:foo 1} {:foo 2} {:foo 3}))]
    (is (= fail-msg
           (join-with-newline
            [" left: {:foo 1}"
             "right: {:foo 2}"
             "   in [:foo]"
             "      left 1"
             "     right 2"
             " left: {:foo 1}"
             "right: {:foo 3}"
             "   in [:foo]"
             "      left 1"
             "     right 3"]))))

  (with-fail-msg [fail-msg (is (= '(:foo :bar) [:foo :bar] {:foo :bar}))]
    (is (= fail-msg
           (join-with-newline
            [" left: (:foo :bar)"
             "right: {:foo :bar}"])))))

(deftest default-fail-report-test
  (with-fail-msg [fail-msg (is (nil? 1))]
    (is (= fail-msg
           (join-with-newline
            ["expected: (nil? 1)"
             "  actual: (not (nil? 1))"]))))
  (with-fail-msg [fail-msg (is (nil? 1) "should fail")]
    (is (= fail-msg
           (join-with-newline
            ["should fail"
             "expected: (nil? 1)"
             "  actual: (not (nil? 1))"]))))

  (with-fail-msg [fail-msg (testing "should fail" (is false))]
    (is (= fail-msg
           (join-with-newline
            ["should fail"
             "expected: false"
             "  actual: false"])))))
