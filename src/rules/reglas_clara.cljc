(ns rules.reglas-clara
  #?(:cljs (:require-macros [clara.macros :refer [defrule defquery defsession]]))
  (:require [clara.rules :refer [mk-session defquery defrule insert! insert fire-rules query]]
            [guis.flights-with-clara :as flights-clara]
            [clara.tools.inspect :as inspect]
            [clara.tools.fact-graph :as f]))

(defrule si-departure-date-es-menor-a-return-date-boton-habilitado
  [?dep <- ::flights-clara/departure-date]
  [?ret <- ::flights-clara/return-date]
  [:test (< (::flights-clara/departure-date ?dep) (::flights-clara/return-date ?ret))]
  =>
  (insert! {::flights-clara/button {:enabled true}}))

(defrule si-departure-date-es-mayor-a-return-date-boton-deshabilitado
  [?dep <- ::flights-clara/departure-date]
  [?ret <- ::flights-clara/return-date]
  [:test (> (::flights-clara/departure-date ?dep) (::flights-clara/return-date ?ret))]
  =>
  (insert! {::flights-clara/button {:enabled false}}))

(defquery obtener-boton
  []
  [?btn <- ::flights-clara/button])


(comment
  
  (defsession sesion 'rules.reglas-clara :fact-type-fn (fn [arg]
                                                         (println arg)
                                                         (let [arg (keys arg)]
                                                           (println arg)
                                                           (some #{::flights-clara/type
                                                                   ::flights-clara/departure-date
                                                                   ::flights-clara/return-date
                                                                   ::flights-clara/button
                                                                   ::flights-clara/booked?} arg))))
  (-> sesion
      (insert {::flights-clara/departure-date 10})
      (insert {::flights-clara/return-date 8})
      (fire-rules)
      (query obtener-boton)
      #_inspect/explain-activations
      #_f/session->fact-graph)
  
  
  (-> sesion
      (insert {::flights-clara/departure-date 10})
      (insert {::flights-clara/return-date 18})
      (fire-rules)
      (query obtener-boton)
      #_inspect/explain-activations
      #_f/session->fact-graph)
  
  (-> sesion
      (insert {::flights-clara/departure-date 10})
      (fire-rules)
      (query obtener-boton)
      #_inspect/explain-activations
      #_f/session->fact-graph) 
  
  
  
  :rcf)
 
(comment
  (defrecord Departure-date [date]) 
  (defrecord Return-date [date]) 
  (defrecord Boton [enabled])
  
  (defrule Departure-date-es-menor-a-Return-date-Boton-esta-habilitado
    [Departure-date (= ?departure-date date)]
    [Return-date (= ?return-date date)]
    [:test (< ?departure-date ?return-date)]
    =>
    (insert! (->Boton true))) 

  (defrule Departure-date-es-mayor-a-Return-date-Boton-esta-deshabilitado
    [Departure-date (= ?departure-date date)]
    [Return-date (= ?return-date date)]
    [:test (> ?departure-date ?return-date)]
    =>
    (insert! (->Boton false)))
  
  (defquery estado-boton
    []
    [?boton <- Boton])
  
  (defsession x 'rules.reglas-clara)
  
  (def sesion-actual (atom x))
  
  (-> x
      (insert (->Departure-date 30))
      (insert (->Return-date 20))
      (fire-rules)
      (query estado-boton))
  
  (swap! sesion-actual #(-> %
                            (insert (->Departure-date 18))
                            (insert (->Return-date 8))
                            (fire-rules)))
  
  (query @sesion-actual estado-boton)
  (query @sesion-actual obtener-tipo)

  (inspect/explain-activations @sesion-actual)
  (f/session->fact-graph @sesion-actual)
  :rcf)

