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

;; string validations
(defn matches
  ([regex key] (matches regex key "is invalid"))
  ([regex key error-msg] (make-claim
                          #(when-let [value (get %1 key "")]
                             (re-find regex value))
                          key error-msg)))
