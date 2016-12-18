(ns promesa-check.core-test
  (:require [promesa.core :as p]
            [promesa-check.util :as util]
            [#?(:clj clojure.test :cljs cljs.test) :as t]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [promesa-check.core :as core]))

(def simple-prop
  (prop/for-all* [gen/string]
    (fn [s]
      (p/delay 0 (= s s)))))

(util/deftest quick-check
  (-> (core/quick-check 10 simple-prop)
      (p/then
        (fn [{:keys [result num-tests] :as m}]
          (t/is (m :seed))
          (t/is (true? result))
          (t/is (= num-tests 10))))))

(def error-prop
  (prop/for-all* [gen/string]
    (fn [s]
      (p/rejected (ex-info "Oops" {::expected? true})))))

(util/deftest quick-check-error
  (-> (core/quick-check 1 error-prop)
      (p/branch
        (fn [x]
          (t/is nil x))
        (fn [e]
          (t/is (-> e ex-data ::expected?))))))

(def failing-prop
  (prop/for-all* [gen/int]
    (fn [x]
      (p/resolved false))))

(util/deftest quick-check-fail
  (-> (core/quick-check 1 failing-prop)
      (p/then
        (fn [{:keys [result num-tests] :as m}]
          (t/is (= num-tests 1))
          (t/is (false? result))
          (t/is (m :promesa-check/failed?))))))
