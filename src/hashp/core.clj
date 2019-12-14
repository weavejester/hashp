(ns hashp.core
  (:require [puget.printer :as puget]
            [puget.color.ansi :as color]
            [clj-stacktrace.core :as stacktrace]))

(def lock (Object.))

(def prefix (color/sgr "#p" :red))

(defn current-stacktrace []
  (->> (.getStackTrace (Thread/currentThread))
       (drop 3)
       (stacktrace/parse-trace-elems)))

(defn trace-str [trace]
  (when-let [t (first (filter :clojure trace))]
    (str "[" (:ns t) "/" (:fn t) ":" (:line t) "]")))

(defn p* [form]
  `(let [result# ~form]
     (locking lock
       (println
        (str prefix
             (color/sgr (trace-str (current-stacktrace)) :green) " "
             (when-not (= result# '~form)
               (str (puget/cprint-str '~form) " => "))
             (puget/cprint-str result#)))
       result#)))
