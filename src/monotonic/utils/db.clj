(ns monotonic.utils.db
  (:require [taoensso.carmine :as redis]))

(def pool         (redis/make-conn-pool))
(def spec-server1
  (let [redistogo (get (System/getenv) "REDISTOGO_URL")]
    (if (nil? redistogo)
        (redis/make-conn-spec)
        (let [
               parsed_url (java.net.URI. redistogo)
               user       (.getUserInfo parsed_url)
               password   (second (.split user ":"))
               host       (.getHost parsed_url)
               port       (.getPort parsed_url)
             ]
        (redis/make-conn-spec :host host :port port :password password)))))

(defmacro redisconn [& body] `(redis/with-conn pool spec-server1 ~@body))

(defn setkey [key val]
  (redisconn (redis/set key val)))

(defn getkey [key]
  (redisconn (redis/get key)))

(defn incrby [key incr]
  (redisconn (redis/incrby key incr)))

