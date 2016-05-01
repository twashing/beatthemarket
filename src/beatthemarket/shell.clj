(ns beatthemarket.shell
  (:require [org.httpkit.server :as server]
            [ring.util.response :as res]
            [ring.middleware.keyword-params]
            [ring.middleware.params]

            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [compojure.handler :as handler]

            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]

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
(defn index-handler [request]
  (res/response "Homepage"))

(defn landing-handler [request]
  (res/response "Landing"))

(def thing
  (let [{:keys [ch-recv send-fn ajax-post-fn
                ajax-get-or-ws-handshake-fn connected-uids]}
        (sente/make-channel-socket! sente-web-server-adapter {})

        ring-ajax-post                ajax-post-fn
        ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
        ch-chsk                       ch-recv ; ChannelSocket's receive channel
        chsk-send!                    send-fn ; ChannelSocket's send API fn
        connected-uids                connected-uids] ; Watchable, read-only atom

    (defroutes routes
      (GET "/" req (index-handler req))
      (GET "/landing" req (landing-handler))

      (GET "/chsk" req (ring-ajax-get-or-ws-handshake req))
      (POST "/chsk" req (ring-ajax-post               req))

      (route/resources "/")
      (route/not-found "Page not found"))))

(def app
  (-> routes
      (handler/site)))


;; ===
(defonce server (atom nil))

(defn start-server []
  (reset! server (server/run-server #'app {:port 8080})))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [&args]
  ;; The #' is useful when you want to hot-reload code
  ;; You may want to take a look: https://github.com/clojure/tools.namespace
  ;; and http://http-kit.org/migration.html#reload
  (start-server))


(comment

  (start-server)
  (start)

  (stop)
  (stop-server)

  )
