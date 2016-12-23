(defproject example "0.1.0"
  :description "Example"
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles
  {:dev {:dependencies [[ayato_p/muscle-assert "0.1.0"]]
         :injections [(require 'muscle-assert.core)
                      (muscle-assert.core/activate!)]}})
