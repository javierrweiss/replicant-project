(ns rules.reglas-clara
  #?(:cljs (:require-macros [clara.macros :refer [defrule defquery defsession]]))
  (:require [clara.rules :refer [mk-session defquery defrule insert! insert fire-rules query]]
            [clara.tools.inspect :as inspect]
            [clara.tools.fact-graph :as f]
            [tick.core :as t]
            [clojure.string :as string]))

(defn bisiesto? [anio]
  (or (zero? (mod anio 400))
      (and (zero? (mod anio 4))
           (not (zero? (mod anio 100))))))

(def dias-por-mes
  {1 31, 2 28, 3 31, 4 30, 5 31, 6 30,
   7 31, 8 31, 9 30, 10 31, 11 30, 12 31})

(defn fecha-valida?
  "Recibe un string y devuelve true si representa una fecha en el formato YYYY-MM-dd"
  [fec]
  (if-let [f (when (string? fec) (re-seq #"^\d{4}-\d{2}-\d{2}$" fec))]
    (let [[y m d] #?(:clj (mapv Integer/parseInt (-> f first (string/split #"-")))
                     :cljs (mapv js/Number.parseInt (-> f first (string/split #"-"))))
          max-dia (if (and (= m 2) (bisiesto? y))
                    29
                    (dias-por-mes m))]
      (and
       (<= 1 m 12)
       (<= 1 d max-dia)))
    false))

(defrule inicializar-departure-date
  [?dep-date <- :inicializar-departure-date [dep] (= ?date (:guis.flights-with-clara/departure-date dep))]
  =>
  (insert! (assoc ?dep-date :guis.flights-with-clara/departure-date {:value (t/date ?date) :valid? (fecha-valida? ?date)})))

(defrule inicializar-return-date
  [?ret-date <- :inicializar-return-date [ret] (= ?date (:guis.flights-with-clara/return-date ret))]
  =>
  (insert! (assoc ?ret-date :guis.flights-with-clara/return-date {:value (t/date ?date) :valid? (fecha-valida? ?date)})))

(defrule si-departure-date-es-menor-a-return-date-boton-habilitado
  [:and
   [?d <- :departure-date-inicializada [d] (get-in d [:guis.flights-with-clara/departure-date :valid?])]
   [?r <- :return-date-inicializada [r] (get-in r [:guis.flights-with-clara/return-date :valid?])]]
  [?dep <- :departure-date-inicializada [dep] (= ?dep-val (get-in dep [:guis.flights-with-clara/departure-date :value]))]
  [?ret <- :return-date-inicializada [ret] (= ?ret-val (get-in ret [:guis.flights-with-clara/return-date :value]))]
  [:test (t/< ?dep-val ?ret-val)]
  =>
  (insert! {:guis.flights-with-clara/button {:enabled true}}))

(defrule si-departure-date-es-mayor-a-return-date-boton-deshabilitado
  [:and
   [?d <- :departure-date-inicializada [d] (get-in d [:guis.flights-with-clara/departure-date :valid?])]
   [?r <- :return-date-inicializada [r] (get-in r [:guis.flights-with-clara/return-date :valid?])]]
  [?dep <- :departure-date-inicializada [dep] (= ?dep-val (get-in dep [:guis.flights-with-clara/departure-date :value]))]
  [?ret <- :return-date-inicializada [ret] (= ?ret-val (get-in ret [:guis.flights-with-clara/return-date :value]))]
  [:test (t/> ?dep-val ?ret-val)]
  =>
  (insert! {:guis.flights-with-clara/button {:enabled false}}))

(defquery obtener-boton
  []
  [?btn <- :guis.flights-with-clara/button])

(defquery obtener-departure-date
  []
  [?dep <- :departure-date-inicializada])

(defquery obtener-return-date
  []
  [?ret <- :return-date-inicializada])
 
(defsession sesion 'rules.reglas-clara :fact-type-fn (fn [arg]
                                                       (println arg)
                                                       (let [llaves (keys arg)
                                                             seleccion (some #{:guis.flights-with-clara/type
                                                                               :guis.flights-with-clara/departure-date
                                                                               :guis.flights-with-clara/return-date
                                                                               :guis.flights-with-clara/button
                                                                               :guis.flights-with-clara/booked?} llaves)]
                                                         (println seleccion)
                                                         (cond
                                                           (= seleccion :guis.flights-with-clara/departure-date) (if (map? (seleccion arg))
                                                                                                                   :departure-date-inicializada
                                                                                                                   :inicializar-departure-date)
                                                           (= seleccion :guis.flights-with-clara/return-date) (if (map? (seleccion arg))
                                                                                                                :return-date-inicializada
                                                                                                                :inicializar-return-date)
                                                           :else seleccion))))

(comment
  (time
   (let [s (-> sesion
               (insert {:guis.flights-with-clara/departure-date "2025-10-25"})
               (insert {:guis.flights-with-clara/return-date "2025-08-25"})
               (fire-rules)
               #_inspect/explain-activations
               #_f/session->fact-graph)]
     (reduce
      merge
      (concat
       (query s obtener-departure-date)
       (query s obtener-return-date)
       (query s obtener-boton)))))
  
  (time
   (let [s (-> sesion
               (insert {:guis.flights-with-clara/departure-date "2025-10-25"})
               #_(insert {:guis.flights-with-clara/return-date "2025-08-25"})
               (fire-rules)
               #_inspect/explain-activations
               #_f/session->fact-graph)]
     (reduce
      merge
      (concat
       (query s obtener-departure-date)
       (query s obtener-return-date)
       (query s obtener-boton)))))


  (last (-> sesion
            (insert {:guis.flights-with-clara/departure-date "2025-01-01"})
            #_(insert {:guis.flights-with-clara/return-date "2025-10-01"})
            (fire-rules)
            (query obtener-estado)
            #_inspect/explain-activations
            #_f/session->fact-graph))

  (-> sesion
      (insert {:guis.flights-with-clara/departure-date 10})
      (fire-rules)
      (query obtener-estado)
      #_inspect/explain-activations
      #_f/session->fact-graph)

  (if-let [f (re-seq #"^\d{4}-\d{2}-\d{2}$" "2025-02-31")]
    (let [[_ m d] #?(:clj (mapv Integer/parseInt (-> f first (string/split #"-")))
                     :cljs (mapv js/Number.parseInt (-> f first (string/split #"-"))))]
      (and
       (< m 13)
       (if (== m 2) (< d 29) (< d 32))))
    false)

  (fecha-valida? "2024")
  (fecha-valida? "2024-01")
  (fecha-valida? "2024-10-12")
  (fecha-valida? "2024-01-01-01")

  (dotimes [_ 5]
    (time
     (do (fecha-valida? "2024")
         (fecha-valida? "2024-01")
         (fecha-valida? "2024-10-12")
         (fecha-valida? "2024-01-01-01"))))


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

