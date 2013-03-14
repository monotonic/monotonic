(ns monotonic.routes.sequence
  (:import java.util.Date)
  (:require
    [clojure.data.json :as json]
    [monotonic.utils.db :as db]
    [monotonic.utils.std-response :as res]
    [monotonic.utils.password :as pw]))

(defn- real-crank [email seq-id incr]
  (let [uniqseq (str email ":" seq-id)
        last-id (db/incrby (str "seq:" uniqseq) incr)
        now (.getTime (Date.))]
    (db/setkey (str "mtime:" uniqseq) now)
    (res/success [(+ 1 (- last-id incr)), last-id])))

(defn crank [email seq-id password get]
  (let [incr (if get (Integer. get) 1)]
    (real-crank email seq-id incr)))

(defn show [email seq-id password]
  (let [meta (db/getkey (str "seq:" email ":" seq-id))]
    (res/success meta)))
