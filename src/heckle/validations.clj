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

(defn is-one-of
  ([collection key] (is-one-of collection key
                               (str "must be either " (to-sentence collection ", or "))))
  ([collection key error-msg] (make-claim
                               #(some #{(get %1 key)} collection)
                               key error-msg)))

(defn length-is-at-least
  ([min-length key] (length-is-at-least
                     min-length key
                     (str "must be at least " (pluralize min-length "character"))))
  ([min-length key error-msg] (make-denial
                               #(-> (get %1 key "") count (< min-length))
                               key error-msg)))

(defn length-is-no-more-than
  ([max-length key] (length-is-no-more-than
                     max-length key
                     (str "must be less than " (pluralize (inc max-length) "character"))))
  ([max-length key error-msg] (make-denial
                               #(-> (get %1 key "") count (> max-length))
                               key error-msg)))
