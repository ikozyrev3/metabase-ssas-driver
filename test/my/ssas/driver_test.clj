(ns my.ssas.driver-test
  (:require [clojure.test :refer :all]
            [my.ssas.driver :refer :all]))

(deftest test-to-table-name
  (testing "Converting strings to table names"
    (is (= "test_table" (#'my.ssas.driver/to-table-name "Test Table")))
    (is (= "my_dimension" (#'my.ssas.driver/to-table-name "My Dimension")))
    (is (= "special_chars" (#'my.ssas.driver/to-table-name "Special@#$%Chars")))))

(deftest test-register-function
  (testing "Driver registration returns correct metadata"
    (let [metadata (register)]
      (is (contains? metadata :metabase.driver/name))
      (is (= "My SSAS Driver" (:metabase.driver/name metadata)))
      (is (contains? metadata :metabase.driver/class-for-name)))))

(deftest test-connection-details-spec
  (testing "Connection details to spec conversion"
    (let [details {:host "localhost" :port 1433 :database "testdb" :username "user" :password "pass"}
          spec (connection-details->spec details)]
      (is (= "my.ssas.driver.Driver" (:classname spec)))
      (is (= "my-ssas-driver" (:subprotocol spec)))
      (is (= "//localhost:1433/testdb" (:subname spec)))
      (is (= "user" (:user spec)))
      (is (= "pass" (:password spec))))))