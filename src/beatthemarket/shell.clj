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
            [figwheel-sidecar.repl-api :as ra]

            [beatthemarket.feed.market :as market])
  (:import [yahoofinance YahooFinance]))


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

(defn streaming-handler [{:keys [ws-channel async-channel] :as req}]
  (go
    (>! ws-channel "First message from server!")

    (def write-ch ws-channel)
    
    (let [{:keys [message]} (<! ws-channel)]
      (println "Message received:" message)
      #_(close! ws-channel))))

#_(def write-ch (chan))

(def app
  (handler/site
   (compojure/routes
    (GET "/"           req (index-handler req))
    (GET "/streaming"  req ((wrap-websocket-handler streaming-handler #_{:write-ch write-ch}) req))
    (route/resources "/")
    (route/not-found "Page not found"))))

(defn write-to-client [msg]
  (go
    (>! write-ch msg)))

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

(defn start-all []
  (start-server)
  (start)
  (market/start-feed ["IBM" "AAPL" "YHOO"]
                     (fn [results]
                       (let [rs (into {} results)
                             ks (keys rs)
                             vs (vals rs)
                             vss vs
                             formatted-results (zipmap ks vss)]
                         (write-to-client formatted-results)
                         (println formatted-results)))))

(defn stop-all []
  (stop)
  (stop-server)
  (market/stop-feed))

(comment

  ;; start web and figwheel
  (start-server)
  (start)

  (stop)
  (stop-server)

  (write-to-client {:msg "Zapp!!"})

  (market/start-feed ["IBM" "AAPL" "YHOO"]
                     (fn [results]
                       (let [rs (into {} results)
                             ks (keys rs)
                             vs (vals rs)
                             #_vss #_(map (fn [ech]
                                        (let [stats (bean (:stats ech))]
                                          (-> (bean ech)
                                              (dissoc :dividend :history :stats)
                                              (assoc :stats stats))))
                                          (vals rs))
                             vss vs
                             formatted-results (zipmap ks vss)]
                         (write-to-client formatted-results)
                         (println formatted-results))))

  (market/stop-feed)
  
  ;; one time cljs build
  (require 'cljs.build.api)
  (cljs.build.api/build "src"
                        {:main 'beatthemarket.client.core
                         :output-to "resources/public/js/main.js"
                         :output-dir "resources/public/js"
                         :asset-path "js"}))
