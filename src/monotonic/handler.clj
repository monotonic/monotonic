(ns monotonic.handler
  (:use
    compojure.core
    [ring.middleware.params :only [wrap-params]])
  (:require 
    [compojure.handler :as handler]
    [compojure.route :as route]
    [ring.util.response :as resp]
    [monotonic.routes.account :as account]
    [monotonic.routes.sequence :as sequence]
    [monotonic.middle.auth :as auth]
    [monotonic.utils.std-response :as res]))


;;; CONFIG

(def api-scheme (System/getenv "MONO_API_SCHEME")) ; nil | http | https
(def www-scheme (System/getenv "MONO_WWW_SCHEME"))
(def ssl-header (System/getenv "MONO_SSL_HEADER"))



;;; UTILITIES

(defn build-url [scheme req]
  (let [{:keys [server-name server-port uri query-string]} req]
    (str scheme "://" server-name
         (if server-port (str ":" server-port))
         uri
         (if query-string (str "?" query-string)))))

(defn schemify-handler [action direction]
  (defn schemify-real [handler]
    (fn [req] 
      (let [scheme (get-in req [:headers ssl-header])
            wrong? (not (= scheme direction))]
        ; check scheme != direction => redirect direction://gethost/getpath
        ; if we don't get the header we passthru, avoid redir loop
        (if (and scheme wrong?)
          (if (= action :fail) 
            (res/err-badrequest (str "Incorrect url scheme: " scheme))
            (res/redirect-perm (build-url direction req)))
          (handler req))))))

(defmacro schemify [action which routes]
  `(if ~which
    (do 
      (println "Turning on scheme checking")
      ((schemify-handler ~action ~which)
        ~routes))
    (do
      (println "No scheme checking")
      ~routes)))



;;; ROUTING
;; all routes get scheme wrapped or not at compile time - does not check wrap each time

(def api-html-routes
  (schemify
    :redirect www-scheme
    (GET "/" [] (resp/resource-response "index.html" {:root "public"}))))

(defroutes public-routes api-html-routes)

(def api-user-routes
  (schemify
    :fail api-scheme
    (routes
      (PUT "/" [em & params] (account/handler em params)))))

(defroutes semi-public-routes
  (context "/a/:em" [em]
    (auth/optional api-user-routes)))

(def api-seq-routes
  (schemify
    :fail api-scheme
    (auth/required
      (routes
        (GET  "/" [em seq-id & params] (sequence/show  em seq-id (params :auth)))
        (POST "/" [em seq-id & params] (sequence/crank em seq-id (params :auth) (params :get))) ))))

(defroutes auth-routes
  (context "/a/:em/:seq-id" [em seq-id] api-seq-routes))

(defroutes all-routes
  auth-routes
  semi-public-routes
  public-routes
  (route/resources "/")
  (route/not-found "Not Found"))


(def app
  (-> all-routes handler/site))
