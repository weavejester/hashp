(ns hashp.preload
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

(defn print-log [trace form value]
  (locking lock
    (println
     (str (color/sgr "#p" :red)
          (color/sgr (trace-str trace) :green) " "
          (when-not (= value form)
            (str (puget/pprint-str form print-opts) " => "))
          (puget/pprint-str value print-opts)))))

(defn p* [form]
  (let [orig-form (walk/postwalk hide-p-form form)]
    `(let [~result-sym ~form]
       (print-log (current-stacktrace) '~orig-form ~result-sym)
       ~result-sym)))
