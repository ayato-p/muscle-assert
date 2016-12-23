(ns muscle-assert.analyzer-test
  (:require [muscle-assert.analyzer :as a]
            [clojure.test :refer :all]))

(deftest analyzer-test-for-maps
  (testing "analyzing two same maps"
    (testing "simple"
      (are [a] (= {} (a/analyze a a))
        {}

        {:foo 1}

        {:foo []}))

    (testing "nested"
      (are [a] (= {} (a/analyze a a))
        {:foo {:hoge 1}}

        {:foo {:hoge {:aaa 1 :bbb 2}}})))

  (testing "analyzing two different maps"
    (testing "simple"
      (are [a b res] (= res (a/analyze a b))
        {:foo 1} {} {[:foo] [1 ::a/missing]}

        {:foo nil} {} {[:foo] [nil ::a/missing]}

        {:foo []} {:foo #{}} {[:foo] [[] #{}]}

        {:foo 1 :bar 2} {:foo 1 :bar 3} {[:bar] [2 3]}))

    (testing "nested"
      (are [a b res] (= res (a/analyze a b))
        {:foo {:hoge 1}} {:foo {:hoge 2}} {[:foo :hoge] [1 2]}

        {:foo {:hoge {:toto 1 :tete 2}}} {:foo {:hoge {:toto 1}}} {[:foo :hoge :tete] [2 ::a/missing]}

        {:foo {:hoge {:toto 1}}} {:foo {:fuga {:toto 1}}} {[:foo :hoge] [{:toto 1} ::a/missing]
                                                           [:foo :fuga] [::a/missing {:toto 1}]}))))

(deftest analyzer-test-for-seqs
  (testing "analyzing two same sequences"
    (testing "simple"
      (are [a] (= {} (a/analyze a a))
        []

        [1 2 3]

        '()

        (let [x 1] `(~x ~x))))

    (testing "nested"
      (are [a] (= {} (a/analyze a a))
        [1 [2 [3 [4 nil]]]]

        [[[[[[1]]]]]]

        (let [x 1 y 2] `(~x (~y))))))

  (testing "analyzing two different sequences"
    (testing "simple"
      (are [a b res] (= res (a/analyze a b))
        [1] [] {[0] [1 ::a/missing]}

        [nil] [] {[0] [nil ::a/missing]}

        [1] [2] {[0] [1 2]}

        [1 2 3] [1 3] {[1] [2 3], [2] [3 ::a/missing]}))

    (testing "nested"
      (are [a b res] (= res (a/analyze a b))
        [1 [2 [3 [4]]]] [1 [2 [3 [5]]]] {[1 1 1 0] [4 5]}

        [[[[4] 3] 2] 1] [[[[5] 3] 2] 1] {[0 0 0 0] [4 5]}

        [[1 2] [3 [4 5]]] [[1 2 3] [4 [5]]] {[0 2] [::a/missing 3]
                                             [1 0] [3 4]
                                             [1 1 0] [4 5]
                                             [1 1 1] [5 ::a/missing]}))))

(deftest analyzer-test-for-complicatd-data-structure
  (let [a {:foo 1 :bar [{:hoge 1} {:fuga 2}]}
        b {:foo 2 :bar [{:hoge 2} {:fuga 2 :piyo 3}]}]
    (is (= {[:foo] [1 2]
            [:bar 0 :hoge] [1 2]
            [:bar 1 :piyo] [::a/missing 3]}
           (a/analyze a b))))

  (let [a [{:foo 1} {:foo 2} {:foo 3}]
        b [{:foo 2} {:foo 2} {:foo 4}]]
    (is (= {[0 :foo] [1 2]
            [2 :foo] [3 4]}
           (a/analyze a b))))

  (let [a [#{1 2} #{3}]
        b [#{1 3} #{3}]]
    (is (= {[0] [#{1 2} #{1 3}]}))))

(defrecord MyRecord [foo bar baz])

(deftype MyType [foo bar baz])

(deftest analyzer-test-for-other-data-types
  (is (= {[] [1 2]}
         (a/analyze 1 2)))

  (is (= {[] [[] {}]}
         (a/analyze [] {})))

  (is (= {[] [#{1 2} #{2 3}]}
         (a/analyze #{1 2} #{2 3})))

  (is (= {}
         (a/analyze #{1} #{1})))

  (is (= {[:foo] [1 2]
          [:baz] [3 2]}
         (a/analyze (map->MyRecord {:foo 1 :bar 2 :baz 3})
                    {:foo 2 :bar 2 :baz 2})))

  (let [a (->MyType 1 2 3)
        b (->MyType 1 2 3)]
    (is (= {[] [a b]}
           (a/analyze a b))))

  (let [a (->MyType 1 2 3)
        b (->MyType 1 2 3)
        aary (into-array [a])
        bary (into-array [b])]
    (is (= {[0] [a b]}
           (a/analyze aary bary)))))
