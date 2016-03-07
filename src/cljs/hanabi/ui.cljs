(ns hanabi.ui
  (:require [hanabi.ui-events :refer [drag-start drag-over start-listener discard-drop play-drop give-hint]]))

(defn current-player [player]
  [:div [:span "current player: " player]])

(defn- card
  ([c] (card c false))
  ([{:keys [id color number]} draggable]
   (let [base-attrs {:class (name (or color :unknown))}
         id-attrs (when id {:key id :id id})
         draggable-attrs (when draggable {:draggable true :onDragStart #(drag-start %)})]
     [:div.card (merge base-attrs id-attrs draggable-attrs) (or number "?")])))

(defn- hand
  ([p] (hand p false))
  ([[name cards] mine]
   [:div.player {:key name :id name}
    [:h2 name]
    [:div.cards (map #(card % mine) (vals cards))]]))

(defn- discard-pile [cards]
  [:div#discard-pile
   {:onDrop #(discard-drop %) :onDragOver #(drag-over %)}
   [:div.card.discard "X"]
   (map card cards)])

(defn- stacks [stacks]
  [:div#stacks
   {:onDrop #(play-drop %) :onDragOver #(drag-over %)}
   (map card (map (fn [[color number]] {:color color :number number}) stacks))])

(defn- hints [free]
  (let [used (- 8 free)]
    [:div#hints (repeat free [:div.hint.free {:onClick #(give-hint %)}]) (repeat used [:div.hint.used])]))

(defn- strikes [struck]
  (let [left (- 3 struck)]
    [:div#strikes (repeat left [:div.strike.left]) (repeat struck [:div.strike.struck])]))

(defn- display-hands [players hands me]
  (let [others (filter (partial not= me) players)]
    [:div#hands (map #(hand [% (hands %)]) others) (hand [me (hands me)] true)]))

(defn table [game me]
  "displays the game state on the table"
  (let [game @game
        players (:players game)
        hands (:hands game)
        player (players (:current-player game))]
    [:div#table
     [current-player player] ;TODO display more elegantly (e.g. marker next to player name)
     [discard-pile (:discard game)]
     [stacks (:table game)]
     [hints (:hints game)]
     [strikes (:lightning game)]
     [display-hands players hands me]]))

(defn- start []
  [:button#start-btn {:onClick #(start-listener %)} "Start"])

(defn- players [names]
  [:ul (map (fn [n] [:li {:key n} n]) names)])

(defn lobby [game]
  (let [playercnt (count (:players @game))]
    [:div#lobby
     [players (:players @game)]
     (when (<= 2 playercnt 5)
       [start])]))

(defn app [game me]
  (if (:running @game)
    [table game me]
    [lobby game]))
