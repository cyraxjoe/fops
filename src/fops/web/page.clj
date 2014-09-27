(ns fops.web.page
  (:use hiccup.core)
  (:require [hiccup.page]
            [fops.document :as document]))

(defmacro -base [& content]
  `(hiccup.page/html5
    (html [:head [:title "Apache FOP Server - FOPS"]]
          [:body ~@content])))

(def table-of-handlers
  (conj [:table {:border "1"}
         [:thead [:tr  [:th "URL"]  [:th "HTTP Method"] [:th "MIME"] [:th "Ext"]]]]
        (for [[k mime] document/FORMATS]
          (let [docfmt (name k)]
            [:tr
             [:td [:a {:href (format "/%s" docfmt)}
                   (format "/%s" docfmt)]]
             [:td "POST"]
             [:td mime]
             [:td docfmt]]))))

(def index
  (-base [:h3 "Welcome to the Apache FOP Server - FOPS"]
         [:div
          [:p "Available handlers"]
          table-of-handlers]))
