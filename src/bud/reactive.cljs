(ns bud.reactive)

(def ^:dynamic *context* nil)

(defn mark-signal [f]
  (set! (.-__signal ^js f) true)
  f)

(defn is-signal? [x]
  (and (fn? x)
       (.-__signal ^js x)))

(defn create-signal [initial]
  (let [value (atom initial)
        subscribers (atom #{})
        getter (fn []
                 (when *context*
                   (swap! subscribers conj *context*))
                 @value)
        setter (fn [new-val]
                 ;; this has no aplicable function when
                 ;; the value is not a string or a number
                 ;; but letting it here and I will see how
                 ;; things go and what to do about it.
                 (when (not= new-val @value)
                   (reset! value new-val)
                   (doseq [sub @subscribers]
                     (sub))))]
    [(mark-signal getter) setter]))

(defn effect [f]
  (letfn [(run []
            (binding [*context* run]
              (f)))]
    (run)))
