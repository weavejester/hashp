(ns hashp.install
  (:require [clj-stacktrace.core :as stacktrace]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [puget.printer :as puget]
            [puget.color.ansi :as color]))

(def default-options
  {:color?    (str/blank? (System/getenv "NO_COLOR"))
   :tag       'p
   :disabled? false
   :template  (str (color/sgr "#{tag}" :yellow)
                   (color/sgr "[{ns}/{fn}:{line}] " :white)
                   "{form} ⇒ {value}")
   :writer    *err*})

(defonce ^:dynamic *options* default-options)

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
  (let [print-opts (if (:color? *options*)
                     color-print-opts
                     no-color-print-opts)
        template   (if (:color? *options*)
                     (:template *options*)
                     (color/strip (:template *options*)))
        param-map  (merge {:form  (puget/pprint-str form print-opts)
                           :value (puget/pprint-str value print-opts)
                           :tag   (:tag *options*)}
                          (first (filter :clojure trace)))]
    (locking lock
      (binding [*out* (:writer *options*)]
        (println (from-template template param-map))))))

(def ^:dynamic *env* {})

(defmacro local-eval [form]
  `(binding [*ns*  (find-ns '~(ns-name *ns*))
             *env* ~(reduce #(assoc %1 `'~%2 %2) {} (keys &env))]
     (eval '(let [~@(mapcat #(list % `(get *env* '~%)) (keys &env))]
              ~form))))

(defn- p-form [form orig-form]
  `(let [result# (local-eval ~form)]
     (print-log (current-stacktrace) '~orig-form result#)
     result#))

(defn hashp [form]
  (if (:disabled? *options*)
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

(defn uninstall! []
  (let [tag (:tag *options*)]
    (alter-var-root #'*data-readers* dissoc tag)
    (when (thread-bound? #'*data-readers*)
      (set! *data-readers* (dissoc *data-readers* tag)))))

(defn install!
  ([] (install! {}))
  ([& {:as options}]
   (uninstall!)
   (let [options (merge default-options options)
         tag     (:tag options)]
     (alter-var-root #'*options* (constantly options))
     (alter-var-root #'*data-readers* assoc tag #'hashp)
     (when (thread-bound? #'*data-readers*)
       (set! *data-readers* (assoc *data-readers* tag #'hashp))))))
