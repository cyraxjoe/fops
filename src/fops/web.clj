(ns fops.web
  (:require [fops.document :as document]
            [fops.web.page :as page]
            [ring.util.response :as response])
  (:use clojure.pprint))


(defn index [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body page/index})

(defn gen-doc [request ext mime]
  {:status 200
   :headers {"Content-Type" mime}
   :body (document/stream-doc (:body request) ext)})

(defn app [request]
  (let [uri (:uri request)
        method (:request-method request)]
    (condp = method
      :get (cond (= uri "/") (index request))
      :post (let [ext (clojure.string/replace uri "/" "")
                  mime ((keyword ext) document/FORMATS)]
              (if mime
                (gen-doc request ext mime)
                (response/not-found (format "Unsupported format %s" ext)))))))
