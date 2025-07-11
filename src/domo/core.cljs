(ns domo.core
  (:require [applied-science.js-interop :as j]
            [clojure.string :as string]))

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

(defn ^:public viewport []
  {:inner-height js/window.innerHeight
   :inner-width js/window.innerWidth
   :inner-height-without-scrollbars js/window.innerHeight 
   :inner-width-without-scrollbars js/document.documentElement.clientWidth})

(defn- viewport-x-fraction [x vp]
  (/ x (:inner-width-without-scrollbars vp)))

(defn- viewport-y-fraction [y vp]
  (/ y (:inner-height-without-scrollbars vp)))

;; TODO use x-center and y-center instead of center and y-center
(defn ^:public client-rect [el]
  
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
    [(if top? :top :bottom)
     (if left? :left :right)]))

(defn ^:public screen-quadrant-from-point
  "Pass an x and y val and get a tuple back reprenting the quadrant in which the point lives.
   (screen-quadrant 10 20) => [:top :left]"
  [x y]
  (let [vp (viewport)]
    (screen-quadrant* (viewport-x-fraction x vp)
                      (viewport-y-fraction y vp))))


(defn ^:public screen-quadrant
  "Pass a dom node and get a tuple back reprenting the quadrant in which the center of the node lives.
   (screen-quadrant (js/document.getElementById \"my-id\")) => [:top :left]"
  [node]
  (let [{:keys [x-fraction y-fraction]} (client-rect node)]
    (screen-quadrant* x-fraction y-fraction)))


(defn ^:public parent [node] (some-> node .-parentNode))
(defn ^:public next-element-sibling [node] (some-> node .-nextElementSibling))
(defn ^:public previous-element-sibling [node] (some-> node .-previousElementSibling))
(defn ^:public grandparent [node] (some-> node .-parentNode .-parentNode))
(defn ^:public has-class [node classname] (some-> node .-classList (.contains (name classname))))
(defn ^:public attribute-true? [node attr] (when node (= "true" (.getAttribute node (name attr)))))

(defn ^:public cet [e] (some-> e .-currentTarget))
(defn ^:public cetv [e] (some-> e .-currentTarget .-value))
(defn ^:public et [e] (some-> e .-target))
(defn ^:public etv [e] (some-> e .-target .-value))
(defn ^:public etv->int [e] (some-> e .-target .-value js/parseInt))
(defn ^:public etv->float [e] (some-> e .-target .-value js/parseFloat))


(defn ^:public el-by-id [id]
  (js/document.getElementById id))

;; TODO - do this in pure js interop
(defn ^:public get-first-onscreen-child-from-top
  [el]
  (first 
   (filter
    (fn [node]
      (pos? (.-top (.getBoundingClientRect node))))
    (js->clj el.childNodes))))

(defn ^:public nearest-ancestor
  [node sel]
  (when sel (some-> node (.closest sel))))

(defn ^:public toggle-class!
  [el & xs]
  (doseq [x xs] (.toggle (.-classList el) (name x))))

(defn ^:public remove-class!
  [el & xs]
  (doseq [x xs] (.remove (.-classList el) (name x))))

;; Add check all xs must be strings
(defn ^:public add-class!
  [el & xs]
  (doseq [x xs] (.add (.-classList el) (name x))))

(defn ^:public set-css-var!
  [el prop val]
  (when el (.setProperty el.style prop val)))

(defn ^:public set-client-wh-css-vars!
  [el]
  (when el
    (set-css-var! el "--client-width" (str el.clientWidth "px"))
    (set-css-var! el "--client-height" (str el.clientHeight "px"))))

(defn ^:public set-neg-client-wh-css-vars!
  [el]
  (set-css-var! el "--client-width" (str "-" el.clientWidth "px"))
  (set-css-var! el "--client-height" (str "-" el.clientHeight "px")))

(defn set-style!*
  [el prop s]
  (when el (.setProperty el.style (name prop) s)))

;; optionally take a coll of styles?
(defn ^:public set-style!
  [el prop s]
  (if (coll? el)
    (doseq [x el] (set-style!* x prop s))
    (set-style!* el prop s)))

(defn ^:public set-attribute!
  [el attr v]
  (when el (.setAttribute el (name attr) v)))

(defn ^:public remove-attribute!
  [el attr]
  (when el (.removeAttribute el (name attr))))

(defn ^:public set-property!
  [el attr v]
  (when el (.setProperty el (name attr) v)))

(defn ^:public set!
  [el attr v]
  (when el (j/assoc! el (name attr) v)))

(defn ^:public has-class?
  [el s]
  (when el (.contains (.-classList el) (name s))))

;; check if string or keyword
(defn ^:public has-attribute?
  [el x]
  (some-> el (.hasAttribute (name x))))


(defn ^:public matches-or-nearest-ancestor? 
  [el sel]
  (when (and el sel) 
    (boolean (or (nearest-ancestor el sel)
                 (= et (.matches el sel))))))

(defn ^:public el-idx
  "Get index of element, relative to its parent"
  [el]
  (when-let [parent (some-> el .-parentNode)]
    (let [children-array (.from js/Array (.-children parent))]
      (.indexOf children-array el))))

;; figure out thing
(defn observe-intersection
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

;; figure out thing
(defn scroll-by!
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

;; figure out thing
(defn scroll-into-view!
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
(defn ^:public writing-direction []
  (.-direction (js/window.getComputedStyle js/document.documentElement)))

(defn as-css-custom-property-name [v]
  (when-let [nm* (some-> v as-str)]
    (let [dollar-syntax? (and (keyword? v) (string/starts-with? nm* "$"))
          nm* (if dollar-syntax? (subs nm* 1) nm*)]
      (str (when-not (string/starts-with? nm* "--") "--") nm*))))

(defn ^:public css-custom-property-value-data
  "Gets computed style for css custom property.
   First checks for the computed style on the element, if supplied.
   If element is not supplied, checks for the computed style on the root html.
   Returns a map of values."
  ([nm]
   (css-custom-property-value-data nil nm))
  ([el nm]
   (when-let [nm (as-css-custom-property-name nm)]
     (when-let [s (some-> (or el js/document.documentElement)
                          js/window.getComputedStyle
                          (.getPropertyValue nm))]
       (let [ret {:string s}
             m   (when-let [matches (re-find #"^(\-?[0-9]+(?:(\.)[0-9]+)?)([a-z-_]+)?$"
                                             s)]
                   (let [decimal? (boolean (nth matches 2 nil))
                         value    (nth matches 1 nil)
                         value    (if decimal?
                                    (some-> value js/parseFloat)
                                    (some-> value js/parseInt))]
                     {:value value
                      :units (nth matches 3 nil)}))]
         (merge ret m))))))

(defn ^:public css-custom-property-value
  "Gets computed style for css custom property.
   First checks for the computed sytle on the element, if supplied.
   If element is not supplied, checks for the computed style on the root html.
   Returns a string."
  ([nm]
   (css-custom-property-value nil nm))
  ([el nm]
   (some-> (or el js/document.documentElement)
           js/window.getComputedStyle
           (.getPropertyValue nm))))

;; NEW!
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
  [x]
  (when-let [s (and (or (string? x) (keyword? x))
                    (let [nm (name x)]
                      (when (re-find #"^--\S+$" nm)
                        (some-> nm css-custom-property-value))))]
    (let [[_ ms]   (some->> s (re-find #"^([0-9]+)ms$"))
          [_ secs] (some->> s (re-find #"^([0-9]+)s$"))
          n        (or ms (some-> secs (* 1000)))
          ret      (some-> n js/parseInt)]
      `~ret)))


(defn ^:public computed-style
  ([nm]
   (computed-style js/document.documentElement nm))
  ([el nm]
   (some-> el
           js/window.getComputedStyle
           (j/get nm))))


(defn dev-only [x]
  (when ^boolean js/goog.DEBUG x))

;; Events


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

(defn ^:public zip-get [el steps]
  (reduce (fn [el x]
            (let [k (get zip-nav x x)]
              ;; TODO - Warning here in case x in not one of:
              ;; :parentNode ;; :firstElementChild ;; :nextElementSibling ;; :previousElementSibling
              (some-> el (j/get k nil))))
          el
          (if (string? steps)
            (string/split steps #" ")
            steps)))


;; querySelector
(defn ^:public data-selector= [attr v]
  (str "[data-" (name attr) "=\"" v "\"]"))


(defn ^:public value-selector= [v]
  (str "[value=\"" (str v) "\"]"))


(defn ^:public qs 
  ([s]
   (qs js/document s))
  ([el s]
   (.querySelector el s)))


(defn ^:public qs-data=
  ([attr v]
   (qs-data= js/document attr v))
  ([el attr v]
   (.querySelector el (data-selector= attr (str v)))))


;; toggling attributes
(defn ^:public toggle-boolean-attribute
  [node attr]
  (let [attr-val (.getAttribute node (name attr))
        newv (if (contains? #{"false" nil} attr-val)
               true
               false)]
    (.setAttribute node (name attr) newv)))


;; TODO - should this be children (plural) friendly?
(defn- ^:public get-sibling-with-attribute
  [el attr v]
  (some-> el
          (zip-get "^")
          (qs (str "[" attr "=\"" v "\"]"))))


;; TODO - should this just toggle with all the siblings? 
(defn ^:public toggle-boolean-attribute-sibling
  [el attr]
  (when-not (some-> el (.getAttribute attr) (= "true"))
    (some-> el
            (get-sibling-with-attribute attr "true") 
            (toggle-boolean-attribute attr))
    (toggle-boolean-attribute el attr)))



(defn ^:public toggle-attribute
  [node attr a b]
  (some-> node 
          (.setAttribute (name attr) 
                         (if (= (.getAttribute node (name attr))
                                (str a))
                           (str b)
                           (str a)))))



;; TODO - should this just toggle with all the siblings? 
(defn ^:public toggle-attribute-sibling
  [el attr a b]
  (let [a (str a)
        b (str b)]
    (when-not (some-> el (.getAttribute attr) (= a))
      (some-> el
              (get-sibling-with-attribute attr a) 
              (toggle-attribute attr a b))
      (toggle-attribute el attr a b))))



;;macro?
(defn ^:public focus! [el] (some-> el .focus))

;;macro?
(defn ^:public click! [el] (some-> el .click))

;; data-* attribute
;;macro?
(defn ^:public data-attr [el nm]
  (.getAttribute el (str "data-" (name nm))))

;; node types
(defn ^:public node-is-of-type? [el s]
  (boolean (some-> el .-nodeName string/lower-case (= s))))

(defn ^:public el-type [el]
  (some-> el .-nodeName string/lower-case keyword))

;; keypresses
;;macro?
(defn ^:public arrow-keycode? [e]
  (< 36 e.keyCode 41))

;; text input
(defn ^:public set-caret! [el i]
  (some-> el (.setSelectionRange i i))
  i)

;;macro?
(defn ^:public prevent-default! [e] 
  (some-> e .preventDefault))

;;macro?
(defn ^:public click-xy [e]
  [e.clientX e.clientY])

;;macro?
(defn ^:public el-from-point [x y]
  (.elementFromPoint js/document x y))


(defn ^:public duration-property-ms 
  [el property]
  (let [s      (-> el
                   (computed-style property)
                   (string/split #",")
                   first
                   string/trim)
        factor (cond (string/ends-with? s "ms") 1
                     (string/ends-with? s "s") 1000)
        ms     (when factor
                 (js/Math.round (* factor (js/parseFloat s))))]
    ms))


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

(defn ^:public matches-media?
  [prop val]
  (when (and prop val)
    (.-matches (js/window.matchMedia (str "(" (as-str prop) ": " (as-str val) ")")))))

(defn ^:public media-supports-hover? []
  (boolean (or (matches-media? "any-hover" "hover")
               (matches-media? "hover" "hover"))))

(defn ^:public media-supports-touch? []
  (boolean (or (matches-media? "pointer" "none")
               (matches-media? "pointer" "coarse"))))

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


(defn ^:public on-key-down-tab-navigation
  "If arrow keys are pressed when an element is focused, this will properly
   handle tab navigation. The element which has focus, and all of its siblings
   elements, must have a `role` of `tab`. All the siblings must be direct
   children of an element with a `role` of `tablist`. "
  [e]
  ;; TODO Add safety checks, with warnings:
  ;; - every sibling has the `role` of `tab`
  ;; - parent has the role of `tablist`
  ;; TODO - what if only other sib is disabled?
  (let [key (.-key e)] 
    (when (contains? #{"ArrowRight"
                       "ArrowLeft"
                       "ArrowUp"
                       "ArrowDown"}
                     key) 
      (let [el          (et e)
            next-sib    (next-element-sibling el)
            prev-sib    (previous-element-sibling el)
            tablist-el  (nearest-ancestor
                         el
                         "[role='tablist']")
            orientation (some-> tablist-el
                                (.getAttribute "aria-orientation"))
            sib         (if (= orientation "vertical")
                          (case key
                            "ArrowUp" (or prev-sib next-sib)
                            "ArrowDown" (or next-sib prev-sib))
                          (case key
                            "ArrowRight" (or next-sib prev-sib)
                            "ArrowLeft" (or prev-sib next-sib)))]
        (when sib
          (.click sib)
          (.focus sib))))))

(defn ^:public hover-class-attrs [s]
  {:on-mouse-enter #(add-class! (cet %) s)
   :on-mouse-leave #(remove-class! (cet %) s)})


(defn ^:public raf
  "Sugar for (js/requestAnimationFrame f)"
  [f]
  (js/requestAnimationFrame f))
