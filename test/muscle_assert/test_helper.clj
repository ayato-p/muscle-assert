(ns muscle-assert.test-helper
  (:require  [clojure.test :as t]))

(defmacro with-test-out-str [& body]
  `(let [s# (java.io.StringWriter.)]
     (binding [t/*test-out* s#]
       ~@body
       (str s#))))

(defmacro ignore-report-counter [& body]
  `(with-redefs [t/inc-report-counter (constantly true)]
     ~@body))
