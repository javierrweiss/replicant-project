(ns gui.counter)

(defn perform-action
  [state [action]]
  (condp = action
    ::inc-number  [[:effect/assoc-in [:number] (inc (:number state))]]
    ::reset [[:effect/assoc-in [:number] 0]]
    nil))

(defn counter-ui
  [state]
  [:div
   [:h1.text-lg "Contador"]
   [:div.m-2 "El número es " (:number state)]
   [:div.flex.gap-4.items-center 
    [:button.btn {:on {:click [[::inc-number]]}}
     "Contar!"]
    [:button.btn {:on {:click [[::reset]]}}
     "Resetear!"]]])