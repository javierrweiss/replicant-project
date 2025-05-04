(ns gui.counter)

(defn counter-ui
  [state]
  [:div.m-8
   [:h1.text-lg "Contador"]
   [:div.flex.gap-4.items-center
    [:div "El número es " (:number state)]
    [:button.btn {:on {:click [[::inc-number]]}}
     "Contar!"]]])