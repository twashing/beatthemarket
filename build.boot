
(set-env!
 :resource-paths #{"src"}
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [http-kit "2.1.18"]
                 [bidi "2.0.6"]
                 [com.taoensso/sente "1.8.1"]
                 [com.taoensso/timbre "4.3.1"]])

(task-options!
 pom {:project 'beatthemarket
      :version "0.0.1"})

(require '[beatthemarket.shell])

(deftask run []
  (beatthemarket.shell/start-server))
