(defproject design.kushi/domo "0.4.0"
  :description "A ClojureScript DOM utility library"
  :url "https://github.com/kushidesign/domo"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [applied-science/js-interop "0.4.2"]]
  :repl-options {:init-ns domo.core}
  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :sign-releases false}]])
