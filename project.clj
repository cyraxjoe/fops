(defproject fops "0.1.0-SNAPSHOT"
  :description "Apache FOP Server"
  :url "http://github.com/cyraxjoe/fops"
  :license {:name "Apache Public License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.apache.xmlgraphics/fop "1.0"]
                 [ring/ring-core "1.3.0"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [hiccup "1.0.5"]
                 [org.clojure/tools.cli "0.3.1"]
                 [com.brainbot/iniconfig "0.2.0"]
;;                 [ring-basic-authentication "1.0.5"]
                 ]
  :plugins [[lein-ring "0.8.7"]]
  :ring {:handler fops.web/app}
  :main ^:skip-aot fops.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
