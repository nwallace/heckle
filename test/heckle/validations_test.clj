(ns heckle.validations-test
  (:require [heckle.validations :refer :all]
            [midje.sweet :refer :all]))

;; general validations
(facts "about 'is-present"
       (fact "it passes if the specified key is present and not empty"
             ((is-present :name) {:name "me"}) => nil)
       (fact "it fails if the specified key is not in the given data"
             ((is-present :name) {}) => [:name "is required"])
       (fact "it fails if the specified key is nil in the given data"
             ((is-present :name) {:name nil}) => [:name "is required"])
       (fact "it fails if the specified key is an empty string"
             ((is-present :name) {:name ""}) => [:name "is required"]
             ((is-present :name) {:name " \t\r\n"}) => [:name "is required"])
       (fact "it ensures the value is not nil when given a data type other than a string"
             ((is-present :age) {:age 30}) => nil
             ((is-present :skills) {:skills []}) => nil)
       (fact "it uses the specified error message if one is given"
             ((is-present :name "Ohs nos!") {:name ""}) => [:name "Ohs nos!"]))

;; string validations
(facts "about 'matches"
       (fact "it passes if the specified value matches the specified regex"
             ((matches #"[a-z]" :password) {:password "pass"}) => nil
             ((matches #"^\$\d+\.\d\d$" :price) {:price "$1.95"}) => nil)
       (fact "it fails if the specified key is not in the given data"
             ((matches #"." :password) {}) => [:password "is invalid"])
       (fact "it fails if the specified key is nil in the given data"
             ((matches #"." :password) {:password nil}) => [:password "is invalid"])
       (fact "it fails if the specified value does not match the specified regex"
             ((matches #"[a-z]" :password) {:password "PASS"}) => [:password "is invalid"])
       (fact "is uses the specified error message if one is given"
             ((matches #"." :password "Ohs nos!") {}) => [:password "Ohs nos!"]))
