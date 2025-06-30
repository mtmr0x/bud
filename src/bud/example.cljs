(ns bud.example
  (:require
    [taoensso.telemere :as t]
    [bud.core :as bud]))

(defn footer-component [value]
  ;; the value in the attr-test will not be reactive
  ;; unless you use it in side a reactive-fragment
  [:footer {:attr-test (value)}
   [:p "This is a footer component. " value]])

(defn editor-js []
  (let [editor-instance (atom nil)]
    [:div
     [:h2 "EditorJS Example"]
     [:div {:class "editor-js"
            :style "text-align: left;"
            :id "editorjs"
            :ref (fn [el]
                   (when el
                     (let [e (js/EditorJS. #js {:autofocus true})]
                       (reset! editor-instance e))))}]]))

(defn app []
  (let [[get-value! set-value!] (bud/create-signal {:value "world"})
        [get-string-value! set-string-value!] (bud/create-signal "world")]
    [:div {:style {:text-align "center"
                   :padding "20px"}}

     [:h1 {:test get-string-value!}
      "Bud Example App"]
     ;; string or number values will be rendered as
     ;; reactive text nodes, so you can use them directly
     [:h3 "Hello, " get-string-value! "!"]

     ;; when the value of the signal is not a string or a number,
     ;; it will not be rendered as a reactive text node, so you
     ;; need to enclose it in a reactive-fragment
     (bud/reactive-fragment
       (fn []
         [:h2 (str "This is a reactive-fragment Current value: " (:value (get-value!)))]) )
     [:p "Type something below:"]

     ;; you can do maps!
     ;; and not be bothered by the keys react error
     (map #(into [] [:div %]) '("a" "b" "c" "d" "e"))

     [:input {:type "text"
              :value get-string-value!
              :on-input #(do
                            (set-string-value! (.. % -target -value))
                            (set-value! {:value (.. % -target -value)}))}]

     (bud/reactive-fragment
       (fn []
          [:div {:id "test"
                 :attr-test (get-string-value!)}
           [:div [:div
                  [:p "fragment value: " (:value (get-value!))]]]
           [:p
            "this updates with a more complex signal structure using reactive-fragment. "
            "The input value is: " (:value (get-value!))]]))

     (bud/reactive-fragment
       #(when (= (get-string-value!) "world")
          [:div {:attr-test (get-string-value!)}
           [:p "this only shows if the word in the input is \"world\""]]))

     [editor-js]

     ;; get-value! is a signal, so it will be reactive
     ;; in any inner scope since kept as a signal and
     ;; used directly in the DOM as text node. If you
     ;; want to use it as a reactive html attribute,
     ;; follow the example after this one.
     [footer-component get-string-value!]

     ]))

(defn ^:dev/after-load start []
  (t/set-min-level! :warn)
  (t/log! :info "Starting app")
  (let [el (js/document.getElementById "app")]
    (bud/dom-render el app)))

(start)
