(ns monotonic.middle.auth
  (:require [clojure.data.json :as json]
            [monotonic.utils.db :as db]
            [monotonic.utils.std_response :as res]
            [monotonic.utils.password :as pw]))

(defn _validate_auth [handler req]
  (let [
         params       (req :params)
         email        (params :em)
         key          (str "a:" email)
         account_json (db/getkey key)
         account      (if account_json (json/read-str account_json) nil)
       ]
    (if (and account (pw/validAuth account (params :auth)))
      (handler (merge req {:params (merge params {:authed true :authed_key key :authed_acct account})}))
      (res/err_unauth "Invalid authentication"))))

(defn required [handler]
  (fn [req] (_validate_auth handler req)))
  
(defn optional [handler]
  (fn [req]
    (let [auth ((req :params) :auth)]
      (if (nil? auth) 
        (handler req)
        (_validate_auth handler req)))))
