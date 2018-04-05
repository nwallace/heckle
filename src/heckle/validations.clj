(ns heckle.validations
  (:require [heckle.core :refer [make-claim make-denial]]
            [clojure.string :as str]))

(defn- to-sentence
  ([coll] (to-sentence coll " and "))
  ([coll conjunction]
   (str/join conjunction
             (if-let [for-commas (seq (drop-last coll))]
               [(str/join ", " for-commas) (last coll)]
               [(last coll)]))))
(defn- pluralize
  [number singular]
  (str number " " singular (when (not= number 1) "s")))

;; general validations
(defn is-present
  "Returns a validator fn that ensures the value of the given field is present (not nil and not a blank string).

Arguments:
  - `key` - the key whose value we will check
  - `error-msg` - (optional) custom error message (defaults to \"is required\")

Return value:
  Returns a validation function that fails if the value of the given field is nil or a blank string.

Example:
  ```
  ((is-present :email) {:email \"me\"}) ; => nil
  ((is-present :email) {:email \"\"}) ; => [:email \"is required\"]
  ((is-present :email \"Uh oh\") {}) ; => [:email \"Uh oh\"]
  ```
"
  ([key] (is-present key "is required"))
  ([key error-msg] (make-claim
                    #(when-let [value (get %1 key "")]
                       (or (not (string? value))
                           (re-find #"\S" value)))
                    key error-msg)))

(defn is-one-of
  "Returns a validator fn that ensures the value of the given field is a member of the given set of permissible values.

Arguments:
  - `collection` - the set of permissible values
  - `key` - the key whose value we will check
  - `error-msg` - (optional) custom error message (defaults to \"must be either `(to-sentence collection)`\")

Return value:
  Returns a validation function that fails if the value of the given field is not a member of `collection`.

Example:
  ```
  ((is-one-of #{:red :blue} :color) {:color :blue}) ; => nil
  ((is-one-of #{:red :blue} :color) {:color :green}) ; => [:color \"must be either :red or :blue\"]
  ((is-one-of #{:red :blue} :color \"Uh oh\") {}) ; => [:color \"Uh oh\"]
  ```
  "
  ([collection key] (is-one-of collection key
                               (str "must be either " (to-sentence collection " or "))))
  ([collection key error-msg] (make-claim
                               #(some #{(get %1 key)} collection)
                               key error-msg)))

(defn is-confirmed
  "Returns a validator fn that ensures the value of the given field matches the value of its confirmation field.

Arguments:
  - `key` - the key whose value we will check is confirmed
  - `confirmation-key` - (optional) the key whose value should match that of `key` (defaults to :`key`-confirmation)
  - `error-msg` - (optional) custom error message (defaults to \"does not match\")

Return value:
  Returns a validation function that fails if the value of the given field doesn't match the value of the confirmation field.

Example:
  ```
  ((is-confirmed :password) {:password \"abcd\" :password-confirmation \"abcd\"}) ; => nil
  ((is-confirmed :password) {:password \"abcd\" :password-confirmation \"bacd\"}) ; => [:password-confirmation \"doesn't match\"]
  ((is-confirmed :password :pswd-confirmation \"Uh oh\") {}) ; => [:pswd-confirmation \"Uh oh\"]
  ```
  "
  ([key] (is-confirmed key (keyword (str (name key) "-confirmation"))))
  ([key confirmation-key] (is-confirmed key confirmation-key "does not match"))
  ([key confirmation-key error-msg]
   (make-claim #(and (contains? %1 key)
                     (contains? %1 confirmation-key)
                     (= (key %1) (confirmation-key %1)))
               confirmation-key error-msg)))

;; string validations
(defn matches
  "Returns a validator fn that ensures the value of the given field matches the given regular expression.

Arguments:
  - `regex` - the regular expression the value should match
  - `key` - the key whose value we will check
  - `error-msg` - (optional) custom error message (defaults to \"is invalid\")

Return value:
  Returns a validation function that fails if the value of the given field doesn't match `regex`.

Example:
  ```
  ((matches #\"\\d\" :password) {:password \"pass123\"}) ; => nil
  ((matches #\"\\d\" :password) {:password \"pass\"}) ; => [:password \"is invalid\"]
  ((matches #\"\\d\" :password \"Uh oh\") {}) ; => [:password \"Uh oh\"]
  ```
  "
  ([regex key] (matches regex key "is invalid"))
  ([regex key error-msg] (make-claim
                          #(when-let [value (get %1 key "")]
                             (re-find regex value))
                          key error-msg)))

(defn length-is-at-least
  "Returns a validator fn that ensures the value of the given field is at least as long as the given length.

Arguments:
  - `min-length` - the minimum permissible length
  - `key` - the key whose value we will check
  - `error-msg` - (optional) custom error message (defaults to \"must be at least `min-length` characters\")

Return value:
  Returns a validation function that fails if the value of the given field is shorter than `min-length`.

Example:
  ```
  ((length-is-at-least 8 :password) {:password \"password\"}) ; => nil
  ((length-is-at-least 8 :password) {:password \"pass\"}) ; => [:password \"must be at least 8 characters\"]
  ((length-is-at-least 8 :password \"Uh oh\") {}) ; => [:password \"Uh oh\"]
  ```
  "
  ([min-length key] (length-is-at-least
                     min-length key
                     (str "must be at least " (pluralize min-length "character"))))
  ([min-length key error-msg] (make-denial
                               #(-> (get %1 key "") count (< min-length))
                               key error-msg)))

(defn length-is-no-more-than
  "Returns a validator fn that ensures the value of the given field is at most as long as the given length.

Arguments:
  - `max-length` - the maximum permissible length
  - `key` - the key whose value we will check
  - `error-msg` - (optional) custom error message (defaults to \"must be no more than `(inc max-length)` characters\")

Return value:
  Returns a validation function that fails if the value of the given field is shorter than `min-length`.

Example:
  ```
  ((length-is-no-more-than 4 :id) {:id \"abcd\"}) ; => nil
  ((length-is-no-more-than 4 :id) {:id \"abcde\"}) ; => [:id \"must be less than 5 characters\"]
  ((length-is-no-more-than 4 :id \"Uh oh\") {}) ; => [:password \"Uh oh\"]
  ```
  "
  ([max-length key] (length-is-no-more-than
                     max-length key
                     (str "must be less than " (pluralize (inc max-length) "character"))))
  ([max-length key error-msg] (make-denial
                               #(-> (get %1 key "") count (> max-length))
                               key error-msg)))

(defn is-at-least
  "Returns a validator fn that ensures the value of the given field is at least the value given.

Arguments:
  - `bound` - the minimum permissible value
  - `key` - the key whose value we will check
  - `error-msg` - (optional) custom error message (defaults to \"must be at least `bound`\")

Return value:
  Returns a validation function that fails if the value of the given field is less than `bound`.

Example:
  ```
  ((is-at-least 18 :age) {:age 18}) ; => nil
  ((is-at-least 18 :age) {:age 17}) ; => [:age \"must be at least 18\"]
  ((is-at-least 18 :age \"Uh oh\") {}) ; => [:age \"Uh oh\"]
  ```
  "
  ([bound key] (is-at-least bound key (str "must be at least " bound)))
  ([bound key error-msg] (make-claim
                          #(and (contains? %1 key)
                                (not (pos? (compare bound (get %1 key)))))
                          key error-msg)))

(defn is-greater-than
  "Returns a validator fn that ensures the value of the given field is strictly greater than the value given.

Arguments:
  - `bound` - the value above which valid values must be
  - `key` - the key whose value we will check
  - `error-msg` - (optional) custom error message (defaults to \"must be greater than `bound`\")

Return value:
  Returns a validation function that fails if the value of the given field is less than or equal to `bound`.

Example:
  ```
  ((is-greater-than 20 :age) {:age 21}) ; => nil
  ((is-greater-than 20 :age) {:age 20}) ; => [:age \"must be greater than 20\"]
  ((is-greater-than 20 :age \"Uh oh\") {}) ; => [:age \"Uh oh\"]
  ```
  "
  ([bound key] (is-greater-than bound key (str "must be greater than " bound)))
  ([bound key error-msg] (make-claim
                          #(and (contains? %1 key)
                                (neg? (compare bound (get %1 key))))
                          key error-msg)))

(defn is-less-than
  "Returns a validator fn that ensures the value of the given field is strictly less than the value given.

Arguments:
  - `bound` - the value below which valid values must be
  - `key` - the key whose value we will check
  - `error-msg` - (optional) custom error message (defaults to \"must be less than `bound`\")

Return value:
  Returns a validation function that fails if the value of the given field is greater than or equal to `bound`.

Example:
  ```
  ((is-less-than 80 :age) {:age 79}) ; => nil
  ((is-less-than 80 :age) {:age 80}) ; => [:age \"must be less than 80\"]
  ((is-less-than 80 :age \"Uh oh\") {}) ; => [:age \"Uh oh\"]
  ```
  "
  ([bound key] (is-less-than bound key (str "must be less than " bound)))
  ([bound key error-msg] (make-claim
                          #(and (contains? %1 key)
                                (pos? (compare bound (get %1 key))))
                          key error-msg)))

(defn is-no-more-than
  "Returns a validator fn that ensures the value of the given field is no more than the value given.

Arguments:
  - `bound` - the maximum permissible value
  - `key` - the key whose value we will check
  - `error-msg` - (optional) custom error message (defaults to \"must be no more than `bound`\")

Return value:
  Returns a validation function that fails if the value of the given field is greater than `bound`.

Example:
  ```
  ((is-no-more-than 80 :age) {:age 80}) ; => nil
  ((is-no-more-than 80 :age) {:age 81}) ; => [:age \"must be no more than 80\"]
  ((is-no-more-than 80 :age \"Uh oh\") {}) ; => [:age \"Uh oh\"]
  ```
  "
  ([bound key] (is-no-more-than bound key (str "must be no more than " bound)))
  ([bound key error-msg] (make-claim
                          #(and (contains? %1 key)
                                (not (neg? (compare bound (get %1 key)))))
                          key error-msg)))
