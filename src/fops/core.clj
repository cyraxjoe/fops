(ns fops.core
  (:import (java.io FileNotFoundException))
  (:require [clojure.string :as string]
            [ring.adapter.jetty :refer [run-jetty]]
            [com.brainbot.iniconfig :as iniconfig]
            [clojure.tools.cli :refer [parse-opts]]
            [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
            [fops.web :as web]
            [fops.utils :refer [error-msg]])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 3000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-H" "--host HOST" "Hostname"
    :default "localhost"]
   ["-c" "--config CONFIG" "Config file"]
   ["-h" "--help"]])
(def banner (str "FOPS Server v" (System/getProperty "fops.version")))
(def section-name "fops")

(defmacro authenticated?
  [refname refpasswd]
  `(fn [name# passwd#]
     (and (= name# ~refname)
          (= passwd# ~refpasswd))))

(defmacro clean-string
  [word]
  `(when ~word
     (-> ~word string/trim strip-quotes)))

(defn- strip-quotes
  "Remove the single and doble quotes from the input string."
  [word]
  (string/replace (string/replace word "'" "") "\"" ""))


(defn- options-from-config
  "Read the configuration file with 'ini' syntax.
  It will look for:
       [fops]
         host = HOST
         port = PORT
         user = USER
         passwd = PASSWD
         realm = REALM
  "
  [config]
  (try
    (let [content (iniconfig/read-ini config)]
      (when-let [cfg-section (get content section-name)]
        (let [host (get cfg-section "host")
              port (get cfg-section "port")]
          (if (and host port)
            {:host (clean-string host)
             :port (Integer/parseInt port)
             :user (clean-string (get cfg-section "user"))
             :passwd (clean-string (get cfg-section "passwd"))
             :realm (clean-string (get cfg-section "realm"))}
            (error-msg "Missing required variables in config file 'host'/'port'.")))))
    (catch FileNotFoundException e (error-msg e))))

(defn- build-app
  [options]
  (let [user (:user options)
        passwd (:passwd options)
        app (if (and user passwd)
              (wrap-basic-authentication web/app
                                         (authenticated? user passwd)
                                         (:realm options "FOP Server"))
              web/app)]
    app))

(defn- run-server
  "Initialize the fops server from the cmdline options."
  [opts]
  (if-let
      [options
       (if-let [config (get-in opts [:options :config])]
         (options-from-config config)
         (:options opts))]
    (run-jetty  (build-app options)
                {:port (:port options)
                 :host (:host options)})
    (System/exit 1)))

(defn- show-cli-errors-and-exit
  "Display the cli errors on stderr and then exit with error code 1.
  The errors  include the initial option validator."
  [opts]
  (error-msg  "Errors: "
              (apply str (:errors opts))
              banner
              (:summary opts))
  (System/exit 1))

(defn- show-help
  [opts]
  (println banner "\n" (:summary opts)))

(defn -main
  "Entry point of the standalone mode of fops."
  [& args]
  (let [opts (parse-opts args cli-options)]
    (cond
      (get-in opts [:options :help]) (show-help opts)
      (empty? (:errors opts)) (run-server opts)
      :else (show-cli-errors-and-exit opts))))
