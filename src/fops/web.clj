(ns fops.web
  (:require [fops.document :as document])
  (:use clojure.pprint))

(defn index [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello Worlds!"})

(defn gen-pdf [request]
  {:status 200
   :headers {"Content-Type" "text/pdf"}
   :body (document/stream-pdf (:body request))})

(defn app [request]
  (let [uri (:uri request)
        method (:request-method request)]
    (condp = method
      :get (cond (= uri "/") (index request))
      :post (cond (= uri "/pdf") (gen-pdf request)))))
