(ns example.core)

(defn mean [xs]
  (/ (double #p (reduce + xs)) #p (count xs)))
