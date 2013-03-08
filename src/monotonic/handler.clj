(ns monotonic.handler
  (:use compojure.core
        [ring.middleware.params :only [wrap-params]])

  (:require [compojure.handler         :as handler]
            [compojure.route           :as route]
            [ring.util.response        :as resp]
            [monotonic.routes.account  :as account]
            [monotonic.routes.sequence :as sequence]
            [monotonic.middle.auth     :as auth]
            [monotonic.utils.std_response :as res]))

; 
; CONFIG
;
(def API_SCHEME (System/getenv "MONO_API_SCHEME")) ; nil | http | https
(def WWW_SCHEME (System/getenv "MONO_WWW_SCHEME"))
(def SSL_HEADER (System/getenv "MONO_SSL_HEADER"))



; 
; UTILITIES
;
(defn build-url [scheme req]
  (let [
         host (:server-name req)
         port (:server-port req)
         uri  (:uri req)
         qs   (:query-string req)
       ]
    (str scheme "://" host
         (if port (str ":" port))
         uri
         (if qs (str "?" qs)))))

(defn schemify_handler [action direction]
  (defn schemify_real [handler]
    (fn [req] 
      (let [
             scheme ((:headers req) SSL_HEADER)
             wrong?  (not (= scheme direction))
           ]
        ; check scheme != direction => redirect direction://gethost/getpath
        ; if we don't get the header we passthru, avoid redir loop
        (if (and scheme wrong?)
          (if (= action :fail) 
            (res/err_badrequest (str "Incorrect url scheme: " scheme))
            (res/redirect_perm (build-url direction req)))
          (handler req))))))

(defmacro schemify [action which routes]
  `(if (not (nil? ~which))
    (do 
      (println "Turning on scheme checking")
      ((schemify_handler ~action ~which)
        ~routes))
    (do
      (println "No scheme checking")
      ~routes)))



; 
; ROUTING
;; all routes get scheme wrapped or not at compile time - does not check wrap each time
;

(def api-html-routes
  (schemify
    :redirect WWW_SCHEME
    (GET "/" [] (resp/resource-response "index.html" {:root "public"}))))

(defroutes public-routes api-html-routes)

(def api-user-routes
  (schemify
    :fail API_SCHEME
    (routes
      (PUT "/" [em & params] (account/handler em params)))))

(defroutes semi-public-routes
  (context "/a/:em" [em]
    (auth/optional api-user-routes)))

(def api-seq-routes
  (schemify
    :fail API_SCHEME
    (auth/required
      (routes
        (GET  "/" [em seq_id & params] (sequence/show  em seq_id (params :auth)))
        (POST "/" [em seq_id & params] (sequence/crank em seq_id (params :auth) (params :get))) ))))

(defroutes auth-routes
  (context "/a/:em/:seq_id" [em seq_id] api-seq-routes))
    ;(auth/required api-seq-routes)))

(defroutes all-routes
  auth-routes
  semi-public-routes
  public-routes
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (-> all-routes handler/site))
