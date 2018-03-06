(ns hiccup-gen.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [hiccup-gen.core-test]))

(doo-tests 'hiccup-gen.core-test)
