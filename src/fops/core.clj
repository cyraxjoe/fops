(ns fops.core
  (:use ring.adapter.jetty
        fops.web)
  (:require [fops.web :as web])
  (:gen-class))

(def default-port "3000")

(defn -main
  "Entry point of the stand alone mode of fops."
  [& args]
  (let [port (Integer/parseInt (or (first args) default-port))]
    (run-jetty  web/app {:port port})))
