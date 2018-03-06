(defproject hiccup-gen "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.908"]
                 [reagent "0.7.0" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [re-frame "0.10.5"]
                 [secretary "1.2.3"]]

  :plugins [[lein-cljsbuild "1.1.5"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.4"]
                   [day8.re-frame/re-frame-10x "0.2.0"]
                   [re-frisk "0.5.3"]]

    :plugins      [[lein-figwheel "0.5.13"]
                   [lein-doo "0.1.8"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "hiccup-gen.core/mount-root"
                    :open-urls ["http://localhost:3449/index.html"]}
     :compiler     {:main                 hiccup-gen.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true

                    :foreign-libs [{:file "public/js/bundle.js"
                                    :provides ["cljsjs.react" "cljsjs.react.dom" "webpack.bundle"]}]
                    :preloads             [devtools.preload
                                           day8.re-frame-10x.preload
                                           re-frisk.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true}
                    :external-config      {:devtools/config {:features-to-install :all}}}}


    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            hiccup-gen.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          hiccup-gen.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}]})



