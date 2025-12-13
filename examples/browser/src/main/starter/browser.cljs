;; domo API Tour ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;                                                                            ;;
;;  Uncomment various calls within the domo-examples fn below.                ;;
;;  Results are printed to the browser dev console.                           ;;
;;  This should help you get a sense of domo's API.                           ;;
;;                                                                            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns starter.browser
  (:require [starter.browser-tests :as browser-tests :refer [run-tests! S]]
            [clojure.repl]
            [domo.core :as d]
            [clojure.string :as string]
            [domo.macros :as dm]               ; <- some of the fns in domo.core have macro versions for perf
            [applied-science.js-interop :as j] ; <- Bundled with domo
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [fireworks.core :refer [? !?]]))

;; We are overriding some of the default configs for fireworks printing.
;; You normally would not do this, but we will be priting things out to the 
;; browser dev console to demonstrate domo's API, and these non-default config
;; values will help readability for this use-case.
(fireworks.core/config!
 {:format-label-as-code? true
  :template              [:file-info :form-or-label :result]
  :label-length-limit    100})


(defn domo-event-examples [e]
  (? (d/event-target-value e))
  (? (d/event-target-value->int e))
  (? (d/event-target e))
  (? (d/current-event-target-value e))
  (? (d/current-event-target e))
  (? (d/event-target-value->float e)))


(defn- domo-examples []
  (let [c      "red"
        app-el (d/el-by-id "app")
        el-1   (d/el-by-id "1")
        el-2   (d/el-by-id "2")
        el-3   (d/el-by-id "3")
        gc     (d/el-by-id "gc")]


    (comment "utilities")
    ;; (? (d/css-style-string {:color c}))
    

    (comment "Map of event-handlers")
    ;; (? (d/mouse-down-a11y (fn [s] (js/console.log s)) "hi"))
    ;; (? (d/mouse-down-a11y-map (fn [s] (js/console.log s)) "hi"))
    

    (comment "Simulated events")
    ;; (d/click! gc)
    ;; (js/console.clear)
    ;; (dm/click! gc)
    

    (comment "viewport")
    ;; (? (-> (? (d/viewport)) (d/viewport-y-fraction 300)))
    ;; (? (d/screen-quadrant-from-point 10 10))
    ;; (? (d/screen-quadrant (d/el-by-id "3")))
    

    (comment "selecting nodes")
    ;; (? (d/el-by-id "app"))
    ;; (? (d/parent el-2))
    ;; (? (d/next-sibling el-2))
    ;; (? (d/previous-sibling el-2))
    ;; (? (d/grandparent gc))
    ;; (? (d/element-node? gc))
    ;; (? (d/el-index el-3))
    
    (comment "node info")
    ;; (? (d/node-name gc))
    ;; (? (dm/node-name gc))
    
    (comment "should be #1")
    ;; (? (d/get-first-onscreen-child-from-top app-el))
    
    (comment "should be true")
    ;; (? (d/has-class? gc "grandchild"))
    
    (comment "should be app el")
    ;; (? (d/nearest-ancestor el-1 "#app"))
    
    (comment "qs syntax helpers")
    ;; (? (d/data-selector :foo :bar))
    ;; (? (d/data-selector :foo))
    ;; (? (d/value-selector :baz))
    
    (comment "Various selectors")
    ;; (? (d/qs-data :foo :baz))
    ;; (? (d/qs ".grandchild"))
    ;; (? (d/qs app-el "[data-foo=two]" ))
    
    (comment "Sibling selector, 2-arity version, should be #1")
    ;; (? (d/sibling-with-attribute el-3 :data-foo :two))
    
    (comment "Sibling selector, single-arity version, should be #1")
    ;; (? (d/sibling-with-attribute el-3 :data-foo))
    
    (comment "Multiple siblings selector, single-arity version, Should be a vector of 1 div")
    (? :log (let [a 1
                  b "asdfasdfasdfasdfasdfasdfasdfasdfa"
                  c "asdfasdfasdfasdfasdfasdfasdfasdfasd"]
              (d/siblings-with-attribute el-3 :data-bar :ok)))
    
    (comment "zip-get examples, all equivalent")
    ;; (? (d/zip-get app-el "v > >"))
    ;; (? (d/zip-get app-el "down right right"))
    ;; (? (d/zip-get app-el ["v" ">" ">"]))
    ;; (? (d/zip-get app-el '[v > >]))
    ;; (? (d/zip-get app-el [:down :right :right]))
    ;; (? (d/zip-get app-el [:down :right :right]))
    

    (comment "Toggle a single class")
    #_(do (? "The #gc element class value" (d/class-string gc))
          (js/setTimeout 
           (fn []
             (d/toggle-class! gc "foo")
             (? "The #gc element, after toggling the \"foo\" class" (d/class-string gc))
             (js/setTimeout
              (fn []
                (d/toggle-class! gc "foo")
                (? "The #gc element, after toggling \"foo\" again" (d/class-string gc)))
              500))
           500))
    

    (comment "Toggling more than one class")
    #_(do (? "The #gc element class value" (d/class-string gc))
          (js/setTimeout 
           (fn []
             (d/toggle-class! gc "foo" "bar")
             (? "after toggling foo bar" (d/class-string gc))
             (js/setTimeout
              (fn []
                (d/toggle-class! gc "foo" "bar")
                (? "after toggling foo bar again" (d/class-string gc)))
              500))
           500))
    

    (comment "With more than one class as keywords")
    #_(do (? "The #gc element class value" (d/class-string gc))
          (js/setTimeout 
           (fn []
             (d/toggle-class! gc :foo :bar)
             (? "after toggling :foo :bar" (d/class-string gc))
             (js/setTimeout
              (fn []
                (d/toggle-class! gc :foo :bar)
                (? "after toggling :foo :bar again" (d/class-string gc)))
              500))
           500))
    

    (comment "Add and remove classes")
    #_(do (? "The #gc element class value" (d/class-string gc))
          (js/setTimeout 
           (fn []
             (d/remove-class! gc :foo :bar)
             (? "after removing :foo :bar" (d/class-string gc))
             (js/setTimeout
              (fn []
                (d/add-class! gc :foo :bar)
                (? "after adding :foo :bar" (d/class-string gc)))
              500))
           500))

    (comment "Check for a class")
    #_(do (? "The #gc element class value" (d/class-string gc))
          (? (d/has-class? gc "grandchild")))
    

    (comment "Set property, using bundled applied-science.js-interop/assoc!")
    #_(do (? "The #gc element `value` property value" (j/get gc "value"))
          ;; The value of the circular input element in the third box should change
          ;; to "1"
          (j/assoc! gc :value "1"))


    (comment "JS utilities")
    ;; (? (d/object-assign #js{:a "b"} #js{:c 3}))
    ;; (? (d/array-from (d/class-list gc)))
    
    (comment "Add and remove attribute basics")
    ;; (? "d/attribute-true? ... should be true" (d/attribute-true? gc "data-foo"))
    ;; (? "d/attribute-true? ... should be false" (d/attribute-true? el-2 "data-foo"))
    ;; (? "d/attribute-true? ... should be false" (d/attribute-true? el-3 "data-foo"))
    ;; (? "d/attribute-false? ... should be false" (d/attribute-false? gc "data-foo"))
    ;; (? "d/attribute-false? ... should be false" (d/attribute-false? el-2 "data-foo"))
    ;; (? "d/attribute-false? ... should be true" (d/attribute-false? el-3 "data-foo"))
    ;; (? "d/has-attribute? ... should be true" (d/has-attribute? el-1 "data-foo"))
    ;; (? "d/has-attribute? ... should be true" (d/has-attribute? el-3 "data-foo"))
    
    
    (comment "Add and remove attribute")
    #_(do (? "The #app element" app-el)
          (js/setTimeout 
           (fn []
             (d/remove-attribute! app-el :data-foo)
             (? "The #app element, after removing data-foo attribute" app-el)
             (js/setTimeout
              (fn []
                (d/set-attribute! app-el :data-foo "baz")
                (? "The #app element, after setting data-foo attribute" app-el))
              500))
           500))
    

    (comment "Toggle boolean attribute")
    #_(do (? "The #app element" el-3)
          (js/setTimeout 
           (fn []
             (d/toggle-boolean-attribute! el-3 :data-foo)
             (? :log "after toggling data-foo attribute" el-3)
             (js/setTimeout
              (fn []
                (d/toggle-boolean-attribute! el-3 :data-foo)
                (? :log "after toggling data-foo attribute" el-3))
              500))
           500))
    

    (comment "Toggle attribute between 2 values")
    #_(do (? :log app-el)
          (js/setTimeout 
           (fn []
             (d/toggle-attribute! app-el :data-foo :baz :bar)
             (? :log "after toggling data-foo attribute" app-el)
             (js/setTimeout
              (fn []
                (d/toggle-attribute! app-el :data-foo :baz :bar)
                (? :log "after toggling data-foo attribute" app-el)
                )
              500))
           500))
    

    (comment "Various style-related functions")
    #_(let [ms (? (d/duration-property-ms el-1))]
        (d/set-style! el-1 "background-color" "blue")
        (js/setTimeout 
         (fn []
           (? "after changing background-color to blue" el-1)
           (d/set-style! el-1 "background-color" "red")
           (js/setTimeout
            (fn []
              (? "after changing background-color to red" el-1)
              (d/set-style! el-1 "background-color" "revert-layer")
              (js/setTimeout
               (fn []
                 (? "after changing background-color to revert-layer" el-1))
               ms)
              )
            ms))
         ms)

        #_(d/set-style! el-1 "background-color" "revert-layer"))
    

    (comment "Various style helpers")
    ;;  (? (d/css-custom-property-value app-el "--order"))
    ;;  (? (d/css-custom-property-value-data app-el :--order))
    ;;  (? (d/css-custom-property-value app-el "--child-sz"))
    ;;  (? (d/css-custom-property-value-data app-el "--child-sz"))
    ;;  (? (d/token->ms app-el "--dur"))
    ;;  (? (d/computed-style-value app-el "width"))
    ;;  (? (d/computed-style-value-data app-el "width"))
    
    (comment "Should be 3000")
    ;; (? (d/duration-property-ms gc "transition-duration"))
    
    (comment "Element geometry")
    ;; (? (d/client-rect el-1))
    ;; (? (d/client-rect-map el-1))
    ;; (? (d/client-rect el-3))
    ;; (? (d/distance-between-els el-1 el-3))
    ))

;; End of API Tour




(defn main-view []
(r/create-class
   {:component-did-mount
    (fn [_ _]

      (domo-examples)
      
      ;; Uncomment below to run tests, if you are deving on the domo lib.
      ;; Note that if you change the contents of the reagent-render function,
      ;; the tests might fail.
      #_(run-tests!)
      )

    :reagent-render 
    (fn []

      ;; CAUTION ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
      ;;                                                                      ;;
      ;;  All of the API examples in the domo-examples function, as well as   ;;
      ;;  the tests in starter.browser.tests assume the existence of the      ;;
      ;;  HTML structure below, so you probably do not want to change the     ;;
      ;;  structure, or any of the attributes, unless you have a specific     ;;
      ;;  reason.                                                             ;;
      ;;                                                                      ;;
      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

      [:<>
       [:div {:id       "1"
              :data-bar "ok"
              :data-foo ""
              :value    "1"} ]
       [:div {:id       "2"
              :data-foo "two"}]
       [:div
        {:id       "3"
         :data-bar "ok"
         :data-foo "false"}
        [:input (merge {:type     "text"
                        :value    (-> @S count)
                        :readOnly true
                        :on-click domo-event-examples
                        :id       "gc"
                        :class    "grandchild foo bar"
                        :data-foo "true"}
                       (when (-> @S count pos?)
                         {:style {:border "1px solid red"}}))]]])}))


;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (js/console.clear)
  



  #_(? [{:a 1} (new js/Map. #js[#js["b" 2]])])
  ;; (? [(js/Map. #js[#js["a" 1] #js["b" 2]])])
  
  #_(? [(into-array [1 2 3])])

  ;; (? (new js/Set #js["foo" "bar"]))

  #_(? {:label-color :red} #_(+ 1 1)
     [
        (new js/Set #js["foo" "bar"])
        #js {:a 1 :b 2}
        (into-array [1 2 3])
        (js/Map. #js[#js[3 1] #js[4 2]])
        (new js/Int8Array #js[1 2 3])
        ])

  #_(js/console.log (js/Map. #js[#js[3 1] #js[4 2]]))








  (let [root-el (.getElementById js/document "app")]
    (rdom/render [main-view] root-el)))


(defn init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop [] )
