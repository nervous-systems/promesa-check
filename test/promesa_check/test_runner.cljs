(ns promesa-check.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [promesa-check.core-test]))

(doo-tests 'promesa-check.core-test)

