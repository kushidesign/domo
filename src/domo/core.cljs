(ns domo.core
  (:require [applied-science.js-interop :as j]
            ;; [fireworks.core :refer [? !? ?> !?>]]
            [clojure.string :as string])
  (:require-macros [domo.core]))

(defn maybe [x pred]
  (when (if (set? pred)
          (contains? pred x)
          (pred x))
    x))

(defn ^:public as-str [x]
  (str (if (or (keyword? x) (symbol? x)) (name x) x)))

(defn ^:public round-by-dpr [n]
  (let [dpr (or js/window.devicePixelRatio 1)
        ret (/ (js/Math.round (* dpr n)) dpr)]
    ret))

(defn ^:public css-style-string [m]
  (string/join ";"
               (map (fn [[k v]]
                      (str (name k)
                           ":"
                           (if (number? v) (str v) (name v))))
                    m)))

;; Culled from:
;; https://gist.github.com/rotaliator/73daca2dc93c586122a0da57189ece13
(defn ^:public copy-to-clipboard!
  ([s]
   (copy-to-clipboard! js/document.body s))
  ([node s]
   (when node 
     (let [el (js/document.createElement "textarea")]
       (set! (.-value el) s)
       (.setAttribute el "class" "offscreen")
       (.appendChild node el)
       (.select el)
       (js/document.execCommand "copy")
       (.removeChild node el)))))


(defn ^:public viewport
  "Returns a map describing the viewport inner-width and inner-height.

   Example:

   (viewport) =>
   {:inner-width                     275
    :inner-height-without-scrollbars 1246
    :inner-width-without-scrollbars  275
    :inner-height                    1246}"
  []
  {:inner-height                    js/window.innerHeight
   :inner-width                     js/window.innerWidth
   :inner-height-without-scrollbars js/window.innerHeight 
   :inner-width-without-scrollbars  js/document.documentElement.clientWidth})


;; BREAkKING!
(defn ^:public viewport-x-fraction 
  "First argument must be a viewport object produced from domo.core/viewport.
   Second argument must be a number representing an x coordinate. Returns the
   position as a fraction of the viewport width."
  [vp x]
  (/ x (:inner-width-without-scrollbars vp)))


;; BREAkKING!
(defn ^:public viewport-y-fraction
  "First argument must be a viewport object produced from domo.core/viewport.
   Second argument must be a number representing an y coordinate. Returns the
   position as a fraction of the viewport height."
  [vp y]
  (/ y (:inner-height-without-scrollbars vp)))


(defn ^:public client-rect 
  "Given an dom node, returns a map describing the element's geometry relative
   to the viewport."
  [el]
  (let [vp (viewport)]
    (j/let [^:js {:keys [left right top bottom x y width height]}
            (.getBoundingClientRect el)]
      {:left       left
       :right      right
       :x-center   (round-by-dpr (- right (/ width 2)))
       :top        top 
       :bottom     bottom
       :y-center   (round-by-dpr (- bottom (/ height 2)))
       :width      width
       :height     height
       :x          x
       :x-fraction (viewport-x-fraction x vp)
       :y          y
       :y-fraction (viewport-y-fraction y vp)
       :vp         vp})))


(defn- screen-quadrant* [x-fraction y-fraction]
  (let [left? (> 0.5 x-fraction)
        top?  (> 0.5 y-fraction)]
    {:y (if top? :top :bottom)
     :x (if left? :left :right)}))


;; Breaking! Now returns map instead of tuple? e.g {:x :left :y :top}
(defn ^:public screen-quadrant-from-point
  "Given an x and y value, returns a tuple back representing the viewport
   quadrant which contains the point.

   (screen-quadrant 10 20) => [:top :left]"
  [x y]
  (let [vp (viewport)]
    (screen-quadrant* (viewport-x-fraction vp x)
                      (viewport-y-fraction vp y))))


;; Breaking! Now returns map instead of tuple? e.g {:x :left :y :top}
(defn ^:public screen-quadrant
  "Given a dom node returns a tuple representing the viewport quadrant which
   which contains the center of the node.

   (screen-quadrant (d/el-by-id \"my-id\")) => [:top :left]"
  [node]
  (let [{:keys [x-fraction y-fraction]} (client-rect node)]
    (screen-quadrant* x-fraction y-fraction)))


(defn ^:public parent [node] (some-> node .-parentNode))

;; BREAKING next-element-sibling -> next-sibling
(defn ^:public next-sibling [node] (some-> node .-nextElementSibling))

;; BREAKING prvious-element-sibling -> previous-sibling
(defn ^:public previous-sibling [node] (some-> node .-previousElementSibling))

(defn ^:public grandparent [node] (some-> node .-parentNode .-parentNode))


;; TODO - test these with reagent
(defn ^:public current-event-target [e] (some-> e .-currentTarget))
(def ^:public cet current-event-target)

(defn ^:public current-event-target-value [e] (some-> e .-currentTarget .-value))
(def ^:public cetv current-event-target-value)

(defn ^:public event-target [e] (some-> e .-target))
(def ^:public et event-target)

(defn ^:public event-target-value [e] (some-> e .-target .-value))
(def ^:public etv event-target-value)

(defn ^:public event-target-value->int [e] (some-> e .-target .-value js/parseInt))
(def ^:public etv->int event-target-value->int)

(defn ^:public event-target-value->float [e] (some-> e .-target .-value js/parseFloat))
(def ^:public etv->float event-target-value->float)

(defn ^:public element-node?
  "If supplied value is a dom element such as <div>, <span>, etc., returns true,
   else returns false."
  [el]
  (boolean (some-> el .-nodeType (= 1))))

(defn ^:public el-by-id [id]
  (js/document.getElementById id))

(defn ^:public get-first-onscreen-child-from-top
  [el]
  (.find (js/Array.from el.childNodes)
         #(when (element-node? %)
            (-> % .getBoundingClientRect .-top pos?))))

;; TODO - add safety
(defn ^:public nearest-ancestor
  [el sel]
  (when sel (some-> el (.closest sel))))

(defn ^:public toggle-class!
  [el & xs]
  (doseq [x xs] (.toggle (.-classList el) (as-str x))))

(defn ^:public remove-class!
  [el & xs]
  (doseq [x xs] (.remove (.-classList el) (as-str x))))

(defn ^:public add-class!
  [el & xs]
  (doseq [x xs] (.add (.-classList el) (as-str x))))

;; BREAKING removed set-css-var!

;; (defn ^:public set-client-wh-css-vars!
;;   [el]
;;   (when el
;;     (set-css-var! el "--client-width" (str el.clientWidth "px"))
;;     (set-css-var! el "--client-height" (str el.clientHeight "px"))))

;; (defn ^:public set-neg-client-wh-css-vars!
;;   [el]
;;   (set-css-var! el "--client-width" (str "-" el.clientWidth "px"))
;;   (set-css-var! el "--client-height" (str "-" el.clientHeight "px")))

(defn ^:public object-assign [& objs]
  (js/Object.assign.apply nil (.concat #js[] (into-array objs))))



;;   /$$$$$$    /$$     /$$               /$$ /$$                   /$$                        
;;  /$$__  $$  | $$    | $$              |__/| $$                  | $$                        
;; | $$  \ $$ /$$$$$$ /$$$$$$    /$$$$$$  /$$| $$$$$$$  /$$   /$$ /$$$$$$    /$$$$$$   /$$$$$$$
;; | $$$$$$$$|_  $$_/|_  $$_/   /$$__  $$| $$| $$__  $$| $$  | $$|_  $$_/   /$$__  $$ /$$_____/
;; | $$__  $$  | $$    | $$    | $$  \__/| $$| $$  \ $$| $$  | $$  | $$    | $$$$$$$$|  $$$$$$ 
;; | $$  | $$  | $$ /$$| $$ /$$| $$      | $$| $$  | $$| $$  | $$  | $$ /$$| $$_____/ \____  $$
;; | $$  | $$  |  $$$$/|  $$$$/| $$      | $$| $$$$$$$/|  $$$$$$/  |  $$$$/|  $$$$$$$ /$$$$$$$/
;; |__/  |__/   \___/   \___/  |__/      |__/|_______/  \______/    \___/   \_______/|_______/ 
;;                                                                                             
;;                                                                                             
;;                                                                                             

;; data-* attribute

(defn ^:public data-attr [el nm]
  (.getAttribute el (str "data-" (name nm))))


(defn ^:public attribute-true? [el attr]
  (when el (= "true" (.getAttribute el (as-str attr)))))


(defn ^:public attribute-false? [el attr] 
  (when el (= "false" (.getAttribute el (as-str attr)))))


(defn ^:public has-attribute? [el attr] 
  (boolean (some-> el (.getAttribute (as-str attr)))))


(defn ^:public set-attribute!
  [el attr v]
  (when el (.setAttribute el (as-str attr) v)))


(defn ^:public remove-attribute!
  [el attr]
  (when el (.removeAttribute el (as-str attr))))


(defn ^:public toggle-boolean-attribute!
  [node attr]
  (let [attr-val (.getAttribute node (name attr))
        newv (if (contains? #{"false" nil} attr-val)
               true
               false)]
    (.setAttribute node (name attr) newv)))


;; BREAKING CHANGE - toggle-attribute -> toggle-attribute!
(defn ^:public toggle-attribute!
  "Toggles an attribute between provided values a and b, depending on the
   current value of the attribute.
   
   Given the following dom node bound to `el`:
   
   <div data-foo=\"baz\">
   
   (toggle-attribute! el :data-foo :baz :bar)
   
   mutates the dome to:
   <div data-foo=\"bar\">"
  [node attr a b]
  (let [attr (as-str attr)]
    (when-let [current-value (-> node (.getAttribute attr))]
      (let [a (as-str a)
            b (as-str b)]
        (-> node 
            (.setAttribute attr 
                           (if (= current-value a) b a)))))))


(defn- set-style!*
  [el prop s]
  (when el (.setProperty el.style (as-str prop) s)))

;; BREAKING - no coll for el, added 1-arity for map
(defn ^:public set-style!
  ([el m] 
   (when (map? m) (set-attribute! el "style" (css-style-string m))))
  ([el prop s] 
   (set-style!* el prop s)))

;; TODO - Add merge-style!



(defn ^:public set-property!
  [el attr v]
  (when el (.setProperty el (as-str attr) v)))

;; BREAKING - removed set! , shadowed set!


;; BREAKING has-class -> has-class? 
(defn ^:public has-class?
  [node classname]
  (some-> node .-classList (.contains (as-str classname))))


;; BREAKING - removed matches-or-has-nearest-ancestor?
;; TODO - Add safety


;; BREAKING - el-idx -> el-index
(defn ^:public el-index
  "Get index of element, relative to its parent"
  [el]
  (when-let [parent (some-> el .-parentNode)]
    (let [children-array (.from js/Array (.-children parent))]
      (.indexOf children-array el))))





;;   /$$$$$$   /$$$$$$  /$$$$$$$   /$$$$$$  /$$       /$$       /$$$$$$ /$$   /$$  /$$$$$$ 
;;  /$$__  $$ /$$__  $$| $$__  $$ /$$__  $$| $$      | $$      |_  $$_/| $$$ | $$ /$$__  $$
;; | $$  \__/| $$  \__/| $$  \ $$| $$  \ $$| $$      | $$        | $$  | $$$$| $$| $$  \__/
;; |  $$$$$$ | $$      | $$$$$$$/| $$  | $$| $$      | $$        | $$  | $$ $$ $$| $$ /$$$$
;;  \____  $$| $$      | $$__  $$| $$  | $$| $$      | $$        | $$  | $$  $$$$| $$|_  $$
;;  /$$  \ $$| $$    $$| $$  \ $$| $$  | $$| $$      | $$        | $$  | $$\  $$$| $$  \ $$
;; |  $$$$$$/|  $$$$$$/| $$  | $$|  $$$$$$/| $$$$$$$$| $$$$$$$$ /$$$$$$| $$ \  $$|  $$$$$$/
;;  \______/  \______/ |__/  |__/ \______/ |________/|________/|______/|__/  \__/ \______/ 
;;                                                                                         



;; TODO - Add tests
(defn ^:public observe-intersection
  [{:keys [element
           intersecting
           not-intersecting
           f
           threshold
           root-margin]
    :or {threshold 0.1
         root-margin "0px"}}]
  (when element
    (let [observer (js/IntersectionObserver.
                    (fn [^js entries]
                      (if (.-isIntersecting (aget entries 0))
                        (when intersecting 
                          (intersecting))
                        (when not-intersecting (not-intersecting)))
                      (when f (f)))
                    #js {:threshold threshold
                         :rootMargin root-margin})]
      (when observer
        (.observe observer element)))))


;; TODO - Add tests 
(defn ^:public scroll-by!
  [{:keys [x y behavior]
    :or   {x        0
           y        0
           behavior "auto"}}]
  (let [behavior (name behavior)]
    (j/call
     js/window
     :scrollBy
     #js
      {"top" y "left" x "behavior" behavior})))


;; TODO - Add tests 
;; Add checks for values
(defn ^:public scroll-into-view!
  ([el]
   (scroll-into-view! el {}))
  ([el {:keys [inline block behavior]
        :or {block "start" inline "nearest" behavior "auto"}}]
   (let [opts {"block"    (name block)
               "inline"   (name inline)
               "behavior" (name behavior)}]
     (j/call el :scrollIntoView (clj->js opts)))))


(defn ^:public scroll-to-top! [] (js/window.scrollTo 0 0))


;; Figure out best heuristic here .-dir vs .-direction vs `writing-mode`(css)
(defn ^:public writing-direction
  []
  (.-direction (js/window.getComputedStyle js/document.documentElement)))


;; BREAKING CHANGE - removed as-css-custom-property name



;; Added this
(defn- valid-css-custom-property-name [v]
  (cond (and (string? v) (string/starts-with? v "--"))
        v
        (and (keyword? v) (string/starts-with? (name v) "--"))
        (name v)))

;; Added this
(defn- value-data [s]
  (let [ret {:string s}
        m   (when-let [matches 
                       (re-find #"^(\-?[0-9]+(?:(\.)[0-9]+)?)([a-z-_]+)?$"
                                s)]
              (let [decimal? (boolean (nth matches 2 nil))
                    value    (nth matches 1 nil)
                    value    (if decimal?
                               (some-> value js/parseFloat)
                               (some-> value js/parseInt))]
                {:unitless-value value
                 :units          (nth matches 3 nil)}))]
    (merge ret m))
  )

;; BREAKING CHANGE :value to :unitless-value
(defn ^:public css-custom-property-value-data
  "Gets computed style for css custom property.
   First checks for the computed style on the element, if supplied.
   If element is not supplied, checks for the computed style on the root html.
   Returns a map of values.
   
   (css-custom-property-value-data my-el \"--sz\") =>
   {:string \"500px\" :value  500 :units  \"px\"}
   

   If the css-custom-property is not set, returns empty string:
   (css-custom-property-value-data my-el \"--szzz\") =>
   {:string \"\"}"

  ([nm]
   (css-custom-property-value-data nil nm))
  ([el nm]
   (when-let [nm (valid-css-custom-property-name nm)]
     (some-> (or el js/document.documentElement)
             js/window.getComputedStyle
             (.getPropertyValue nm)
             value-data))))


;; BREAKING CHANGE - stricter input
(defn ^:public css-custom-property-value
  "Gets computed style for css custom property.
   First checks for the computed style on the element, if supplied.
   If element is not supplied, checks for the computed style on the root html.
   Returns a string."
  ([nm]
   (css-custom-property-value nil nm))
  ([el nm]
   (when-let [nm (valid-css-custom-property-name nm)]
     (or (some-> el
                 js/window.getComputedStyle
                 (.getPropertyValue nm))
         (some-> js/document.documentElement
                 js/window.getComputedStyle
                 (.getPropertyValue nm))))))


(defn token->ms
  "Expects a key or string which maps to an existing design token (css custom
   property). If the value of the token is a valid (css) microseconds or seconds
   unit, an integer representing the number of microseconds will be returned.

   Example:

   /* css */
   :root {
     --xxfast: 100;
   }

   ;; cljs
   (token->ms \"--xxfast\") ; => 100
   (token->ms :--xxfast)    ; => 100
   (token->ms 42)           ; => nil
   "
  ([x]
   (token->ms js/document.documentElement
              x))
  ([el x] 
   (when el
     (when-let [s (some->> x
                           valid-css-custom-property-name
                           (css-custom-property-value el))]
       (let [[_ ms]   (some->> s (re-find #"^([0-9]+)ms$"))
             [_ secs] (some->> s (re-find #"^([0-9]+)s$"))
             n        (or ms (some-> secs (* 1000)))
             ret      (some-> n js/parseInt)]
         ret)))))


;; BREAKING CHANGE - computed-style -> computed-style-value
(defn ^:public computed-style-value
  ([nm]
   (computed-style-value js/document.documentElement nm))
  ([el nm]
   (some-> el
           js/window.getComputedStyle
           (j/get nm))))



;; ADDED this
(defn ^:public computed-style-value-data
  ([nm]
   (computed-style-value-data js/document.documentElement nm))
  ([el nm]
   (some-> el
           js/window.getComputedStyle
           (j/get nm)
           value-data)))


(defn ^:public dev-only [x]
  (when ^boolean js/goog.DEBUG x))


;; Primitive Zipper navigation
(def zip-nav 
  {"^"     :parentNode
   "up"    :parentNode

   "v"     :firstElementChild
   "down"  :firstElementChild

   ">"     :nextElementSibling
   "right" :nextElementSibling

   "<"     :previousElementSibling
   "left"  :previousElementSibling
   })


(defn ^:public zip-get
  "Zipper-esque navigation for the DOM.
   
   The following 4 calls are equivalent:

   (def el (domo.core/el-by-id \"my-id\"))

   (zip-get el \"v > >\")

   (zip-get el [:v :> :>])

   (zip-get app-el '[v > >])

   (zip-get el \"down right right\")

   (zip-get el [:down :right :right])

   (zip-get el '[down right right])"
  [el steps]
  (reduce (fn [el x]
            (let [k (get zip-nav (as-str x) x)]
              ;; TODO - Warning here in case x in not one of:
              ;; :parentNode ;; :firstElementChild ;; :nextElementSibling ;; :previousElementSibling
              (some-> el (j/get k nil))))
          el
          (if (string? steps)
            (string/split steps #" ")
            steps)))


;; querySelector
(defn ^:public data-selector= 
  "(data-selector= :foo :bar) => \"[data-foo=\\\"bar\\\"]\""
  [attr v]
  (str "[data-" (as-str attr) "=\"" (as-str v) "\"]"))


(defn ^:public value-selector= 
  "(value-selector= :baz) => \"[value=\\\"baz\\\"]\""
  [v]
  (str "[value=\"" (as-str v) "\"]"))


;macro?
(defn ^:public qs 
  ([s]
   (qs js/document s))
  ([el s]
   (.querySelector el s)))

;macro?
(defn ^:public qsa
  ([s]
   (qsa js/document s))
  ([el s]
   (.querySelectorAll el s)))


(defn ^:public qs-data=
  ([attr v]
   (qs-data= js/document (as-str attr) (as-str v)))
  ([el attr v]
   (some-> el (.querySelector (data-selector= (as-str attr) (as-str v))))))

(defn- direct-children-qs-syntax [attr v]
  (str ":scope > *["
       (as-str attr) 
       (when v (str "=\"" (as-str v) "\""))
       "]"))

(defn ^:public sibling-with-attribute
  "Returns the first sibling with attribute match"
  ([el attr]
   (sibling-with-attribute el attr nil))
  ([el attr v]
   (when-let [sib
              (some-> el
                      (zip-get "^")
                      (qs (direct-children-qs-syntax attr v)))]
     (when-not (= el sib) sib))))

(defn ^:public siblings-with-attribute
  "Returns a vector of siblings with attribute matches."
  ([el attr]
   (siblings-with-attribute el attr nil))
  ([el attr v]
   (some-> el
           (zip-get "^")
           (qsa (direct-children-qs-syntax attr v))
           (->> (reduce (fn [acc sibling]
                          (if (= el sibling) acc (conj acc sibling)))
                        [])))))


;; BREAKING CHANGE - Removed toggle-boolean-attribute-sibling
;; BREAKING CHANGE - toggle-attribute-sibling

;;macro?
(defn ^:public focus! [el] (some-> el .focus))


;;macro?
(defn ^:public click! [el] (some-> el .click))


;; node types
(defn ^:public node-is-of-type? [el s]
  (boolean (some-> el .-nodeName string/lower-case (= s))))


(defn ^:public el-type [el]
  (some-> el .-nodeName string/lower-case keyword))




;;  /$$$$$$$$ /$$    /$$ /$$$$$$$$ /$$   /$$ /$$$$$$$$ /$$$$$$ 
;; | $$_____/| $$   | $$| $$_____/| $$$ | $$|__  $$__//$$__  $$
;; | $$      | $$   | $$| $$      | $$$$| $$   | $$  | $$  \__/
;; | $$$$$   |  $$ / $$/| $$$$$   | $$ $$ $$   | $$  |  $$$$$$ 
;; | $$__/    \  $$ $$/ | $$__/   | $$  $$$$   | $$   \____  $$
;; | $$        \  $$$/  | $$      | $$\  $$$   | $$   /$$  \ $$
;; | $$$$$$$$   \  $/   | $$$$$$$$| $$ \  $$   | $$  |  $$$$$$/
;; |________/    \_/    |________/|__/  \__/   |__/   \______/ 
;;                                                             



;; keypresses
;;macro?
(defn ^:public arrow-keycode? [e]
  (< 36 e.keyCode 41))


;; text input
;; TODO add checks
(defn ^:public set-caret! [el i]
  (some-> el (.setSelectionRange i i))
  i)


;;macro?
(defn ^:public prevent-default! [e] 
  (some-> e .preventDefault))

(defn ^:public event-xy 
  "Returns vector of x and y coords of event."
  [e]
  [e.clientX e.clientY])


;;macro?
(defn ^:public click-xy 
  "Returns vector of x and y coords of click event."
  [e]
  (event-xy e))


;;macro?
(defn ^:public el-from-point 
  "Expects x and y viewport coordinates and returns the element found at that
   point."
  [x y]
  (.elementFromPoint js/document x y))


;; TODO - add check, with warning
(defn ^:public duration-property-ms 
  "Given a dom node and a style property, returns the computed style value of
   that property, in milliseconds.
   
   Properties that take a time value:
   transition-duration
   transition-delay
   animation-duration
   animation-delay
   animation-iteration-count
   transition
   animation"
  [el property]
  (let [s (as-str property)]
    (when (contains? #{"transition-duration"
                       "transition-delay"
                       "animation-duration"
                       "animation-delay"
                       "animation-iteration-count"
                       "transition"
                       "animation"}
                     s)
     (let [s      (-> el
                      (computed-style-value property)
                      (string/split #",")
                      first
                      string/trim)
           factor (cond (string/ends-with? s "ms") 1
                        (string/ends-with? s "s") 1000)
           ms     (when factor
                    (js/Math.round (* factor (js/parseFloat s))))]
       ms))))


(defn ^:public keyboard-event!
  ([nm]
   (keyboard-event! nm nil))
  ([nm opts]
   (let [nm (name nm)
         opts* #js {"view"       js/window
                    "bubbles"    true
                    "cancelable" true}
         opts (if opts
                (.assign js/Object #js {} opts* opts)
                opts*)]
     (new js/KeyboardEvent nm opts))))


(defn ^:public mouse-event!
  ([nm]
   (mouse-event! nm nil))
  ([nm opts]
   (let [opts* #js {"view" js/window "bubbles" true "cancelable" true}
         opts (if opts
                (.assign js/Object #js {} opts* opts)
                opts*)]
     (new js/MouseEvent (name nm) opts))))


(defn ^:public dispatch-event!
  ([el e]
   (some-> el (.dispatchEvent e))))


(defn ^:public add-event-listener! [el nm f opts]
  (.addEventListener el (name nm) f opts))


(defn ^:public prefers-reduced-motion? []
  (let [mm (.matchMedia js/window "(prefers-reduced-motion: reduce)")]
    (or (true? mm)
        (.-matches mm))))


(defn ^:public dispatch-mousedown-event
  ([x y]
   (dispatch-mousedown-event js/document.body x y))
  ([el x y]
   (js/console.log el)
   (js/console.log (dispatch-event! el (mouse-event! :mousedown {:left x :top y})))))


(defn ^:public matches-media?
  "On a desktop:
   (matches-media? \"any-hover\" \"hover\") => true

   On a touch device such as a smartphone that does not support hover:
   (matches-media? \"any-hover\" \"hover\") => false
   "
  [prop val]
  (when (and prop val)
    (.-matches (js/window.matchMedia (str "(" (as-str prop) ": " (as-str val) ")")))))


(defn ^:public media-supports-hover? []
  (boolean (or (matches-media? "any-hover" "hover")
               (matches-media? "hover" "hover"))))


(defn ^:public media-supports-touch? []
  (boolean (or (matches-media? "pointer" "none")
               (matches-media? "pointer" "coarse"))))


;; Investigate a11y alignment between mousedown and keydown
(defn ^:public mouse-down-a11y
  "Sets up a partial attributes map for using `on-mouse-down` instead of `on-click`.
   Intended for buttons, switches, checkboxes, radios, etc.

   The function passed in may accept any number of args, but the last arg needs 
   to be the event.

   Contrived example with reagent:

  (defn sidenav-item-handler [label modal? e]
    (domo/scroll-into-view!
    (domo/qs-data= \"foo-bar\" label))
    (domo/scroll-by! {:y -50})
    (when modal?
      (dismiss-popover! e)))
   
  (defn my-reagent-component
    [{:keys [coll modal?]}]
    (into [:ul]
          (for [{:keys [label]} coll]
            [:li 
            [button
              (merge-attrs
              (sx :.pill
                  ...)
              (mouse-down-a11y sidenav-item-handler label modal?))
              label]])))"
  [f & args]
  {:on-key-down   #(when (contains? #{" " "Enter"} (.-key %))
                     (apply f (concat args [%])))
   :on-mouse-down #(when (= 0 (.-button %))
                     (apply f (concat args [%])))})

(defn ^:public mouse-down-a11y-js
  "Sets up a partial attributes js-obj for using `on-mouse-down` instead of `on-click`.
   Intended for buttons, switches, checkboxes, radios, etc.

   The function passed in may accept any number of args, but the last arg needs 
   to be the event.

   Contrived example with reagent:

  (defn sidenav-item-handler [label modal? e]
    (domo/scroll-into-view!
    (domo/qs-data= \"foo-bar\" label))
    (domo/scroll-by! {:y -50})
    (when modal?
      (dismiss-popover! e)))
   
  (defn my-reagent-component
    [{:keys [coll modal?]}]
    (into [:ul]
          (for [{:keys [label]} coll]
            [:li 
            [button
              (merge-attrs
              (sx :.pill
                  ...)
              (mouse-down-a11y sidenav-item-handler label modal?))
              label]])))"
  [f & args]
  #js {:onkeydown   #(when (contains? #{" " "Enter"} (.-key %))
                       (apply f (concat args [%])))
       :onmousedown #(when (= 0 (.-button %))
                       (apply f (concat args [%])))})


;; Breaking hover-class-attrs -> add-class-on-mouse-enter-attrs
(defn ^:public add-class-on-mouse-enter-attrs 
  [s]
  {:on-mouse-enter #(add-class! (cet %) s)
   :on-mouse-leave #(remove-class! (cet %) s)})


(defn ^:public raf
  "Sugar for (js/requestAnimationFrame f)"
  [f]
  (js/requestAnimationFrame f))


(defn ^:public fade-in 
  "Orchestrates the fading in of an element."
  ([el]
   (fade-in el nil))
  ([el {:keys [display opacity duration]}]

   ;; Is this redundant?
   (set-style! el "display" (or (as-str display) "block"))

   (some-> duration
           as-str
           (maybe #(or (pos-int? %) (zero? %)))
           (str "ms")
           (->> (set-style! el "transition-duration")))

   ;; Is this redundant?
   (set-style! el "display" (or (as-str display) "block"))

   (raf #(set-style! el "opacity" (or (as-str opacity) "100%")))))


(defn ^:public css-duration-value->int [s]
  (let [[n unit] (re-find #"([0-9]+\.?[0-9]*)(m?s)$" s)]
    (when (and n unit)
      (if (= unit "s")
        (js/round (* n 1000))
        n))))


(defn ^:public fade-out
  "Orchestrates the fading out of an element."
  ([el]
   (fade-out el nil))
  ([el {:keys [duration           ; <- pos-int
               ]}]
   (let [supplied-fade-duration
         (maybe duration #(or (pos-int? %) (zero? %)))
         
         computed-fade-duration
         (some-> el
                 (computed-style-value "transition-duration")
                 css-duration-value->int)

         fade-duration (or supplied-fade-duration computed-fade-duration 250)]

    ;; (? {:supplied-fade-duration supplied-fade-duration
    ;;     :computed-fade-duration computed-fade-duration
    ;;     :fade-duration          fade-duration})

    ;; Set fade duration if one is provided, or if no computed-transition-duration is resolved.
     (some-> (or supplied-fade-duration (when-not computed-fade-duration fade-duration))
             (str "ms")
             (->> (set-style! el "transition-duration")))
     (set-style! el "opacity" "0")
     (js/setTimeout #(set-style! el "display" "none")
                    (or fade-duration fade-duration)))))
