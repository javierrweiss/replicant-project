(ns rules.rules
  (:require [rules.reglas-clara :as rclara]
            [clojure.test :refer [deftest is testing run-test]]))

(deftest fecha-valida?-test
  (testing "Fechas válidas"
    (is (rclara/fecha-valida? "2024-01-01"))
    (is (rclara/fecha-valida? "1999-12-31"))
    (is (rclara/fecha-valida? "2020-02-28"))
    (is (rclara/fecha-valida? "2024-02-29"))) 
  
  (testing "Fechas inválidas por formato"
    (is (not (rclara/fecha-valida? "2024/01/01")))
    (is (not (rclara/fecha-valida? "2024-1-1")))
    (is (not (rclara/fecha-valida? "01-01-2024")))
    (is (not (rclara/fecha-valida? "")))
    (is (not (rclara/fecha-valida? nil))))
  
  (testing "Fechas inválidas por mes"
    (is (not (rclara/fecha-valida? "2024-13-01")))
    (is (not (rclara/fecha-valida? "2024-00-10"))))
  
  (testing "Fechas inválidas por día"
    (is (not (rclara/fecha-valida? "2024-01-32"))) 
    (is (not (rclara/fecha-valida? "2024-04-31")))))
 
(comment
  
  (run-test fecha-valida?-test)
  
  :rcf)