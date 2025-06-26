(ns guis.timer-tests
  (:require [clojure.test :refer [deftest is run-test run-tests testing]]
            [guis.timer :as timer]))

(deftest get-view-state-test
  (testing "Uses sane defaults"
    (is (= (timer/get-view-state {})
           {:pct 0
            :elapsed "0s"
            :duration 20})))
  (testing "Uses set duration"
    (is (= (timer/get-view-state {::timer/duration 15})
           {:pct 0
            :elapsed "0s"
            :duration 15})))
  (testing "Calcula tiempo trascurrido"
    (is (= (timer/get-view-state {:now #time/date-time "2025-06-23T12:00:05"
                                  ::timer/started #time/date-time "2025-06-23T12:00:00"
                                  ::timer/duration 10})
           {:pct 50
            :elapsed "5s"
            :duration 10})))
  (testing "Calcula tiempo trascurrido en decimas de segundo"
    (is (= (timer/get-view-state {:now #time/date-time "2025-06-23T12:00:05.123"
                                  ::timer/started #time/date-time "2025-06-23T12:00:00.456"
                                  ::timer/duration 10})
           {:pct 46
            :elapsed "4.6s"
            :duration 10})))
  (testing "Se detiene cuando el tiempo trascurrido es igual a duración"
    (is (= (timer/get-view-state {:now #time/date-time "2025-06-23T12:01:05.123"
                                  ::timer/started #time/date-time "2025-06-23T12:00:00.456"
                                  ::timer/duration 10})
           {:pct 100
            :elapsed "10s"
            :duration 10}))))


(comment
  (run-test get-view-state-test)
  :rcf ) 