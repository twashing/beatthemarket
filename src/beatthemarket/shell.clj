(ns beatthemarket.shell
  (:require [org.httpkit.server :as httpkit]
            [ring.util.response :as res]
            [ring.middleware.params :as params]
            [ring.middleware.defaults :as defaults]

            [compojure.core :as compojure :refer [defroutes GET POST]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.core.async :as a :refer [<! >! put! close! go go-loop]]
            [chord.http-kit :refer [wrap-websocket-handler with-channel]]

            #_[aleph.http :as http]
            #_[manifold.stream :as s]
            #_[manifold.deferred :as d]
            
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
  #_(println request)
  (res/response "Homepage"))

(defn landing-handler [request]
  #_(println request)
  (res/response "Landing"))

#_(defn streaming-handler [{:keys [ws-channel] :as req}]
  (go
    (let [{:keys [message]} (<! ws-channel)]
      (println "Message received:" message)
      (>! ws-channel "Hello client from server!")
      (close! ws-channel))))

(defn streaming-handler [{:keys [ws-channel] :as req}]
  (>pprint req)
  (go
    (let [{:keys [message]} (<! ws-channel)]
      (prn "Message received:" message)
      (>! ws-channel "Hello client from server!")
      )))

#_(defn streaming-handler [req]
  (println req)
  (->
   (d/let-flow [socket (http/websocket-connection req)]
     (s/connect socket socket))
   (d/catch
       (fn [_]
         non-websocket-request))))

#_(defroutes routes
  (GET "/" req (index-handler req))
  (GET "/landing" req (landing-handler req))

  (route/resources "/")
  (route/not-found "Page not found"))


(def app
  (wrap-websocket-handler
   (compojure/routes
    (GET "/"           req (index-handler req))
    (GET "/landing"    req (landing-handler req))
    (GET "/streaming"  req (streaming-handler req))
    (route/resources "/")
    (route/not-found "Page not found"))))


#_(defn send-to-client [msg]
  (let [uid (:any @connected-uids)]
    (println uid)
    (println msg)
    (chsk-send! nil [:some/msg msg])))

#_(def app
  (-> routes
      (handler/site)))


;; ===
#_(defonce server (atom nil))
#_(defn start-server []
  (reset! server (server/run-server #'app {:port 8080})))

#_(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

#_(defn -main [&args]
  ;; The #' is useful when you want to hot-reload code
  ;; You may want to take a look: https://github.com/clojure/tools.namespace
  ;; and http://http-kit.org/migration.html#reload
  (start-server))

(defonce !server
  (atom nil))

(defn start-server []
  (swap! !server
         (fn [running-server]
           (or running-server
               (httpkit/run-server app {:port 8080})))))

(defn stop-server []
  (swap! !server
         (fn [running-server]
           (when running-server
             ;; call the server to stop it
             (running-server)
             nil))))


(comment

  (start-server)
  (start)

  (stop)
  (stop-server)

  (send-to-client {:foo :bar}))
