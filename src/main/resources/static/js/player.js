import { joinGame } from "./lobby.js";

export const playerAnswers = document.getElementById("player-answers");
export const playerArea = document.getElementById("player-area");

const joinGameButton = document.getElementById("join-game");

joinGameButton.addEventListener('click', joinGame);