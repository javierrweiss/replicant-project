(ns guis.flights-with-clara 
  (:require [clara.rules :refer [insert fire-rules query]]
            [rules.reglas-clara :refer [sesion obtener-estado]]
            [tick.core :as t]))

(defn get-form-state
  [state]
  (let [s (atom sesion)
        _ (doseq [[k v] state] (swap! s insert {k v}))
        estado (some-> (-> @s
                          fire-rules
                          (query obtener-estado))
                      last)]
    (cond-> {}
      (:?ret estado) (merge (:?ret estado))
      (:?dep estado) (merge (:?dep estado))
      (:?btn estado) (merge (:?btn estado)))
    #_{::type flight-type
     ::departure-date departure-date
     ::return-date return-date
     ::button {:enabled? button-enabled?}}))

(defn render-form
  [{}])

(comment
  (let [m {::type :one-way
           ::departure-date "2025-01-10"
           ::return-date "2025-01-01"}]
    (get-form-state m))

(let [m {::type :one-way
         ::departure-date "2025-01-01"
         ::return-date "2025-01-10"}]
  (get-form-state m))
  
  (let [m {::type :one-way
           ::departure-date "2025-01-99"
           ::return-date "2025-01-10"}]
    (get-form-state m))
  
  :rcf)