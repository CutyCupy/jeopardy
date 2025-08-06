import { hideJoinButtons } from "./lobby.js";
import { questionAnswerToolWrapper } from "./question.js";
import { registerSubscription, stompClient } from "./websocket.js";

export let isGameMaster;


const startGameButton = document.getElementById("start-game");

startGameButton.addEventListener('click', startGame);

const gamemasterButton = document.getElementById("gamemaster");

gamemasterButton.addEventListener('click', becomeGameMaster);


export function registerGamemaster() {
    registerSubscription((client) => {
        client.subscribe("/topic/gamemaster-update", gamemasterUpdate)
        client.subscribe("/user/topic/gamemaster-update", gamemasterUpdate)

        client.subscribe("/user/topic/gamemaster", (_) => {
            isGameMaster = true;

            hideJoinButtons();
            startGameButton.style.display = null;
            questionAnswerToolWrapper.style.display = 'none';
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
    const password = prompt("Bitte gib das Master-Passwort ein:");
    stompClient.send("/app/gamemaster", {}, password);
}

export function reveal(more) {
    stompClient.send("/app/reveal-question", {}, more);
}
