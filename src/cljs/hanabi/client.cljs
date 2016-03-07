(ns hanabi.client
  (:require [hanabi.ui :as ui]
            [hanabi.communication :refer [start-router!]]
            [reagent.core :as r]))

(defonce game (r/atom {:running false
                       :discard []
                       :cards-drawn 0
                       :players []
                       :hands {}
                       :current-player nil
                       :hints 8
                       :table {:red 0 :green 0 :blue 0 :yellow 0 :white 0}
                       :lightning 0
                       }))

(def me (.-textContent (.getElementById js/document "me")))

(start-router! game) ;start event router

;;;; reagent setup
(r/render [ui/app game me] (.getElementById js/document "app"))
