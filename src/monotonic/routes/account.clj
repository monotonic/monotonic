(ns monotonic.routes.account
  (:import java.util.Date)
  (:require [clojure.data.json :as json]
            [monotonic.utils.db :as db]
            [monotonic.utils.std_response :as res]
            [monotonic.utils.password :as pw]))

(defn _real_create [key password]
  (let [
         salt   (pw/getSalt)
         hashed (pw/hashPw password salt)
         now    (.getTime (Date.))
       ]
     (db/setkey key (json/write-str {"pw" hashed "salt" salt "ctime" now "mtime" now}))
     (json/write-str {:success true})))

(defn _is_valid_email [email]
  (let [email_rx #"^[a-z0-9.+_-]+@[^@ ]+\.[a-z]{2,4}$"]
    (not (nil? (re-find email_rx email)))))

(defn _acct_exists [key]
  (let [acct (db/getkey key)]
    (not (nil? acct))))

(defn create [email password]
  (let [acct_key (str "a:" email)]
    (if (_acct_exists acct_key)
      (res/err_badrequest "That account exists already.")
      (if (_is_valid_email email)
        (_real_create acct_key password)
        (res/err_badrequest "Sorry, that is an invalid email.") ))))

(defn update [key account password]
  (let [
         salt   (pw/getSalt)
         hashed (pw/hashPw password salt)
         now    (.getTime (Date.))
         account (merge account {"pw" hashed "salt" salt "mtime" now})
       ]
     (db/setkey key (json/write-str account))
     (json/write-str {:success true :result {}})))

(defn handler [email params]
  (let [
         password (params :password)
         authed   (params :authed)
       ]
    (if authed
      (update (params :authed_key) (params :authed_acct) password)
      (create email password))))
