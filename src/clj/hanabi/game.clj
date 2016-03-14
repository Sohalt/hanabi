(ns hanabi.game)

(def ^:const DECK (for [color [:red :green :blue :yellow :white] number (mapcat repeat [3 2 2 2 1] (range 1 6))]
                      {:color color :number number}))

(def ^:const HINTS 8)
(def ^:const LIGHTNING 3)

(def game
  {:running false
   :deck (vec (map #(assoc %1 :id %2) (shuffle DECK) (range (count DECK))))
   :discard []
   :cards-drawn 0
   :players []
   :hands {}
   :current-player nil
   :hints HINTS
   :table {:red 0 :green 0 :blue 0 :yellow 0 :white 0}
   :lightning 0})

(defn join [game player]
  (assert (not (:running game)));can only join a game that is not running yet
  (assert (not (some #{player} (:players game))));username must be unique
  (assert (< (count (:players game)) 5));can only join a game with less than 5 players
  (update game :players conj player))

(defn start [game]
  (assert (not (:running game)))
  (let [{:keys [deck players]} game
        num-players (count players)
        cards-per-player (if (<= num-players 3) 5 4)
        cards-drawn (* num-players cards-per-player)]
    (assert (<= 2 num-players 5));can only start a game with 2 to 5 players
    (merge game {:running true
                 :current-player (rand-int num-players)
                 :cards-drawn cards-drawn
                 :hands (zipmap players
                                (map (fn [hand]
                                       (let [ids (mapv :id hand)]
                                         {:order ids
                                          :cards (zipmap ids hand)}))
                                     (partition cards-per-player (:deck game))))})))

(defn current-player [game]
  ((:players game) (:current-player game)))

(defn- next-turn [game]
  (update game :current-player #(mod (inc %) (count (:players game)))))

(defn- add-card [hand {:keys [id] :as card}]
  (-> hand
      (update :order conj id)
      (update :cards assoc id card)))

(defn- remove-card [hand card-id]
  (-> hand
      (update :order (fn [order] (filterv #(not= % card-id) order)))
      (update :cards dissoc card-id)))

#_(defn reorder-hand [game player new-order]
  (let [old-order (get-in game [:hands player :order])]
    (assert (= (count new-order) (count old-order)) "reordering does not change card count")
    (assert (= (set old-order) (set new-order)) "reordering does not change cards"))
  (assoc-in game [:hands player :order] new-order))

(defn swap-cards [game player [card1 card2]]
  (let [order (get-in game [:hands player :order])]
    (assert (some #{card1} order) "player has card1")
    (assert (some #{card2} order) "player has card2"))
  (update-in game [:hands player :order] #(replace {card1 card2 card2 card1} %)))

(defn- draw [game]
  (assert (< (:cards-drawn game) (count DECK)))
  (let [player (current-player game)
        card ((:deck game) (:cards-drawn game))]
    (-> game
        (update :cards-drawn inc)
        (update-in [:hands player] add-card card)
        (next-turn))))

(defn- has-card? [game player card-id]
  (contains? (get-in game [:hands player :cards]) card-id))

(defn- inc-hints [hints]
  (min HINTS (inc hints)))

(defn play [game card-id]
  (assert (< (:lightning game) LIGHTNING))
  (assert (:running game))
  (let [player (current-player game)]
    (assert (has-card? game player card-id));player has to have the card
    (let [{:keys [color number] :as card} ((:deck game) card-id)]
      (->
       (if (= (get-in game [:table color]) (dec number)) ; does the card match?
         (-> game
             (assoc-in [:table color] number) ; yes -> play it
             (update :hints (if (= number 5) inc-hints identity)))
         (-> game
             (update :lightning inc) ; no -> lightning strikes
             (update :discard conj card)))
       (update-in [:hands player] remove-card card-id)
       (draw)))))

(defn discard [game card-id]
  (assert (< (:lightning game) LIGHTNING))
  (assert (:running game))
  (let [player (current-player game)]
    (assert (has-card? game player card-id));player has to have the card
    (-> game
        (update :discard conj ((:deck game) card-id))
        (update :hints inc-hints)
        (update-in [:hands player] remove-card card-id)
        (draw))))

(defn hint [game]
  (assert (< (:lightning game) LIGHTNING))
  (assert (> (:hints game) 0));hints left?
  (assert (:running game))
  (-> game
      (update :hints dec)
      (next-turn)))

(defn hint->ids [game player hint]
   "takes a hint in the form of a color as a keyword e.g. :red or a number e.g 5 and returns the card ids of all cards in the player's hand matching the hint"
   (let [type (if (keyword? hint) :color :number)]
     (set (map :id (filter #(= (type %) hint) (vals (get-in game [:hands player :cards])))))))
