(ns domo.macros)

;; TODO convert some fns to macros, 
;; TODO create versions of fns that return js objects?

(defmacro ^:public parent [el] `(some-> ~el .-parentNode))

;; BREAKING next-element-sibling -> next-sibling
(defmacro ^:public next-sibling [el] `(some-> ~el .-nextElementSibling))

;; BREAKING previous-element-sibling -> previous-sibling
(defmacro ^:public previous-sibling [el] `(some-> ~el .-previousElementSibling))

(defmacro ^:public grandparent [el] `(some-> ~el .-parentNode .-parentNode))

;; Added
(defmacro ^:public element-node?
  "If supplied value is a dom element such as <div>, <span>, etc., returns true,
   else returns false."
  [el]
  `(boolean (some-> ~el .-nodeType (= 1))))

(defmacro ^:public el-by-id [id]
  `(js/document.getElementById ~id))

(defmacro ^:public get-first-onscreen-child-from-top
  [el]
  `(.find (js/Array.from (.-childNodes ~el))
          #(when (element-node? %)
             (-> % .getBoundingClientRect .-top pos?))))

;; TODO - add checks
(defmacro ^:public nearest-ancestor
  [el sel]
  `(when ~sel (some-> ~el (.closest ~sel))))


;; Added
(defmacro ^:public class-string
  "Returns the element's class value as a string"
  [el]
  `(.getAttribute ~el "class"))

;; Added
(defmacro ^:public class-list
  "Returns the element's classList, which is a DOMTokenList"
  [el]
  `(.-classList ~el))

(defmacro ^:public toggle-class!
  [el & xs]
  `(doseq [x# ~xs] (.toggle (.-classList ~el) (domo.core/as-str x#))))

(defmacro ^:public remove-class!
  [el & xs]
  `(doseq [x# ~xs] (.remove (.-classList ~el) (domo.core/as-str x#))))

(defmacro ^:public add-class!
  [el & xs]
  `(doseq [x# ~xs] (.add (.-classList ~el) (domo.core/as-str x#))))

(defmacro ^:public has-class?
  [el classname]
  `(some-> ~el .-classList (.contains (domo.core/as-str ~classname))))


(defmacro ^:public array-from [iterable]
  `(js/Array.from ~iterable))


(defmacro ^:public data-attr [el nm]
  `(.getAttribute ~el (str "data-" (domo.core/as-str ~nm))))


(defmacro ^:public attribute-true? [el attr]
  `(when ~el (= "true" (.getAttribute ~el (domo.core/as-str ~attr)))))


(defmacro ^:public attribute-false? [el attr] 
  `(when ~el (= "false" (.getAttribute ~el (domo.core/as-str ~attr)))))


(defmacro ^:public has-attribute? [el attr] 
  `(boolean (some-> ~el (.getAttribute (domo.core/as-str ~attr)))))


(defmacro ^:public set-attribute!
  [el attr v]
  `(when ~el (.setAttribute ~el (domo.core/as-str ~attr) ~v)))


(defmacro ^:public remove-attribute!
  [el attr]
  `(when ~el (.removeAttribute ~el (domo.core/as-str ~attr))))


(defmacro ^:public toggle-boolean-attribute!
  [el attr]
  `(let [attr-val# (.getAttribute ~el (domo.core/as-str ~attr))
         newv#     (if (contains? #{"false" nil} attr-val#)
                     true
                     false)]
     (.setAttribute ~el (domo.core/as-str ~attr) newv#)))


;; TODO - test these with reagent
(defmacro ^:public current-event-target [e] `(some-> ~e .-currentTarget))

;; (def ^:public cet current-event-target)

;; Added
(defmacro ^:public current-event-target-value [e] `(some-> ~e .-currentTarget .-value))
;; (def ^:public cetv current-event-target-value)

;; Added
(defmacro ^:public event-target [e] `(some-> ~e .-target))
;; (def ^:public et event-target)

;; Added
(defmacro ^:public event-target-value [e] `(some-> ~e .-target .-value))
;; (def ^:public etv event-target-value)

;; Added
(defmacro ^:public event-target-value->int [e] `(some-> ~e .-target .-value js/parseInt))
;; (def ^:public etv->int event-target-value->int)


;; Added
(defmacro ^:public event-target-value->float [e] `(some-> ~e .-target .-value js/parseFloat))
;; (def ^:public etv->float event-target-value->float)


(defmacro ^:public data-selector= 
  "(data-selector= :foo :bar) => \"[data-foo=\\\"bar\\\"]\""
  [attr v]
  `(str "[data-" (domo.core/as-str ~attr) "=\"" (domo.core/as-str ~v) "\"]"))


(defmacro ^:public value-selector= 
  "(value-selector= :baz) => \"[value=\\\"baz\\\"]\""
  [v]
  `(str "[value=\"" (domo.core/as-str ~v) "\"]"))


(defmacro ^:public qs 
  ([s]
   `(.querySelector js/document ~s))
  ([el s]
   `(.querySelector ~el ~s)))

(defmacro ^:public qsa
  ([s]
   `(.querySelectorAll js/document ~s))
  ([el s]
   `(.querySelectorAll ~el ~s)))

(defmacro ^:public qs-data=
  ([attr v]
   `(some-> js/document
            (.querySelector (data-selector= (domo.core/as-str ~attr) 
                                            (domo.core/as-str ~v)))))
  ([el attr v]
   `(some-> ~el
            (.querySelector (data-selector= (domo.core/as-str ~attr)
                                            (domo.core/as-str ~v))))))

(defmacro ^:public focus! [el]
  `(some-> ~el .focus))

(defmacro ^:public click! [el]
  `(some-> ~el .click))

(defmacro ^:public node-name-match? [el s]
  `(boolean (some-> ~el .-nodeName clojure.string/lower-case (= ~s))))

(defmacro ^:public node-name [el]
  `(some-> ~el .-nodeName))

