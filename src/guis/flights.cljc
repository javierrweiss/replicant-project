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
                                                :value nil
                                                :error e}))
                          :cljs
                          (try
                            (if-let [d (::departure-date state)]
                              {:value (t/date d)
                               :valid? true}
                              {:value (t/date (:now state))
                               :valid? true})
                            (catch js/Error e {:valid? false
                                               :value nil
                                               :error e})))
        return-date #?(:clj
                       (try
                         (let [now (when (:now state) (t/date (:now state)))
                               return-date (when (::return-date state) (t/date (::return-date state)))]
                           (cond
                             (= :one-way flight-type) {:value (:now state) :enabled? false :valid? true}
                             (= :roundtrip flight-type) {:value (or return-date (:value departure-date) now) :enabled? true :valid? true}
                             :else (throw (ex-info "Estado ilegal" {:estado state}))))
                         (catch Exception e {:valid? false
                                             :value nil
                                             :error e}))
                       :cljs
                       (try
                         (let [now (t/date (:now state))
                               return-date (t/date (::return-date state))]
                           (cond
                             (= :one-way flight-type) {:value (:now state) :enabled? false}
                             (= :roundtrip flight-type) {:value (or return-date (:value departure-date) now) :enabled? true}
                             :else (throw (ex-info "Estado ilegal" {:estado state}))))
                         (catch js/Error e {:valid? false
                                            :value nil
                                            :error e})))]
    {::type flight-type
     ::departure-date departure-date
     ::return-date return-date
     ::button {:enabled? (every? true? [(:valid? departure-date) (:valid? return-date)])}}))

(defn perform-action
  [state [action & args]])

(defn flights-ui
  [state]
  [:h1.text-lg "Vuelos"])


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