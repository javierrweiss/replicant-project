(ns guis.flights-tests
  (:require [clojure.test :refer [deftest testing is run-tests run-test]]
            [guis.flights :as flights]
            [tick.core :as t]))

(deftest get-form-state-test
  (testing "Defaults to one-way flight"
    (is (= (-> (flights/get-form-state {})
               ::flights/type)
           :one-way)))
  (testing "Uses selected flight type"
    (is (= (-> (flights/get-form-state {::flights/type :roundtrip})
               ::flights/type)
           :roundtrip)))
  (testing "Defaults to today for departure date"
    (is (= (-> (flights/get-form-state {:now (t/date "2025-07-05")})
               ::flights/departure-date
               :value)
           #time/date "2025-07-05")))
  (testing "Uses entered departure date"
    (is (= (-> (flights/get-form-state {::flights/departure-date #time/date "2025-12-01"})
               ::flights/departure-date
               :value)
           #time/date "2025-12-01")))
  (testing "Marks invalid departure date"
    (is (false? (-> (flights/get-form-state {::flights/departure-date "2025-12"})
                    ::flights/departure-date
                    :valid?))))
  (testing "Defaults to today for return date"
    (is (= (-> (flights/get-form-state {:now (t/date "2025-07-05")})
               ::flights/return-date
               :value)
           #time/date "2025-07-05")))
  (testing "Uses entered return date"
    (is (= (-> (flights/get-form-state {::flights/return-date #time/date "2025-12-01"
                                        ::flights/type :roundtrip})
               ::flights/return-date
               :value)
           #time/date "2025-12-01")))
  (testing "Defaults return date to selected departure date"
    (is (= (-> {:now #time/date  "2025-10-10"
                ::flights/type :roundtrip
                ::flights/departure-date #time/date "2025-12-01"}
               flights/get-form-state
               ::flights/return-date
               :value)
           #time/date "2025-12-01")))
  (testing "Disables button when departure date is invalid"
    (is (false? (-> (flights/get-form-state {::flights/departure-date "2025-12"})
                    ::flights/button
                    :enabled?))))
  (testing "Defaults return date to disabled"
    (is (false? (-> {:now #time/date "2025-05-18"}
                    flights/get-form-state
                    ::flights/return-date
                    :enabled?))))
  (testing "Return date is enabled if and only if flight type is roundtrip"
    (is (true? (-> {:now #time/date "2025-05-18"
                    ::flights/type :roundtrip}
                   flights/get-form-state
                   ::flights/return-date
                   :enabled?))))
  (testing "Marks invalid return date"
    (is (false? (-> (flights/get-form-state {::flights/return-date "2025-12"})
                    ::flights/return-date
                    :valid?))))
  (testing "Disables button when return date is invalid"
    (is (false? (-> (flights/get-form-state {::flights/return-date "2025-12"})
                    ::flights/button
                    :enabled?))))
  (testing "Does not mark invalid return date for one-way flights"  ;; En realidad no debería aceptar un flights/type :one-way sin departure-date
    (is (nil? (-> {::flights/return-date "2025-12"
                   ::flights/type :one-way}
                  flights/get-form-state
                  ::flights/return-date
                  :valid?)))))
   
(comment
  
  (run-tests)

  (flights/get-form-state {:now #time/date "2025-05-18"
                           ::flights/type :roundtrip})
   
  
  (flights/get-form-state {::flights/return-date "2025-12"
                           ::flights/type :one-way})
  :rcf)