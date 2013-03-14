(ns monotonic.routes.account
  (:import java.util.Date)
  (:require
    [clojure.data.json :as json]
    [monotonic.utils.db :as db]
    [monotonic.utils.std-response :as res]
    [monotonic.utils.password :as pw]))

(defn- real-create [key password]
  (let [salt (pw/get-salt)
        hashed (pw/hash-pw password salt)
        now (.getTime (Date.))]
     (db/setkey key (json/write-str {"pw" hashed "salt" salt "ctime" now "mtime" now}))
     (json/write-str {:success true})))

(defn- valid-email? [email]
  (let [email-rx #"^[a-z0-9.+_-]+@[^@ ]+\.[a-z]{2,4}$"]
    (not (nil? (re-find email-rx email)))))

(defn- acct-exists? [key]
  (let [acct (db/getkey key)]
    (not (nil? acct))))

(defn create [email password]
  (let [acct-key (str "a:" email)]
    (if (acct-exists? acct-key)
      (res/err-badrequest "That account exists already.")
      (if (valid-email? email)
        (real-create acct-key password)
        (res/err-badrequest "Sorry, that is an invalid email.") ))))

(defn update [key account password]
  (let [salt (pw/get-salt)
        hashed (pw/hash-pw password salt)
        now (.getTime (Date.))
        account (merge account {"pw" hashed "salt" salt "mtime" now})]
     (db/setkey key (json/write-str account))
     (json/write-str {:success true :result {}})))

(defn handler [email params]
  (let [password (params :password)
        authed (params :authed)]
    (if authed
      (update (params :authed-key) (params :authed-acct) password)
      (create email password))))
