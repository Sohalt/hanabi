(ns hanabi.utils
  (:require [clojure.string :refer [capitalize split-lines]]))

(def adjs (split-lines (slurp "adjs.txt")))

(def nouns (split-lines (slurp "nouns.txt")))

(defn random-identifier []
  (apply str (map (comp capitalize rand-nth) [adjs nouns])))
