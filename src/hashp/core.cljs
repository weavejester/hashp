(ns hashp.core
  (:require [zprint.core :as zprint])
  (:require-macros hashp.core))

(def prefix "#p")

(def print-opts {:color? true
                 :map {:lift-ns? true}
                 :color-map {:nil :blue :none :blue}})
