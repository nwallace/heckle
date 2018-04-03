(ns heckle.core-test
  (:require [midje.sweet :refer :all]
            [heckle.core :refer :all]))

(defn email-presence-validation [data]
  (when (empty? (get data :email "")) [:email "is required"]))
(defn email-format-validation [data]
  (when-not (re-find #".@." (get data :email "")) [:email "is invalid"]))
(defn password-presence-validation [data]
  (when (empty? (get data :password "")) [:password "is required"]))

(facts "about 'validate"
       (fact "it returns no errors when no validations are given"
             (validate [] {}) => {}
             (validate [] {:data "value"}) => {})
       (fact "it returns no errors when the data passes all validations"
             (validate [email-presence-validation]
                       {:email "any non-empty string"}) => {}
             (validate [email-presence-validation
                        email-format-validation]
                       {:email "me@example.com"}) => {}
             (validate [email-presence-validation
                        password-presence-validation]
                       {:email "me@example.com" :password "pass"}) => {})
       (fact "it returns error messages grouped by error key when the data"
             (validate [email-presence-validation]
                       {}) => {:email #{"is required"}}
             (validate [email-presence-validation
                        email-format-validation
                        password-presence-validation]
                       {}) => {:email #{"is required" "is invalid"}
                               :password #{"is required"}}))

(facts "about 'make-claim"
       (let [only-even (make-claim even? :number "must be even")]
         (fact "it returns a function that returns `nil` when called with data that passes the predicate"
               (only-even 2) => nil)
         (fact "it returns a function that returns an error tuple with the given key and message when called with data that fails the predicate"
               (only-even 1) => [:number "must be even"])))

(facts "about 'make-denial"
       (let [only-even (make-denial odd? :number "must be even")]
         (fact "it returns a function that returns `nil` when called with data that passes the predicate"
               (only-even 2) => nil)
         (fact "it returns a function that returns an error tuple with the given key and message when called with data that fails the predicate"
               (only-even 1) => [:number "must be even"])))
