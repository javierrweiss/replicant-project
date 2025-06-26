(ns guis.fligths-test-with-clara
  (:require [clojure.test :refer [deftest testing is run-tests run-test]]
            [guis.flights-with-clara :as flights]
            [tick.core :as t]
            [lookup.core :as lookup]))

;; La idea es que los mismos test que pasan en flights_tests.cljc pasen acá, ya que sólo varía la implementación

(deftest get-form-state-test
  #_(testing "Defaults to one-way flight"
    (is (= (-> (flights/get-form-state {})
               ::flights/type)
           :one-way)))
  #_(testing "Uses selected flight type"
    (is (= (-> (flights/get-form-state {::flights/type :roundtrip})
               ::flights/type)
           :roundtrip)))
  #_(testing "Defaults to today for departure date"
    (is (= (-> (flights/get-form-state {:now (t/date "2025-07-05")})
               ::flights/departure-date
               :value)
           #time/date "2025-07-05")))
  #_(testing "Uses entered departure date"
    (is (= (-> (flights/get-form-state {::flights/departure-date #time/date "2025-12-01"})
               ::flights/departure-date
               :value)
           #time/date "2025-12-01")))
  #_(testing "Marks invalid departure date"
    (is (false? (-> (flights/get-form-state {::flights/departure-date "2025-12"})
                    ::flights/departure-date
                    :valid?))))
  #_(testing "Defaults to today for return date"
    (is (= (-> (flights/get-form-state {:now (t/date "2025-07-05")})
               ::flights/return-date
               :value)
           #time/date "2025-07-05")))
  #_(testing "Uses entered return date"
    (is (= (-> (flights/get-form-state {::flights/departure-date "2025-11-01"
                                        ::flights/return-date #time/date "2025-12-01"
                                        ::flights/type :roundtrip})
               ::flights/return-date
               :value)
           #time/date "2025-12-01")))
  #_(testing "Defaults return date to selected departure date"
    (is (= (-> {:now #time/date  "2025-10-10"
                ::flights/type :roundtrip
                ::flights/departure-date #time/date "2025-12-01"}
               flights/get-form-state
               ::flights/return-date
               :value)
           #time/date "2025-12-01")))
  #_(testing "Disables button when departure date is invalid"
    (is (false? (-> (flights/get-form-state {::flights/departure-date "2025-12"})
                    ::flights/button
                    :enabled?))))
  #_(testing "Defaults return date to disabled"
    (is (false? (-> {:now #time/date "2025-05-18"}
                    flights/get-form-state
                    ::flights/return-date
                    :enabled?))))
  #_(testing "Return date is enabled if and only if flight type is roundtrip"
    (is (true? (-> {:now #time/date "2025-05-18"
                    ::flights/type :roundtrip}
                   flights/get-form-state
                   ::flights/return-date
                   :enabled?))))
  #_(testing "Marks invalid return date"
    (is (false? (-> (flights/get-form-state {::flights/departure-date #time/date "2025-12-01"
                                             ::flights/type :roundtrip
                                             ::flights/return-date "2025-12"})
                    ::flights/return-date
                    :valid?))))
  #_(testing "Disables button when return date is invalid"
    (is (false? (-> (flights/get-form-state {::flights/return-date "2025-12"})
                    ::flights/button
                    :enabled?))))
  #_(testing "Does not mark invalid return date for one-way flights"
    (is (nil? (-> {::flights/return-date "2025-12"
                   ::flights/type :one-way}
                  flights/get-form-state
                  ::flights/return-date
                  :valid?))))
  (testing "Disables button when return date is before departure date"
    (is (false? (-> (flights/get-form-state {::flights/type :roundtrip
                                             ::flights/return-date "2025-12-01"
                                             ::flights/departure-date "2025-12-12"})
                    ::flights/button
                    :enabled?)))))

(deftest render-form-test
  (testing "Takes user input on flight type select"
    (is (= (->> (flights/render-form {})
                (lookup/select-one :select)
                lookup/attrs
                :on
                :input)
           [[:action/assoc-in [::flights/type] :event.target/value-as-keyword]])))
  (testing "Takes user input on departure date"
    (is (= (->> (flights/render-form {})
                (lookup/select-one "input[name=departure-date]")
                lookup/attrs
                :on
                :input)
           [[:action/assoc-in [::flights/departure-date] :event.target/value]])))
  (testing "Marks invalid departure date"
    (is  (->> (flights/render-form {::flights/departure-date {:valid false}})
              (lookup/select-one "input[name=departure-date].input-error"))))
  (testing "Takes user input on return date"
    (is (= (->> (flights/render-form {})
                (lookup/select-one "input[name=return-date]")
                lookup/attrs
                :on
                :change)
           [[:action/assoc-in [::flights/return-date] :event.target/value]])))
  (testing "Marks invalid return date"
    (is  (->> (flights/render-form {::flights/return-date {:valid false}})
              (lookup/select-one "input[name=return-date].input-error"))))
  (testing "Clicking button books flight"
    (is (= (->> (flights/render-form {})
                (lookup/select-one :button)
                lookup/attrs
                :on
                :click)
           [[:action/assoc-in [::flights/booked?] true]]))))


(comment
  
  (run-tests)

  (run-test get-form-state-test)
  
  :rcf)