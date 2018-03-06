(ns jsx-to-hiccup.middleware
  (:require
    [macchiato.middleware.defaults :as defaults]))

(defn wrap-defaults [handler]
  (defaults/wrap-defaults
    handler
    {:params    {:urlencoded true
                 :multipart  true
                 :nested     true
                 :keywordize true}
     :session   {:flash        true
                 :cookie-attrs {:http-only true}}
     :security  {;:anti-forgery         true
                 :xss-protection       {:enable? true, :mode :block}
                 :frame-options        :sameorigin
                 :content-type-options :nosniff}
     :static    {:resources "public"}
     :responses {:not-modified-responses true
                 :absolute-redirects     true
                 :content-types          true
                 :default-charset        "utf-8"}}))
    ;(dissoc defaults/site-defaults :security)))


