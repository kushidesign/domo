(ns starter.browser-tests)

(defmacro browser-test
  [fcall expected]
  (let [[f & args] fcall
        args (into [] args)]
    `(let [actual# ~fcall]
       (when (not= actual# ~expected)
         (swap! S conj {:f        ~f
                        :args     ~args
                        :expected ~expected
                        :actual   actual#
                        })))))
