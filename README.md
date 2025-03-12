# Domo

A ClojureScript DOM utility lib

<img src="https://img.shields.io/clojars/v/design.kushi/domo.svg?color=0969da&style=flat-square&cacheSeconds=3" alt="Domo Clojars badge"></img>

<img src="resources/domo-autocomplete-2.gif" alt="Domo Clojars badge"></img>


## Intro
Once in a while, you may find yourself needing to interact directly with the
[DOM](https://developer.mozilla.org/en-US/docs/Web/API/Document_Object_Model/Introduction).

Domo is a collection of utility functions designed to make this easier.

It can help with:
 - Avoiding the process of digging through browser API docs
 - Reducing the amount of interop syntax in your cljs source code

It features many useful functions for selecting and manipulating DOM elements. A few highlights:


```Clojure
(copy-to-clipboard s)
(viewport)
(client-rect el)
(screen-quadrant el)
(toggle-boolean-attribute el attr)
(nearest-ancestor el sel)
(add-class! el class)
(remove-class! el class)
(set-style! el prop style)
(has-attribute? el attr)
(css-custom-property-value el property)
(computed-style el property)
```
There are many more -- checkout source of `domo.core` namespace. 

<br>

## Usage
Add as a dependency to your project:

```clojure
[design.kushi/domo "0.3.0"]
```
<br>

Require:

```clojure
(ns myns.core
  (:require
    [domo.core :as domo]))
```

Domo bundles the excellent [`js-interop`](https://github.com/applied-science/js-interop),
so you can also require that if you need it.

```clojure
(ns myns.core
  (:require
    [domo.core :as domo]
    [applied-science.js-interop :as j]))
```

<br>

## API
API docs coming soon. In the meantime you can checkout all the functions tagged `^:public` in the source `domo.core` namespace. 

<br>

## Status / Roadmap
Alpha, subject to change.

<br>

## Contributing
Issues for bugs, improvements, or features are very welcome. Please file an issue for discussion before starting or issuing a PR.

<br>


## License

Copyright © 2024-2025 Jeremiah Coyle

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
