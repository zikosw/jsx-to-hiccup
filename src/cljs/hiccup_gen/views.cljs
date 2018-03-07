(ns hiccup-gen.views
  (:require [re-frame.core :as re-frame]
            [generate.core :as g]
            [cljs.pprint :as pp]
            [reagent.core :refer [atom]]
            [hiccup-gen.subs :as subs]))

(def state (atom {:code "" :hiccuped ""}))

(defn convert-clicked []
  (prn :convert-clicked)
  (let [parsed (g/parse (:code @state))
        hiccuped (-> parsed g/to-hiccup)
        pretty (with-out-str (pp/pprint hiccuped))]
    ;(prn :parsed parsed)
    (prn :hiccup hiccuped)
    (pp/pprint hiccuped)
    (prn :pretty pretty)
    (swap! state assoc :hiccuped pretty)))

(defn on-code-changed [e]
  (prn :code-changed)
  (let [code (-> e .-target .-value)]
    (prn :code-changed code)
    (swap! state assoc :code code)))

;; home

(defn home-panel []
  (let [name (re-frame/subscribe [::subs/name])
        hiccuped (:hiccuped @state)]
    (prn :state @state)
    [:div (str "Hello from " @name ". This is the Home Page.")
     [:div
      [:div
        [:p "Put JS code here"]
        [:textarea {:style {:height 300
                            :width 400}
                    :on-change on-code-changed}]]
      [:button {:on-click convert-clicked} "Convert"]
      [:hr]
      [:h3 "Hiccup Output"
       [:div
        [:pre hiccuped]]]]]))


;; about

(defn about-panel []
  [:div "This is the About Page."
   [:div [:a {:href "#/"} "go to Home Page"]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
