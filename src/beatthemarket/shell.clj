(ns beatthemarket.shell
  (:require [org.httpkit.server :as server]
            [ring.util.response :as res]
            [ring.middleware.keyword-params]
            [ring.middleware.params]
            [bidi.ring :refer [make-handler]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]))


;; ===
(defn index-handler
  [request]
  (res/response "Homepage"))

(def handler
  (let [{:keys [ch-recv send-fn ajax-post-fn
                ajax-get-or-ws-handshake-fn connected-uids]}
        (sente/make-channel-socket! sente-web-server-adapter {})

        ring-ajax-post                ajax-post-fn
        ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
        ch-chsk                       ch-recv ; ChannelSocket's receive channel
        chsk-send!                    send-fn ; ChannelSocket's send API fn
        connected-uids                connected-uids] ; Watchable, read-only atom

    (make-handler ["/" {"" index-handler
                        {:get {"/chsk" (fn [req] (ring-ajax-get-or-ws-handshake req))}}
                        {:post {"/chsk" (fn [req] (ring-ajax-post                req))}}}])))

(def app
  (-> handler
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params))

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
