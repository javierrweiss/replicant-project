(ns gui.core
  (:require [replicant.dom :as r]
            [gui.counter :as counter]
            [gui.layout :as layout]))

(def views
  [{:id :counter
    :text "Counter"}
   {:id :temperatures
    :text "Temperatures"}])

(defn get-current-view
  [state]
  (:current-view state))

(defn render-ui
  [state]
  (let [current-view (get-current-view state)]
    [:div.m-8
     (layout/tab-bar current-view views)
     (case current-view
       :counter (counter/counter-ui state)
       [:h1.text-lg "No disponible"])]))

(defn process-effect 
  [store [effect & args]]
  (case effect
    :effect/assoc-in (apply swap! store assoc-in args)))

(defn perform-actions 
  [state event-data]
  (mapcat 
   (fn [action]
     (prn (first action) (rest action))
     (or (counter/perform-action state action)
         (case (first action)
           :action/assoc-in
           [(into [:effect/assoc-in] (rest action))]
           (prn "Acción desconocida"))))
   event-data))

(defn init [store]
  (add-watch store ::render (fn [_ _ _ new-state]
                              (r/render js/document.body (render-ui new-state))))
  (r/set-dispatch! 
   (fn [_ event-data]
     (->> (perform-actions @store event-data)
          (run! #(process-effect store %)))))
  
  (swap! store assoc ::loaded-at (.getTime (js/Date.))))