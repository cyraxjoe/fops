(ns fops.web
  (:require [clojure.java.io :as io]
            [fops.document :as document]
            [fops.web.page :as page]
            [fops.utils :as utils]
            [ring.util.io :as ring-io]
            [ring.util.response :as rsp]
            [ring.middleware.params :refer [wrap-params]]))

(defn- index
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body page/index})

(defn- gen-doc
  [document ext mime]
  (let [doc-path (document/get-document
                  (ring-io/string-input-stream document)
                  ext)]
    (utils/delete-in-future doc-path)
    (-> doc-path
        rsp/file-response
        (rsp/content-type mime))))

(defn- main-handler
  [request]
  (let [uri (:uri request)
        method (:request-method request)]
    (condp = method
      :get (cond (= uri "/") (index request))
      :post (let [ext (clojure.string/replace uri "/" "")
                  mime ((keyword ext) document/FORMATS)]
              (if mime
                (if-let [document (get-in request [:form-params "document"])]
                  (gen-doc document ext mime)
                  (rsp/status
                   (rsp/response "Missing required parameter 'document'.")
                   400))
                (rsp/not-found (format "Unsupported format %s" ext)))))))

(defn app
    [request]
    (let [gateway (wrap-params main-handler)]
      (gateway request)))
