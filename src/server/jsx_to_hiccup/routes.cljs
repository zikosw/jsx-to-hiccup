(ns jsx-to-hiccup.routes
  (:require
    [bidi.bidi :as bidi]
    [hiccups.runtime]
    [generate.core :as g]
    [macchiato.middleware.anti-forgery :as af]
    [macchiato.middleware.restful-format :as rf]
    [macchiato.util.response :as r]
    [macchiato.util.request :as rq])
  (:require-macros
    [hiccups.core :refer [html]]))

(defn code-form [code]
  [:form {:method "POST" :action "/form"}
   [:textarea
    {:name "code"}
    code]
   [:input
    {:type "submit"}]])

(defn home [req res raise]
  (-> (html
        [:html
         [:head
          [:link {:rel "stylesheet" :href "/css/site.css"}]
          [:script {:src "js/compiled/app.js"}]]

         [:body
          [:h2 "Hello World!"]
          [:p (str "token : " af/*anti-forgery-token*)]
          [:p
           "Your user-agent is: "
           (str (get-in req [:headers "user-agent"]))]
          (code-form "")]])

      (r/ok)
      (r/content-type "text/html")
      (res)))

(defn not-found [req res raise]
  (-> (html
        [:html
         [:body
          [:h2 (:uri req) " was not found"]]])
      (r/not-found)
      (r/content-type "text/html")
      (res)))


(defn convert-code [req res raise]
  (let [body (-> req :body)
        code (get-in req [:body "code"])
        parsed (-> code g/parse g/to-hiccup str)]
    (prn :body (type body))
    (prn :code code)
    (prn :parse parsed)
    (-> (r/ok {:code parsed})
        (res))))


(defn convert-code-form [req res raise]
  (let [code (get-in req [:params :code])
        parsed (-> code g/parse g/to-hiccup str)]
    (-> (html
          [:html
           [:body
            [:h2 "From form"]
            (code-form code)
            [:pre parsed]]])
        (r/ok)
        (r/content-type "text/html")
        (res))))


(def routes
  ["/" {"code" {:post (rf/wrap-restful-format convert-code)}
        "form" {:post convert-code-form}
        "" {:get home}}])

(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))


