(ns beatthemarket.client.core
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [<! >! put! close!]]
            [cljs.pprint :refer [pprint]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   ))


(enable-console-print!)
(println "Hello World")

#_(go
  (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:8080/streaming"))]
    (if-not error
      (>! ws-channel "Hello server from client!")
      (js/console.log "Error:" (pr-str error)))))

#_(go-loop []
  (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:8080/streaming"))]
    (js/console.log (str "ws-channel: " ws-channel))
    (if-not error
      (let [msg (<! ws-channel)]
        (js/console.log "in loop: " msg)
        (>! ws-channel "Hello server from client!")
        (recur))
      (js/console.log "Error: " (pr-str error)))))

#_(go
  (let [{:keys [ws-channel]} (<! (ws-ch "ws://localhost:8080/streaming"))
        {:keys [message error]} (<! ws-channel)]
    (js/console.log (str "ws-channel: " ws-channel))
    (if error
      (js/console.log "Uh oh:" error)
      (do
        (js/console.log "Hooray! Message:" (pr-str message))
        (>! ws-channel {:foo "client"})))))

(go
  (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:8080/streaming"))]
    (js/console.log (str "ws-channel: " ws-channel))
    (>! ws-channel "First message from client!")
    (if-not error
      (loop [msg (<! ws-channel)]
        (js/console.log "in loop: " (cljs.pprint/pprint msg))
        (>! ws-channel "Hello server from client!")
        (recur (<! ws-channel)))
      (js/console.log (str "error: " error)))))
