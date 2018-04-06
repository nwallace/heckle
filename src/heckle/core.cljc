(ns heckle.core)

(defn validate
  "Check the given validation functions against the given data and return a (possibly empty) collection of errors.

Arguments:
  - `validations` - a list of validation functions to be checked against the data. Each validation function should return a tuple of `[error-key error-msg]` if the validation failed or `nil` otherwise.
  - `data` - the data you're trying to validate (typically a hash-map, though it could be anything else you want as long as the validation functions know how to handle it)

Return value:
  Returns a hash-map of error messages grouped by their key. If all validations pass, it returns an empty hash-map. Otherwise, the hash-map will look something like this: `{key1 #{message1}, key2 #{message2 message3}}`

Example:
  ```
  (def sign-up-validations
    [(fn [data] (when (empty? (:email data)) [:email \"is required\"]))
     (fn [data] (when-not (re-find #\"[a-z]\" (:password data)) [:password \"must contain at least one lowercase letter\"]))
     (fn [data] (when-not (re-find #\"[A-Z]\" (:password data)) [:password \"must contain at least one capital letter\"]))
     (fn [data] (when-not (re-find #\"\\d\" (:password data)) [:password \"must contain at least one number\"]))])

  (heckle.core/validate
    sign-up-validations
    {:email \"\" :password \"pass\"})
  ; => {:email #{\"is required\"}
  ;     :password #{\"must conatin at least one capital letter\"
  ;                 \"must contain at least one number\"}}

  (heckle.core/validate
    sign-up-validations
    {:email \"me@example.com\" :password \"s3cRet\"})
  ; => {}
  ```"
  [validations data]
  (reduce (fn [errors validation]
            (if-let [[error-key error-msg] (validation data)]
              (update errors error-key (fnil conj #{}) error-msg)
              errors))
          {}
          validations))

(defn make-claim
  "A helper function to create a positive validation function.

Arguments:
  - `pred` - a predicate function that should return a truthy value when there is no error
  - `error-key` - will be returned as the first item in a tuple if the predicate check fails
  - `error-msg` - will be returned as the second item in a tuple if the predicate check fails

Return value:
  Returns a validation function that fails if the predicate check fails.

Example:
  ```
  (def email-is-present-validation
    (heckle.core/make-claim
      #(seq (get %1 :email \"\"))
      :email \"is required\"))

  (email-is-present-validation {:email \"\"}) ; => [:email \"is required\"]
  (email-is-present-validation {:email \"me@example.com\"}) ; => nil
  ```"
  [pred error-key error-msg]
  (fn [data]
    (when-not (pred data)
      [error-key error-msg])))

(defn make-denial
  "A helper function to create a negative validation function.

Arguments:
  - `pred` - a predicate function that should return a falsey value when there is no error
  - `error-key` - will be returned as the first item in a tuple if the predicate check fails
  - `error-msg` - will be returned as the second item in a tuple if the predicate check fails

Return value:
  Returns a validation function that fails if the predicate check passes.

Example:
  ```
  (def email-is-present-validation
    (heckle.core/make-denial
      #(empty? (get %1 :email \"\"))
      :email \"is required\"))

  (email-is-present-validation {:email \"\"}) ; => [:email \"is required\"]
  (email-is-present-validation {:email \"me@example.com\"}) ; => nil
  ```"
  [pred error-key error-msg]
  (fn [data]
    (when (pred data)
      [error-key error-msg])))
