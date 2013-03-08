(ns monotonic.routes.sequence
  (:import java.util.Date)
  (:require [clojure.data.json            :as json]
            [monotonic.utils.db           :as db]
            [monotonic.utils.std_response :as res]
            [monotonic.utils.password     :as pw]))

(defn _real_crank [email seq_id incr]
  (let [
         uniqseq (str email ":" seq_id)
         last_id (db/incrby (str "seq:" uniqseq) incr)
         now     (.getTime (Date.))
       ]
    (db/setkey (str "mtime:" uniqseq) now)
    (res/success [ (+ 1 (- last_id incr)), last_id ])))

(defn crank [email seq_id password get]
  (let [incr (if get (Integer. get) 1)]
    (_real_crank email seq_id incr)))

(defn show [email seq_id password]
  (let [ meta (db/getkey (str "seq:" email ":" seq_id)) ]
    (res/success meta)))
