(defproject monotonic "0.0.1"
  :description "App to make global sequences"
  :url "http://monotonic.io/"
  :dependencies [
                 [org.clojure/clojure   "1.4.0"]
                 [org.clojure/data.json "0.2.1"]
                 [com.taoensso/carmine  "1.5.0"]
                 [cljs-uuid             "0.0.3"]
                 [compojure             "1.1.5"]
                ]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler monotonic.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
