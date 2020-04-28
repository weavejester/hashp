# Hashp

Hashp is a better `prn` for debugging Clojure code. Inspired by
projects like [Spyscope][], Hashp (ab)uses data readers to make it
easier to get useful debugging data sent to STDOUT.

[spyscope]: https://github.com/dgrnbrg/spyscope

## Usage

Once installed, you can add `#p` in front of any form you wish to
print:

```clojure
(ns example.core)

(defn mean [xs]
  (/ (double #p (reduce + xs)) #p (count xs)))
```

It's faster to type than `(prn ...)`, returns the original result, and
produces more useful output by printing the original form, function
and line number:

```
user=> (mean [1 4 5 2])
#p[example.core/mean:4] (reduce + xs) => 12
#p[example.core/mean:4] (count xs) => 4
3.0
```

## Install

### Leiningen

Add the following to `~/.lein/profiles.clj`:

```edn
{:user
 {:dependencies [[hashp "0.1.1"]]
  :injections [(require 'hashp.core)]}}
```

### Boot

Add the following to `~/.boot/profile.boot`:

```clojure
(set-env! :dependencies #(conj % '[hashp "0.1.1"]))

(require 'hashp.core)
(boot.core/load-data-readers!)
```

### Shadow-CLJS

Add the following to `shadow-cljs.edn`:
```clojure
{:dependencies [hashp "0.1.2"]
 :nrepl {:init-ns user}
 :builds {:app {:devtools {:preloads [hashp.core]}}}}
```

Create a file `src/user.clj`:
```clojure
(ns user
  (:require [hashp.core]))
```

## License

Copyright © 2019 James Reeves

Released under the MIT license.
