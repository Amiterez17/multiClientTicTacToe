# Multi-Client Tic-Tac-Toe (JavaFX & OCSF)

A full-stack, thread-safe, multiplayer Tic-Tac-Toe game developed as part of my Software Engineering studies. The project features a robust Server-Client architecture utilizing the Object Client-Server Framework (OCSF) and JavaFX for the graphical user interface.

## 🚀 Key Features
* **Server-Authoritative Logic:** Game state, turn validation, and win/tie conditions are managed entirely by the server to prevent race conditions or faulty client states.
* **Smart Spectator Mode:** Supports automatic locking and UI restriction for any 3rd client attempting to join an active 2-player session.
* **Concurrent Programming (Multi-Threading):** Utilizes custom Java threads for smooth visual feedback, delays, and game-state transitions without blocking the main network or UI loops.
* **Event-Driven Architecture:** Decoupled messaging network powered by `EventBus` to handle incoming asynchronous client events cleanly.

## 🛠️ Tech Stack
* **Language:** Java (JDK 21)
* **GUI Framework:** JavaFX (with FXML layout)
* **Networking:** OCSF (Object Client-Server Framework)
* **Dependency Management:** Maven
* **Libraries:** GreenRobot EventBus