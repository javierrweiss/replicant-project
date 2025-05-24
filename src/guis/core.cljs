(ns guis.core
  (:require [replicant.dom :as r]
            [guis.counter :as counter]
            [guis.layout :as layout]
            [guis.temperature :as temperature]
            [guis.flights :as flights]
            [clojure.walk :as walk]
            [tick.core :as t]))

(def views
  [{:id :counter
    :text "Counter"}
   {:id :temperatures
    :text "Temperatures"}
   {:id :flights
    :text "Flights"}])

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
       :temperatures (temperature/temperature-ui state)
       :flights (flights/flights-ui state)
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
         (temperature/perform-action state action)
         (case (first action)
           :action/assoc-in
           [(into [:effect/assoc-in] (rest action))]
           (prn "Acción desconocida"))))
   event-data))

(defn interpolate
  [event data]
  (walk/postwalk 
   (fn [x]
     (case x
       :event.target/value (some-> event .-target .-value)
       :event.target/value-as-number (some-> event .-target .-valueAsNumber)
       :event.target/value-as-keyword (some-> event .-target .-value keyword)
       x))
   data))

(defn init [store]
  (add-watch store ::render (fn [_ _ _ new-state]
                              (r/render js/document.body (render-ui (assoc new-state :now (t/date))))))

  (r/set-dispatch!
   (fn [{:replicant/keys [dom-event]} event-data]
     (js/console.log dom-event)
     (->> (interpolate dom-event event-data)
          (perform-actions @store)
          (run! #(process-effect store %)))))

  (swap! store assoc ::loaded-at (.getTime (js/Date.))))