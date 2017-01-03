(ns promesa-check.core
  "Promise-based / asynchronous implementations of [[quick-check]]
  and [[defspec]].  Shrinking of failures is not implemented."
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.random :as random]
            [clojure.test.check.rose-tree :as rose]
            [clojure.test.check.clojure-test :as ct]
            [clojure.test.check.properties :as prop]
            [clojure.test :refer [is]]
            [promesa.core :as p]
            #? (:cljs [cljs.test :include-macros]))
  #?(:cljs (:require-macros [promesa-check.core])))

(defn- get-current-time-millis []
  #?(:clj  (System/currentTimeMillis)
     :cljs (.valueOf (js/Date.))))

(defn- make-rng
  [seed]
  (if seed
    [seed (random/make-random seed)]
    (let [non-nil-seed (get-current-time-millis)]
      [non-nil-seed (random/make-random non-nil-seed)])))

(defn- failure [state]
  (let [{:keys [result args] :as root} (rose/root (state :tree))]
    {:promesa-check/complete? true
     :promesa-check/failed?    true
     :result       (state :result)
     :seed         (state :seed)
     :failing-size (state :size)
     :trial-number (state :index)
     :num-tests    (inc (state :index))
     :fail         (vec args)
     :property     (state :property)}))

(defn- exception? [x]
  (instance? #?(:clj Throwable :cljs js/Error) x))

(defn- result-ok? [x]
  (and x (not (exception? x))))

(defn- quick-check-step [{:keys [index trials seq-size rng property] :as state}]
  (if (== index trials)
    (p/resolved {:promesa-check/complete? true
                 :result                  true
                 :num-tests               index
                 :seed                    (state :seed)})
    (let [[size & size-seq]  (state :size-seq)
          [r1 r2]            (random/split rng)
          result-map-rose    (gen/call-gen property r1 size)
          result-map         (rose/root result-map-rose)]
      (p/then (result-map :result)
        (fn [result]
          (if (result-ok? result)
            (assoc state :index (inc index) :size-seq size-seq :rng r2)
            (failure {:property property
                      :tree     result-map-rose
                      :result   result
                      :index    index
                      :size     size
                      :seed     (state :seed)}))) ))))

(defn- quick-check-iter [{:keys [property trials] :as state}]
  (-> (quick-check-step state)
      (p/bind
        (fn [{:keys [index] :as state}]
          (if (state :promesa-check/failed?)
            (ct/report-failure (state :property) (state :result) (state :index) (state :fail))
            (ct/report-trial property index trials))
          (if (state :promesa-check/complete?)
            (p/resolved state)
            (quick-check-iter state))))))

(defn quick-check
  "Implementation of clojure.test.check's `quick-check`, which returns a promise
  describing the outcome of the trials.  If a trial fails, the returned promise
  will be resolved (not rejected) with a map containing
  `:promesa-check/failed?`.  A _rejected_ promise indicates an error
  originating in the property.

```clojure
@(quick-check 10
   (prop/for-all* [gen/int]
     (fn [x]
      (p/resolved (number? x)))))
=>
{:result    true
 :num-tests 10
 :seed      1482058620269}
```
"
  [num-tests property & [{:keys [seed max-size] :or {max-size 200}}]]
  (let [[seed rng] (make-rng seed)]
    (quick-check-iter
     {:property property
      :index    0
      :trials   num-tests
      :size-seq (gen/make-size-range-seq max-size)
      :seed     seed
      :rng      rng})))

(defn ^:no-doc run-test [f]
  #?(:clj
     (let [{:keys [result] :as state} (f)]
       (prn state)
       (if (exception? result)
         (throw result)
         (clojure.test/is result)))
     :cljs
     (cljs.test/async
      done
      (-> (f)
          (p/branch
            (fn [{:keys [result] :as state}]
              (prn state)
              (cljs.test/is result)
              (done))
            (fn [x]
              (cljs.test/is (not x))
              (done)))))))

#?(:clj
   (defmacro defspec
     "Analog of clojure.test.check's `defspec`, which expects properties
  returning promises.  When running on the JVM, the resulting test will be
  synchronous (the promise returned by [[quick-check]] will be forced).  In
  Clojurescript, `cljs.test/async` will be used.

```clojure
(def prop
 (prop/for-all* [gen/string]
  (fn [s]
    (p/delay 1 (= s s)))))

(defspec test 100 prop)
```"
     ([name property]
      `(defspec ~name nil ~property))
     ([name options property]
      (let [test    `#(run-test (fn [] (~name)))
            unwrap  (when-not (:ns &env) '(deref))]
        `(defn ~(vary-meta name assoc :test test)
           ([] (let [options# (ct/process-options ~options)]
                 (~name options#)))
           ([opts#]
            (-> (quick-check (opts# :num-tests) ~property opts#)
                ~@unwrap)))))))
