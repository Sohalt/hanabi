(ns hanabi.server-event-handlers
  (:require [hanabi.game :refer [join start play discard hint hint->ids current-player]]))

(defmulti update-game-state!
  (fn [game event]
    (:id event)))

(defmethod update-game-state!
  :hanabi/join
  [game {username :uid}]
  (swap! game join username))

(defmethod update-game-state!
  :hanabi/start
  [game _]
  (swap! game start))

(defmethod update-game-state!
  :hanabi/play
  [game {card-id :?data uid :uid}]
  (assert (= uid (current-player @game)))
  (swap! game play card-id))

(defmethod update-game-state!
  :hanabi/discard
  [game {card-id :?data uid :uid}]
  (assert (= uid (current-player @game)))
  (swap! game discard card-id))

(defmethod update-game-state!
  :default
  [_ event]
  (println "unmatched event" (:id event)))

(defn- filter-hands [player hands]
  (if (contains? hands player)
    (update hands player (fn [hand] (reduce-kv (fn [a k v] (assoc a k (select-keys v [:id]))) {} hand)))
    hands))

(defn- filter-game [game player]
  (-> game
      (dissoc :deck)
      (update :hands #(filter-hands player %))))

(defmulti dispatch-event!
  (fn [sente game event]
    (:id event)))

(defmethod dispatch-event!
  :hanabi/game-state
  [{:keys [send-fn]} game {:keys [uid ?data]}]
  (send-fn uid [:hanabi/game-state (filter-game @game uid)]))

(defmethod dispatch-event!
  :hanabi/hint
  [{:keys [connected-uids send-fn]} game {uid :uid [player hint-data] :?data}]
  (assert (= uid (current-player @game)))
  (doseq [uid (:any @connected-uids)] ;broadcast the hints
    (send-fn uid [:hanabi/hint [hint-data (hint->ids @game player hint-data)]]))
  (swap! game hint) ;update gamestate
  (doseq [uid (:any @connected-uids)] ;broadcast new game state
    (send-fn uid [:hanabi/game-state (filter-game @game uid)])))

(defmethod dispatch-event!
  :default
  [{:keys [connected-uids send-fn]} game event]
  (update-game-state! game event) ;update gamestate
  (doseq [uid (:any @connected-uids)] ;broadcast new game state
    (send-fn uid [:hanabi/game-state (filter-game @game uid)])))
