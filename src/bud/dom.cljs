(ns bud.dom
  (:require
    [taoensso.telemere :as t]
    [bud.reactive :as r]))

(defn render [node]
  (cond
    (instance? js/Node node)
    (do
      (t/log! :info "rendering prebuilt DOM node")
      node)

    (string? node)
    (do
      (t/log! :info "rendering string node")
      (js/document.createTextNode node))

    (number? node)
    (do
      (t/log! :info "rendering number node")
      (js/document.createTextNode (str node)))

    (r/is-signal? node)
    (let [el (js/document.createTextNode "")]
      (t/log! :info "rendering signal node")
      (r/effect
        #(set! (.-textContent el) (node)))
      el)

    (vector? node)
    (let [[tag & args] node]
      (cond
        (fn? tag)
        (render (apply tag args)) ;; call the component, re-render its result

        :else
        (let [[attrs children] (if (map? (first args))
                                 [(first args) (rest args)]
                                 [nil args])
              el (js/document.createElement (name tag))]
          ;; attributes
          ;; TODO: check of attr is a signal. If it is,
          ;; we should either allow reactive update or
          ;; throw an error.
          (doseq [[k v] attrs]
            (cond
              (.startsWith (name k) "on-")
              (.addEventListener el (subs (name k) 3) v)
              :else
              (.setAttribute el (name k) v)))
          ;; children
          (doseq [child children]
            (.appendChild el (render child)))
          el)))

    (seq? node)
    (let [frag (js/DocumentFragment.)]
      (t/log! :info "rendering seq node")
      (doseq [child node]
        (.appendChild frag (render child)))
      frag)

    :else
    (do
      (t/log! :info "rendering anything node - defaulting to text")
      (if (nil? node)
        (js/document.createTextNode "")
        (js/document.createTextNode node)))))

;; TODO: make a macro to handle thisreact
(defn reactive-fragment [compute]
  (let [container (js/document.createElement "div")
        child-nodes (atom nil)]
    (r/effect
      (fn []
        (let [new-content (compute)]
          ;; remove previous children
          (when @child-nodes
            (doseq [n @child-nodes]
              (.remove n)))
          (let [;; TODO: get the reference so it can be manipulated
                ;; without needing the container
                new-el (render new-content)
                node-list (if (.-childNodes new-el)
                            (js/Array.from (.-childNodes new-el))
                            [new-el])]
            (reset! child-nodes node-list)
            (set! (.-innerHTML container) "")
            (.appendChild container new-el)
            new-el))))
    container))
