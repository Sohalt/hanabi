(ns hanabi.ui-events
  (:require [hanabi.communication :refer [chsk-send!]]
            [clojure.set :refer [intersection]]))

;;;; UI Events
(defn start-listener [ev]
  (chsk-send! [:hanabi/start]))

(defn drag-start [ev]
  (.log js/console "drag start")
  (let [datr (.-dataTransfer ev)]
    #_(set! (.-dropEffect datr) "move")
    (.setData datr "text" (.-id (.-target ev)))))

(defn drag-over [ev]
  (.preventDefault ev)
  (.log js/console "drag enter")
  (let [t (.-target ev)]
    (set! (.-scale (.-style t)) 1.1))
  false)

(defn discard-drop [ev]
  "gets called when the player drops a card on the discard pile (i.e. discards it)"
  (.preventDefault ev)
  (.log js/console "discard drop")
  (let [id (int (.getData (.-dataTransfer ev) "text"))]
    (chsk-send! [:hanabi/discard id]))
  false)

(defn play-drop [ev]
  "gets called when the player drops a card on the table (i.e. plays it)"
  (.preventDefault ev)
  (.log js/console "play drop")
  (let [id (int (.getData (.-dataTransfer ev) "text"))]
    (chsk-send! [:hanabi/play id]))
  false)

;TODO proper hint UI
(defn give-hint [ev]
  "prompt for the hint to give"
  (.log js/console "hint clicked")
  (let [player (.prompt js/window "player")
        h (.prompt js/window "hint")
        hint (if (re-matches #"\d" h) (int h) (keyword h))]
    (.log js/console "give hint")
    (chsk-send! [:hanabi/hint [player hint]])))

;;;; UI idea: click on cards to mark them for a hint.
;;;; Dynamically display possible hints givent the current selection
;;;; Click on a displayed hint to give it

;; (def marked-for-hint (atom {:player nil :cards #{}}))

;; (defn mark-for-hint [ev]
;;   (let [card (.-target ev)
;;         player (.-parentNode (.-parentNode card))
;;         card-id (.-id card)
;;         player-name (.-id player)
;;         {:keys [player cards]} @marked-for-hint]
;;     (if (not= player-name player)
;;       (reset! marked-for-hint {:player player-name :cards #{card-id}})
;;       (if (contains? cards card-id)
;;         (swap! marked-for-hint update :cards disj id)
;;         (swap! marked-for-hint update :cards conj id)))))

;; (defn- ids->hints [game player ids]
;;   "given a list of card ids returns possible hints to give matching those cards"
;;   (let [hand (get-in game [:hands player])]
;;     (apply intersection (map #(set (select-keys [:color :numbebr] (hand %))) ids))))
