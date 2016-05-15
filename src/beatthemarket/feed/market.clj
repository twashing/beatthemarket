(ns beatthemarket.feed.market
  (:require [chime :refer [chime-ch chime-at]]
            [clj-time.periodic :refer [periodic-seq]]
            [clj-time.core :as t]
            [clojure.core.async :as a :refer [<! go-loop]])
  (:import [yahoofinance YahooFinance]))


(defn kill-feed-fn [])

(defn start-feed-internal
  ([stocks]
   (start-feed-internal stocks
                        (fn [results]
                          (println (str " Stocks > " results)))))
  ([stocks handle-fn]
   (let [every-second (rest    ; excludes *right now*
                       (periodic-seq (t/now)
                                     (-> 1 t/seconds)))]
     (chime-at every-second
               (fn [time]
                 (let [results (YahooFinance/get (into-array stocks))]
                   (handle-fn results)))))))

(defn start-feed

  ([stocks]
   (start-feed stocks
               (fn [results]
                 (println (str " Stocks > " results)))))

  ([stocks handler-fn]
   (alter-var-root
    (var kill-feed-fn)
    (fn [f]
      (start-feed-internal stocks handler-fn)))))

(defn stop-feed []
  (kill-feed-fn))


(comment

  (start-feed ["GE"])

  (start-feed
   ["IBM" "AAPL" "YHOO"])

  (stop-feed)

  )
