(defproject fops "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.apache.xmlgraphics/fop "1.0"]
                 [ring/ring-core "1.3.0"]
                 [ring/ring-jetty-adapter "1.3.0"]]
  :plugins [[lein-ring "0.8.7"]]
  :ring {:handler fops.web/app}
  :main ^:skip-aot fops.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
