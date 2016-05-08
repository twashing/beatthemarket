(defproject beatthemarket "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [http-kit "2.1.18"]
                 [compojure "1.5.0"]
                 [ring/ring-defaults "0.1.5"]
                 [aleph "0.4.1"]
                 [com.taoensso/timbre "4.3.1"]
                 [com.yahoofinance-api/YahooFinanceAPI "3.2.0"]
                 [jarohen/chime "0.1.9"]
                 [jarohen/chord "0.7.0"]]
  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.2"]
                                  [javax.servlet/servlet-api "2.5"]]}})
