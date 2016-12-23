(defproject ayato_p/muscle-assert "0.1.0"
  :description "MuscleAssert for Clojure"
  :url "http://example.com/FIXME"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :java-source-paths ["src-java"]

  :deploy-repositories [["snapshots" {:url      "https://clojars.org/repo/"
                                      :username [:gpg :env]
                                      :password [:gpg :env]}]
                        ["releases" {:url   "https://clojars.org/repo/"
                                     :creds :gpg}]]

  :profiles
  {:dev {:dependencies [[org.clojure/clojure "1.8.0"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
   :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
   :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
   :1.9 {:dependencies [[org.clojure/clojure "1.9.0-alpha14"]]}
   :cloverage {:plugins [[lein-cloverage "1.0.9"]]}}

  :aliases {"all" ["with-profile" "+1.6:+1.7:+1.8:+1.9"]
            "coverage" ["with-profile" "+cloverage" "cloverage" "--codecov"]}

  :release-tasks [["vcs" "assert-committed"]
                  ["all" "test"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy" "releases"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
