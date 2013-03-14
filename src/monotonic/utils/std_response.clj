(ns monotonic.utils.std-response
  (:require [clojure.data.json :as json]))

(defn- json-response [status body]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body (json/write-str body)})

(defn success [result]
  (let [envelope {:success true :result result}]
    (json-response 200 envelope)))

(defn err-unauth [msg]
  (let [envelope {:success false :msg msg}]
    (json-response 401 envelope)))

(defn err-badrequest [msg]
  (let [envelope {:success false :msg msg}]
    (json-response 400 envelope)))

(defn redirect-perm [url]
  {:status 301 :headers {"Location" url}})
