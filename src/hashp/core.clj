(ns hashp.core
  (:require [clj-stacktrace.core :as stacktrace]
            [clojure.walk :as walk]
            [net.cgrand.macrovich :as macrovich]
            [puget.printer :as puget]
            [puget.color.ansi :as color]
            [zprint.core :as zprint]))

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

(def print-opts
  (merge puget/*options*
         {:print-color    true
          :namespace-maps true
          :color-scheme
          {:nil [:bold :blue]}}))

(defn p* [form]
  (let [orig-form (walk/postwalk hide-p-form form)]
    `(let [~result-sym ~form]
       (macrovich/case
         :clj (locking lock
                (println
                 (str prefix
                      (color/sgr (trace-str (current-stacktrace)) :green) " "
                      (when-not (= ~result-sym '~orig-form)
                        (str (puget/pprint-str '~orig-form print-opts) " => "))
                      (puget/pprint-str ~result-sym print-opts)))
                ~result-sym)
         :cljs (do
                 (println
                  (str prefix " "
                       (when-not (= ~result-sym '~orig-form)
                         (str (zprint/zprint-str '~orig-form print-opts) " => "))
                       (zprint/zprint-str ~result-sym print-opts)))
                 ~result-sym)))))

(def t-prefix "#t")

(defn t* [form]
  (let [orig-form (walk/postwalk hide-p-form form)]
    `(let [~result-sym ~form]
       (macrovich/case
        :clj (locking lock
               (tap> [(str t-prefix (trace-str (current-stacktrace)))
                      (when-not (= ~result-sym '~orig-form)
                        '~orig-form)
                      :=>
                      ~result-sym])
               ~result-sym)
        :cljs (do
                (tap> [t-prefix
                       (when-not (= ~result-sym '~orig-form)
                         '~orig-form)
                       :=>
                       ~result-sym])
                ~result-sym)))))
