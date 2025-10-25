# Hashp

Hashp is a better `prn` for debugging Clojure code. Inspired by
projects like [Spyscope][], Hashp (ab)uses data readers to make it
easier to get useful debugging data sent to STDERR.

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
{:deps {dev.weavejester/hashp {:mvn/version "0.4.1"}}}
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
 {:dependencies [[dev.weavejester/hashp "0.4.1"]]
  :injections [(require 'hashp.preload)]}}
```

### Boot

To make `#p` globally available to all Boot projects, add the following
to `~/.boot/profile.boot`:

```clojure
(set-env! :dependencies #(conj % '[dev.weavejester/hashp "0.4.1"]))
(require 'hashp.preload)
(boot.core/load-data-readers!)
```

## Configuration

Hashp can be turned on or off via the `hashp.config/*disable-hashp*`
var. This is checked when `#p` is **initially evaluated**, so if it is
changed, any existing namespace that uses `#p` will need to be reloaded
before the change takes effect.

```
user=> (alter-var-root #'hashp.config/*disable-hashp* (constantly true))
true
user=> (require 'example.core :reload)
nil
user=> (mean [1 4 5 2])
3.0
```

You can also turn the colors off. Hashp respects the [NO_COLOR][]
environment variable, and you can also set it via the
`hashp.config/*disable-color*` var. Unlike `*disable-hashp*`, this is
checked on each print, so there's no need to reload the namespaces.

```
user=> (alter-var-root #'hashp.config/*disable-color* (constantly true))
true
```

Finally, you can change the output writer. By default this is `*err*`,
which outputs to STDOUT, but you can set this to any other print writer,
such as `*out*`.

```
user=> (alter-var-root #'hashp.config/*hashp-output* (constantly *out*))
#object[java.io.PrintWriter 0x2deddab6 "java.io.PrintWriter@2deddab6"]
```

[no_color]: https://no-color.org/

## License

Copyright © 2025 James Reeves

Released under the MIT license.
