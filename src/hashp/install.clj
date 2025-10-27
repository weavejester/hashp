(ns hashp.install
  (:require [clj-stacktrace.core :as stacktrace]
            [clojure.walk :as walk]
            [hashp.config :as config]
            [puget.printer :as puget]
            [puget.color.ansi :as color]))

(defn current-stacktrace []
  (->> (.getStackTrace (Thread/currentThread))
       (drop 3)
       (stacktrace/parse-trace-elems)))

(defn- trace-str [trace]
  (when-let [t (first (filter :clojure trace))]
    (str "[" (:ns t) "/" (:fn t) ":" (:line t) "]")))

(defn- hide-p-form [form]
  (if (and (seq? form)
           (seq? (first form))
           (= ::undef (second form)))
    (-> form first second second second second)
    form))

(def ^:private lock (Object.))

(def ^:private print-opts
  (merge puget/*options*
         {:print-color    true
          :namespace-maps true
          :color-scheme
          {:nil [:bold :blue]}}))

(def ^:private no-color-print-opts
  (assoc print-opts :print-color false))

(defn print-log [trace form value]
  (locking lock
    (binding [*out* config/*hashp-output*]
      (println
       (if config/*disable-color*
         (str "#p" (trace-str trace) " "
              (when-not (= value form)
                (str (puget/pprint-str form no-color-print-opts) " => "))
              (puget/pprint-str value no-color-print-opts))
         (str (color/sgr "#p" :red)
              (color/sgr (trace-str trace) :green) " "
              (when-not (= value form)
                (str (puget/pprint-str form print-opts) " => "))
              (puget/pprint-str value print-opts)))))))

(defn- p-form [form orig-form]
  `(let [result# ~form]
     (print-log (current-stacktrace) '~orig-form result#)
     result#))

(defn hashp [form]
  (if config/*disable-hashp*
    form
    (let [x (gensym "x")
          y (gensym "y")
          orig-form (walk/postwalk hide-p-form form)]
      `((fn
          ([_#] ~(p-form form orig-form))
          ([~x ~y]
           (cond
            (= ::undef ~x) ~(p-form `(->> ~y ~form) orig-form)
            (= ::undef ~y) ~(p-form `(-> ~x ~form) orig-form))))
         ::undef))))

(defn install! []
  (alter-var-root #'*data-readers* assoc 'p #'hashp)
  (when (thread-bound? #'*data-readers*)
    (set! *data-readers* (assoc *data-readers* 'p #'hashp))))

(defn uninstall! []
  (alter-var-root #'*data-readers* dissoc 'p)
    (when (thread-bound? #'*data-readers*)
      (set! *data-readers* (dissoc *data-readers* 'p))))
