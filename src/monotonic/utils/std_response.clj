(ns monotonic.utils.std_response
  (:require [clojure.data.json :as json]))

(defn _json_response [status body]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body (json/write-str body)})

(defn success [result]
  (let [envelope {:success true :result result}]
    (_json_response 200 envelope)))

(defn err_unauth [msg]
  (let [envelope {:success false :msg msg}]
    (_json_response 401 envelope)))

(defn err_badrequest [msg]
  (let [envelope {:success false :msg msg}]
    (_json_response 400 envelope)))

(defn redirect_perm [url]
  {:status 301 :headers {"Location" url}})
