(ns muscle-assert.analyzer
  (:require [clojure.data :as data]
            [clojure.set :as set]))

(declare analyze*)

(defn- analyze-atom [a b path]
  [path [a b]])

(defn- analyze-associative-key [a b k path]
  (let [av (get a k ::missing)
        bv (get b k ::missing)]
    (analyze* av bv (conj path k))))

(defn- analyze-associative [a b ks path]
  (->> (keep #(analyze-associative-key a b % path) ks)
       (apply concat)))

(defn- analyze-sequential [a b path]
  (analyze-associative
   (if (vector? a) a (vec a))
   (if (vector? b) b (vec b))
   (range (max (count a) (count b)))
   path))

(defprotocol Analyze
  (analyze-similar [a b path]))

(extend-protocol Analyze
  nil
  (analyze-similar [a b path]
    (analyze-atom a b path))

  Object
  (analyze-similar [a b path]
    ((if (.. a getClass isArray) analyze-sequential analyze-atom) a b path))

  java.util.List
  (analyze-similar [a b path]
    (analyze-sequential a b path))

  java.util.Map
  (analyze-similar [a b path]
    (let [ks (->> (set/union (keys a) (keys b))
                  (into #{}))]
      (analyze-associative a b ks path))))

(defn- analyze*
  ([a b] (analyze* a b []))
  ([a b path]
   (cond
     (= a b) nil
     (= (data/equality-partition a) (data/equality-partition b)) (analyze-similar a b path)
     :else (analyze-atom a b path))))

(defn analyze [a b]
  (->> (partition 2 (analyze* a b))
       (map vec)
       (into {})))
