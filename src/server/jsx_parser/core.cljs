(ns jsx-parser.core
  (:require
    [jsx-parser.config :refer [env]]
    [jsx-parser.middleware :refer [wrap-defaults]]
    [jsx-parser.routes :refer [router]]
    [macchiato.server :as http]
    [macchiato.middleware.session.memory :as mem]
    [mount.core :as mount :refer [defstate]]
    [taoensso.timbre :refer-macros [log trace debug info warn error fatal]]
    [instaparse.core :as ip]))


(defn server []
  (mount/start)
  (let [host (or (:host @env) "127.0.0.1")
        port (or (some-> @env :port js/parseInt) 3000)]
    (http/start
      {:handler    (wrap-defaults router)
       :host       host
       :port       port
       :on-success #(info "jsx-parser started on" host ":" port)})))

;; -----------------

(def as-and-bs
  (ip/parser
    "S = AB*
     AB = A B
     A = 'a'+
     B = 'b'+"))

(def elem
  (ip/parser
    "
     TAG = (OC_TAG | SELF_CLOSE_TAG)+
     OC_TAG = OPENTAG CHILD* CLOSETAG
     SELF_CLOSE_TAG = '<' HEAD '/>'
     CHILD = TAG | TEXT
     TEXT = #'[a-zA-Z0-9]+' | \"'\"      (* Add more symbol *)
     QUOTE_STRING = (SINGLE_QUOTE_STRING | DOUBLE_QUOTE_STRING)
     SINGLE_QUOTE_STRING = #\"('.*?')\"
     DOUBLE_QUOTE_STRING = #'(\".*?\")'
     WHITESPACE = ' '*
     ID = WHITESPACE #'[a-zA-Z]+[a-zA-Z0-9]*' WHITESPACE
     OPENTAG = '<' HEAD '>'
     KEY = ID                     (* it can be more than id eg. col-md-9 *)
     QUOTE = '\"' | \"'\"
     VALUE = QUOTE_STRING | INLINE_JS
     ATTR = WHITESPACE KEY '=' VALUE WHITESPACE
     BATTR = KEY
     ATTRS = (ATTR | BATTR)*
     HEAD = ID ATTRS
     CLOSETAG = '</' ID '>'
     QUOTE_TEXT = QUOTE TEXT QUOTE
     NUMBER = #'\\d'
     DATA = NUMBER | QUOTE_TEXT | TAG
     INLINE_JS = '{' DATA '}'              (* Add more symbol *)
    "))

(def lookahead-example
  (ip/parser
    "S = &'\"' STR
     STR = #'(.+)'"))

(def lookahead-example2
  (ip/parser
    "STR = #'\"(.+?)\"'"))
;; TODO: need unit test that run every save, so we don't need to re-check each statement manually

;; Simple test
(def stms
  ["<a href='1dsfsd--23321'>Link</a>"
   "<aa/>"
   "<aa><b/></aa>"
   "<aa>TEXT</aa>"
   "<aa><b></b></aa>"
   "<div k1=\"v1\" >block</div>"
   "<div hidden >block</div>"
   "<div hidden>block</div>"
   "<div hide k1=\"v1\" >block</div>"
   "<div hidden k1=\"v1\" k2=\"v2\">block</div>"
   "<div hidden k1=\"v1\" k2=\"v2\"><p>Hello</p></div>"
   "<div hidden k1=\"v1\" k2=\"v2\"><img src=\"gogle\"/></div>"
   "<div hidden k1=\"v1\" show k2=\"v2\" disabled>block</div>"
   "<div hidden k1=\"v1\" k2=\"v2\" disabled>block</div>"])

(comment
  (test-stm)
  elem
  (elem (first stms))
  (stm-check "<a>")
  (stm-check "<a/>"))


(defn stm-check [stm]
  (let [res (elem stm)]
    (if-not (vector? res)
      {:stm stm
       :res res}
      false)))


(defn test-stm []
  (filter #(not= false %) (map stm-check stms)))



