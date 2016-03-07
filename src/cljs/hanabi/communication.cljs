(ns hanabi.communication
  (:require [taoensso.sente :as sente]
            [hanabi.client-event-handlers :refer [dispatch-event!]]))

(defn path [] (-> js/window .-location .-pathname))

;;;; Socket

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client! (str (path) "/ws") {:type :auto
                                                             :wrap-recv-evs? false})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

;;;; Router

(defn wrap-dispatch-event! [game {:keys [id ?data] :as event}]
  (.log js/console (str "got " id " : " ?data))
  (dispatch-event! game event))

(defonce router_ (atom nil))
(defn stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! [game]
  (stop-router!)
  (reset! router_
          (sente/start-client-chsk-router!
           ch-chsk (partial wrap-dispatch-event! game))))
