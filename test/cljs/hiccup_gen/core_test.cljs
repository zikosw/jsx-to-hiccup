(ns hiccup-gen.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [hiccup-gen.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
