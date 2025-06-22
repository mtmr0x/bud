(ns bud.example
  (:require
    [taoensso.telemere :as t]
    [bud.core :as bud]))

(defn footer-component [value]
  ;; the value in the attr-test will not be reactive
  ;; unless you use it in side a reactive-fragment
  [:footer {:attr-test (value)}
   [:p "This is a footer component. " value]])

(defn app []
  (let [[get-value! set-value!] (bud/create-signal {:value "world"})
        [get-string-value! set-string-value!] (bud/create-signal "world")]
    [:div
     ;; string or number values will be rendered as
     ;; reactive text nodes, so you can use them directly
     [:h1 "Hello, " get-string-value! "!"]

     ;; when the value of the signal is not a string or a number,
     ;; it will not be rendered as a reactive text node, so you
     ;; need to enclose it in a reactive-fragment
     (bud/reactive-fragment
       #(do [:h2 (str "This is a reactive app. Current value: " (:value (get-value!)))]) )
     [:p "Type something below:"]

     ;; you can do maps!
     ;; and not be bothered by the keys react error
     (map #(into [] [:div %]) '("a" "b" "c" "d" "e"))

     [:input {:type "text"
              :value (:value (get-value!))
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
           [:p "this only shows if the word in the input is \"world\""]]))]))

(defn ^:dev/after-load start []
  (t/set-min-level! :warn)
  (t/log! :info "Starting app")
  (let [el (js/document.getElementById "app")]
    (bud/dom-render el app)))

(start)
