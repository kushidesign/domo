
;; TODO - maybe use emoji library
(ns domo.emoji)

(def emojis
  ["👹"
   "👺"
   "🤡"
   "💩"
   "👻"
   "💀"
   "☠️"
   "👽"
   "👾"
   "🤖"
   "🎃"
   "🍄"
   "🌞"
   "🌝"
   "🌛"
   "🌜"
   "🌎"
   "🪐"
   "💫"
   "⭐️"
   "🌟"
   "✨"
   "⚡️"
   "☄️"
   "💥"
   "🔥"
   "🐉"
   "🐲"
   "🐢"
   "🐍"
   "🦎"
   "🦖"
   "🦕"
   "🐙"
   "🦑"
   "🦐"
   "🦞"
   "🦀"
   "🐋"
   "🦈"
   "🐊"])

(def total-emojis (count emojis))

(defn random-emoji
  ([]
   (nth emojis (rand-int total-emojis) nil))
  ([coll]
   (let [total (count coll)]
     (nth coll (rand-int total) nil))))

(defn random-emojis
  ([n]
   (random-emojis n emojis))
  ([n coll]
   (let [total (count coll)]
     (into []
           (for [_ (range n)]
             (nth coll (rand-int total) nil))))))
