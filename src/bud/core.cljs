(ns bud.core
  (:require
    [bud.reactive :as r]
    [bud.dom :as dom]))

(defn create-signal [initial]
  (r/create-signal initial))

(defn reactive-fragment [f]
  (dom/reactive-fragment f))

(defn render [node]
  (dom/render node))

(defn dom-render [element app]
  (set! (.-innerHTML element) "")
  (.appendChild element (render (app))))
