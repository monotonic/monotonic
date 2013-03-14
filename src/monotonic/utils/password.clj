(ns monotonic.utils.password
  (:import [java.security MessageDigest])
  (:require [clojure.data.json :as json]))

(defn get-rand-mult [max]
  (fn ([i] (int (+ 1 (* i max))))))

(defn get-rands [max howmany]
  (let [rand-mult (get-rand-mult max)
        rand-seq (repeatedly rand)] 
    (map rand-mult (take howmany rand-seq))))

(def salt-set "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")

(defn real-get-salt [howmany]
  (let [saltseq (map (fn ([i] (nth salt-set i))) (get-rands (count salt-set) howmany))]
    (apply str saltseq)))

(defn get-salt
  ([] (get-salt 8))
  ([howmany]
    (let [salt-set-len (dec (count salt-set)) 
          pluck-salt (fn ([i] (nth salt-set i)))
          saltseq (map pluck-salt (get-rands salt-set-len howmany))]
      (apply str saltseq))))

(defn hash-pw [password salt]
  (let [hash-src (str salt password)    
        hash (MessageDigest/getInstance "SHA-256")]
     (.update hash (.getBytes hash-src))
     (let [digest (.digest hash)]
        (apply str (map #(format "%02x" (bit-and % 0xff)) digest)))))

(defn valid-auth? [account password]
  (if (= (account "pw") (hash-pw password (account "salt")))
    account
    false))
