import { callbackClosure, showAlert } from "./main.js";
import { playerArea } from "./player.js";
import { makeIcon } from "./question.js";
import { registerSubscription, stompClient } from "./websocket.js";


const joinButtons = document.getElementById("join");
export const lobby = document.getElementById("lobby");

export var myID;


function onLobbyUpdate(msg) {
    const update = JSON.parse(msg.body);

    const statusClasses = ['connected', 'disconnected', 'active'];

    for (var i = 0; i < update.players.length; i++) {
        const player = update.players[i];

        var rowID = `lobby.row:${player.name}`;
        var statusID = `lobby.status:${player.name}`;
        var scoreID = `lobby.score:${player.name}`;

        var row = document.getElementById(rowID);
        var score = document.getElementById(scoreID);
        var status = document.getElementById(statusID);
        if (!row) {
            row = lobby.insertRow(-1);
            row.id = rowID;
            row.classList.add("player-row");

            status = row.insertCell(-1);
            status.id = statusID;
            status.classList.add("player-status");

            var name = row.insertCell(-1);
            name.innerText = player.name;
            name.classList.add("name");

            score = row.insertCell(-1);
            score.id = scoreID;
            score.innerText = player.score;
            score.classList.add("text-end", "score");
        }

        if (parseInt(score.innerText, 10) != player.score) {
            animateCount(score, parseInt(score.innerText, 10), player.score);
        }

        statusClasses.forEach(cls => status.classList.remove(cls));
        row.classList.remove("active");

        if (!player.connected) {
            status.classList.add("disconnected");
            var icon = makeIcon("x-circle-fill");
            status.replaceChildren(icon);
        } else if (player.active) {
            row.classList.add("active");
            status.classList.add("active");
            icon = makeIcon("lightning-fill")
            status.replaceChildren(icon);
        } else {
            status.replaceChildren();
        }
    }

    // TODO: Send more information for a detailed update analysis and more detailed notification to the players.
    showAlert('info', `Die Lobby wurde geupdated!`);
}

function animateCount(element, start, end, callback) {
    const tr = element.parentElement;

    function swapRowsIfNeeded() {
        const tbody = tr.parentElement;
        const prev = tr.previousElementSibling;
        const next = tr.nextElementSibling;
        const currentScore = parseInt(element.innerText, 10);

        // Check vorheriges <tr>
        if (prev) {
            const prevScore = parseInt(prev.querySelector('td.score').innerText);
            if (currentScore > prevScore) {
                tbody.insertBefore(tr, prev);
                swapRowsIfNeeded();
            }
        }
        // Check nächstes <tr>
        if (next) {
            const nextScore = parseInt(next.querySelector('td.score').innerText);
            if (currentScore < nextScore) {
                tbody.insertBefore(next, tr);
                swapRowsIfNeeded();
            }
        }
    }

    if (document.visibilityState != 'visible') {
        element.innerText = end;
        swapRowsIfNeeded();
        return;
    }

    const pointsPerSecond = 333;
    const diff = Math.abs(end - start);
    const duration = (diff / pointsPerSecond) * 1000;

    const range = end - start; // Positiv für hochzählen, negativ für runterzählen

    element.classList.add(range < 0 ? "decrement" : "increment");

    element.style.setProperty('--animation-duration', `${duration / 1000}s`);

    const startTime = performance.now();

    function step(currentTime) {
        const elapsed = currentTime - startTime;
        swapRowsIfNeeded();

        if (elapsed >= duration) {
            element.classList.remove(range < 0 ? "decrement" : "increment");
            element.style.removeProperty('--animation-duration');
            element.innerText = end;
            if (callback) callback();
            return;
        }
        const progress = elapsed / duration;
        // Berechne aktuellen Wert basierend auf Progress und Richtung
        const value = Math.round(start + range * progress);
        element.innerText = value;

        requestAnimationFrame(step);
    }

    requestAnimationFrame(step);
}

export function registerLobby() {
    registerSubscription((client) => {
        client.subscribe("/topic/lobby-update", onLobbyUpdate);
        client.subscribe("/user/topic/lobby-update", onLobbyUpdate);

        client.subscribe("/user/topic/join", (msg) => {
            myID = JSON.parse(msg.body);

            hideJoinButtons();
            playerArea.style.display = null;

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