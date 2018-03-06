(ns hiccup-gen.core
  (:require [webpack.bundle]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [generate.core :as g]
            [hiccup-gen.events :as events]
            [hiccup-gen.routes :as routes]
            [hiccup-gen.views :as views]
            [hiccup-gen.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (prn :dev :Dev)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
