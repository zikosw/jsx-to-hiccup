(ns jsx-parser.app
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [jsx-parser.core-test]))

(doo-tests 'jsx-parser.core-test)


