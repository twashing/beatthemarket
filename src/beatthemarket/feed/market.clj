(ns beatthemarket.feed.market
  (:require [chime :refer [chime-ch chime-at]]
            [clj-time.periodic :refer [periodic-seq]]
            [clj-time.core :as t]
            [clojure.core.async :as a :refer [<! go-loop]])
  (:import [yahoofinance YahooFinance]))


(defn kill-feed-fn [])

(defn start-feed-internal [stocks]
  (let [every-second (rest    ; excludes *right now*
                      (periodic-seq (t/now)
                                    (-> 1 t/seconds)))]
    (chime-at every-second
              (fn [time]
                (println (str time " Stocks > " (YahooFinance/get (into-array stocks))))))))

(defn start-feed [stocks]
  (alter-var-root
   (var kill-feed-fn)
   (fn [f]
     (start-feed-internal stocks))))

(defn stop-feed []
  (kill-feed-fn))


(comment

  (start-feed ["GE"])

  (start-feed
   ["IBM" "AAPL" "YHOO"])

  (stop-feed)

  )
