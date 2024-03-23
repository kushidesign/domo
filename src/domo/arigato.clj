(ns domo.arigato)

;; TODO
;; PUT it in a bb script
;; Add watcher
;; Incorporate cljstyle
;; Add the namespace thing
;; some kind of caching thing


(defn file-info [form-meta]
  (when-let [{ln  :line
              col :column} form-meta]
    (let [ns-str (some-> *ns* ns-name str)]
      (str ns-str ":%c" ln "%c:%c" col "%c"))))

;; (defmacro round-by-dpr2 [n bool s]
;;   (let [file-info (file-info (meta &form))]
;;     `(do (domo.core/warn 'round-by-dpr2
;;                          [[(quote ~n) ~n number? 'number?]
;;                           [(quote ~bool) ~bool boolean? 'boolean?]
;;                           [(quote ~s) ~s domo.core/element? 'domo.core/element?]]
;;                          ~file-info)
         
;;          (domo.core/round-by-dpr2 ~n ~bool ~s))))

;; (defmacro round-by-dpr2 [n bool s]
;;   (let [file-info (file-info (meta &form))]
;;     `(do (domo.core/warn
;;           'round-by-dpr2
;;           [[(quote ~n) ~n number? 'number?]
;;            [(quote ~bool) ~bool boolean? 'boolean?] 
;;            [(quote ~s) ~s domo.core/element? 'domo.core/element?]]
;;           ~file-info)
;;          (domo.core/round-by-dpr2 ~n ~bool ~s))))

;; (defmacro round-by-dpr2 [n bool s]
;;   (let [file-info (file-info (meta &form))] `(do (domo.core/warn 'round-by-dpr2 [[(quote ~n) ~n number? 'number?] [(quote ~bool) ~bool boolean? 'boolean?] [(quote ~s) ~s string? 'string?]] ~file-info) (domo.core/round-by-dpr2 ~n ~bool ~s))))

;; (defmacro round-by-dpr2 [n m_1 s] (let [file-info (file-info (meta &form))] `(do (domo.core/warn 'round-by-dpr2 [[(quote ~n) ~n number? 'number?] [(quote ~m_1) ~m_1 map? 'map?] [(quote ~s) ~s string? 'string?]] ~file-info) (domo.core/round-by-dpr2 ~n ~m_1 ~s))))

(defmacro round-by-dpr2 [n]
  (let [file-info (file-info (meta &form))]
    `(do (domo.core/warn 'round-by-dpr2
                         [[(quote ~n) ~n number? 'number?]]
                         ~file-info)
         (domo.core/round-by-dpr2 ~n))))

