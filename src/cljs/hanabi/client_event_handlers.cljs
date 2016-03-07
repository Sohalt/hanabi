(ns hanabi.client-event-handlers)

(defmulti dispatch-event!
  (fn [game event]
    (:id event)))

(defmethod dispatch-event!
  :hanabi/game-state
  [game {game-state :?data}]
  (reset! game game-state))

(defmethod dispatch-event!
  :hanabi/hint
  [game {[hint ids] :?data}]
  (.alert js/window (str hint))
  (doseq [id ids]
    (let [card (.getElementById js/document id)
          cl (.-classList card)]
      (.add cl "hint-marker")
      (.log js/console "add hint-marker")
      (.setTimeout js/window #((.remove cl "hint-marker") (.log js/console "remove hint-marker")) 3000)))
  ;TODO display hint properly
  )

(defmethod dispatch-event!
  :chsk/handshake
  [game {send-fn :send-fn [?uid _ _] :?data}]
  (when (not= ?uid :taoensso.sente/nil-uid)
    (send-fn [:hanabi/join]))
  (send-fn [:hanabi/game-state]))

(defmethod dispatch-event!
  :default
  [game event]
  (.log js/console (str "unhandled event: " (:id event))))
