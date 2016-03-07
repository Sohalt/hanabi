# hanabi

An implementation of the cooperative card game [Hanabi](https://en.wikipedia.org/wiki/Hanabi_%28card_game%29), winner of Spiel des Jahres 2013, in Clojure and ClojureScript for playing in the Browser.
If you like the game please consider buying the physical version to support the original author. Additionally it is a kind of game that is quite a bit more fun to play face to face at a table.
If you want to support me as well for the development of this version you can buy the game through the Amazon Affiliate Program:
[Normal Edition](http://www.amazon.de/gp/product/B009CQLZR0/ref=as_li_tl?ie=UTF8&camp=1638&creative=6742&creativeASIN=B009CQLZR0&linkCode=as2&tag=sohaltnet-21)
[Deluxe Edition](http://www.amazon.de/gp/product/B00ERK4GV8/ref=as_li_tl?ie=UTF8&camp=1638&creative=6742&creativeASIN=B00ERK4GV8&linkCode=as2&tag=sohaltnet-21)

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To compile the clojurescript and start a web server for the application, run:

    lein start
    
## Feedback

Any sort of constructive feedback is highly welcome. Just open an issue or pull request.
I am especially looking for feedback on the frontend side, both UX/design wise, as well as comments on code.

## WIP Notice

This is work in progress. The core mechanics are all implemented, but currently the UI is horrible.

Some things which I plan to implement:

- animation (make player moves and effects more visually apparent, e.g. animate which card got played and where it ended up)
- reordering of cards in a hand
- support for advanced game modes including the multicolored cards
- scoring and leaderboard

## License

Copyright © 2016 sohalt
