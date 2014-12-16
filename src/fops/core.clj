(ns fops.core
  (:use ring.adapter.jetty
        fops.web)
  (:import (java.io FileNotFoundException))
  (:require [fops.web :as web])
  (:require [clojure.string :as string])
  (:require [com.brainbot.iniconfig :as iniconfig])
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [ring.middleware.basic-authentication :refer [wrap-basic-authentication]])
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


(defn- error-msg
  "Display the msgs and on stderr one by line."
  [& msgs]
  (binding [*out* *err*]
    (println (string/join "\n" (map str msgs)))))

(defn- options-from-config
  "Read the configuration file with 'ini' syntax.
  It will look for:
       [fops]
         host = HOST
         port = PORT
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
             :passwd (clean-string (get cfg-section "passwd"))}
            (error-msg "Missing required variables in config file 'host'/'port'.")))))
    (catch FileNotFoundException e (error-msg e))))


(defn- run-server
  "Initialize the fops server from the cmdline options."
  [opts]
  (if-let [options
           (if-let [config  (get-in opts [:options :config])]
             (options-from-config config)
             (:options opts))]
    (let [user (:user options)
          passwd (:passwd options)
          app (if (and user passwd)
                (wrap-basic-authentication web/app (authenticated? user passwd))
                web/app)]
      (run-jetty  app {:port (:port options)
                       :host (:host options)}))
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
  "Entry point of the stand alone mode of fops."
  [& args]
  (let [opts (parse-opts args cli-options)]
    (cond
      (get-in opts [:options :help]) (show-help opts)
      (empty? (:errors opts)) (run-server opts)
      :else (show-cli-errors-and-exit opts))))
