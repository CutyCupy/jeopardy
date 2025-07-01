import { hideJoinButtons } from "./lobby.js";
import { playerArea } from "./player.js";
import { registerSubscription, stompClient } from "./websocket.js";

export let isGameMaster;


const startGameButton = document.getElementById("start-game");

startGameButton.addEventListener('click', startGame);

const gamemasterButton = document.getElementById("gamemaster");

gamemasterButton.addEventListener('click', becomeGameMaster);

export const gamemasterAnswers = document.getElementById("gamemaster-answers");


export function registerGamemaster() {
    registerSubscription((client) => {
        client.subscribe("/topic/gamemaster-update", gamemasterUpdate)
        client.subscribe("/user/topic/gamemaster-update", gamemasterUpdate)

        client.subscribe("/user/topic/gamemaster", (_) => {
            isGameMaster = true;

            hideJoinButtons();
            startGameButton.style.display = null;
            playerArea.style.display = 'none';
        });
    })
}


function gamemasterUpdate(msg) {
    var masterExists = JSON.parse(msg.body);

    gamemasterButton.style.display = masterExists ? 'none' : null;
}

function startGame() {
    stompClient.send("/app/start-game", {});
}

function becomeGameMaster() {
    stompClient.send("/app/gamemaster", {});
}

export function reveal(more) {
    stompClient.send("/app/reveal-question", {}, more);
}
