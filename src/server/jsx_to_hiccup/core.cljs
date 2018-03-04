(ns jsx-to-hiccup.core
  (:require
    [jsx-to-hiccup.config :refer [env]]
    [jsx-to-hiccup.middleware :refer [wrap-defaults]]
    [jsx-to-hiccup.routes :refer [router]]
    [jsx-to-hiccup.utils :as utils]
    [macchiato.server :as http]
    [macchiato.middleware.session.memory :as mem]
    [mount.core :as mount :refer [defstate]]
    [taoensso.timbre :refer-macros [log trace debug info warn error fatal]]
    ["acorn-jsx" :as acorn]))

(comment
  (utils/obj->clj (acorn/parse "<p>Hello</p>" (clj->js {:plugins {:jsx true}}))))

(defn server []
  (mount/start)
  (let [host (or (:host @env) "127.0.0.1")
        port (or (some-> @env :port js/parseInt) 3000)]
    (http/start
      {:handler    (wrap-defaults router)
       :host       host
       :port       port
       :on-success #(info "jsx-to-hiccup started on" host ":" port)})))
