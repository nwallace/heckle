(ns heckle.core)

(defn validate
  "Check the given validation functions against the given data and return a (possibly empty) list of errors.

Arguments:
  - `validations` - a list of validation functions to be checked against the data. Each validation function should return ether `nil` if the validation passed or a tuple of `[error-key error-msg]` if the validation failed.
  - `data` - the data you're trying to validate (typically a hash-map, though it could be anything else you want as long as the validation functions know how to handle it)

Return value:
  Returns a hash-map of error messages grouped by their key. If all validations pass, it returns an empty hash-map `{}`. Otherwise, the hash-map will look something like this: `{key1 [message1], key2 [message2 message3]}`

Example:
  ```
  (heckle.core/validate
    [(fn [data] (when (empty? (:email data)) [:email \"is required\"]))
     (fn [data] (when-not (re-find #\"[a-z]\" (:password data)) [:password \"must contain at least 1 lowercase letter\"]))
     (fn [data] (when-not (re-find #\"[A-Z]\" (:password data)) [:password \"must contain at least 1 capital letter\"]))
     (fn [data] (when-not (re-find #\"\\d\" (:password data)) [:password \"must contain at least 1 number\"]))]
    {:email \"\"
     :password \"pass\"})
   => {:email #{\"is required\"}
       :password #{\"must conatin at least one lowercase letter\"
                   \"must contain at least one capital letter\"}}
  ```
"
  [validations data]
  (reduce (fn [errors validation]
            (if-let [[error-key error-msg] (validation data)]
              (update errors error-key (fnil conj #{}) error-msg)
              errors))
          {}
          validations))
