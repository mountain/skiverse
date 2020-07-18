(ns skiverse.main
  (:import [java.lang Thread])
  (:use [ring.middleware.defaults :only [wrap-defaults api-defaults]]
        [ring.middleware.json :only [wrap-json-response]]
        [ring.middleware.not-modified :only [wrap-not-modified]]
        [org.httpkit.server :only [run-server]])
  (:require [clojure.tools.logging :as log]
            [ring.util.codec :as codec]
            [ring.util.response :as response]
            [ring.util.request :as request]
            [ring.middleware.head :as head])
  (:gen-class))

(defn api [req]
  (try
    ((wrap-defaults (wrap-json-response apiroutes) api-defaults) req)
    (catch Throwable e (log/error e))))

(defn img [req]
  (try
    ((wrap-not-modified imgroutes) req)
    (catch Throwable e (log/error e))))

(defonce api-server (atom nil))

(defonce img-server (atom nil))

(defn init []
  (log/info "init settings ...")
  (System/setProperty "utw.path.home" (skiverse-home))
  (System/setProperty "utw.path.data" (skiverse-data-dir))
  (System/setProperty "utw.path.conf" (skiverse-conf-dir)))

(defn start-img []
  (.start (Thread. (fn []
                     (Thread/sleep 60000)
                     (try
                       (log/info "starting page server at " (skiverse-img-server))
                       (reset! img-server (run-server #'img {:ip (skiverse-public-ip) :port (skiverse-img-port) :thread 12}))
                       (catch Throwable e (log/error e)))))))

(defn load-data []
  (let [thd (Thread. (fn []
                       (try
                         (log/info "loading data ...")
                         (load-page-data)
                         (catch Throwable e (log/error e)))))]
    (.setPriority thd Thread/MAX_PRIORITY)
    (.start thd)))

(defn skiverse-app-start []
  (init)
  (load-data)
  (start-img))

(defn -main []
  (skiverse-app-start))
