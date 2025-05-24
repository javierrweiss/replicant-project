(ns guis.flights
  (:require [tick.core :as t]))

(defn get-form-state [state]
  (let [flight-type (if-let [t (::type state)]
                      t
                      :one-way)
        departure-date #?(:clj
                          (try
                            (if-let [d (::departure-date state)]
                              {:value (t/date d)
                               :valid? true}
                              {:value (t/date (:now state))
                               :valid? true})
                            (catch Exception e {:valid? false
                                                :value (::departure-date state)
                                                :error e}))
                          :cljs
                          (try
                            (if-let [d (::departure-date state)]
                              {:value (t/date d)
                               :valid? true}
                              {:value (t/date (:now state))
                               :valid? true})
                            (catch js/Error e {:valid? false
                                               :value (::departure-date state)
                                               :error e})))
        return-date #?(:clj
                       (try
                         (let [now (when (:now state) (t/date (:now state)))
                               return-date (when (::return-date state) (t/date (::return-date state)))]
                           (cond
                             (= :one-way flight-type) {:value (:now state) :enabled? false :valid? true}
                             (= :roundtrip flight-type) {:value (or return-date (:value departure-date) now) :enabled? true :valid? true}
                             :else (throw (ex-info "Estado ilegal" {:estado state}))))
                         (catch Exception e {:valid? (when (= :roundtrip flight-type) false)
                                             :value (::return-date state)
                                             :error e}))
                       :cljs
                       (try
                         (let [now (when (:now state) (t/date (:now state)))
                               return-date (when (::return-date state) (t/date (::return-date state)))]
                           (cond
                             (= :one-way flight-type) {:value (:now state) :enabled? false :valid? true}
                             (= :roundtrip flight-type) {:value (or return-date (:value departure-date) now) :enabled? true :valid? true}
                             :else (throw (ex-info "Estado ilegal" {:estado state}))))
                         (catch js/Error e {:valid? (when (= :roundtrip flight-type) false)
                                            :value (::return-date state)
                                            :error e
                                            :enabled? true})))
        button-enabled? (let [both-valid (and (:valid? departure-date) (:valid? return-date) (:enabled? return-date))
                              no-contradiction (when both-valid (t/> (:value return-date) (:value departure-date)))]
                          (cond
                            (and (= :one-way flight-type) (:valid? departure-date)) true
                            (and (= :roundtrip flight-type) both-valid no-contradiction) true
                            :else false))]
    {::type flight-type
     ::departure-date departure-date
     ::return-date return-date
     ::button {:enabled? button-enabled?}}))
  
(defn perform-action
  [state [action & args]])

(defn render-form [form-state]
  (prn form-state)
  [:form.flex.flex-col.max-w-48.gap-4
   [:select.select 
    {:on 
     {:input [[:action/assoc-in [::type] :event.target/value-as-keyword]]}}
    [:option {:value "one-way"} "One-way"]
    [:option {:value "roundtrip"} "Roundtrip"]]
   [:input.input
    (cond-> 
     {:type "text"
      :name "departure-date"
      :value (-> form-state ::departure-date :value)
      :on
      {:input [[:action/assoc-in [::departure-date] :event.target/value]]}}
      (not (-> form-state ::departure-date :valid)) (assoc :class :input-error))]
   [:input.input
    (cond-> {:type "text"
             :name "return-date"
             :value (-> form-state ::return-date :value)
             :on
             {:change [[:action/assoc-in [::return-date] :event.target/value]]}
             :disabled (not (:enabled? (::return-date form-state)))}
      (not (-> form-state ::return-date :valid)) (assoc :class :input-error))]
   [:button.btn
    {:disabled (not (:enabled? (::button form-state)))
     :on {:click [[:action/assoc-in [::booked?] true]]}}
    "Reservar"]])

(defn render-receipt
  [state]
  [:div
   [:p (str "Has reservado un vuelo " (name (::type state)) " para el " (:value (::departure-date state)) 
            (when (= :roundtrip (::type state))
              (str ", con fecha de regreso para el " (:value (::return-date state))))
            ".")]
   [:button.btn.m-2
    {:on {:click [[:action/assoc-in [::booked?] false]]}}
    "Volver"]])

(defn flights-ui
  [state]
  [:div 
   [:h1.text-lg.m-2 "Vuelos"]
   (if (::booked? state)
     (render-receipt (get-form-state state))
     (render-form (get-form-state state)))])


(comment

  (format-date (java.time.Instant/now))


  (let [state {}]
    (cond-> {}
      (::type state) (assoc ::type (::type state))
      (not (::type state)) (assoc ::type :one-way)
      (::departure-date state) (assoc ::departure-date (::departure-date state))
      (and (:now state) (not (::departure-date state))) (assoc ::departure-date (format-date (:now state)))))

  ;; WTF!!
  (format-date #inst "2025-05-07") ;=> "2025-07-04"
  
  (apply str ((juxt t/year t/month t/day-of-month) (t/instant)))

  (t/day-of-month (t/instant #inst "2025-05-07T10:00:00.00Z"))

  (let [f (fn [a] (assoc a :a 1))] 
    (cond-> {}
      true f))
  
  #?(:clj (try
          (throw (ex-info "Exception loca" {:m 'm}))
          (catch Exception e {:a 1})))
   
 
  :rfc
  )