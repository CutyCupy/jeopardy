import { showAlert } from "./main.js";
import { registerSubscription, stompClient } from "./websocket.js";


const joinButtons = document.getElementById("join");
export const lobby = document.getElementById("lobby");


function onLobbyUpdate(msg) {
    const q = JSON.parse(msg.body);

    const rowCount = lobby.rows.length;
    for (var i = 0; i < rowCount; i++) {
        lobby.deleteRow(0);
    }

    for (var i = 0; i < q.length; i++) {
        const player = q[i];

        var row = lobby.insertRow(-1);

        if (player.disconnected) {
            row.classList.add("table-danger")
        }

        row.id = `player:${player.name}`

        var place = document.createElement("th");
        place.scope = "row"
        place.classList.add("text-end")
        place.innerText = i + 1;

        row.appendChild(place);

        var name = row.insertCell(1);
        name.innerText = player.name;

        var score = row.insertCell(2);
        score.innerText = player.score;
        score.classList.add("text-end");
    }

    // TODO: Send more information for a detailed update analysis and more detailed notification to the players.
    showAlert('info', `Die Lobby wurde geupdated!`);
}

export function registerLobby() {
    registerSubscription((client) => {
        client.subscribe("/topic/lobby-update", onLobbyUpdate);
        client.subscribe("/user/topic/lobby-update", onLobbyUpdate);

        client.subscribe("/user/topic/join", (msg) => {
            hideJoinButtons();

            showAlert('success', `${JSON.parse(msg.body) ? 'Herzlich Willkommen' : 'Willkommen zurück'}! Jeden Moment sollte das Spiel starten!`);
        });
    })
}

export function hideJoinButtons() {
    Array.from(joinButtons.children).forEach((v) => v.style.display = 'none');
}

// joinGame prompts the user to enter a name for themselves and sends given name to the backend
export function joinGame() {
    const name = prompt("Bitte gib deinen Namen ein:");
    stompClient.send("/app/join", {}, name);
}