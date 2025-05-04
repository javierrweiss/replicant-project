(ns guis.temperature)

(defn fahrenheit->celsius
  [temp]
  (* (- temp 32) (/ 5 9)))

(defn celsius->fahrenheit
  [temp]
  (+ (* temp (/ 9 5)) 32))

(defn set-temperature
  [{:keys [fahrenheit celsius]}]
  [[:effect/assoc-in [:celsius] (or celsius (fahrenheit->celsius fahrenheit))]
   [:effect/assoc-in [:fahrenheit] (or fahrenheit (celsius->fahrenheit celsius))]])

(defn perform-action [_ [action & args]]
  (when (= ::set-temperature action)
    (set-temperature (first args))))

(defn temperature-ui
  [state]
  [:div
   [:h1.text-lg.mb-4 "Conversión de temperaturas"]
   [:div.flex.gap-8
    [:div.flex.gap-4.items-center
     [:input.input.w-14 {:type "number"
                         :id "celsius"
                         :on {:input [[::set-temperature {:celsius :event.target/value-as-number}]]}
                         :value (:celsius state)}]
     [:label {:for "celsius"} "Celsius"]]
    [:div.flex.gap-4.items-center
     [:input.input.w-14 {:type "number"
                         :id "fahrenheit"
                         :on {:input [[::set-temperature {:fahrenheit :event.target/value-as-number}]]}
                         :value (:fahrenheit state)}]
     [:label {:for "fahrenheit"} "Fahrenheit"]]]])