<p align="left"><sub>どうもありがとう ミスターロボット</sub></p>

# domo

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
[design.kushi/domo "0.4.0"]
```
<br>

Require:

```clojure
(ns myns.core
  (:require
    [domo.core :as d]))
```

Domo bundles the excellent [`js-interop`](https://github.com/applied-science/js-interop),
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
API docs coming soon. In the meantime you can checkout all the functions tagged `^:public` in the source `domo.core` namespace. Here is an exhaustive list, with
function signatures:

```Clojure
dispatch-event! [el e]
observe-intersection [m]
current-event-target-value [e]
class-list [el]
mouse-down-a11y [var_args]
viewport-y-fraction [vp y]
keyboard-event! [var_args]
mouse-event! [var_args]
viewport []
toggle-boolean-attribute! [el attr]
dev-only [x]
click! [el]
set-style! [var_args]
event-target-value->float [e]
current-event-target-value [e]
add-event-listener! [el nm f opts]
has-class? [el classname]
siblings-with-attribute [var_args]
el-from-point [x y]
client-rect [el]
screen-quadrant [el]
fade-in [var_args]
qs-data= [var_args]
event-target-value->int [e]
value-selector= [v]
event-xy [e]
focus! [el]
distance-between-els [a b]
array-from [iterable]
media-supports-touch? []
previous-sibling [el]
event-target-value->float [e]
copy-to-clipboard! [var_args]
scroll-into-view! [var_args]
next-sibling [el]
attribute-false? [el attr]
add-class! [var_args]
scroll-by! [m]
qsa [var_args]
nearest-ancestor [el sel]
el-by-id [id]
parent [el]
round-by-dpr [n]
css-custom-property-value [var_args]
sibling-with-attribute [var_args]
get-first-onscreen-child-from-top [el]
media-supports-hover? []
fade-out [var_args]
css-style-string [m]
toggle-attribute! [el attr a b]
maybe [x pred]
remove-class! [var_args]
remove-attribute! [el attr]
scroll-to-top! []
attribute-true? [el attr]
as-str [x]
writing-direction []
event-target [e]
add-class-on-mouse-enter-attrs [s]
dispatch-mousedown-event [var_args]
event-target [e]
css-custom-property-value-data [var_args]
screen-quadrant-from-point [x y]
duration-property-ms [var_args]
zip-get [el steps]
el-index [el]
event-target-value [e]
arrow-keycode? [e]
event-target-value->int [e]
data-attr [el nm]
prefers-reduced-motion? []
set-caret! [el i]
object-assign [var_args]
class-string [el]
current-event-target [e]
set-attribute! [el attr v]
event-target-value [e]
element-node? [el]
data-selector= [attr v]
toggle-class! [var_args]
css-duration-value->int [s]
has-attribute? [el attr]
viewport-x-fraction [vp x]
node-name [el]
qs [var_args]
add-class-on-mouse-enter-attrs-map [s]
matches-media? [prop val]
intersecting-client-rects? [a b]
computed-style-value [var_args]
current-event-target [e]
click-xy [e]
computed-style-value-data [var_args]
grandparent [el]
mouse-down-a11y-map [var_args]
distance-between-points [x1 y1 x2 y2]
viewport-map []
token->ms [var_args]
client-rect-map [el]
prevent-default! [e]
raf [f]
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
