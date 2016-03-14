(defproject hanabi "0.1.0-SNAPSHOT"
  :description "An implementation of the cooperative card game Hanabi in Clojure and ClojureScript"
  :url "https://www.github.com/sohalt/hanabi"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 [com.taoensso/sente "1.8.0"]
                 [http-kit "2.1.18"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [reagent "0.6.0-alpha"]
                 [cprop "0.1.6"]]
  :aliases {"start" ["do" "cljsbuild" "once" "adv," "run"]}
  :hooks [leiningen.cljsbuild]
  :source-paths ["src/clj"]
  :main hanabi.server
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]
         :plugins [[lein-cljsbuild "1.1.2"]
                   [lein-ancient "0.6.8"]]
         :cljsbuild
         {:builds {:dev {:source-paths ["src/cljs"]
                         :compiler {:main "hanabi.client"
                                    :output-to "resources/public/js/game.js"
                                    :output-dir "resources/public/js/out"
                                    :asset-path "/js/out"
                                    :optimizations :none}}
                   :adv {:source-paths ["src/cljs"]
                         :compiler {:main "hanabi.client"
                                    :output-to "resources/public/js/game.js"
                                    :output-dir "resources/public/js/out-adv"
                                    :asset-path "/js/out-adv"
                                    :optimizations :advanced
                                    :pretty-print false}}}}}
   :clean-targets ["resource/public/js" :target-path]})
