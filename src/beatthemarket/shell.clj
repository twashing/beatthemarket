(ns beatthemarket.shell
  (:require [org.httpkit.server :as httpkit]
            [ring.util.response :as res]
            [ring.middleware.params :as params]
            [ring.middleware.defaults :as defaults]

            [compojure.core :as compojure :refer [defroutes GET POST]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.core.async :as a :refer [<! >! put! close! chan go go-loop]]
            [chord.http-kit :refer [wrap-websocket-handler with-channel]]

            [figwheel-sidecar.repl :as r]
            [figwheel-sidecar.repl-api :as ra]))


;; ===
(defn start []
  (ra/start-figwheel!
   {:figwheel-options {} ;; <-- figwheel server config goes here
    :build-ids ["dev"]   ;; <-- a vector of build ids to start autobuilding
    :all-builds          ;; <-- supply your build configs here
    [{:id "dev"
      :figwheel true
      :source-paths ["src"]
      :compiler {:main "beatthemarket.client.core"
                 :asset-path "js"
                 :output-to "resources/public/js/main.js"
                 :output-dir "resources/public/js"
                 :verbose true}}]}))

;; Please note that when you stop the Figwheel Server http-kit throws
;; a java.util.concurrent.RejectedExecutionException, this is expected

(defn stop []
  (ra/stop-figwheel!))

(defn repl []
  (ra/cljs-repl))

;; ===
(def non-websocket-request
  {:status 400
   :headers {"content-type" "application/text"}
   :body "Expected a websocket request."})

(defn index-handler [request]
  (println request)
  (res/response "Homepage"))


(defn streaming-handler [{:keys [ws-channel] :as req}]
  (go
    (let [{:keys [message]} (<! ws-channel)]
      (println "Message received:" message)
      (>! ws-channel "Hello client from server!")
      #_(close! ws-channel))))


(def app
  (handler/site
   (compojure/routes
    (GET "/"           req (index-handler req))
    (GET "/streaming"  req (streaming-handler req))
    (route/resources "/")
    (route/not-found "Page not found"))))


;; ====
(defonce server (atom nil))

(defn start-server []
  (reset! server (httpkit/run-server #'app {:port 8080})))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))


(comment

  ;; start web and figwheel
  (start-server)
  (start)

  (stop)
  (stop-server)

  ;; one time cljs build
  (require 'cljs.build.api)
  (cljs.build.api/build "src"
                        {:main 'beatthemarket.client.core
                         :output-to "resources/public/js/main.js"
                         :output-dir "resources/public/js"
                         :asset-path "js"}))
