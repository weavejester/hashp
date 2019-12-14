(ns hashp.core
  (:require [clj-stacktrace.core :as stacktrace]
            [clojure.walk :as walk]
            [puget.printer :as puget]
            [puget.color.ansi :as color]))

(defn current-stacktrace []
  (->> (.getStackTrace (Thread/currentThread))
       (drop 3)
       (stacktrace/parse-trace-elems)))

(defn trace-str [trace]
  (when-let [t (first (filter :clojure trace))]
    (str "[" (:ns t) "/" (:fn t) ":" (:line t) "]")))

(def result-sym (gensym "result"))

(defn- hide-p-form [form]
  (if (and (seq? form)
           (vector? (second form))
           (= (-> form second first) result-sym))
    (-> form second second)
    form))

(def lock (Object.))

(def prefix (color/sgr "#p" :red))

(defn p* [form]
  (let [orig-form (walk/postwalk hide-p-form form)]
    `(let [~result-sym ~form]
       (locking lock
         (println
          (str prefix
               (color/sgr (trace-str (current-stacktrace)) :green) " "
               (when-not (= ~result-sym '~orig-form)
                 (str (puget/cprint-str '~orig-form) " => "))
               (puget/cprint-str ~result-sym)))
         ~result-sym))))
