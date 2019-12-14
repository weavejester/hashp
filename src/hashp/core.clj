(ns hashp.core
  (:require [puget.printer :as puget]
            [clj-stacktrace.core :as stacktrace]))

(def lock (Object.))

(defn p* [form]
  `(let [result# ~form]
     (locking lock
       (println (puget/cprint-str '~form) "=>" (puget/cprint-str result#))
       result#)))
