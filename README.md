# Bud 

**Bud** is a minimalist DOM library for ClojureScript.
It lets you build reactive single-page applications using real DOM elements.
No Virtual DOM, no Shadow DOM, no magic compiler.

---

## ⚡ Why Bud?

- Built directly on top of **vanilla JS** — no abstraction layers.
- Uses **native DOM APIs** for actual elements, not proxies or clones.
- Embraces **signals** for composable, explicit reactivity.
- No Shadow DOM. No Virtual DOM. Just DOM.

> _Bud is to reactivity what Clojure is to state: explicit, simple, and powerful (although still working on this last one 😅)._

---

## Table of Contents

- [Project status](#project-status)
- [Installation](#installation)
- [Features and usage](#features-and-usage)
  - [Creating signals (reactive text nodes)](#creating-signals-reactive-text-nodes)
  - [Reactive fragments](#reactive-fragments)
  - [Dynamic collections](#dynamic-collections)
  - [Event handling](#event-handling)
  - [Reactive conditional rendering](#reactive-conditional-rendering)
  - [Composing components](#composing-components)
  - [Rendering your app](#rendering-your-app)
- [Example app](#example-app)
- [Roadmap](#roadmap)

## Project status

🚧 The project is in early-stage development. It is not yet ready for production use.

Try it, break it, file issues, and send feedback.

## Installation

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.mat/bud.svg)](https://clojars.org/org.clojars.mat/bud)

## Features and usage

The core of Bud consists in just 3 functions, `create-signal`, `reactive-fragment`, and `dom-render`. 
The rest of the problems to build reactive UIs is built-in for great ergonomics.

 - `create-signal` creates a signal, which is a reactive value that can be used in the DOM.
 - `reactive-fragment` is a function that takes a function and returns a reactive fragment, which is a piece of DOM that will be updated when the signals it uses change.
 - `dom-render` is a function that takes a DOM element and a component function, and renders the component into the DOM element.

#### Creating signals (reactive text nodes)

```clojure
(ns bud.example
  (:require
    [bud.core :as bud]))

(defn app []
  (let [[get! set!] (bud/create-signal "world")]
    [:h1 "Hello, " get! "!"]))
```

Text nodes update automatically when signals change.

#### Reactive fragments

```clojure 
(defn app []
  (let [[get! _] (bud/create-signal {:value 42})]
    [:div 
     [:h1 "Hello, world!"]
     (bud/reactive-fragment #(vector :h2 (str "Value: " (:value (get!)))))]))
```

Wrap dynamic expressions in reactive-fragment when they go beyond simple strings/numbers.

#### Dynamic collections

```clojure 
(defn list-component []
  (map #(into [] [:div %]) ["a" "b" "c"]))
```

You can use map, loops, conditionals — it's just Clojure data.

#### Event handling 

```clojure
(defn app []
  (let [[getter! setter!] (bud/create-signal "world")]
    [:div
     [:input {:type "text"
              :value getter!
              :on-input #(setter! (.. % -target -value))}]
     [:p "Current value: " getter!]]))
```

Event listeners are auto-wired via :on-*.

#### Reactive conditional rendering 

```clojure 
(defn app []
  (let [[get-value! set-value!] (bud/create-signal "world")]
    [:div
     (bud/reactive-fragment
       #(when (= (get-value!) "world")
          [:p "This only shows if the value is 'world'"]))]))
```

Only renders when the condition is true — rerenders on signal change.

#### Composing components

```clojure 
(defn footer-component [value]
  [:footer
   [:p "This is a footer component. " value]])

(defn app []
    (let [[get-value! set-value!] (bud/create-signal "")]
        [:div
         [:h1 "Hello, " get-value! "!"]
         [:input {:type "text"
                :value get-value!
                :on-input #(set-value! (.. % -target -value))}]
         ;; footer component
         [footer-component get-value!]]))
```

#### Rendering your app 

```clojure 
(ns bud.example 
    (:require
        [bud.core :as bud]))

;; { ... your app code ... }

(defn ^:dev/after-load start []
  (let [el (js/document.getElementById "app")]
    (bud/dom-render el app)))
```

## Example app

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


## Roadmap

Roadmap to 0.2.0 (which will be a beta release)

 - [ ] Make `reactive-fragment` a macro and improve its ergonomics (and maybe name)
 - [ ] Evaluate when attribute is a signal and decide if it should be reactive or throw an error
 - [ ] Find a good way to manage the `ref` problem for rendering libs in the DOM
 - [ ] Stress test it

