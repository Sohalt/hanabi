#Architecture

The following describes the architecture of the application as well as ideas for alternative implementation approaches or future features.

##Current Implementation

The application consists of a server, written in Clojure, and a client for the browser, written in ClojureScript.
The server manages the gamestate and the communication between clients.
The client takes user input and displays the game state to the user.
Both components communicate via WebSockets using the sente library.

###Server
The routes to be acessed by a user are:

`/`, which displays a login form, when the user is not currently logged in or a list of all open games when the user is logged in.
`/game/:id` (where `:id` is an arbitrary string), which displays the UI for a game.

Additionally there are
`/login`' where the login form POSTs its data
and
`/game/:id/ws` where the sente handlers wait for GET or POST requests.

The server creates a new sente channel for each new game and stores the handlers for `/game/:id/ws` in a global hashmap keyed by the id.
The gamestate itself is closed over by the server eventhandler and not explicitly stored in the map, altough one can simply add it if the need arises (e.g. during debugging).

###Client
The client holds the gamestate in an atom and updates it with new information from the server each turn. The UI is created using reagent which automatically triggeres a rerender of the necessary parts when something in the gamestate changes.

###Protocol
The protocol is event based. Each event is identified by a fully qualified keyword. Events can optionally carry arbitrary edn data.

####Client to Server

| Event ID | Data | Description |
| -------- | ---- | ----------- |
| `:hanabi/join` | none (the server determines the username from the sente uid, which in turn relies on the ring session) | The client automatically tries to join a game so this gets sent directly after the WebSocket connection got established. |
| `:hanabi/play` | `card-id` (an integer identifying the card to play) | Gets sent when the player wants to play a card (i.e. advance the fireworks) |
| `:hanabi/discard`| `card-id` (an integer identifying the card to play) | Gets sent when the player wants to discard a card to regain a hint. |
| `:hanabi/hint` | `[player hint]` (the player which should receive the hint, the hint itself, which can be either an integer denoting the number to hint at (1,2,3,4,5) or a keyword denoting the color to hint at (:red,:blue,:green,:yellow,:white)) | Gets sent when the player wants to give a hint to another player. |

####Server to Client

| Event ID | Data | Description |
| -------- | ---- | ----------- |
| `:hanabi/join` | username (the name of the joining player) | Gets sent when a new player joins the game |
| `:hanabi/game-state` | `game-state` (the current gamestate, filtered to not contain information on the players own cards and the deck) | Gets sent after the gamestate changed |
| `:hanabi/hint` | `[hint card-ids]` (the hint (integer or keyword for a number or color respectively) and the card-ids for which the hint matches) | Gets sent when a player gives a hint |

##Ideas for the Future

It might be beneficial to send more information than the updated gamestate to the client after a player ends their turn, so that the client can properly display the action taken. When only sending the new gamestate the client has to deduce which card got moved where to display a correct animation.

At some point the implementation shared the gamelogic between the server and the client (using cljc files) and would basically only relay the received action from the current player to all other players. The clients there would apply the appropriate update much in the same way that the server did.
This has the problem, that to fully support shared game logic, which only requires simple broadcasting of the action taken by the current player to the other players, the client has to have the same state as the server, especially the deck to look up cards, which would permit cheating by looking up concealed cards in the deck.
