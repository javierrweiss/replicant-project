(ns guis.timer
  (:require [tick.core :as t]))

(def on-load [[:action/assoc-in [::started] :clock/now]
              [::tick]])

(defn format-seconds
  [s]
  (let [s10 (int (* 10 s))]
    (if (== 0 (mod s10 10))
      (int (/ s10 10))
      (float (/ s10 10)))))

(defn get-view-state
  [state] 
  (println state)
  (let [duration (or (::duration state) 20)
        elapsed (min
                 (if-let [started (::started state)]
                   (/ 
                    (abs (t/between (:now state) started :millis))
                      1000)
                   0)
                 duration)
        m {:pct (int (* 100 (/ elapsed duration)))
           :elapsed (str (format-seconds elapsed) "s")
           :duration duration}] 
    m))

(defn label 
  [s]
  [:span.w-30.shrink-0.p-4.m-2 s])

(defn timer-ui
  [state]
  #_(println state)
  (let [{:keys [pct duration elapsed]} (get-view-state state)]
    [:div.max-w-96.flex.flex-col.gap-4
     [:h1.text-lg.p-2 "Temporizador"]
     [:div.flex.items-center
      (label "Tiempo trascurrido")
      [:progress.progress {:value pct :max 100}]]
     (label elapsed)
     [:div.flex.items-center.p-2
     (label "Duración: ") 
      [:input.range
       {:type "range"
        :min 0
        :max 100
        :value duration
        :on {:input [[:action/assoc-in [::duration] :event.target/value-as-number]]}}]]
     [:button.btn.max-w-20 
      {:on {:click [[:action/assoc-in [::started] :clock/now]]}}
      "Resetear"]]))

(defn perform-action 
  [state [action & _args]]
  (when (= ::tick action)
    [[:effect/schedule 1000
      [[:action/assoc-in [::last-tick] (:now state)]
       [::tick]]]]))

(comment

  (get-time #inst "2025-01-01T11:12:00")

  (/ (t/between (t/date-time "2025-07-01T10:23:21.56")
                (t/date-time)
                :millis)
     1000)

  (let [state {:now (t/inst)
               ::started (t/inst "2025-12-05T10:11:23")}]
    (min 
     (if-let [started (::started state)]
       (abs (t/between (:now state) started :seconds))
       0)
     20))

  :rcf) 