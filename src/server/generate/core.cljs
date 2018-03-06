(ns generate.core
  (:require ["acorn-jsx" :as acorn]
            [generate.utils :as utils]
            [clojure.string :as string]))

(defn parse-debug [code]
  (-> code
    (acorn/parse (clj->js {:plugins {:jsx true}}))))

(defn parse [code]
  (let [trimed
        (-> code
          (string/split-lines)
          (->>
            (map string/trim)
            (string/join "")))
        parsed
        (try
          (acorn/parse trimed (clj->js {:plugins {:jsx true}}))
          (catch js/Error e
            (prn :parse code)
            (prn :parse-error e)
            nil))]
    (if parse
      (-> parsed
          (utils/obj->clj true)
          (get-in [:body 0 :expression])))))



(defn is-not-capital-case [val]
  (let [f (first val)
        up-f (string/lower-case f)]
    (= f up-f)))

(defn get-tag [name]
  (if (is-not-capital-case name)
    (keyword name)
    (symbol name)))


(def JSLiteral "Literal")
(def JSIdentifier "Identifier")
(def JSAssignmentExpression "AssignmentExpression")
(def JSLogicalExpression "LogicalExpression")
(def JSBinaryExpression "BinaryExpression")
(def JSMemberExpression "MemberExpression")


;; Element
(def JSXElement "JSXElement")
(def JSXFragment "JSXFragment")
;(def JSXSelfClosingElement "JSXSelfClosingElement")
(def JSXOpeningElement "JSXOpeningElement")
(def JSXClosingElement "JSXClosingElement")
(def JSXElementName  "JSXElementName")
(def JSXIdentifier "JSXIdentifier")
;(def JSXNamespacedName "JSXNamespacedName")
(def JSXMemberExpression "JSXMemberExpression")

;; Attr
;(def JSXAttributes "JSXAttributes")
(def JSXSpreadAttribute "JSXSpreadAttribute")
(def JSXAttribute "JSXAttribute")
(def JSXAttributeName "JSXAttributeName")
(def JSXAttributeInitializer "JSXAttributeInitializer")
(def JSXAttributeValue "JSXAttributeValue")
;(def JSXDoubleStringCharacters "JSXDoubleStringCharacters")
;(def JSXDoubleStringCharacter "JSXDoubleStringCharacter")
;(def JSXSingleStringCharacters "JSXSingleStringCharacters")
;(def JSXSingleStringCharacter "JSXSingleStringCharacter")
;; Children
;(def JSXChildren "")
;(def JSXChild "")
(def JSXText "JSXText")
;(def JSXTextCharacter "")
;(def JSXChildExpression "")

;; From acorn
(def JSXExpressionContainer "JSXExpressionContainer")
;; New from me
(def BooleanAttribute "BooleanAttribute")


(defn get-operator-symbol [operator]
  (case operator
    "==" (symbol "=")
    "!=" (symbol "not=")
    "&&" (symbol "and")
    "||" (symbol "or")
    (symbol operator)))


(defn to-hiccup [ast]
  (let [is-vector (vector? ast)
        is-map (map? ast)
        to-attr (fn [attr]
                  (let [attr-key (keyword (get-in attr [:name :name]))
                        val (get-in attr [:value :value])
                        val-type (get-in attr [:value :type] BooleanAttribute)]
                    (condp = val-type
                      JSLiteral
                      [attr-key val]

                      JSXExpressionContainer
                      (let [node (get-in attr [:value :expression])]
                        [attr-key (to-hiccup node)])
                      BooleanAttribute
                      [attr-key true]
                      ;; unmatch
                      [attr-key val-type])))

        to-attrs (fn [attrs] (into {} (map to-attr attrs)))]
    (cond
      is-vector
      (let [res (map to-hiccup ast)
            cnt (count res)]
        (if (= cnt 1)
          (first res)
          res))
      is-map
      (condp = (get ast :type)
        JSXElement
        (let [-name (get-in ast [:openingElement :name :name])
              name (get-tag -name)
              -attrs (get-in ast [:openingElement :attributes])
              attrs (to-attrs -attrs)
              children (get ast :children)]
          (cond
            (and (empty? attrs)
                 (empty? children))
            [name]

            (and (not (empty? attrs))
                 (empty? children))
            [name attrs]

            (and (empty? attrs)
                 (not (empty? children)))
            [name (to-hiccup children)]

            (and (not (empty? attrs))
                 (not (empty? children)))
            [name attrs (to-hiccup children)]))

        JSXText
        (let [value (get ast :value)]
          value)

        JSXExpressionContainer
        (let [node (get ast :expression)]
          (to-hiccup node))

        ;; JavaScript
        JSIdentifier
          (let [val (get ast :name)]
            (symbol val))
        JSLiteral
        (let [val (get ast :value)]
          val)

        JSLogicalExpression
        (let [operator (get-operator-symbol (get ast :operator))
              left (to-hiccup (get ast :left))
              right (to-hiccup (get ast :right))
              right-type (get-in ast [:right :type])]
          (list (symbol "if")
                (list operator left right)))

        JSBinaryExpression
        (let [operator (get-operator-symbol (get ast :operator))
              left (to-hiccup (get ast :left))
              right (to-hiccup (get ast :right))]
          (list operator left right))

        [:default (get ast :type) (= JSXElement (get ast :type))])
      :else :unknown)))



(defn as-hiccup [ast]
  (case (get ast :type)
    JSXElement
    ;; TODO: check this again
    [:tag :attrs :children]))
