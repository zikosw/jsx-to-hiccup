(ns hiccup-gen.core
    (:require [reagent.core :as reagent :refer [atom]]
              [generate.core :as g]
              [cljs.pprint :as pp]))

(enable-console-print!)

(def state (atom {:code "" :hiccuped ""}))

(defn convert-clicked []
  (swap! state assoc :error nil)
  ;(prn :code (:code @state))
  ;(prn :code-- (g/trim (:code @state)))
  (try
    (let [parsed (g/parse (:code @state))
          hiccuped (-> parsed g/to-hiccup)
          pretty (with-out-str (pp/pprint hiccuped))]
      (pp/pprint parsed)
      (swap! state assoc :hiccuped pretty))
    (catch js/Error e
      (js/console.log "Parse Error : " e.message)
      (swap! state assoc :error e.message))))

(defn on-code-changed [e]
  (let [code (-> e .-target .-value)]
    (swap! state assoc :code code)))


(defn home-panel []
  (let [hiccuped (:hiccuped @state)
        err-msg (:error @state)]
    [:div.container
     [:h1 "JSX to Hiccup"]
     [:div
      [:div
       [:textarea.form-control
        {:style {:height 300
                 :width 400}
         :on-change on-code-changed}]]
      [:button.btn.btn-success {:on-click convert-clicked} "Convert"]

      (if err-msg
        [:div.alert.alert-danger (str err-msg)])

      [:hr]
      [:h3 "Hiccup Output"]
      [:div
       [:pre {:class "prettyprint lang-clojure"} hiccuped]]]]))



(reagent/render-component [home-panel]
                          (. js/document (getElementById "app")))

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

