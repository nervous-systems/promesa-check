# promesa-check

[![Clojars Project](https://img.shields.io/clojars/v/io.nervous/promesa-check.svg)](https://clojars.org/io.nervous/promesa-check)

A small Clojure & Clojurescript library for verifying asynchronous
([promise](https://github.com/funcool/promesa)-returning)
[test.check](https://github.com/clojure/test.check) properties.

## Usage

[API documentation](https://nervous.io/doc/promesa-check)

### quick-check

```clojure
@(promesa-check.core/quick-check 10
  (prop/for-all* [gen/int]
    (fn [x]
     (p/resolved (number? x)))))
=>
{:result    true
 :num-tests 10
 :seed      1482058620269}
```

### clojure.test/defspec

```clojure
(def prop
 (prop/for-all* [gen/string]
  (fn [s]
   (p/delay 1 (= s s)))))

(promesa-check.core/defspec test 100 prop)
```

# License

promesa-check is free and unencumbered public domain software. For more
information, see http://unlicense.org/ or the accompanying UNLICENSE file.
