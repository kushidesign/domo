(ns domo.core
  (:require [applied-science.js-interop :as j]
            [clojure.string :as string]))

(defn ^:public maybe->
  "If `(= (pred x) true)`, returns x, otherwise nil.
   Useful in a `clojure.core/some->` threading form."
  [x pred]
  (when (or (true? (pred x))
            (when (set? pred) (contains? pred x)))
    x))

(defn ^:public maybe->>
  "If (= (pred x) true), returns x, otherwise nil.
   Useful in a `clojure.core/some->>` threading form."
  [pred x]
  (when (or (true? (pred x))
            (when (set? pred) (contains? pred x)))
    x))


(defn as-str {:public true :domo/category "Utilities"}
  [x]
  (str (cond
         (string? x)
         x

         (or (keyword? x) (symbol? x)) 
         (name x)

         :else            
         x)))


(defn round-by-dpr {:public true :domo/category "Utilities"}
  [n]
  (let [dpr (or js/window.devicePixelRatio 1)
        ret (/ (js/Math.round (* dpr n)) dpr)]
    ret))


(defn css-style-string 
  {:public true
   :domo/tags [:styles]
   :category "CSS & Styling"}
  [m]
  (string/join ";"
               (map (fn [[k v]]
                      (str (as-str k)
                           ":"
                           (if (number? v) (str v) (as-str v))))
                    m)))

;; Inspiration from:
;; https://gist.github.com/rotaliator/73daca2dc93c586122a0da57189ece13
(defn copy-to-clipboard!
  {:public        true
   :domo/tags     [:utilities]
   :domo/category "Utilities"}
  ([s]
   (copy-to-clipboard! js/document.body s))
  ([el s]
   (when el 
     (let [ta (js/document.createElement "textarea")]
       (set! (.-value ta) s)
       (.setAttribute ta "class" "offscreen")
       (.appendChild el ta)
       (.select ta)
       (js/document.execCommand "copy")
       (.removeChild el ta)))))

;;******************************************************************************














;;******************************************************************************
;; Nodes
;;******************************************************************************

(defn viewport
  "Returns a js object describing the viewport inner-width and inner-height.

   Example:

   (viewport) =>
   {inner-width:                     275
    inner-height-without-scrollbars: 1246
    inner-width-without-scrollbars:  275
    inner-height:                    1246}"
  {:public        true
   :domo/tags     [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  []
  #js {:inner-height                    js/window.innerHeight
       :inner-width                     js/window.innerWidth
       :inner-height-without-scrollbars js/window.innerHeight 
       :inner-width-without-scrollbars  js/document.documentElement.clientWidth})

(defn viewport-map
  "Returns a cljs hashmap describing the viewport inner-width and inner-height.

   Example:

   (viewport) =>
   {:inner-width                     275
    :inner-height-without-scrollbars 1246
    :inner-width-without-scrollbars  275
    :inner-height                    1246}"
  {:public        true
   :domo/tags     [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  []
  {:inner-height                    js/window.innerHeight
   :inner-width                     js/window.innerWidth
   :inner-height-without-scrollbars js/window.innerHeight 
   :inner-width-without-scrollbars  js/document.documentElement.clientWidth})


(defn viewport-x-fraction
  "First argument must be a viewport js object produced from domo.core/viewport.
   Second argument must be a number representing an x coordinate. Returns the
   position as a fraction of the viewport width." 
  {:public        true
   :domo/tags     [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  [vp x]
  (/ x (j/get vp :inner-width-without-scrollbars)))


(defn viewport-y-fraction
  "First argument must be a viewport js object produced from domo.core/viewport.
   Second argument must be a number representing an y coordinate. Returns the
   position as a fraction of the viewport height."
  {:public    true
   :domo/tags [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  [vp y]
  (/ y (j/get vp :inner-height-without-scrollbars)))


(defn- client-rect* [el k]
  (let [vp (viewport)]
    (j/let [^:js {:keys [left right top bottom x y width height]}
            (.getBoundingClientRect el)]
      ((if (= k :js) js-obj hash-map)
       :left       left
       :right      right
       :x-center   (round-by-dpr (- right (/ width 2)))
       :top        top 
       :bottom     bottom
       :y-center   (round-by-dpr (- bottom (/ height 2)))
       :width      width
       :height     height
       :x          x
       :x-fraction (viewport-x-fraction vp x)
       :y          y
       :y-fraction (viewport-y-fraction vp y)
       :vp         (if (= k :js) vp (js->clj vp :keywordize-keys true))))))


(defn client-rect
  "Given an dom node, returns a js-object describing the element's geometry
   relative to the viewport." 
  {:public        true
   :domo/tags     [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  [el]
  (client-rect* el :js))


(defn client-rect-map
  "Given an dom node, returns a cljs map describing the element's geometry
   relative to the viewport." 
  {:public        true
   :domo/tags     [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  [el]
  (client-rect* el :cljs))


(defn- screen-quadrant* [x-fraction y-fraction]
  (let [left? (> 0.5 x-fraction)
        top?  (> 0.5 y-fraction)]
    #js {:y (if top? :top :bottom)
         :x (if left? :left :right)}))


(defn screen-quadrant-from-point
  "Given an x and y value, returns a tuple back representing the viewport
   quadrant which contains the point.

   (screen-quadrant 10 20) => [:top :left]"
  {:public        true
   :domo/tags     [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  [x y]
  (let [vp (viewport)]
    (screen-quadrant* (viewport-x-fraction vp x)
                      (viewport-y-fraction vp y))))


(defn screen-quadrant
  "Given a dom node, returns a tuple representing the viewport quadrant which
   which contains the center of the node.

   (screen-quadrant (domo.core/el-by-id \"my-id\")) => [:top :left]"
  {:public        true
   :domo/tags     [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  [el]
  (let [{:keys [x-fraction y-fraction]} (client-rect el)]
    (screen-quadrant* x-fraction y-fraction)))


(defn distance-between-points [x1 y1 x2 y2]
  {:public        true
   :domo/tags     [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  (js/Math.sqrt (+ (js/Math.pow (- x2 x1) 2)
                   (js/Math.pow (- y2 y1) 2))))


(defn intersecting-client-rects?
  "Expects two js objects representing client-rect instances."
  {:public true :domo/tags [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  [a b]
  (not (or (<= (+ (.-left a) (.-width a)) (.-left b))
           (<= (+ (.-left b) (.-width b)) (.-left a))
           (<= (+ (.-top a) (.-height a)) (.-top b))
           (<= (+ (.-top b) (.-height b)) (.-top a)))))


(defn distance-between-els
  {:public        true
   :domo/tags     [:viewport :geometry]
   :domo/category "Viewport & Geometry"}
  [a b]
  (j/let [^:js {a-left   :left
                a-right  :right
                a-width  :width
                a-top    :top
                a-bottom :bottom
                a-height :height
                :as      a} (.getBoundingClientRect a)
          ^:js {b-left   :left
                b-right  :right
                b-width  :width
                b-top    :top
                b-bottom :bottom
                b-height :height
                :as      b} (.getBoundingClientRect b)]
    (if (intersecting-client-rects? a b)
      nil
      (cond
        ;; check left side areas 
        (<= a-right b-left)
        (cond
          ;; lt corner
          (<= a-bottom b-top)
          (distance-between-points a-right a-bottom b-left b-top)
          
          ;; lb corner
          (>= a-top b-bottom)
          (distance-between-points a-right a-top b-left b-bottom)
          
          ;; l
          :else
          (- b-left a-right))
        
        ;; check right side areas
        (>= a-left b-right)
        (cond
          ;; rt corner
          (<= a-bottom b-top)
          (distance-between-points a-left a-bottom b-right b-top)
          
          ;; rb corner
          (>= a-top b-bottom)
          (distance-between-points a-left a-top b-right b-bottom)
          
          ;; rs
          :else
          (- a-left b-right))
        
        ;; bheck t and b
        :else
        (cond
          ;; t
          (<= a-bottom b-top)
          (- b-top a-bottom)
          
          ;; b
          :else
          (- a-top b-bottom))))))

;;******************************************************************************














;;******************************************************************************
;; Nodes
;;******************************************************************************

(defn parent {:public true :domo/category "Node Selection"} [el] (some-> el .-parentNode))

(defn next-sibling {:public true :domo/category "Node Selection"} [el] (some-> el .-nextElementSibling))

(defn previous-sibling {:public true :domo/category "Node Selection"} [el] (some-> el .-previousElementSibling))

(defn grandparent {:public true :domo/category "Node Selection"} [el] (some-> el .-parentNode .-parentNode))


(defn current-event-target {:public true :domo/category "Events"} [e] (some-> e .-currentTarget))
(def ^:public cet current-event-target)

(defn current-event-target-value {:public true :domo/category "Events"} [e] (some-> e .-currentTarget .-value))
(def ^:public cetv current-event-target-value)

(defn event-target {:public true :domo/category "Events"} [e] (some-> e .-target))
(def ^:public et event-target)

(defn event-target-value {:public true :domo/category "Events"} [e] (some-> e .-target .-value))
(def ^:public etv event-target-value)

(defn event-target-value->int {:public true :domo/category "Events"} [e] (some-> e .-target .-value js/parseInt))
(def ^:public etv->int event-target-value->int)

(defn event-target-value->float {:public true :domo/category "Events"} [e] (some-> e .-target .-value js/parseFloat))
(def ^:public etv->float event-target-value->float)

(defn element-node? 
  "If supplied value is a dom element such as <div>, <span>, etc., returns true,
   else returns false."
  {:public true :domo/category "Events"}
  [el]
  (boolean (some-> el .-nodeType (= 1))))

(defn el-by-id {:public true :domo/category "Events"} [id]
  (js/document.getElementById id))

(defn get-first-onscreen-child-from-top {:public true :domo/category "Events"}
  [el]
  (.find (js/Array.from el.childNodes)
         #(when (element-node? %)
            (-> % .getBoundingClientRect .-top pos?))))

(defn nearest-ancestor {:public true :domo/category "Events"}
  [el sel]
  (when sel (some-> el (.closest sel))))


(defn class-string "Returns the element's class value as a string"
  {:public true :domo/category "CSS & Styling"}
  [el]
  (.getAttribute el "class"))

(defn class-list "Returns the element's classList, which is a DOMTokenList"
  {:public true :domo/category "CSS & Styling"}
  [el]
  (.-classList el))

(defn toggle-class! {:public true :domo/category "CSS & Styling"}
  [el & xs]
  (doseq [x xs] (.toggle (.-classList el) (as-str x))))

(defn remove-class! {:public true :domo/category "CSS & Styling"}
  [el & xs]
  (doseq [x xs] (.remove (.-classList el) (as-str x))))

(defn add-class! {:public true :domo/category "CSS & Styling"}
  [el & xs]
  (doseq [x xs] (.add (.-classList el) (as-str x))))

(defn has-class? {:public true :domo/category "CSS & Styling"}
  [el classname]
  (some-> el .-classList (.contains (as-str classname))))

(defn object-assign {:public true :domo/category "Utilities"}
  [& objs]
  (js/Object.assign.apply nil (into-array objs)))

(defn array-from {:public true :domo/category "Utilities"}
  [iterable]
  (js/Array.from iterable))

;;******************************************************************************














;;******************************************************************************
;; Attributes
;;******************************************************************************

(defn data-attr 
  {:public true :domo/category "Attributes"} [el nm]
  (.getAttribute el (str "data-" (as-str nm))))


(defn attribute-true? 
  {:public true :domo/category "Attributes"} [el attr]
  (when el (= "true" (.getAttribute el (as-str attr)))))


(defn attribute-false? 
  {:public true :domo/category "Attributes"} [el attr] 
  (when el (= "false" (.getAttribute el (as-str attr)))))


(defn has-attribute? 
  {:public true :domo/category "Attributes"} [el attr] 
  (boolean (some-> el (.getAttribute (as-str attr)))))


(defn set-attribute! 
  {:public true :domo/category "Attributes"}
  [el attr v]
  (when el (.setAttribute el (as-str attr) v)))


(defn remove-attribute! 
  {:public true :domo/category "Attributes"}
  [el attr]
  (when el (.removeAttribute el (as-str attr))))


(defn toggle-boolean-attribute! 
  {:public true :domo/category "Attributes"}
  [el attr]
  (let [attr-val (.getAttribute el (as-str attr))
        newv (if (contains? #{"false" nil} attr-val)
               true
               false)]
    (when (contains? #{"false" "true"} attr-val)
      (.setAttribute el
                     (as-str attr)
                     (if (= "false" attr-val)
                       "true"
                       "false")))))


(defn toggle-attribute!
  "Toggles an attribute between provided values a and b, depending on the
   current value of the attribute.
   
   Given the following dom node bound to `el`:
   
   <div data-foo=\"baz\">
   
   (toggle-attribute! el :data-foo :baz :bar)
   
   mutates the dome to:
   <div data-foo=\"bar\">"
  {:public true :domo/category "Attributes"}
  [el attr a b]
  (let [attr (as-str attr)]
    (when-let [current-value (-> el (.getAttribute attr))]
      (let [a (as-str a)
            b (as-str b)]
        (-> el 
            (.setAttribute attr 
                           (if (= current-value a) b a)))))))


(defn- set-style!*
  [el prop s]
  (when el (.setProperty el.style (as-str prop) s)))


(defn set-style!
  {:public true :domo/category "CSS & Styling"}
  ([el m] 
   (when (map? m) (.setAttribute el "style" (css-style-string m))))
  ([el prop s] 
   (set-style!* el prop s)))


(defn el-index
  "Get index of element, relative to its parent"
  {:public true :domo/category "Node Selection"}
  [el]
  (when-let [parent (some-> el .-parentNode)]
    (let [children-array (.from js/Array (.-children parent))]
      (.indexOf children-array el))))

;;******************************************************************************
;;******************************************************************************
















;;******************************************************************************
;; Scrolling
;;******************************************************************************

(defn observe-intersection 
  {:public true :domo/category "Events"}
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


(defn scroll-by! 
  {:public true :domo/category "Events"}
  [{:keys [x y behavior]
    :or   {x        0
           y        0
           behavior "auto"}}]
  (let [behavior (as-str behavior)]
    (j/call
     js/window
     :scrollBy
     #js
      {"top" y "left" x "behavior" behavior})))


(defn scroll-into-view!
  {:public true :domo/category "Events"}
  ([el]
   (scroll-into-view! el {}))
  ([el {:keys [inline block behavior]
        :or {block "start" inline "nearest" behavior "auto"}}]
   (let [opts {"block"    (as-str block)
               "inline"   (as-str inline)
               "behavior" (as-str behavior)}]
     (j/call el :scrollIntoView (clj->js opts)))))


(defn scroll-to-top! {:public true :domo/category "Events"} [] (js/window.scrollTo 0 0))


;; Figure out best heuristic here .-dir vs .-direction vs `writing-mode`(css)
(defn writing-direction 
  {:public true :domo/category "CSS & Styling"}
  []
  (.-direction (js/window.getComputedStyle js/document.documentElement)))


(defn- valid-css-custom-property-name [v]
  (cond (and (string? v) (string/starts-with? v "--"))
        v
        (and (keyword? v) (string/starts-with? (name v) "--"))
        (name v)))



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
    (merge ret m)))


(defn css-custom-property-value-data 
  "Gets computed style for css custom property.
   First checks for the computed style on the element, if supplied.
   If element is not supplied, checks for the computed style on the root html.
   Returns a map of values.
   
   (css-custom-property-value-data my-el \"--sz\") =>
   {:string \"500px\" :value  500 :units  \"px\"}
   

   If the css-custom-property is not set, returns empty string:
   (css-custom-property-value-data my-el \"--szzz\") =>
   {:string \"\"}"
  {:public true :domo/category "CSS & Styling"}
  ([nm]
   (css-custom-property-value-data nil nm))
  ([el nm]
   (when-let [nm (valid-css-custom-property-name nm)]
     (some-> (or el js/document.documentElement)
             js/window.getComputedStyle
             (.getPropertyValue nm)
             value-data))))



(defn css-custom-property-value
  "Gets computed style for css custom property.
   First checks for the computed style on the element, if supplied.
   If element is not supplied, checks for the computed style on the root html.
   Returns a string."
  {:public true :domo/category "CSS & Styling"}
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
  {:public true :domo/category "CSS & Styling"}
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


(defn computed-style-value 
  {:public true :domo/category "CSS & Styling"}
  ([nm]
   (computed-style-value js/document.documentElement nm))
  ([el nm]
   (some-> el js/window.getComputedStyle (j/get nm))))


(defn computed-style-value-data 
  {:public true :domo/category "CSS & Styling"}
  ([nm]
   (computed-style-value-data js/document.documentElement nm))
  ([el nm]
   (some-> el
           js/window.getComputedStyle
           (j/get nm)
           value-data)))


(defn dev-only 
  {:public true :domo/category "Utilities"}
  [x]
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


(defn zip-get 
  "Zipper-esque navigation for the DOM.
   
   The following 4 calls are equivalent:

   (def el (domo.core/el-by-id \"my-id\"))

   (zip-get el \"v > >\")

   (zip-get el [:v :> :>])

   (zip-get app-el '[v > >])

   (zip-get el \"down right right\")

   (zip-get el [:down :right :right])

   (zip-get el '[down right right])"
  {:public true :domo/category "Node Selection"}
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


(defn data-selector
  "(data-selector :foo :bar) => \"[data-foo=\\\"bar\\\"]\"" 
  {:public true :domo/category "Node Selection"}
  ([attr]
   (str "[data-" (as-str attr) "]"))
  ([attr v]
   (str "[data-" (as-str attr) "=\"" (as-str v) "\"]")))


(defn value-selector 
  "(value-selector :baz) => \"[value=\\\"baz\\\"]\""
  {:public true :domo/category "Node Selection"}
  [v]
  (str "[value=\"" (as-str v) "\"]"))


 (defn qs 
   {:public true :domo/category "Node Selection"} 
   ([s]
    (qs js/document s))
   ([el s]
    (.querySelector el s)))

(defn qsa 
  {:public true :domo/category "Node Selection"}
  ([s]
   (qsa js/document s))
  ([el s]
   (.querySelectorAll el s)))


(defn qs-data 
  {:public true :domo/category "Node Selection"}
  ([attr v]
   (qs-data js/document (as-str attr) (as-str v)))
  ([el attr v]
   (some-> el (.querySelector (data-selector (as-str attr) (as-str v))))))


(defn- direct-children-qs-syntax [attr v]
  (str ":scope > *["
       (as-str attr) 
       (when v (str "=\"" (as-str v) "\""))
       "]"))


(defn sibling-with-attribute 
  "Returns the first sibling with attribute match"
  {:public true :domo/category "Node Selection"}
  ([el attr]
   (sibling-with-attribute el attr nil))
  ([el attr v]
   (when-let [sib
              (some-> el
                      (zip-get "^")
                      (qs (direct-children-qs-syntax attr v)))]
     (when-not (= el sib) sib))))


(defn siblings-with-attribute
  "Returns a vector of siblings with attribute matches."
  {:public true :domo/category "Node Selection"}
  ([el attr]
   (siblings-with-attribute el attr nil))
  ([el attr v]
   (some-> el
           (zip-get "^")
           (qsa (direct-children-qs-syntax attr v))
           (js/Array.from)
           (.filter #(not (= el %))))))


(defn focus!
  {:public true :domo/category "Events"} 
  [el]
  (some-> el .focus))


(defn click!
  {:public true :domo/category "Events"}
  [el]
  (some-> el .click))


(defn node-name 
  {:public true :domo/category "Utilities"} 
  [el]
  (some-> el .-nodeName))

;;******************************************************************************














;;******************************************************************************
;; Events
;;******************************************************************************


(defn arrow-keycode? 
  {:public true :domo/category "Events"}
  [e]
  (< 36 e.keyCode 41))


(defn set-caret! 
  {:public true :domo/category "CSS & Styling"}
  [el i]
  (some-> el (.setSelectionRange i i))
  i)


(defn prevent-default!
  {:public true :domo/category "Events"}
  [e] 
  (some-> e .preventDefault))


(defn event-xy 
  "Returns a js array of x and y coords of event." 
  {:public true :domo/category "Events"}
  [e]
  #js [e.clientX e.clientY])


(defn click-xy 
  "Returns a js-array of x and y coords of click event." 
  {:public true :domo/category "Events"}
  [e]
  (event-xy e))


(defn el-from-point
  "Expects x and y viewport coordinates and returns the element found at that
   point." 
  {:public true :domo/category "Node Selection"}
  [x y]
  (.elementFromPoint js/document x y))


(defn duration-property-ms
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

  {:public true :domo/category "CSS & Styling"}
  ([el]
   (duration-property-ms el "transition-duration"))
  ([el property]
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
         ms)))))


(defn keyboard-event! 
  {:public true :domo/category "Events"}
  ([nm]
   (keyboard-event! nm nil))
  ([nm opts]
   (let [nm (as-str nm)
         opts* #js {"view"       js/window
                    "bubbles"    true
                    "cancelable" true}
         opts (if opts
                (.assign js/Object #js {} opts* opts)
                opts*)]
     (new js/KeyboardEvent nm opts))))


(defn mouse-event! 
  {:public true :domo/category "Events"}
  ([nm]
   (mouse-event! nm nil))
  ([nm opts]
   (let [opts* #js {"view" js/window "bubbles" true "cancelable" true}
         opts (if opts
                (.assign js/Object #js {} opts* opts)
                opts*)]
     (new js/MouseEvent (as-str nm) opts))))


(defn dispatch-event! 
  {:public true :domo/category "Events"}
  ([el e]
   (some-> el (.dispatchEvent e))))


(defn add-event-listener! 
  {:public true :domo/category "Events"}
  [el nm f opts]
  (.addEventListener el (as-str nm) f opts))


(defn prefers-reduced-motion? 
  {:public true :domo/category "CSS & Styling"}
  []
  (let [mm (.matchMedia js/window "(prefers-reduced-motion: reduce)")]
    (or (true? mm)
        (.-matches mm))))


(defn dispatch-mousedown-event 
  {:public true :domo/category "Events"}
  ([x y]
   (dispatch-mousedown-event js/document.body x y))
  ([el x y]
   (js/console.log el)
   (js/console.log (dispatch-event! el (mouse-event! :mousedown {:left x :top y})))))


(defn matches-media? 
  "On a desktop:
   (matches-media? \"any-hover\" \"hover\") => true

   On a touch device such as a smartphone that does not support hover:
   (matches-media? \"any-hover\" \"hover\") => false"
  {:public true :domo/category "CSS & Styling"}
  [prop val]
  (when (and prop val)
    (.-matches (js/window.matchMedia (str "(" (as-str prop) ": " (as-str val) ")")))))


(defn media-supports-hover? 
  {:public true :domo/category "CSS & Styling"}
  []
  (boolean (or (matches-media? "any-hover" "hover")
               (matches-media? "hover" "hover"))))


(defn media-supports-touch? 
  {:public true :domo/category "CSS & Styling"}
  []
  (boolean (or (matches-media? "pointer" "none")
               (matches-media? "pointer" "coarse"))))


(defn mouse-down-a11y-map 
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
  {:public true :domo/category "Events"}
  [f & args]
  {:on-key-down   #(when (contains? #{" " "Enter"} (.-key %))
                     (apply f (concat args [%])))
   :on-mouse-down #(when (= 0 (.-button %))
                     (apply f (concat args [%])))})


(defn mouse-down-a11y "Sets up a partial attributes js-obj for using `on-mouse-down` instead of `on-click`.
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
  {:public true :domo/category "Events"}
  [f & args]
  #js {:onkeydown   #(when (contains? #{" " "Enter"} (.-key %))
                       (apply f (concat args [%])))
       :onmousedown #(when (= 0 (.-button %))
                       (apply f (concat args [%])))})


(defn add-class-on-mouse-enter-attrs 
  {:public true :domo/category "Events"} 
  [s]
  #js {:on-mouse-enter #(.add (.-classList (some-> % .-currentTarget)) (as-str s))
       :on-mouse-leave #(.remove (.-classList (some-> % .-currentTarget)) (as-str s))})


(defn add-class-on-mouse-enter-attrs-map 
  {:public true :domo/category "Events"}
  [s]
  {:on-mouse-enter #(.add (.-classList (some-> % .-currentTarget)) (as-str s))
   :on-mouse-leave #(.remove (.-classList (some-> % .-currentTarget)) (as-str s))})


(defn raf
  "Sugar for (js/requestAnimationFrame f)"
  {:public true :domo/category "Utilities"}
  [f]
  (js/requestAnimationFrame f))


(defn fade-in 
  "Orchestrates the fading in of an element." 
  {:public true :domo/category "CSS & Styling"}
  ([el]
   (fade-in el nil))
  ([el {:keys [display opacity duration]}]

   ;; Is this redundant?
   (set-style! el "display" (or (as-str display) "block"))

   (some-> duration
           as-str
           (maybe-> #(or (pos-int? %) (zero? %)))
           (str "ms")
           (->> (set-style! el "transition-duration")))

   ;; Is this redundant?
   (set-style! el "display" (or (as-str display) "block"))

   (raf #(set-style! el "opacity" (or (as-str opacity) "100%")))))


(defn css-duration-value->int
  {:public true :domo/category "CSS & Styling"} [s]
  (let [[n unit] (re-find #"([0-9]+\.?[0-9]*)(m?s)$" s)]
    (when (and n unit)
      (if (= unit "s")
        (js/round (* n 1000))
        n))))


(defn fade-out 
  "Orchestrates the fading out of an element."
  {:public true :domo/category "CSS & Styling"}
  ([el]
   (fade-out el nil))
  ([el {:keys [duration           ; <- pos-int
               ]}]
   (let [supplied-fade-duration
         (maybe-> duration #(or (pos-int? %) (zero? %)))
         
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







