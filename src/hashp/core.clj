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

(def print-opts
  (merge puget/*options*
         {:print-color    true
          :namespace-maps true
          :color-scheme
          {:nil [:bold :blue]}}))

(defn p* [form]
  (let [orig-form (walk/postwalk hide-p-form form)]
    `(let [~result-sym ~form]
       (locking lock
         (println
          (str (color/sgr "#p" :red)
               (color/sgr (trace-str (current-stacktrace)) :green) " "
               (when-not (= ~result-sym '~orig-form)
                 (str (puget/pprint-str '~orig-form print-opts) " => "))
               (puget/pprint-str ~result-sym print-opts)))
         ~result-sym))))
