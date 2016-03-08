(ns hanabi.pages
  (:require [hanabi.utils :refer [random-identifier]]
            [hiccup.core :refer :all]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.form :refer [form-to label text-field submit-button]]
            [hiccup.element :refer [unordered-list link-to]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn home []
  (html5
   [:h1 "Hanabi"]
   [:h2 "Login"]
   (form-to [:post "/login"]
            (anti-forgery-field)
            (label "username" "Username")
            (text-field {:required true} "username")
            (submit-button "Login"))))

(defn lobby [games]
  (html5
   [:h1 "Hanabi"]
   (link-to (str "game/" (random-identifier)) "Create game") ;TODO make sure this is unique?
   [:hr]
   [:h2 "Join a game"]
   (unordered-list (map #(link-to (str "game/" %) %) games))))

(defn game [player]
  (html5
   (include-css "/css/game.css")
   [:div#me {:style "display:none"} player]
   [:div#app]
   (include-js "/js/game.js")))
