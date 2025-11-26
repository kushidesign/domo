<p align="left"><sub>どうもありがとう ミスターロボット</sub></p>

# domo

A ClojureScript DOM utility library

<img src="https://img.shields.io/clojars/v/design.kushi/domo.svg?color=0969da&style=flat-square&cacheSeconds=3" alt="Domo Clojars badge"></img>

<img src="resources/domo-autocomplete-2.gif" alt="Domo Clojars badge"></img>


## Intro
Once in a while, you may find yourself needing to interact directly with the
[DOM](https://developer.mozilla.org/en-US/docs/Web/API/Document_Object_Model/Introduction).

domo is a collection of 80+ utility functions designed to make this easier.

It can help with:
 - Avoiding the process of digging through browser API docs
 - Reducing the amount of interop syntax in your cljs source code

It features many useful functions for selecting and manipulating DOM elements, querying the viewport, and dealing with events. A few highlights:


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
And many more -- check out the API list below, as well as the source of `domo.core` namespace. 

<br>

## Quickstart API Tour
Clone the repo, cd into the `examples/browser` directory, and follow the instructions [here](https://github.com/kushidesign/domo/tree/main/examples/browser).

<br>

## Usage
Add as a dependency to your project:

```clojure
[design.kushi/domo "0.5.0"]
```
<br>

Require:

```clojure
(ns myns.core
  (:require
    [domo.core :as d]))
```

domo bundles the excellent [js-interop](https://github.com/applied-science/js-interop),
so you can also require that if you need it.

```clojure
(ns myns.core
  (:require
    [domo.core :as d]
    [applied-science.js-interop :as j]))
```


<br>

## Macros
There is a **`domo.macros`** namespace which includes macro counterparts for a subset of domo's public functions. These are available if you want to avoid the overhead of function calls for performance reasons.

```clojure
(ns myns.core
  (:require
    [domo.core :as d]
    [domo.macros :as dm]))
```

<br>

## API
API docs coming soon. In the meantime you can checkout all the public functions in the source `domo.core` namespace. Here is an exhaustive list, with function signatures:

```Clojure
;; Viewport & Geometry

client-rect [el]
client-rect-map [el]
distance-between-els [a b]
intersecting-client-rects? [a b]
screen-quadrant [el]
screen-quadrant-from-point [x y]
viewport []
viewport-map []
viewport-x-fraction [vp x]
viewport-y-fraction [vp y]


;; Node Selection

data-selector [var_args]
el-from-point [x y]
el-index [el]
grandparent [el]
next-sibling [el]
parent [el]
previous-sibling [el]
qs [var_args]
qs-data [var_args]
qsa [var_args]
sibling-with-attribute [var_args]
siblings-with-attribute [var_args]
value-selector [v]
zip-get [el steps]


;; Events

add-class-on-mouse-enter-attrs [s]
add-class-on-mouse-enter-attrs-map [s]
add-event-listener! [el nm f opts]
arrow-keycode? [e]
click! [el]
click-xy [e]
current-event-target [e]
current-event-target-value [e]
dispatch-event! [el e]
dispatch-mousedown-event [var_args]
el-by-id [id]
element-node? [el]
event-target [e]
event-target-value [e]
event-target-value->float [e]
event-target-value->int [e]
event-xy [e]
focus! [el]
get-first-onscreen-child-from-top [el]
keyboard-event! [var_args]
mouse-down-a11y [f & args]
mouse-down-a11y-map [f & args]
mouse-event! [var_args]
nearest-ancestor [el sel]
observe-intersection [opts]
prevent-default! [e]
scroll-by! [opts]
scroll-into-view! [var_args]
scroll-to-top! []


;; CSS & Styling

add-class! [el & xs]
class-list [el]
class-string [el]
computed-style-value [var_args]
computed-style-value-data [var_args]
css-custom-property-value [var_args]
css-custom-property-value-data [var_args]
css-duration-value->int [s]
duration-property-ms [var_args]
fade-in [var_args]
fade-out [var_args]
has-class? [el classname]
matches-media? [prop val]
media-supports-hover? []
media-supports-touch? []
prefers-reduced-motion? []
remove-class! [el & xs]
set-caret! [el i]
set-style! [var_args]
toggle-class! [el & xs]
token->ms [var_args]
writing-direction []


;; Utilities

array-from [iterable]
as-str [x]
copy-to-clipboard! [var_args]
dev-only [x]
node-name [el]
object-assign [& objs]
raf [f]
round-by-dpr [n]
```

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
