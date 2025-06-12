let stompClient;

let isGameMaster;

const connect = () => {
    // ngrok http --host-header=localhost 8080
    // <ngrok url>/ws
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        // TODO: Add needed subscriptions to updates when implemented

        // subscribes to user specific messages about lobby join information. Displays a message when joined successfully.
        stompClient.subscribe("/user/topic/join", (msg) => {
            const join = document.getElementById("join");

            Array.from(join.children).forEach((v) => v.hidden = true);

            showAlert('success', `${JSON.parse(msg.body) ? 'Herzlich Willkommen' : 'Willkommen zurück'}! Jeden Moment sollte das Spiel starten!`);
        });

        // subscribes to lobby update messages and rebuilds the lobby table according to the given informations about the lobby.
        stompClient.subscribe("/topic/lobby-update", (msg) => {
            const q = JSON.parse(msg.body);

            const lobby = document.getElementById("lobby");

            const rowCount = lobby.rows.length;
            for (var i = 0; i < rowCount; i++) {
                lobby.deleteRow(0);
            }

            for (var i = 0; i < q.length; i++) {
                var row = lobby.insertRow(-1);

                row.id = `player:${q[i].name}`

                var place = document.createElement("th");
                place.scope = "row"
                place.classList.add("text-end")
                place.innerText = i + 1;

                row.appendChild(place);

                var name = row.insertCell(1);
                name.innerText = q[i].name;

                var score = row.insertCell(2);
                score.innerText = q[i].score;
                score.classList.add("text-end");
            }

            // TODO: Send more information for a detailed update analysis and more detailed notification to the players.
            showAlert('info', `Die Lobby wurde geupdated!`);
        });

        const boardUpdate = (msg) => {
            const update = JSON.parse(msg.body);

            highlightPlayer(update.player?.name);

            const board = document.getElementById("board");

            const createQuestionRow = (qIdx) => {
                const row = document.createElement("div");
                row.classList.add("row");

                for (var cIdx = 0; cIdx < update.board.length; cIdx++) {
                    const category = update.board[cIdx];
                    const question = category.questions[qIdx];
                    const wrapper = document.createElement("div");
                    wrapper.classList.add("col", "text-center", "d-grid", "my-1");

                    isChosen = (update.active && update.active.question == qIdx && update.active.category == cIdx);

                    console.log(JSON.stringify(update.active))

                    const button = document.createElement("button");
                    var buttonStyle = "btn-outline-primary";
                    if (question.answered) {
                        buttonStyle = "btn-success";
                    } else if (isChosen) {
                        buttonStyle = "btn-primary";
                    } else if (update.active) {
                        buttonStyle = "btn-outline-secondary"
                    }
                    button.disabled = question.answered || (update.active && !isChosen);
                    button.classList.add("btn", buttonStyle, "align-self-md-center");

                    button.innerText = question.points;

                    if (!update.active) {
                        button.addEventListener('click', callbackClosure(cIdx, (x) =>
                            stompClient.send(
                                "/app/question", {},
                                JSON.stringify(
                                    {
                                        question: qIdx,
                                        category: x,
                                    },
                                ),
                            ),
                        ));
                    }

                    wrapper.appendChild(button);
                    row.appendChild(wrapper);
                }

                return row;
            }

            const row = document.createElement("div");
            row.classList.add("row");

            var rows = [row];

            for (var category of update.board) {
                const wrapper = document.createElement("div");
                wrapper.classList.add("col", "text-center");

                const header = document.createElement("h5");
                header.innerText = category.name;

                wrapper.appendChild(header);
                row.appendChild(wrapper);
            }

            // TODO: Nicht optimal gelöst
            for (var i = 0; i < update.board[0].questions.length; i++) {
                rows.push(createQuestionRow(i));
            }


            board.replaceChildren(...rows);
        };

        stompClient.subscribe("/user/topic/board-update", boardUpdate);
        stompClient.subscribe("/topic/board-update", boardUpdate);



        stompClient.subscribe("/topic/question-update", (msg) => {
            hideQuestion();
            var update = JSON.parse(msg.body);
            if (!update.question) {
                return;
            }
            var header = document.getElementById("question-header");
            var question = document.getElementById("question");
            var question_data = document.getElementById("question-data");

            toDisplay = [
                header, question
            ];

            header.innerText = `${update.category.name} - ${update.question.points} Punkte`
            question.innerText = update.question.question;
            switch (update.question.type) {
                case 'NORMAL':
                    updateBuzzerState(true);
                    toDisplay.push(document.getElementById("buzzer"));
                    break;
                case 'TEXT':
                case 'ESTIMATE':
                    var text = document.getElementById("answer-textfield");
                    text.value = '';
                    toDisplay.push(text);
            }


            toDisplay.forEach((v) => v.style.display = null)
        })


        stompClient.subscribe("/topic/buzzer-update", (msg) => {
            var state = JSON.parse(msg.body);

            updateBuzzerState(state);
        });

        const gamemasterUpdate = (msg) => {
            var masterExists = JSON.parse(msg.body);

            document.getElementById("gamemaster").hidden = masterExists;
        }

        stompClient.subscribe("/topic/gamemaster-update", gamemasterUpdate)
        stompClient.subscribe("/user/topic/gamemaster-update", gamemasterUpdate)

        stompClient.subscribe("/user/topic/answer", (msg) => {
            var answer = JSON.parse(msg.body);

            var answers = document.getElementById("answers");

            var row = answers.insertRow(-1);

            var nameCell = row.insertCell(0);
            nameCell.innerText = answer.player.name;

            var answerCell = row.insertCell(1);
            answerCell.innerText = answer.answer;

            var judgeCell = row.insertCell(2);
            row.classList.add("text-center");

            var correctButton = document.createElement("button");
            var wrongButton = document.createElement("button")

            var eventListenerFactory = (correct) => function () {
                stompClient.send("/app/answer", {}, JSON.stringify({
                    playerName: answer.player.name,
                    isCorrect: correct,
                }))

                wrongButton.disabled = true;
                correctButton.disabled = true;
            }

            correctButton.classList.add("btn", "btn-success", "mx-1");
            correctButton.style.fontFamily = "'Segoe UI Symbol', 'Arial', sans-serif";
            correctButton.innerText = "✔"
            correctButton.addEventListener('click', eventListenerFactory(true))

            wrongButton.classList.add("btn", "btn-danger", "mx-1");
            wrongButton.style.fontFamily = "'Segoe UI Symbol', 'Arial', sans-serif";
            wrongButton.innerText = "✖"
            wrongButton.addEventListener('click', eventListenerFactory(false))

            judgeCell.appendChild(correctButton);
            judgeCell.appendChild(wrongButton);
        })

        stompClient.subscribe("/user/topic/gamemaster", (_) => {
            isGameMaster = true;

            document.getElementById("player-area").hidden = true;
            document.getElementById("gamemaster-area").hidden = false;
        });


        stompClient.send('/app/on-connect');
    });
}

function highlightPlayer(name) {
    var lobby = document.getElementById("lobby");

    Array.from(lobby.getElementsByTagName("tr")).forEach((v) => name && v.id === `player:${name}` ? v.className = "table-dark" : v.className = "");
}

function hideQuestion() {
    var q = document.getElementById("question-wrapper");
    var qat = document.getElementById("question-answer-tool-wrapper");

    Array.from(q.children).forEach((v) => v.style.display = 'none');
    Array.from(qat.children).forEach((v) => v.style.display = 'none');

    var answers = document.getElementById("answers");
    answers.replaceChildren();
}

function skipQuestion() {
    stompClient.send("/app/skip-question", {});
}

function startGame() {
    stompClient.send("/app/start-game", {});
}

function updateBuzzerState(state) {
    var buzzer = document.getElementById("buzzer");
    const newBuzzer = buzzer.cloneNode(true);
    if (state) {
        newBuzzer.addEventListener('click', submitAnswerFactory("buzzered"));
        newBuzzer.src = "./img/enabutton.png"
    } else {
        newBuzzer.src = "./img/disbutton.png"
    }

    buzzer.parentNode.replaceChild(newBuzzer, buzzer);
}

function submitAnswerFactory(answer) {
    return function () { stompClient.send("/app/submit-answer", {}, answer) };
}

function callbackClosure(i, callback) {
    return function () {
        return callback(i);
    }
}


// joinGame prompts the user to enter a name for themselves and sends given name to the backend
function joinGame() {
    const name = prompt("Bitte gib deinen Namen ein:");
    stompClient.send("/app/join", {}, name);
}

function becomeGameMaster() {
    stompClient.send("/app/gamemaster", {});
}


// showAlert is a simple utility function, that displays an information / notification for the player.
// type and message are required to identify the type of message ('danger', 'success', 'warning', etc.) and the content of the message.
// duration is the display duration of the notification and if omitted will default to 5 seconds.
function showAlert(type, message, duration) {
    const alertPlaceholder = document.getElementById('alert')
    const wrapper = document.createElement('div')
    wrapper.innerHTML = [
        `<div class="alert alert-${type} alert-dismissible" role="alert">`,
        `   ${message}`,
        '</div>'
    ].join('')

    alertPlaceholder.append(wrapper)

    setTimeout(() => wrapper.remove(), duration || 5000);
}

connect();
hideQuestion();
