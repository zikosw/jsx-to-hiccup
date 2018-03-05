(ns hiccup.utils
  (:require [goog :as goog]
            [goog.object :as gobj]))

(defn obj->clj
  ([obj]
   (obj->clj obj false))
  ([obj keywordize-keys]
   (case (goog/typeOf obj)
     "array"
     (into []
           (for [i obj]
             (obj->clj i keywordize-keys)))
     "object"
     (into {}
           (for [k (gobj/getKeys obj)]
             (let [v (gobj/get obj k)
                   new-k (if keywordize-keys (keyword k) k)]
               (if-not (some #{(goog/typeOf v)} ["array" "object"])
                 [new-k v]
                 [new-k (obj->clj v keywordize-keys)]))))
     obj)))
