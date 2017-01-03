(let [gh "https://github.com/nervous-systems/promesa-check"]
  (defproject io.nervous/promesa-check "0.1.0"
    :description  "Asynchronous test.check utilities"
    :url          "https://github.com/nervous-systems/promesa-check"
    :license      {:name "Unlicense" :url "http://unlicense.org/UNLICENSE"}
    :scm          {:name "git" :url ~gh}
    :dependencies [[org.clojure/clojure       "1.9.0-alpha14"]
                   [org.clojure/clojurescript "1.9.293"]
                   [org.clojure/test.check    "0.9.0"]
                   [funcool/promesa           "1.6.0"]]
    :plugins      [[lein-cljsbuild "1.1.4"]
                   [lein-doo       "0.1.7"]
                   [lein-codox     "0.10.2"]]
    :codox        {:source-paths ["src"]
                   :metadata     {:doc/format :markdown}
                   :themes       [:default [:nervous {:nervous/github ~gh}]]
                   :source-uri   ~(str gh "/blob/master/{filepath}#L{line}")}
    :cljsbuild    {:builds
                   [{:id "test"
                     :source-paths ["src" "test"]
                     :compiler {:output-to     "target/test/promesa-check.js"
                                :output-dir    "target/test"
                                :target        :nodejs
                                :language-in   :ecmascript5
                                :optimizations :none
                                :main          promesa-check.test-runner}}]}
    :profiles     {:dev {:dependencies
                         [[io.nervous/codox-nervous-theme "0.1.0"]]}}))
