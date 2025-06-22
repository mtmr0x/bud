# Bud 

A minimalist ClojureScript DOM library with precise, signal-driven reactivity for single-page applications.

## Project status

🚧 The project is in early-stage development. It is not yet ready for production use, but you can try it out and give feedback.

## Installation

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.mat/bud.svg)](https://clojars.org/org.clojars.mat/bud)

## Features and usage

```clojure
(ns bud.example
  (:require
    [bud.core :as bud]))

(defn footer-component [value]
  ;; the value in the attr-test will not be reactive
  ;; unless you use it in side a reactive-fragment
  [:footer {:attr-test (value)} ;; <- ⚠️  won't be reactive
   [:p "This is a footer component. " value]])

(defn app []
  (let [[get-value! set-value!] (bud/create-signal {:value "world"})
        [get-string-value! set-string-value!] (bud/create-signal "world")]

    [:div
     ;; string or number values will be rendered as
     ;; reactive text nodes, so you can use them directly
     [:h1 "Hello, " get-string-value! "!"]

     ;; you can do maps!
     (map #(into [] [:div %]) '("a" "b" "c" "d" "e"))

     ;; when the value of the signal is not a string or a number,
     ;; it will not be rendered as a reactive text node, so you
     ;; need to enclose it in a reactive-fragment
     (bud/reactive-fragment
       #(do [:h2 (str "This is a reactive app. Current value: " (:value (get-value!)))]) )

     [:p "Type something below:"]
     [:input {:type "text"
              ;; works because get-value has a default value.
              ;; if this value needs to be reactive, use 
              ;; reactive-fragment to wrap the input
              :value (:value (get-value!))
              ;; on- attributes will create event listeners automatically
              :on-input #(do
                            (set-string-value! (.. % -target -value))
                            (set-value! {:value (.. % -target -value)}))}]

     ;; get-value! is a signal, so it will be reactive
     ;; in any inner scope since kept as a signal and
     ;; used directly in the DOM as text node. If you
     ;; want to use it as a reactive html attribute,
     ;; follow the example after this one.
     [footer-component get-value!]

     (bud/reactive-fragment
       #(when (= (get-string-value!) "world")
          [:div {:attr-test (get-string-value!)}
           [:p "this only shows if the word in the input is \"world\""]
           [:p "input value: " get-string-value!]]))]))

(defn ^:dev/after-load start []
  (let [el (js/document.getElementById "app")]
    (bud/dom-render el app)))

```



