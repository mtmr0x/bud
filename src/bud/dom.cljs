(ns bud.dom
  (:require
    [taoensso.telemere :as t]
    [bud.reactive :as r]))

(def dev? ^boolean goog.DEBUG)

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
          (doseq [[k v] attrs]
            (cond
              ;; signal attributes
              (r/is-signal? v)
              (r/effect
                #(let [value (v)]
                   (.setAttribute el (name k) value)))
              ;; events
              (.startsWith (name k) "on-")
              (.addEventListener el (subs (name k) 3) v)
              ;; ref attribute
              (= (name k) "ref")
              (if (fn? v)
                (v el) ;; call the ref function with the element
                (do
                  (t/log! :warn "ref function called with element")
                  (js/console.error "ref attribute should be a function, got:" (type v))))
              ;; style attribute
              (= (name k) "style")
              (if (map? v)
                (doseq [[style-key style-value] v]
                  (.setProperty (.-style el) (name style-key) style-value))
                (.setAttribute el "style" v)) ;; fallback to string
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

(defn reactive-fragment [compute]
  (let [element-marker (js/document.createComment "bud-fragment")
        new-el-reference (atom nil)]
    (js/requestAnimationFrame
      (fn [_]
        (r/effect
          (fn []
            (let [new-content (compute)]
              (when (and @new-el-reference
                         (.-parentElement @new-el-reference))
                (.removeChild (.-parentElement @new-el-reference) @new-el-reference))

              (let [new-el (render new-content)]
                (reset! new-el-reference new-el)

                (when (.-parentElement element-marker)
                  (.insertBefore
                    (.-parentElement element-marker)
                    new-el
                    element-marker))))))))
    element-marker))
