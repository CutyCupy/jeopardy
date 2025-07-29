import { joinGame } from "./lobby.js";


const joinGameButton = document.getElementById("join-game");

joinGameButton.addEventListener('click', joinGame);