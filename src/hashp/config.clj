(ns hashp.config
  (:require [clojure.string :as str]))

(def ^:dynamic *disable-hashp* false)

(def ^:dynamic *disable-color*
  (not (str/blank? (System/getenv "NO_COLOR"))))
