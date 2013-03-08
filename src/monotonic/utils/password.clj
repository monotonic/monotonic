(ns monotonic.utils.password
  (:import [java.security MessageDigest])
  (:require [clojure.data.json :as json]))

(defn getRandMult [max]
  (fn ([i] (int (+ 1 (* i max))))))

(defn getRands [max howmany]
  (let [ randMult (getRandMult max)
         randSeq  (repeatedly rand) ] 
    (map randMult (take howmany randSeq))))

(def saltSet [ 0 1 2 3 4 5 6 7 8 9 "A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N" "O" "P" "Q" "R" "S" "T" "U" "V" "W" "X" "Y" "Z" ])

(defn realGetSalt [howmany]
  (let [ saltseq (map (fn ([i] (nth saltSet i))) (getRands (count saltSet) howmany)) ]
    (apply str saltseq)))

(defn getSalt
  ([] (realGetSalt 8))
  ([i] (realGetSalt i)))

(defn hashPw [password salt]
  (let [
         hash_src (str salt password)    
         hash     (MessageDigest/getInstance "SHA-256")
       ]
     (. hash update (.getBytes hash_src))
     (let [ digest (.digest hash) ]
        (apply str (map #(format "%02x" (bit-and % 0xff)) digest)))))

(defn validAuth [account password]
  (if (= (account "pw") (hashPw password (account "salt")))
    account
    false))
