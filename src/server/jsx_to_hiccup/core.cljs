(ns jsx-to-hiccup.core
  (:require
    [jsx-to-hiccup.config :refer [env]]
    [jsx-to-hiccup.middleware :refer [wrap-defaults]]
    [jsx-to-hiccup.routes :refer [router]]
    [jsx-to-hiccup.utils :as utils]
    [macchiato.server :as http]
    [macchiato.middleware.session.memory :as mem]
    [mount.core :as mount :refer [defstate]]
    [clojure.string :as string]
    [taoensso.timbre :refer-macros [log trace debug info warn error fatal]]
    ["acorn-jsx" :as acorn]))


(defn parse [code]
  (let [parsed
        (try
           (acorn/parse code (clj->js {:plugins {:jsx true}}))
           (catch js/Error e
             (prn :parse code)
             (prn :parse-error e)
             nil))]
    (if parse
      (-> parsed
        (utils/obj->clj true)
        (get-in [:body 0 :expression])))))


(def stms
  ["<a href=\"1dsfsd--23'321\">Link</a>"
   "<aa/>"
   "<aa><b/></aa>"
   "<aa>TEXT</aa>"
   "<aa><b></b></aa>"
   "<div k1=\"v1\" >block</div>"
   "<div hidden >block</div>"
   "<div hidden>block</div>"
   "<div hide k1=\"v1\" >block</div>"
   "<div hidden k1=\"v1\" k2=\"v2\">block</div>"
   "<div hidden k1=\"v1\" k2={val2} k3={null}><p>Hello</p><p>World</p></div>"
   "<div hidden k1=\"v1\" k2=\"v2\"><img src=\"gogle\"/></div>"
   "<div hidden k1=\"v1\" show k2=\"v2\" disabled>block</div>"
   "<div hidden k1=\"v1\" k2=\"v2\" disabled>block</div>"])

(defn is-not-capital-case [val]
  (let [f (first val)
        up-f (string/lower-case f)]
    (= f up-f)))

(defn get-name [name]
  (if (is-not-capital-case name)
    (keyword name)
    name))

(defn to-attr [attr]
  (let [name (keyword (get-in attr [:name :name]))
        val (get-in attr [:value :value])
        val-type (get-in attr [:value :type])
        val-id (get-in attr [:value :expression :name])]
    (case val-type
      "Literal"
        [name val]
      "JSXExpressionContainer"
        [name val-id]    ;; This is ID not value, Quote this or do something with it.
      [name true])))


(defn to-attrs [attrs]
  (into {} (map to-attr attrs)))


(defn to-hiccup [ast]
  (let [is-vector (vector? ast)
        is-map (map? ast)]
    (cond
      is-vector
        (let [res (map to-hiccup ast)
              cnt (count res)]
          (if (= cnt 1)
            (first res)
            res))
      is-map
      (case (get ast :type)
        "JSXElement"
          (let [-name (get-in ast [:openingElement :name :name])
                name (get-name -name)
                -attrs (get-in ast [:openingElement :attributes])
                attrs (to-attrs -attrs)
                children (get-in ast [:children])]
            (if (empty? children)
              [name attrs]
              [name attrs (to-hiccup children)]))
        "JSXText"
         (let [value (get-in ast [:value])]
           value)
        [:default ast])
      :else :unknown)))


(defn parse-test [code]
  (let [res (parse code)]
    {:code code
     :res res}))

(defn test-stm []
  (filter #(nil? (:res %)) (map parse-test stms)))

(comment
  (-> (get stms 10)
      parse
      to-hiccup)
  (parse (get stms 10))
  (test-stm)
  (is-not-capital-case "asdf") ; true
  (is-not-capital-case "Asdf") ; false
  (get-name "div")
  (get-name "Div")
  (parse-test "<p>Hello<p>")
  (parse "<p>Hello</p>")
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
