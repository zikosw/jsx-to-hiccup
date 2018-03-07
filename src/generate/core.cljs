(ns generate.core
  (:require ["acorn-jsx" :as acorn]
            ["babylon" :as babel]
            [generate.utils :as utils]
            [clojure.string :as string]))


(comment
  (let [bbpp (aget js/window.deps "babylon" "parse")]
    (->
       ;"
       ;<CodeMirror
       ;    value='<h1>I ♥ react-codemirror2</h1>'
       ;    />
       ;"
       ;"
       ;<CodeMirror\n  value='<h1>I ♥ react-codemirror2</h1>'\n  options={{\n    mode: 'xml',\n    theme: 'material',\n    lineNumbers: true\n  }}\n  onChange={(editor, data, value) => {\n  }}\n/>
       ;"
       "
       <p>
       Hi
       </p>
       "
       ;trim)))
      (babel/parse (clj->js {:sourceType "module"
                             :plugins ["jsx"]}))
      (utils/obj->clj true)
      (get-in [:program :body 0])
      (to-hiccup))))

(defn parse-debug [code]
  (-> code
      (acorn/parse (clj->js {:plugins {:jsx true}}))))

(def err (atom {}))

(defn trim [code]
  (-> code
      (string/split-lines)
      (->>
        (map string/trim)
        (string/join "\n"))))

(defn parse [code]
  (-> code
      trim
      (babel/parse (clj->js {:sourceType "module" :plugins [:jsx]}))
      (utils/obj->clj true)
      (get-in [:program :body 0 :expression])))



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
(def JSObjectExpression "ObjectExpression")
(def JSExpressionStatement "ExpressionStatement")
(def JSThisExpression "ThisExpression")
(def JSArrowFunctionExpression "ArrowFunctionExpression")
(def JSBlockStatement "BlockStatement")
(def JSStringLiteral "StringLiteral")
(def JSNumericLiteral "NumericLiteral")



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
                      BooleanAttribute
                      [attr-key true]
                      ;; unmatch
                      [attr-key (-> attr :value to-hiccup)])))

        to-attrs (fn [attrs] (into {} (map to-attr attrs)))]
    (cond
      is-vector
      (let [res (filter #(not= "" %) (map to-hiccup ast))
            cnt (count res)]
        (if (= cnt 1)
          (first res)
          res))
      is-map
      (condp = (get ast :type)
        JSXElement
        (let [;-name (get-in ast [:openingElement :name :name])
              ;name (get-tag -name)
              name (-> (get-in ast [:openingElement :name])
                       to-hiccup)
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

        JSXIdentifier
        (let [name (-> ast :name)]
          (get-tag name))

        JSXMemberExpression
        (let [left (-> ast :object to-hiccup)
              right (-> ast :property to-hiccup)]
          (get-tag (str left "." right)))

        JSXText
        (-> ast :value string/trim)
        ;(let [value (get ast :value)]
        ;  value)


        JSXExpressionContainer
        (let [node (get ast :expression)]
          (to-hiccup node))


        ;; JavaScript
        JSExpressionStatement
        (-> ast :expression to-hiccup)

        JSThisExpression
        (symbol "this")


        JSIdentifier
        (let [val (get ast :name)
              type (get ast :internal-type)]
          (case type
            :Key
            (keyword val)
            (symbol val)))
        JSLiteral
        (let [val (get ast :value)]
          val)

        JSStringLiteral
        (-> ast :value)

        JSNumericLiteral
        (-> ast :value)

        JSLogicalExpression
        (let [operator (-> ast :operator get-operator-symbol)
              left (-> ast :left to-hiccup)
              right (-> ast :right to-hiccup)
              right-type (get-in ast [:right :type])]
          (list (symbol "if")
                (list operator left)
                (list right)))

        JSBinaryExpression
        (let [operator (get-operator-symbol (get ast :operator))
              left (-> ast :left to-hiccup)
              right (-> ast :right to-hiccup)]
          (list operator left right))

        JSObjectExpression
        (let [props (get ast :properties)]
          (into {}
                (for [p props]
                  (let [k (-> p :key (assoc :internal-type :Key) to-hiccup)
                        v (-> p :value to-hiccup)]
                    ;v (-> p :value (assoc :internal-type :ObjectExpressionValue))]
                    [k v]))))

        JSMemberExpression
        (let [left (-> ast :object to-hiccup)
              right (-> ast :property to-hiccup)]
          (symbol (str left "." right)))

        JSArrowFunctionExpression
        (list (symbol "fn")
              (into [] (map #(to-hiccup %) (:params ast)))
              (-> ast :body to-hiccup))

        JSBlockStatement
        (list (symbol "do") "something here")

        [:unknown-type (get ast :type) (= JSXElement (get ast :type))])
      :else :unknown)))


