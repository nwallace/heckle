(ns heckle.validations
  (:require [heckle.core :refer [make-claim make-denial]]))

;; general validations
(defn is-present
  ([key] (is-present key "is required"))
  ([key error-msg] (make-claim
                    #(when-let [value (get %1 key "")]
                       (or (not (string? value))
                           (re-find #"\S" value)))
                    key error-msg)))
