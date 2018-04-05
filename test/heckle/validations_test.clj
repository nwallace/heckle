(ns heckle.validations-test
  (:require [heckle.validations :refer :all]
            [midje.sweet :refer :all]
            [clojure.instant :refer [read-instant-date]]))

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

(facts "about 'is-one-of"
       (fact "it passes if the specified value is one of the given values"
             ((is-one-of #{:red :blue} :color) {:color :red}) => nil
             ((is-one-of #{:red :blue} :color) {:color :blue}) => nil)
       (fact "it fails if the specified key is not in the given data"
             ((is-one-of #{:red :blue} :color) {}) => [:color "must be either :red or :blue"])
       (fact "it fails if the specified key is nil in the given data"
             ((is-one-of #{:red :blue} :color) {:color nil}) => [:color "must be either :red or :blue"])
       (fact "it fails if the specified value is not one of the given values"
             ((is-one-of #{:red :blue} :color) {:color :green}) => [:color "must be either :red or :blue"])
       (fact "it uses the specified error message if one is given"
             ((is-one-of #{:red :blue} :color "Ohs nos!") {}) => [:color "Ohs nos!"])
       (fact "it works when given a vector instead of a set"
             ((is-one-of [:red :blue] :color) {:color :red}) => nil
             ((is-one-of [:red :blue] :color) {:color :green}) => [:color "must be either :red or :blue"]))

(facts "about 'is-confirmed"
       (fact "it passes if the specified value has a confirmation value with the same value"
             ((is-confirmed :password) {:password "pass" :password-confirmation "pass"}) => nil)
       (fact "it fails if the specified value has a confirmation value with a different value"
             ((is-confirmed :password) {:password "pass" :password-confirmation "psas"}) => [:password-confirmation "does not match"])
       (fact "it fails if the specified value has no confirmation field"
             ((is-confirmed :password) {:password "pass"}) => [:password-confirmation "does not match"])
       (fact "it fails if the specified field is not present"
             ((is-confirmed :password) {:password-confirmation "pass"}) => [:password-confirmation "does not match"])
       (fact "is uses a custom confirmation field name if one is given"
             ((is-confirmed :password :pwd-conf) {:password "pass" :pwd-conf "pass"}) => nil
             ((is-confirmed :password :pwd-conf) {:password "pass" :pwd-conf "psas"}) => [:pwd-conf "does not match"])
       (fact "it uses a specified error message if one is given"
             ((is-confirmed :pwd :pwdc "Ohs nos!") {:pwd "pass" :pwdc "pass"}) => nil
             ((is-confirmed :pwd :pwdc "Ohs nos!") {:pwd "pass" :pwdc "psas"}) => [:pwdc "Ohs nos!"])
       (fact "it works for values other than strings"
             ((is-confirmed :age) {:age 24 :age-confirmation 24}) => nil
             ((is-confirmed :age) {:age 24 :age-confirmation 25}) => [:age-confirmation "does not match"]
             ((is-confirmed :interests)
              {:interests ["Cooking" "Running"] :interests-confirmation ["Cooking" "Running"]}) => nil
             ((is-confirmed :interests)
              {:interests ["Cooking" "Running"] :interests-confirmation ["Cooking" "Cycling"]}) => [:interests-confirmation "does not match"]
             ((is-confirmed :nil) {:nil nil :nil-confirmation nil}) => nil
             ((is-confirmed :nil) {:nil nil :nil-confirmation 1}) => [:nil-confirmation "does not match"]
             ((is-confirmed :nil) {}) => [:nil-confirmation "does not match"]))

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

(facts "about 'length-is-at-least"
       (fact "it passes if the specified value is at least as long as the given length"
             ((length-is-at-least 2 :username) {:username "me"}) => nil
             ((length-is-at-least 2 :username) {:username "itsame"}) => nil
             ((length-is-at-least 5 :username) {:username "itsame"}) => nil)
       (fact "it fails if the specified key is not in the given data"
             ((length-is-at-least 2 :username) {}) => [:username "must be at least 2 characters"])
       (fact "it fails if the specified key is nil in the given data"
             ((length-is-at-least 2 :username) {:username nil}) => [:username "must be at least 2 characters"])
       (fact "it fails if the specified value is shorter than the given length"
             ((length-is-at-least 1 :username) {:username ""}) => [:username "must be at least 1 character"]
             ((length-is-at-least 2 :username) {:username "x"}) => [:username "must be at least 2 characters"]
             ((length-is-at-least 3 :username) {:username "me"}) => [:username "must be at least 3 characters"])
       (fact "is uses the specified error message if one is given"
             ((length-is-at-least 2 :username "Ohs nos!") {}) => [:username "Ohs nos!"]))

(facts "about 'length-is-no-more-than"
       (fact "it passes if the specified value is no longer than the given length"
             ((length-is-no-more-than 2 :username) {:username "me"}) => nil
             ((length-is-no-more-than 2 :username) {:username "x"}) => nil)
       (fact "it passes if the specified value is not given"
             ((length-is-no-more-than 2 :username) {:username ""}) => nil
             ((length-is-no-more-than 2 :username) {:username nil}) => nil
             ((length-is-no-more-than 2 :username) {}) => nil)
       (fact "it fails if the specified value is longer than the given length"
             ((length-is-no-more-than 1 :username) {:username "me"}) => [:username "must be less than 2 characters"]
             ((length-is-no-more-than 2 :username) {:username "itsame"}) => [:username "must be less than 3 characters"])
       (fact "is uses the specified error message if one is given"
             ((length-is-no-more-than 2 :username "Ohs nos!") {:username "itsame"}) => [:username "Ohs nos!"]))

;; comparable validations
(facts "about 'is-at-least"
       (fact "it passes if the specified value is greater than or equal to that given"
             ((is-at-least 21 :age) {:age 21}) => nil
             ((is-at-least 21 :age) {:age 50}) => nil)
       (fact "it fails if the specified value is less than that given"
             ((is-at-least 21 :age) {:age 20}) => [:age "must be at least 21"]
             ((is-at-least 21 :age) {:age 0}) => [:age "must be at least 21"])
       (fact "it fails if the specified key is not in the given data"
             ((is-at-least 21 :age) {}) => [:age "must be at least 21"])
       (fact "it uses the specified error message if one is given"
             ((is-at-least 21 :age "Ohs nos!") {}) => [:age "Ohs nos!"])
       (fact "it works for any comparable data type"
             ((is-at-least "l" :char) {:char "m"}) => nil
             ((is-at-least "l" :char) {:char "k"}) => [:char "must be at least l"]
             ((is-at-least (read-instant-date "2018-01-01") :date) {:date (read-instant-date "2018-01-01")})
               => nil
             ((is-at-least (read-instant-date "2018-01-01") :date) {:date (read-instant-date "2017-12-31")})
               => [:date (str "must be at least " (read-instant-date "2018-01-01"))]))

(facts "about 'is-greater-than"
       (fact "it passes if the specified value is greater than that given"
             ((is-greater-than 20 :age) {:age 21}) => nil
             ((is-greater-than 20 :age) {:age 50}) => nil)
       (fact "it fails if the specified value is less than or equal to that given"
             ((is-greater-than 20 :age) {:age 20}) => [:age "must be greater than 20"]
             ((is-greater-than 20 :age) {:age 0}) => [:age "must be greater than 20"])
       (fact "it fails if the specified key is not in the given data"
             ((is-greater-than 20 :age) {}) => [:age "must be greater than 20"])
       (fact "it uses the specified error message if one is given"
             ((is-greater-than 20 :age "Ohs nos!") {}) => [:age "Ohs nos!"])
       (fact "it works for any comparable data type"
             ((is-greater-than "k" :char) {:char "l"}) => nil
             ((is-greater-than "k" :char) {:char "k"}) => [:char "must be greater than k"]
             ((is-greater-than "k" :char) {:char "k"}) => [:char "must be greater than k"]
             ((is-greater-than (read-instant-date "2018-01-01") :date) {:date (read-instant-date "2018-01-02")})
               => nil
             ((is-greater-than (read-instant-date "2018-01-01") :date) {:date (read-instant-date "2018-01-01")})
               => [:date (str "must be greater than " (read-instant-date "2018-01-01"))]))

(facts "about 'is-less-than"
       (fact "it passes if the specified value is less than that given"
             ((is-less-than 21 :age) {:age 20}) => nil
             ((is-less-than 21 :age) {:age 5}) => nil)
       (fact "it fails if the specified value is greater than or equal to that given"
             ((is-less-than 21 :age) {:age 50}) => [:age "must be less than 21"]
             ((is-less-than 21 :age) {:age 21}) => [:age "must be less than 21"])
       (fact "it fails if the specified key is not in the given data"
             ((is-less-than 21 :age) {}) => [:age "must be less than 21"])
       (fact "it uses the specified error message if one is given"
             ((is-less-than 21 :age "Ohs nos!") {}) => [:age "Ohs nos!"])
       (fact "it works for any comparable data type"
             ((is-less-than "l" :char) {:char "k"}) => nil
             ((is-less-than "l" :char) {:char "l"}) => [:char "must be less than l"]
             ((is-less-than "l" :char) {:char "z"}) => [:char "must be less than l"]
             ((is-less-than (read-instant-date "2018-01-02") :date) {:date (read-instant-date "2018-01-01")})
               => nil
             ((is-less-than (read-instant-date "2018-01-01") :date) {:date (read-instant-date "2018-01-01")})
               => [:date (str "must be less than " (read-instant-date "2018-01-01"))]))

(facts "about 'is-no-more-than"
       (fact "it passes if the specified value is less than or equal to that given"
             ((is-no-more-than 21 :age) {:age 5}) => nil
             ((is-no-more-than 21 :age) {:age 21}) => nil)
       (fact "it fails if the specified value is greater than that given"
             ((is-no-more-than 21 :age) {:age 50}) => [:age "must be no more than 21"]
             ((is-no-more-than 21 :age) {:age 22}) => [:age "must be no more than 21"])
       (fact "it fails if the specified key is not in the given data"
             ((is-no-more-than 21 :age) {}) => [:age "must be no more than 21"])
       (fact "it uses the specified error message if one is given"
             ((is-no-more-than 21 :age "Ohs nos!") {}) => [:age "Ohs nos!"])
       (fact "it works for any comparable data type"
             ((is-no-more-than "l" :char) {:char "l"}) => nil
             ((is-no-more-than "l" :char) {:char "m"}) => [:char "must be no more than l"]
             ((is-no-more-than "l" :char) {:char "z"}) => [:char "must be no more than l"]
             ((is-no-more-than (read-instant-date "2018-01-01") :date) {:date (read-instant-date "2018-01-01")})
               => nil
             ((is-no-more-than (read-instant-date "2018-01-01") :date) {:date (read-instant-date "2018-01-02")})
               => [:date (str "must be no more than " (read-instant-date "2018-01-01"))]))
