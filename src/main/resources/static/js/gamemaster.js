import { hideJoinButtons } from "./lobby.js";
import { playerArea } from "./player.js";
import { registerSubscription, stompClient } from "./websocket.js";

export let isGameMaster;


const startGameButton = document.getElementById("start-game");

startGameButton.addEventListener('click', startGame);

const gamemasterButton = document.getElementById("gamemaster");

gamemasterButton.addEventListener('click', becomeGameMaster);

export const gamemasterAnswers = document.getElementById("gamemaster-answers");
export const gamemasterArea = document.getElementById("gamemaster-area");

export const revealMore = document.getElementById("reveal-more");
export const revealLess = document.getElementById("reveal-less");

revealMore.addEventListener('click', () => reveal(true));
revealLess.addEventListener('click', () => reveal(false));


export function registerGamemaster() {
    registerSubscription((client) => {
        client.subscribe("/topic/gamemaster-update", gamemasterUpdate)
        client.subscribe("/user/topic/gamemaster-update", gamemasterUpdate)

        client.subscribe("/user/topic/gamemaster", (_) => {
            isGameMaster = true;

            hideJoinButtons();
            playerArea.style.display = 'none';
            gamemasterArea.style.display = null;
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

function reveal(more) {
    stompClient.send("/app/reveal-question", {}, more);
}
