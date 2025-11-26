(ns starter.browser-tests
  (:require [domo.core :as d]
            ;; [fireworks.core :refer [? !? ?> !?>]]
            [clojure.string :as string]
            [reagent.core :as r]
            [bling.core :as bling]
            [bling.hifi :as hifi])
  (:require-macros [starter.browser-tests :rename {browser-test bt}]))

(def S (r/atom []))

;; Makeshift browser test suite
;; If tests fail, the circle input elemnent on the page receives a red border,
;; and the value is populated with the number of failed tests.
;; A report for each failure is printed to the browser dev console.
(defn run-tests! []
  (let [app-el (d/el-by-id "app")
        el-1   (d/el-by-id "1")
        el-2   (d/el-by-id "2")
        el-3   (d/el-by-id "3")
        gc     (d/el-by-id "gc")]

    (reset! S [])

    (bt (d/css-style-string {:color "red" :font-size "12px"})
        "color:red;font-size:12px")
    (bt (d/get-first-onscreen-child-from-top app-el) el-1)
    (bt (d/has-class? gc "grandchild") true)
    (bt (d/next-sibling el-2) el-3)
    (bt (d/previous-sibling el-2) el-1)
    (bt (d/grandparent gc) app-el)
    (bt (d/element-node? gc) true)

    (when (seq @S)
      (doseq [{:keys [f] :as m} @S]
        (bling/callout {}
                       (bling/bling [:red.bold "Domo: browser test failed"])
                       "\n\n"
                       (hifi/hifi f)
                       "\n\n"
                       (hifi/hifi (dissoc m :f)) )
        (println "\n\n")))))
     

;; Call this from starter.browser/main-view
;; (? (browser-tests/list-of-public-domo-fns-by-category-in-markdown))
(defn list-of-public-domo-fns-by-category-in-markdown
  "Returns list of public fns in domo.core, for codeblock in readme.md"
  []
  (let [order
        ["Viewport & Geometry"
         "Node Selection"
         "Events"
         "CSS & Styling"
         "Utilities"]
        
        domo-fns-by-category                   
        (select-keys
         (->> (ns-publics 'domo.core)
              (group-by #(some-> % second meta :domo/category)))
         order)

        ret 
        (reduce-kv
         (fn [s k vc]
           (str s
                "\n;; " k "\n\n"
                (string/join 
                 "\n"
                 (sort 
                  (reduce 
                   (fn [vc [k v]]
                     (conj vc
                           (let [arglists (some-> v meta :arglists)]
                             (when (= (str k) "copy-to-clipboard!")
                               (println k arglists))
                             (str k
                                  " "
                                  (into [] 
                                        (if (< 1 (count arglists))
                                          ['var_args]
                                          (let [fa (first arglists)]
                                            (if (map? (first fa))
                                              ['opts]
                                              fa))))))))
                   []
                   vc)))
                "\n\n"))

         ""
         domo-fns-by-category)]
    ret))
