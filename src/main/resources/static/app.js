let stompClient;
let isGameMaster;

const gamemasterButton = document.getElementById("gamemaster");
const joinButtons = document.getElementById("join")
const lobby = document.getElementById("lobby");
const board = document.getElementById("board");

const question_header = document.getElementById("question-header");
const question = document.getElementById("question");
const question_data = document.getElementById("question-data");

var buzzer = document.getElementById("buzzer");
const answerText = document.getElementById("answer-textfield");
const answerNumber = document.getElementById("answer-numberfield");
const answer = document.getElementById("answer");
const answers = document.getElementById("answers");

const playerArea = document.getElementById("player-area")
const gamemasterArea = document.getElementById("gamemaster-area")

const questionWrapper = document.getElementById("question-wrapper");
const questionAnswerToolWrapper = document.getElementById("question-answer-tool-wrapper");

const alertPlaceholder = document.getElementById('alert');


answerText.addEventListener('input', debounce((args) => submitAnswerFactory(args[0].srcElement.value)(), 333))
answerNumber.addEventListener('input', debounce((args) => submitAnswerFactory(args[0].srcElement.value)(), 333))

const connect = () => {
    // ngrok http --host-header=localhost 8080
    // <ngrok url>/ws
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        // TODO: Add needed subscriptions to updates when implemented

        // subscribes to user specific messages about lobby join information. Displays a message when joined successfully.
        stompClient.subscribe("/user/topic/join", (msg) => {
            hideJoinButtons();

            showAlert('success', `${JSON.parse(msg.body) ? 'Herzlich Willkommen' : 'Willkommen zurück'}! Jeden Moment sollte das Spiel starten!`);
        });

        // subscribes to lobby update messages and rebuilds the lobby table according to the given informations about the lobby.
        stompClient.subscribe("/topic/lobby-update", (msg) => {
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
        });

        const boardUpdate = (msg) => {
            const update = JSON.parse(msg.body);
            console.log(update);

            highlightPlayer(update.player?.name);

            const selectedQuestion = update.selectedQuestion?.identifier;

            const createQuestionRow = (qIdx) => {
                const row = document.createElement("div");
                row.classList.add("row");

                for (var cIdx = 0; cIdx < update.board.length; cIdx++) {
                    const category = update.board[cIdx];
                    const question = category.questions[qIdx];
                    const wrapper = document.createElement("div");
                    wrapper.classList.add("col", "text-center", "d-grid", "my-1");

                    isChosen = (selectedQuestion && selectedQuestion.question == qIdx && selectedQuestion.category == cIdx);

                    const button = document.createElement("button");
                    var buttonStyle = "btn-outline-primary";
                    if (question.answered) {
                        buttonStyle = "btn-success";
                    } else if (isChosen) {
                        buttonStyle = "btn-primary";
                    } else if (selectedQuestion) {
                        buttonStyle = "btn-outline-secondary"
                    }
                    button.disabled = question.answered || (selectedQuestion && !isChosen);
                    button.classList.add("btn", buttonStyle, "align-self-md-center");

                    button.innerText = question.points;

                    if (!selectedQuestion) {
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

            toDisplay = [
                question_header, question
            ];

            question_header.innerText = `${update.category.name} - ${update.question.points} Punkte`
            question.innerText = update.question.question;
            switch (update.question.type) {
                case 'NORMAL':
                    updateBuzzerState(true);
                    toDisplay.push(buzzer);
                    break;
                case 'TEXT':
                    answerText.value = '';
                    toDisplay.push(answerText);
                    break;
                case 'ESTIMATE':
                    answerNumber.value = '';
                    toDisplay.push(answerNumber);
                    break;
                case 'VIDEO':
                    answerText.value = '';
                    toDisplay.push(answerText);
                    if (!isGameMaster) {
                        question_data.innerHTML = makeVideoHTML(update.question.path);
                    }
                    toDisplay.push(question_data);
                    break;
            }

            toDisplay.forEach((v) => {
                v.style.display = null;
                v.readOnly = false;
            })
        });

        stompClient.subscribe("/user/topic/answer-update", answerUpdate)
        stompClient.subscribe("/topic/answer-update", answerUpdate)


        stompClient.subscribe("/topic/lock-question", (msg) => {
            updateBuzzerState(false);

            Array.from(questionAnswerToolWrapper.children).forEach((v) => v.readOnly = true);
        });

        stompClient.subscribe("/topic/buzzer-update", (msg) => {
            var state = JSON.parse(msg.body);

            updateBuzzerState(state);
        });

        stompClient.subscribe("/user/topic/buzzer-update", (msg) => {
            var state = JSON.parse(msg.body);

            updateBuzzerState(state);
        });

        const gamemasterUpdate = (msg) => {
            var masterExists = JSON.parse(msg.body);

            gamemasterButton.style.display = masterExists ? 'none' : null;
        }

        stompClient.subscribe("/topic/gamemaster-update", gamemasterUpdate)
        stompClient.subscribe("/user/topic/gamemaster-update", gamemasterUpdate)

        stompClient.subscribe("/user/topic/answer", (msg) => {
            var answer = JSON.parse(msg.body);

            const id = `answer:${answer.player.name}`;
            var answerCell = document.getElementById(id);
            if (answerCell) {
                answerCell.innerText = answer.answer;
                return;
            }
            var row = answers.insertRow(-1);

            var nameCell = row.insertCell(0);
            nameCell.innerText = answer.player.name;

            answerCell = row.insertCell(1);
            answerCell.id = id;
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

            hideJoinButtons();
            playerArea.style.display = 'none';
            gamemasterArea.style.display = null;
        });


        stompClient.send('/app/on-connect');
    });
}

function answerUpdate(msg) {
    var update = JSON.parse(msg.body);
    switch (update.question.type) {
        case 'NORMAL':
        case 'TEXT':
        case 'ESTIMATE':
            const span = document.createElement("span");
            span.innerText = update.question.answer;

            answer.replaceChildren(span);
            break;
        case 'VIDEO':
            question_data.innerHTML = '';
            answer.innerHTML = makeVideoHTML(update.question.answer);
            break;
    }

    answer.style.display = null;
}

function makeVideoHTML(src) {
    return `<div style="position:relative; width:100%; height:0px; padding-bottom:56.250%">
                <iframe allow="fullscreen;autoplay" allowfullscreen height="100%" 
                src="https://streamable.com/e/${src}?autoplay=1" width="100%" 
                style="border:none; width:100%; height:100%; position:absolute; left:0px; top:0px; overflow:hidden;">
                </iframe>
            </div>`


}

function debounce(fn, delay) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => fn(args), delay);
    };
}

function highlightPlayer(name) {
    Array.from(lobby.getElementsByTagName("tr")).forEach((v) => {
        if (name && v.id == `player:${name}`) {
            v.classList.add("table-primary");
        }
    });
}

function hideQuestion() {
    Array.from(questionWrapper.children).forEach((v) => {
        v.replaceChildren();
        v.innerText = '';
    });
    Array.from(questionAnswerToolWrapper.children).forEach((v) => v.style.display = 'none');

    answers.replaceChildren();
}

function hideJoinButtons() {
    Array.from(joinButtons.children).forEach((v) => v.style.display = 'none');
}

function lockQuestion() {
    stompClient.send("/app/lock-question", {});
}

function revealQuestion() {
    stompClient.send("/app/reveal-question", {});
}

function skipQuestion() {
    stompClient.send("/app/skip-question", {});
}

function startGame() {
    stompClient.send("/app/start-game", {});
}

function updateBuzzerState(state) {
    const newBuzzer = buzzer.cloneNode(true);
    if (state) {
        newBuzzer.addEventListener('click', submitAnswerFactory(""));
        newBuzzer.src = "./img/enabutton.png"
    } else {
        newBuzzer.src = "./img/disbutton.png"
    }

    buzzer.parentNode.replaceChild(newBuzzer, buzzer);
    buzzer = newBuzzer;
}

function submitAnswerFactory(answer) {
    return function () { stompClient.send("/app/submit-answer", {}, JSON.stringify({ answer })) };
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
