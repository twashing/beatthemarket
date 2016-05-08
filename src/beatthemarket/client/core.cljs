(ns beatthemarket.client.core
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [<! >! put! close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(enable-console-print!)
(println "Hello World")

(go
  (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:8080/streaming"))]
    (println (str  "ws-channel: " ws-channel))
    (if-not error
      (>! ws-channel "Hello server from client!")
      (js/console.log "Error:" (pr-str error)))))

#_(go
  (let [{:keys [ws-channel]} (<! (ws-ch "ws://localhost:8080/streaming"))
        {:keys [message error]} (<! ws-channel)]
    (js/console.log ws-channel)
    (if error
      (js/console.log "Uh oh:" error)
      (do
        (js/console.log "Hooray! Message:" (pr-str message))
        (>! ws-channel {:foo "client"})))))
