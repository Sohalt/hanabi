(ns hanabi.game-test
  (:require [clojure.test :refer :all]
            [hanabi.game :refer :all]))

(def deck [{:color :white, :number 2, :id 0} {:color :yellow, :number 4, :id 1} {:color :red, :number 4, :id 2} {:color :blue, :number 3, :id 3} {:color :green, :number 1, :id 4} {:color :red, :number 4, :id 5} {:color :yellow, :number 4, :id 6} {:color :yellow, :number 1, :id 7} {:color :red, :number 3, :id 8} {:color :green, :number 1, :id 9} {:color :yellow, :number 1, :id 10} {:color :red, :number 5, :id 11} {:color :red, :number 1, :id 12} {:color :yellow, :number 5, :id 13} {:color :blue, :number 5, :id 14} {:color :green, :number 4, :id 15} {:color :blue, :number 2, :id 16} {:color :green, :number 1, :id 17} {:color :green, :number 3, :id 18} {:color :yellow, :number 3, :id 19} {:color :green, :number 4, :id 20} {:color :red, :number 2, :id 21} {:color :blue, :number 1, :id 22} {:color :red, :number 2, :id 23} {:color :white, :number 4, :id 24} {:color :white, :number 1, :id 25} {:color :green, :number 5, :id 26} {:color :red, :number 1, :id 27} {:color :blue, :number 1, :id 28} {:color :white, :number 1, :id 29} {:color :white, :number 3, :id 30} {:color :white, :number 2, :id 31} {:color :blue, :number 3, :id 32} {:color :blue, :number 2, :id 33} {:color :white, :number 3, :id 34} {:color :white, :number 4, :id 35} {:color :yellow, :number 2, :id 36} {:color :blue, :number 4, :id 37} {:color :white, :number 5, :id 38} {:color :yellow, :number 3, :id 39} {:color :white, :number 1, :id 40} {:color :red, :number 1, :id 41} {:color :red, :number 3, :id 42} {:color :green, :number 2, :id 43} {:color :green, :number 2, :id 44} {:color :blue, :number 4, :id 45} {:color :blue, :number 1, :id 46} {:color :yellow, :number 1, :id 47} {:color :green, :number 3, :id 48} {:color :yellow, :number 2, :id 49}])
(def empty-game (assoc game :deck deck))
(def one-player (assoc empty-game :players ["foo"]))
(def two-players (assoc empty-game :players ["foo" "bar"]))
(def three-players (assoc empty-game :players ["foo" "bar" "baz"]))
(def four-players (assoc empty-game :players ["foo" "bar" "baz" "quux"]))
(def five-players (assoc empty-game :players ["foo" "bar" "baz" "quux" "xyzzy"]))
(def running-game (assoc (start two-players) :current-player 0)) ; make current player deterministic
(def min-hints (assoc running-game :hints 0))
(def lost-game (-> running-game
                   (play 0)
                   (play 5)
                   (play 1)))

(deftest test-deck
  (let [deck (:deck game)]
    (testing "card count"
      (is (= 50 (count deck))))
    (testing "colors"
      (let [color-groups (group-by :color deck)]
        (is (= 5 (count color-groups)) "5 different colors")
        (is (every? #(= 10 (count %)) (vals color-groups)) "10 cards of a color")))
    (testing "numbers"
      (let [number-groups (group-by :number deck)]
        (is (= 5 (count number-groups)) "5 different numbers")
        (is (= 15 (count (number-groups 1))) "15 cards of value 1")
        (is (every? #(= 10 (count %)) (map number-groups (range 2 5))) "10 cards each of values 2 to 4")
        (is (= 5 (count (number-groups 5))) "5 cards of value 5")))
    (testing "ids"
      (is (= 50 (count (group-by :id deck))) "unique ids"))))

(deftest test-join
  (testing "join empty game"
    (is (= ["foo"] (:players (join empty-game "foo")))))
  (testing "join twice (i.e. with existing username)"
    (is (thrown? AssertionError (join one-player "foo"))))
  (testing "join running game"
    (is (thrown? AssertionError (join running-game "asdf"))))
  (testing "join full game"
    (is (thrown? AssertionError (join five-players "asdf")))))

(deftest test-start
  (testing "start with 2 players"
    (let [game (start two-players)
          hands (:hands game)]
      (is (:running game))
      (is (= 2 (count hands)))
      (is (every? #(= 5 (count %)) (vals hands)))))
  (testing "start with 1 player"
    (is (thrown? AssertionError (start one-player))))
  (testing "start running game"
    (is (thrown? AssertionError (start running-game)))))

(deftest test-play
  (testing "play matching card"
    (let [g (play running-game 4)]
      (is (= 1 (get-in g [:table :green])) "card got played")
      (is (= 0 (count (:discard g))) "card did not get discarded")
      (is (not (contains? (get-in g [:hands "foo"]) 4)) "player does not have the card anymore")
      (is (= 5 (count (get-in g [:hands "foo"]))) "player has drawn a new card")
      (is (= 1 (:current-player g)) "next turn")))
  (testing "play non matching card"
    (let [g (play running-game 0)]
      (is (= 1 (:lightning g)) "lightning struck")
      (is (= 1 (count (:discard g))) "card got discarded")
      (is (not (contains? (get-in g [:hands "foo"]) 0)) "player does not have the card anymore")
      (is (= 5 (count (get-in g [:hands "foo"]))) "player has drawn a new card")
      (is (= 1 (:current-player g)) "next turn")))
  (testing "play card that does not belong to the current player"
    (is (thrown? AssertionError (play running-game 5))))
  (testing "play in not running game"
    (is (thrown? AssertionError (play two-players 0))))
  (testing "play in lost game"
    (is (thrown? AssertionError (play lost-game 6)))))

(deftest test-discard
  (testing "discard a card while at max hints"
    (let [g (discard running-game 0)]
      (is (= 8 (:hints g)) "no more than 8 hints")
      (is (= [{:color :white, :number 2, :id 0}] (:discard g)) "card got discarded")
      (is (= 1 (:current-player g)) "next turn")))
  (testing "discard a card when at min hints"
    (let [g (discard min-hints 0)]
      (is (= 1 (:hints g)) "got a hint")
      (is (= [{:color :white, :number 2, :id 0}] (:discard g)) "card got discarded")
      (is (= 1 (:current-player g)) "next turn")))
  (testing "discard a card that does not belong to the current player"
    (is (thrown? AssertionError (discard running-game 5)))))

(deftest test-hint
  (testing "give a hint while hints left"
    (let [g (hint running-game)]
      (is (= 7 (:hints g)) "used up a hint")))
  (testing "give a hint when no hints left"
    (is (thrown? AssertionError (hint min-hints)))))

(deftest test-hint->ids
  (testing "existing color hint"
    (is (= #{5 8} (hint->ids running-game "bar" :red))))
  (testing "nonexisting color hint"
    (is (= #{} (hint->ids running-game "bar" :white))))
  (testing "existing number hint"
    (is (= #{5 6} (hint->ids running-game "bar" 4))))
  (testing "nonexisting number hint"
    (is (= #{} (hint->ids running-game "bar" 5)))))
