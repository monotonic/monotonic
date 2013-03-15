(ns monotonic.routes.sequence
  (:import java.util.Date)
  (:require
    [monotonic.utils.db :as db]
    [monotonic.utils.std-response :as res]
    [monotonic.utils.password :as pw]))

(def ^{:private true} radix-pat #"(.*?)(?:\.b(\d{1,2}))?")
(def ^{:private true} valid-radices #{8 10 16 36})
(defn- valid-radix? [radix] (valid-radices (Long. radix)))

(defn- base [long radix]
  (let [radix-num (Long. radix)] ;radix comes as string
    (clojure.string/upper-case (Long/toString long radix-num))))

(defn- real-crank [email seq-id incr radix]
  (let [uniqseq (str email ":" seq-id)
        last-id (db/incrby (str "seq:" uniqseq) incr)
        new-id (+ 1 (- last-id incr))
        now (.getTime (Date.))]
    (db/setkey (str "mtime:" uniqseq) now)
    (if (or (nil? radix) (not (valid-radix? radix))) 
      (res/success [new-id last-id])
      (res/success [(base new-id radix) (base last-id radix)]))))
      

(defn crank [email seq-id-raw password get]
  (let [[seq-id radix] (nthrest (re-matches radix-pat seq-id-raw) 1)
        incr (if get (Integer. get) 1)]
    (real-crank email seq-id incr radix)))

(defn show [email seq-id password]
  (let [meta (db/getkey (str "seq:" email ":" seq-id))]
    (res/success meta)))
