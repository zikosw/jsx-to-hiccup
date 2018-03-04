(ns jsx-to-hiccup.app
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [jsx-to-hiccup.core-test]))

(doo-tests 'jsx-to-hiccup.core-test)


