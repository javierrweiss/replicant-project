(ns guis.dev
  (:require [guis.core :refer [init]]))

(def store (atom {:number 0}))

(defn main
  []
  (init store)
  (println "Loaded!"))

(defn ^:dev/after-load reload
  []
  (init store)
  (println "Reloaded!")) 
