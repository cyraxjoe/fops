(ns fops.utils
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn error-msg
  "Display the msgs and on stderr one by line."
  [& msgs]
  (binding [*out* *err*]
    (println (string/join "\n" (map str msgs)))))

(defn delete-in-future
  [file-path
   &{:keys [timeout]
     :or {timeout 5}}]
  (future
    (Thread/sleep (* timeout 1000))
    (io/delete-file file-path)))
