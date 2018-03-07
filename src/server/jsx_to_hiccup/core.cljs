(ns jsx-to-hiccup.core
  (:require
    [jsx-to-hiccup.config :refer [env]]
    [jsx-to-hiccup.middleware :refer [wrap-defaults]]
    [jsx-to-hiccup.routes :refer [router]]
    [generate.core :as h]
    [macchiato.server :as http]
    [macchiato.middleware.session.memory :as mem]
    [mount.core :as mount :refer [defstate]]
    [taoensso.timbre :refer-macros [log trace debug info warn error fatal]]))


(defn server []
  (mount/start)
  (let [host (or (:host @env) "127.0.0.1")
        port (or (some-> @env :port js/parseInt) 3000)]
    (http/start
      {:handler    (wrap-defaults router)
       :host       host
       :port       port
       :on-success #(info "jsx-to-generate started on" host ":" port)})))



(defn parse-test [code]
  (let [res (h/parse code)]
    {:code code
     :res res}))

(def stms
  ["<a href=\"1dsfsd--23'321\">Link</a>"
   "<div className=\"shopping-list\">\n        <h1>Shopping List for {this.props.name}</h1>\n        <ul>\n          <li>Instagram</li>\n          <li>WhatsApp</li>\n          <li>Oculus</li>\n        </ul>\n      </div>"
   "<aa style={{a:99, b:c, d:{e:nil}, f:<img src={URL}/>, g:x.y}}/>"
   "a.b.c"
   "a==b"
   "<p>Hi</p>"
   "<Tab.Header />"
   "<aa><b/></aa>"
   "<aa>TEXT</aa>"
   "<aa><b></b></aa>"
   "<div k1=\"v1\" >block</div>"
   "<div hidden >block</div>"
   "<div hidden}>{var1}</div>"
   "<div hidden>{var1==88&&var2&&<p>True</p>}</div>"
   "<div hide k1=\"v1\" >block</div>"
   "<div hidden k1=\"v1\" k2=\"v2\">block</div>"
   "<div hidden k1={\"text\"} k1=\"v1\" k2={val2} k3={null} k4={99} k5={<img src=\"google.com\"/>}><p>Hello</p><p>World</p></div>"
   "<div hidden k1=\"v1\" k2={val2} k3={null} k4={99}><p>Hello</p><p>World</p></div>"
   "<div hidden k1=\"v1\" k2=\"v2\"><img src=\"gogle\"/></div>"
   "<div hidden k1=\"v1\" show k2=\"v2\" disabled>block</div>"
   "<div hidden k1=\"v1\" k2=\"v2\" disabled>block</div>"])

(defn test-stm []
  (filter #(nil? (:res %)) (map parse-test stms)))

(comment
  (-> (get stms 6)
      h/parse
      h/to-hiccup)
      ;str)
      ;cljs.reader/read-string)
      ;prn)
      ;hi/to-str)

  (h/parse (get stms 3))
  (h/parse-debug (get stms 8))
  (test-stm)
  (h/is-not-capital-case "asdf") ; true
  (h/is-not-capital-case "Asdf") ; false
  (h/get-name "div")
  (h/get-name "Div")
  (parse-test "<p>Hello<p>")

  (cljs.pprint/write
    (quote
     (defn prime? [n known](loop [cnt (dec (count known)) acc []](if (< cnt 0) (not (any? acc)))))
     (recur (dec cnt) (concat acc [(zero? (mod n (nth known cnt)))]))))
                        ;:dispatch clojure.pprint/code-dispatch))))

  (h/parse "<p>Hello</p>"))
  ;(utils/obj->clj (acorn/parse "<p>Hello</p>" (clj->js {:plugins {:jsx true}}))))
