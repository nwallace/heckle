# heckle

The Clojure(Script) validation library you deserve.

> Heckle is a *flexible*, *function-oriented*, and *composable* validations library for Clojure and ClojureScript.

[![Build Status](https://travis-ci.org/nwallace/heckle.svg)](https://travis-ci.org/nwallace/heckle)
[![Clojars Project](https://img.shields.io/clojars/v/heckle.svg)](https://clojars.org/heckle)

## Why another validation library?

Clojure has several existing validation libraries, but as I looked around I was stunned to see how inflexibly they were designed. Many only support working with hash-maps. Many only support validating one piece of data in isolation (making it hard to validate things like a "Password confirmation" field). I couldn't find a validations library that elegantly addressed all of my validation needs, so I built one.

## Usage

Let's dive right in with a quick example:

```clojure
(require '[heckle.core :refer [validate]]
         '[heckle.validations :as v])

(def login-validations
  [(v/matches #".@." :email)
   (v/matches #"[a-z]" :password "must include a lower-case letter")
   (v/matches #"[A-Z]" :password "must include a capital letter")
   (v/matches #"\d" :password "must include a number")
   (v/length-is-at-least 8 :password)])

(validate login-validations {:email "me@example.com" :password "MyPa55word"})
  ; => {}

(validate login-validations {:email "me" :password "passwd"})
  ; => {:email #{"is invalid"}
  ;     :password #{"must include a capital letter"
  ;                 "must include a number"
  ;                 "must be at least 8 characters"}}
```

As you can see, `heckle.core/validate` take a list of "validations" and some data, and returns a hash-map of errors. The errors is a hash-map where the key is the invalid field and the value is a list of error messages. When everything is okay, the errors hash-map is empty.

All the hard work is handled by the validation functions you provide. The validation functions have a very simple interface: they take the data to validate as their only argument, and they return error information in the form of `[error-key error-message]` if the data fails the validation, or `nil` if the data passes.

The simplicity of the validation functions is the key difference between Heckle and other validation libraries. Since they receive the entire input data, they can check one field or many fields at will. The error key they return is independent from the data its validating.

Heckle ships with many standard validation functions built-in, but it's easy to define your own:

```clojure
(defn can-vote [data]
  (when (< (:age data) 18)
    [:voter "is too young to vote"]))

(defn candidate-is-valid [data]
  (when-not (contains? #{"Clinton" "Stein" "Johnson"} (:candidate data))
    [:candidate "is a menace to society"]))

(heckle.core/validate [can-vote candidate-is-valid] {:age 28 :candidate "Clinton"})
  ; => {}

(heckle.core/validate [can-vote candidate-is-valid] {:age 17 :candidate "Trump"})
  ; => {:voter #{"is too young to vote"}
  ;     :candidate #{"is a menace to society"}}
```

## Built-in validation function builders

Heckle comes with functions to build your own validation functions. The `heckle.validations` namespace contains builder functions for validationg hash-map input data. Example usage: 

```clojure
(heckle.core/validate
  [(heckle.validations/is-present :email)
   (heckle.validations/is-confirmed :email)
   (heckle.validations/is-at-least 18 :age "is too young")]
  {:email "me@example.com" :age 30}) ; => {}
```

| Fn name                  | Description                                                                                       | Arguments                                                                                                                                                                                               | Default error message                    |
| ---                      | ---                                                                                               | ---                                                                                                                                                                                                     | ---                                      |
| `is-present`             | If value is a string, ensures it is not blank. If value is not a string, ensures it is not `nil`. | `key` - the key whose value we will check<br/>`error-msg` - (optional) custom error message when invalid                                                                                                | `"is required"`                          |
| `matches`                | Ensure a string matches against a regular expression                                              | `regex` - the regular expression<br/>`key` - the key whose value we will check<br/>`error-msg` - (optional) custom error message when invalid                                                           | `"is invalid"`                           |
| `is-one-of`              | Ensure a value is one of a list of values                                                         | `collection` - the set of permissible values<br/>`key` - the key whose value we will check<br/>`error-msg` - (optional) custom error message when invalid                                               | e.g. `"must be either A, B or C"`        |
| `is-confirmed`           | Ensure a value has been confirmed accurately (the value and its confirmation are equal)           | `key` - the key whose value we will check is confirmed<br/>`confirmation-key` - (optional) the name of the key of the confirmation value<br/>`error-msg` - (optional) custom error message when invalid | `"does not match"`                       |
| `length-is-at-least`     | Ensure a string or other sequence is at least the given length                                    | `min-length` - the minimum permissible length<br/>`key` - the key whose value we will check<br/>`error-msg` - (optional) custom error message when invalid                                              | e.g. `"must be at least 8 characters"`   |
| `length-is-no-more-than` | Ensure a string or other sequence is at most the given length                                     | `max-length` - the maximum permissible length<br/>`key` - the key whose value we will check<br/>`error-msg` - (optional) custom error message when invalid                                              | e.g. `"must be less than 25 characters"` |
| `is-at-least`            | Ensure a value is greater than or equal to a minimum value                                        | `bound` - the minimum permissible value<br/>`key` - the key whose value we will check<br/>`error-msg` - (optional) custom error message when invalid                                                    | e.g. `"must be at least 3"`              |
| `is-greater-than`        | Ensure a value is strictly greater than a given value                                             | `bound` - the value above which valid values must be<br/>`key` - the key whose value we will check<br/>`error-msg` - (optional) custom error message when invalid                                       | e.g. `"must be greater than 2"`          |
| `is-less-than`           | Ensure a value is strictly less than a given value                                                | `bound` - the value below which valid values must be<br/>`key` - the key whose value we will check<br/>`error-msg` - (optional) custom error message when invalid                                       | e.g. `"must be less than 10"`            |
| `is-no-more-than-than`   | Ensure a value is less than or equal to a maximum value                                           | `bound` - the maximum permissible value<br/>`key` - the key whose value we will check<br/>`error-msg` - (optional) custom error message when invalid                                                    | e.g. `"must be no more than 9"`          |

## Building your own validation functions

If none of the built-in validation functions work for you, it's easy to write your own validation functions. You can just write a your own validation from scratch (it's very easy!), or you can use Heckle's helper functions `make-claim` and `make-denial`.

#### Completely custom validations

A validation is just a function that accepts the input data as an argument and returns `nil` if everything is okay, or error information in the form `[error-key error-message]` if there's an error.

Here are some completely custom validation functions you can use with `validate`:

```clojure
(def sign-up-validations
  [(fn [data] (when (empty? (:email data)) [:email "is required"]))
   (fn [data] (when-not (re-find #"[a-z]" (:password data)) [:password "must contain at least one lowercase letter"]))
   (fn [data] (when-not (re-find #"[A-Z]" (:password data)) [:password "must contain at least one capital letter"]))
   (fn [data] (when-not (re-find #"\d" (:password data)) [:password "must contain at least one number"]))])

(heckle.core/validate sign-up-validations {:email "" :password "pass"})
  ; => {:email #{"is required"}
  ;     :password #{"must conatin at least one capital letter"
  ;                 "must contain at least one number"}}
```

#### Using `make-claim` and `make-denial`

It's even easier to write custom validation functions using two helper functions `make-claim` and `make-denial`. These functions both take a predicate function, the error key and the error message. `make-claim` expects the predicate to return a truthy value when there is no error, and `make-denial` expects the predicate to return a falsey value when there is no error.

Let's see the same example as above, but using these helper functions:

```clojure
(def sign-up-validations
  [(heckle.core/make-denial #(empty? (:email %1)) :email "is required")
   (heckle.core/make-claim #(re-find #"[a-z]" (:password %1)) :password "must contain at least one lowercase letter")
   (heckle.core/make-claim #(re-find #"[A-Z]" (:password %1)) :password "must contain at least one capital letter")
   (heckle.core/make-claim #(re-find #"\d" (:password %1)) :password "must contain at least one number")])

(heckle.core/validate sign-up-validations {:email "" :password "pass"})
  ; => {:email #{"is required"}
  ;     :password #{"must conatin at least one capital letter"
  ;                 "must contain at least one number"}}
```

## Roadmap

* Compile to CLJS as well
* Add collection validations
* Add a syntax shortcut for setting up multiple validations for the same field
* Enable validation to short-circuit other validations for invalid field
* Enable validation to short-circuit all other validations upon first error on any field
* Allow customization of the default error messages
* Support records in addition to hash-maps for built-in validation functions
* Add data type validations?

## License

Copyright © 2018 Nathan Wallace

Distributed under the MIT License.
