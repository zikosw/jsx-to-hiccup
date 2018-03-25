(ns hiccup-gen.core
    (:require [reagent.core :as reagent :refer [atom]]
              [generate.core :as g]
              [cljs.pprint :as pp]))

(enable-console-print!)

(def log js/console.log)

(def code-mirror js/CodeMirror)


(defn load-editor [{:keys [elem-id] :as config}]
  (let [dom (js/document.getElementById elem-id)]
    (set! dom.innerHTML "")
    (code-mirror
      dom
      (clj->js (merge config {:theme "dracula" :lineNUmbers true})))))

(defonce editor (load-editor {:elem-id "editor"
                              :mode "jsx"
                              :value "<div><p>Hello</p></div>"}))

(defonce output (load-editor {:elem-id "output"
                              :mode "clojure"
                              :readOnly true
                              :lineWrapping true
                              ;:scrollbarStyle "null"
                              :value "[:div [:p \"Hello\"]]\n"}))


(def state (atom {:error nil :code ""}))

(defn convert []
  (swap! state assoc :error nil)
  (try
    (let [value (.getValue editor)
          parsed (g/parse value)
          hiccuped (-> parsed g/to-hiccup)
          pretty (with-out-str (pp/pprint hiccuped))]
      (swap! state assoc :code pretty)
      (.setValue output pretty))
    (catch js/Error e
      (js/console.log "Parse Error : " e.message)
      (swap! state assoc :error e.message))))

(js/setTimeout
  (fn []
    (log :register-onchange)
    (.on editor "change" convert))
  500)

(defn home-panel []
  (let [error (:error @state)
        code (:code @state)]
    [:div
      (if error
        [:div.alert.alert-danger (str error)])]))


(reagent/render-component [home-panel] (. js/document (getElementById "app")))

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

