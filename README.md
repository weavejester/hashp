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

## Installation

### tools.deps

To use `#p` in a tools.deps project, add the following dependency to
your project's `deps.edn` file:

```edn
{:deps {dev.weavejester/hashp {:mvn/version "0.3.0"}}}
```

You will then need to load the `hashp.core` namespace before using `#p`:

```clojure
(require 'hashp.preload)
```

### Leiningen

To make `#p` globally available to all Leiningen projects, add the
following to `~/.lein/profiles.clj`:

```edn
{:user
 {:dependencies [[dev.weavejester/hashp "0.3.0"]]
  :injections [(require 'hashp.preload)]}}
```

### Boot

To make `#p` globally available to all Boot projects, add the following
to `~/.boot/profile.boot`:

```clojure
(set-env! :dependencies #(conj % '[dev.weavejester/hashp "0.3.0"]))
(require 'hashp.preload)
(boot.core/load-data-readers!)
```

## License

Copyright © 2025 James Reeves

Released under the MIT license.
