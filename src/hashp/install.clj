(ns hashp.install
  (:require [clj-stacktrace.core :as stacktrace]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [hashp.config :as config]
            [puget.printer :as puget]
            [puget.color.ansi :as color]))

(def default-template
  (str (color/sgr "#p" :red)
       (color/sgr "[{ns}/{fn}:{line}]" :green)
       " {form} => {value}"))

(defn- from-template [templ param-map]
  (str/replace templ #"\{([^}]+)\}"
               (fn [[_ k]] (str (param-map (keyword k))))))

(defn current-stacktrace []
  (->> (.getStackTrace (Thread/currentThread))
       (drop 3)
       (stacktrace/parse-trace-elems)))

(defn- hide-p-form [form]
  (if (and (seq? form)
           (seq? (first form))
           (= ::undef (second form)))
    (-> form first second second second second)
    form))

(def ^:private lock (Object.))

(def ^:private color-print-opts
  (merge puget/*options*
         {:print-color    true
          :namespace-maps true
          :color-scheme
          {:nil [:bold :blue]}}))

(def ^:private no-color-print-opts
  (assoc color-print-opts :print-color false))

(defn print-log [trace form value]
  (let [print-opts (if config/*disable-color*
                     no-color-print-opts
                     color-print-opts)
        template   (if config/*disable-color*
                     (color/strip default-template)
                     default-template)
        param-map  (merge {:form  (puget/pprint-str form print-opts)
                           :value (puget/pprint-str value print-opts)}
                          (first (filter :clojure trace)))]
    (locking lock
      (binding [*out* config/*hashp-output*]
        (println (from-template template param-map))))))

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
