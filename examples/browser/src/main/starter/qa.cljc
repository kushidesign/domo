(ns starter.qa
  (:require [bling.explain]
            [bling.core]
            [fireworks.core :refer [? !? ?> !?>]]))

(def Address
  [:map
   [:id string?]
   [:tags [:set keyword?]]
   [:address
    [:map
     [:street string?]
     [:city string?]
     [:zip int?]
     [:lonlat [:tuple double? double?]]]]])

(def v 
  {:id      "Lillan"
   :tags    #{:artesan "coffee" :garden}
   :address {:street "Ahlmanintie 29"
             :city   "Tempare"
             :zip    33100
             :lonlat [61.4858322, 87.34]}})

(defn wtf []
  #?(:cljs (do 
             (? {:street "Ahlmanintie 29"
                 :city   "Tempare"
                 :zip    33100
                 :lonlat [61.4858322, 87.34]})
             #_(bling.core/callout {:type :info}
                                 "Probably\n"
                                 "Probably")
             #_(bling.explain/explain-malli 
              Address
              v
              {:display-schema? false
               :callout-opts    {:label "Custom Label"}})
             )))
