(ns gui.core
  (:require [replicant.dom :as r]
            [gui.counter :as counter]))

(defn render-ui
  [state]
  (r/render js/document.body (counter/counter-ui state)))

(defn init [store]
  (add-watch store ::render (fn [_ _ _ new-state]
                              (render-ui new-state)))
  (r/set-dispatch! 
   (fn [_ event-data]
     (doseq [[action & args] event-data]
       (case action
         ::counter/inc-number (swap! store update :number inc)))))
  
  (swap! store assoc ::loaded-at (.getTime (js/Date.))))