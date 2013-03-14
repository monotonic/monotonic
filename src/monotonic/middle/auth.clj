(ns monotonic.middle.auth
  (:require
    [clojure.data.json :as json]
    [monotonic.utils.db :as db]
    [monotonic.utils.std-response :as res]
    [monotonic.utils.password :as pw]))

(defn- validate-auth [handler req]
  (let [params (req :params)
        email (params :em)
        key (str "a:" email)
        account-json (db/getkey key)
        account (if account-json (json/read-str account-json) nil)]
    (println (str "AUTH" account))
    (if (and account (pw/valid-auth? account (params :auth)))
      (handler (merge req {:params (merge params {:authed true :authed-key key :authed-acct account})}))
      (res/err-unauth "Invalid authentication"))))

(defn required [handler]
  (fn [req] (validate-auth handler req)))
  
(defn optional [handler]
  (fn [req]
    (let [auth ((req :params) :auth)]
      (if (nil? auth) 
        (handler req)
        (validate-auth handler req)))))
