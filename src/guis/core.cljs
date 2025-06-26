(ns guis.core
  (:require [replicant.dom :as r]
            [guis.counter :as counter]
            [guis.layout :as layout]
            [guis.temperature :as temperature]
            [guis.flights :as flights]
            [guis.timer :as timer]
            [clojure.walk :as walk]
            [tick.core :as t]))

(def views
  [{:id :counter
    :text "Counter"}
   {:id :temperatures
    :text "Temperatures"}
   {:id :flights
    :text "Flights"}
   {:id :timer
    :text "Timer"
    :on-load-actions timer/on-load}])

(def id->view (into {} (map (juxt :id identity) views)))

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
       :timer (timer/timer-ui state)
       [:h1.text-lg "No disponible"])]))

(defn process-effect 
  [store [effect & args] {:keys [handle-actions]}]
  (case effect
    :effect/assoc-in (apply swap! store assoc-in args)
    :effect/schedule (let [[ms actions] args]
                       (js/setTimeout #(handle-actions actions) ms))))

(defn perform-actions 
  [state event-data]
  (mapcat 
   (fn [action]
     #_(prn (first action) (rest action))
     (or (counter/perform-action state action)
         (temperature/perform-action state action)
         (timer/perform-action state action)
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
       :clock/now (t/date-time)
       x))
   data))

(defn handle-actions
  [store dom-event actions]
  (let [handle-actions* (partial handle-actions store dom-event)]
    (->> (interpolate dom-event actions)
         (perform-actions (assoc @store :now (t/date-time)))
         (run! #(process-effect store % {:handle-actions handle-actions*})))))

(defn trigger-on-load
  [store old-state new-state]
  (let [new-view (get-current-view new-state)]
    (when-not (= (get-current-view old-state) new-view)
      (when-let [actions (get-in id->view [new-view :on-load-actions])]
        (handle-actions store nil actions)))))

(defn init [store]
  (add-watch store ::render (fn [_ _ old-state new-state]
                              (trigger-on-load store old-state new-state)
                              (r/render js/document.body (render-ui (assoc new-state :now (t/date-time))))))

  (r/set-dispatch!
   (fn [{:replicant/keys [dom-event]} event-data]
     (handle-actions store dom-event event-data)))

  (swap! store assoc ::loaded-at (t/date-time)))