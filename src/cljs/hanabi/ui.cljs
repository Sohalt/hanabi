(ns hanabi.ui
  (:require [hanabi.ui-events :refer [drag-start drag-over drag-leave start-listener discard-drop play-drop swap-drop give-hint]]))

(defn- card
  ([c] (card c false))
  ([{:keys [id color number]} mine]
   (let [base-attrs {:class (name (or color :unknown))}
         id-attrs (when id {:key id :id id})
         draggable-attrs (when mine {:draggable true :onDragStart #(drag-start %) :onDragOver #(drag-over %) :onDragLeave #(drag-leave %) :onDrop #(swap-drop %)})]
     [:div.card (merge base-attrs id-attrs draggable-attrs) (or number "?")])))

(defn- hand ;store marked cards (for hints) here?
  ([p] (hand p false))
  ([p a] (hand p a false))
  ([[name cards] active mine]
   [:div.player {:key name :id name}
    [:h2 (if active {:id "active"} {}) name]
    [:div.cards (map #(card % mine) cards)]]))

(defn- discard-pile [cards]
  [:div#discard-pile
   {:onDrop #(discard-drop %) :onDragOver #(drag-over %) :onDragLeave #(drag-leave %)}
   [:div.card.discard "X"]
   (map card cards)])

(defn- stacks [stacks]
  [:div#stacks
   {:onDrop #(play-drop %) :onDragOver #(drag-over %) :onDragLeave #(drag-leave %)}
   (map card (map (fn [[color number]] {:color color :number number}) stacks))])

(defn- hints [free]
  (let [used (- 8 free)]
    [:div#hints (repeat free [:div.hint.free {:onClick #(give-hint %)}]) (repeat used [:div.hint.used])]))

(defn- strikes [struck]
  (let [left (- 3 struck)]
    [:div#strikes (repeat left [:div.strike.left]) (repeat struck [:div.strike.struck])]))

(defn- display-hands [players hands current-player me]
  (let [others (filter (partial not= me) players)
        ordered-cards (fn [{:keys [order cards]}] (map cards order))]
    [:div#hands
     (map (fn [player] (hand [player (ordered-cards (hands player))] (= current-player player))) others)
     (hand [me (ordered-cards (hands me))] (= current-player me) true)]))

(defn table [game me]
  "displays the game state on the table"
  (let [game @game
        players (:players game)
        hands (:hands game)
        current-player (players (:current-player game))]
    [:div#table (if-not (= me current-player) (let [prevent-event (fn [e] (.preventDefault e) (.stopPropagation e))] {:onMouseDownCapture prevent-event :onClickCapture prevent-event}) {}) ; prevent events when it's no the player's turn
     [discard-pile (:discard game)]
     [stacks (:table game)]
     [hints (:hints game)]
     [strikes (:lightning game)]
     [display-hands players hands current-player me]]))

(defn- start []
  [:button#start-btn {:onClick #(start-listener %)} "Start"])

(defn- players [names]
  [:ul (map (fn [n] [:li {:key n} n]) names)])

(defn lobby [game]
  (let [playercnt (count (:players @game))]
    [:div#lobby
     [players (:players @game)]
     (if (<= 2 playercnt)
       [start]
       [:span "Waiting for players…"])]))

(defn app [game me]
  (if (:running @game)
    [table game me]
    [lobby game]))
