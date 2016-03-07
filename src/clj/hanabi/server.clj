(ns hanabi.server
  (:require [compojure
             [core :refer :all]
             [route :as route]]
            [hanabi.game :as game]
            [hanabi.pages]
            [hanabi.server-event-handlers :refer [dispatch-event!]]
            [org.httpkit.server :as http-kit]
            [ring.middleware
             [defaults :refer [site-defaults wrap-defaults]]
             [reload :as reload]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit
             :refer
             [sente-web-server-adapter]]))

(defn wrap-dispatch-event!
  [sente game {:keys [id ?data uid] :as event}]
  (println "got" id ":" ?data "from" uid)
  (dispatch-event! sente game event))

(def handlers (atom {}))

(defn setup-game! [id]
  (let [game (atom hanabi.game/game)
        {:keys [ch-recv send-fn connected-uids] :as sente} (sente/make-channel-socket! sente-web-server-adapter)
        dispatch-fn (partial wrap-dispatch-event! (select-keys sente [:send-fn :connected-uids]) game)
        stop-chsk-router! (sente/start-chsk-router! ch-recv dispatch-fn)]
    (swap! handlers assoc id (select-keys sente [:ch-recv :ajax-post-fn :ajax-get-or-ws-handshake-fn]) #_(merge {:game game} (select-keys sente [:ch-recv :ajax-post-fn :ajax-get-or-ws-handshake-fn]))))) ;use the second form if you need access to the gamestate during debugging.

(defroutes app-routes
  (GET "/" {{uid :uid} :session} (if uid (hanabi.pages/lobby (keys @handlers)) (hanabi.pages/home)))
  (POST "/login" {session :session {:keys [username]} :params} {:status 302 :session (assoc session :uid username) :headers {"Location" "/"}})
  (context "/game/:id" [id]
    (GET "/" {{uid :uid} :session} (when-not (contains? @handlers id) (setup-game! id)) (hanabi.pages/game uid))
    (GET "/ws" ring-req ((get-in @handlers [id :ajax-get-or-ws-handshake-fn]) ring-req))

    (POST "/ws" ring-req ((get-in @handlers [id :ajax-post-fn]) ring-req)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn -main [& args]
  (println "starting...")
  (let [handler (if (some #{"-reload"} args) (reload/wrap-reload #'app) app)]
    (http-kit/run-server handler {:port 3000}))) ;TODO specify port in config or on commandline
