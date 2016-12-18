(defproject promesa-check "0.1.0-SNAPSHOT"
  :description  "Asynchronous test.check utilities"
  :url          "https://github.com/nervous-systems/promesa-check"
  :license      {:name "Unlicense" :url "http://unlicense.org/UNLICENSE"}
  :scm          {:name "git" :url "https://github.com/nervous-systems/promesa-check"}
  :dependencies [[org.clojure/clojure       "1.9.0-alpha14"]
                 [org.clojure/clojurescript "1.9.293"]
                 [org.clojure/test.check    "0.9.0"]
                 [funcool/promesa           "1.6.0"]]
  :plugins      [[lein-cljsbuild "1.1.4"]
                 [lein-doo       "0.1.7"]
                 [lein-codox     "0.9.4"]]
  :codox
  {:source-paths ["src"]
   :metadata {:doc/format :markdown}
   :html
   {:transforms
    ~(read-string (slurp "codox-transforms.edn"))}
   :source-uri "https://github.com/nervous-systems/promesa-check/blob/master/{filepath}#L{line}"}
  :cljsbuild {:builds
              [{:id "test"
                :source-paths ["src" "test"]
                :compiler {:output-to     "target/test/promesa-check.js"
                           :output-dir    "target/test"
                           :target        :nodejs
                           :language-in   :ecmascript5
                           :optimizations :none
                           :main          promesa-check.test-runner}}]})
