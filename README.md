# domo

A ClojureScript DOM utility library.

Many useful helpers for selecting and manipulating DOM elements such as:

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
And many more - checkout source of `domo.core` namespace. 

<br>

## Usage
Add as a dependency to your project:

```clojure
[design.kushi/domo "0.2.0"]
```
<br>

Import into your namespace:

```clojure
(ns myns.core
  (:require
    [domo.core :as domo]))
```
<br>

## API
API docs coming soon. In the meantime you can checkout all the functions tagged `^:public` in the source `domo.core` namespace. 

## License

Copyright © 2024 Jeremiah Coyle

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
