(ns promesa-check.util
  (:require [#?(:clj clojure.test :cljs cljs.test)])
  #?(:cljs (:require-macros [promesa-check.util])))

#?(:clj
   (defmacro deftest "Expects test bodies which evaluate to promises.  On the
  JVM, the promises will forced and the test will execute synchronously.  In
  Clojurescript, `cljs.test/async` will be used.  Rejected promises will cause
  the test to fail."
     [test-var & forms]
     (if (:ns &env)
       `(cljs.test/deftest ~test-var
          (cljs.test/async
           done#
           (-> (do ~@forms)
               (p/catch
                   (fn [e#]
                     (println (.. e# -stack))
                     (cljs.test/is (not e#))))
               (p/then #(done#)))))
        `(clojure.test/deftest ~test-var
           @(do ~@forms)))))
