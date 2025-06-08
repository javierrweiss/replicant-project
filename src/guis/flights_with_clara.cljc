(ns guis.flights-with-clara
  #?(:cljs (:require-macros [clara.macros :refer [defsession]]))
  (:require [clara.rules :refer [insert fire-rules query]]))

(defsession sesion 'rules.reglas-clara :fact-type-fn (fn [arg]
                                                       (println arg)
                                                       (let [arg (keys arg)]
                                                         (println arg)
                                                         (some #{::type
                                                                 ::departure-date
                                                                 ::return-date
                                                                 ::button
                                                                 ::booked?} arg))))
(defn get-form-state
  [state]
  (let [s (atom sesion)
        _ (doseq [[k v] state] (swap! s insert {k v}))
        boton (some-> (-> @s
                          fire-rules
                          (query rules.reglas-clara/obtener-boton))
                      first
                      :?btn)]
    boton
    #_{::type flight-type
     ::departure-date departure-date
     ::return-date return-date
     ::button {:enabled? button-enabled?}}))

(defn render-form
  [{}])

(comment
  (let [m {::type :one-way
           ::departure-date 2
           ::return-date 43}]
    (get-form-state m))
  
  )