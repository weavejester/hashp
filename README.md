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
#p[example.core/mean:4] (reduce + xs) ⇒ 12
#p[example.core/mean:4] (count xs) ⇒ 4
3.0
```

## Installation

### tools.deps

To use `#p` in a tools.deps project, add the following dependency to
your project's `deps.edn` file:

```edn
{:deps {dev.weavejester/hashp {:mvn/version "0.5.0"}}}
```

You will then need to install hashp before any other file is loaded.

```clojure
((requiring-resolve 'hashp.install/install!))
```

### Leiningen

To make `#p` globally available to all Leiningen projects, add the
following to `~/.lein/profiles.clj`:

```edn
{:user
 {:dependencies [[dev.weavejester/hashp "0.5.0"]]
  :injections [((requiring-resolve 'hashp.install/install!))]}}
```

### Boot

To make `#p` globally available to all Boot projects, add the following
to `~/.boot/profile.boot`:

```clojure
(set-env! :dependencies #(conj % '[dev.weavejester/hashp "0.5.0"]))
((requiring-resolve 'hashp.install/install!))
(boot.core/load-data-readers!)
```

## Configuration

The `hashp.install/install!` function may be given named options:

```clojure
(require 'hashp.install)
(hashp.install/install :color? false)
```

There are several options supported:

- `:color?` - set to true if the output should be in color. Defaults to
  true unless the [NO_COLOR][] environment variable was set.

- `:disabled?` - if true then `#p` will do exactly nothing. This is
  useful if you want to disable `#p` in a production environment with
  no loss of performance. Defaults to false.

- `:tag` - a symbol used for the tag, defaults to `'p`.

- `:template` - a string that can be used to customize the format of
  `#p`. Defaults to: `"#{tag}[{ns}/{fn}:{line}] {form} ⇒ {value}`

- `:writer` - a `Writer` to use for the output, defaults to `*err*`,
  which writes to STDERR. If you want to write to STDOUT instead, set
  this to `*out*`.

[no_color]: https://no-color.org/

## License

Copyright © 2025 James Reeves

Released under the MIT license.
