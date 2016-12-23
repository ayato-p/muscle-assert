(ns muscle-assert.reporter
  (:require [clojure.data :as data]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test]
            [muscle-assert.analyzer :as a])
  (:import name.fraser.neil.plaintext.diff_match_patch))

(defn- display-string-diff [str1 str2]
  (->> (.patch_make (diff_match_patch.) str1 str2)
       (mapcat (comp str/split-lines str))
       (map #(str "   " %))
       (str/join \newline)
       println))

(defn- display-set-diff [set1 set2]
  (println "   only in left " (pr-str (set/difference set1 set2)))
  (println "   only in right" (pr-str (set/difference set2 set1)))
  (println "   in both      " (pr-str (set/intersection set1 set2))))

(defprotocol ReportDetail
  (report-detail [a b]))

(extend-protocol ReportDetail
  Object
  (report-detail [a b])

  String
  (report-detail [a b]
    (println "   ----- diff details -----")
    (display-string-diff a b))

  java.util.Set
  (report-detail [a b]
    (println "   ----- diff details -----")
    (display-set-diff a b)))

(defn- equals-fail-report [m]
  (let [[_ [_ expected & actuals]] (:actual m)]
    (doseq [actual actuals
            :when (not= actual expected)]
      (println " left:" (pr-str expected))
      (println "right:" (pr-str actual))
      (doseq [[path [a b]] (a/analyze expected actual)]
        (when (seq path)
          (println "   in" path)
          (println "      left" (if-not (= ::a/missing a) (pr-str a) ""))
          (println "     right" (if-not (= ::a/missing b) (pr-str b) "")))
        (when (= (data/equality-partition a)
                 (data/equality-partition b))
          (report-detail a b))))))

(defn- default-fail-report [m]
  (println "expected:" (pr-str (:expected m)))
  (println "  actual:" (pr-str (:actual m))))

(defmethod test/report :fail [{:keys [message expected] :as m}]
  (test/with-test-out
    (test/inc-report-counter :fail)
    (println "\nFAIL in" (test/testing-vars-str m))
    (when (seq test/*testing-contexts*) (println (test/testing-contexts-str)))
    (when-let [message message] (println message))
    (if (and (seq? expected) (= (first expected) '=))
      (equals-fail-report m)
      (default-fail-report m))))
