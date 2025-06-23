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
const answerSort = document.getElementById("answer-sort");
const answer = document.getElementById("answer");
const answers = document.getElementById("answers");

const playerArea = document.getElementById("player-area")
const gamemasterArea = document.getElementById("gamemaster-area")

const questionWrapper = document.getElementById("question-wrapper");
const questionAnswerToolWrapper = document.getElementById("question-answer-tool-wrapper");

const alertPlaceholder = document.getElementById('alert');

const buzzerAudio = document.getElementById("buzzer-audio");

const revealMore = document.getElementById("reveal-more");
const revealLess = document.getElementById("reveal-less");


answerText.addEventListener('input', debounce((args) => submitAnswerFactory(args[0].srcElement.value)(), 333))
answerNumber.addEventListener('input', debounce((args) => submitAnswerFactory(parseInt(args[0].srcElement.value || '0', 10))(), 333))

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
        stompClient.subscribe("/topic/lobby-update", onLobbyUpdate);
        stompClient.subscribe("/user/topic/lobby-update", onLobbyUpdate);

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



        stompClient.subscribe("/topic/question-update", onQuestionUpdate);
        stompClient.subscribe("/user/topic/question-update", onQuestionUpdate);


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
            correctButton.innerHTML = makeIcon("check-lg")
            correctButton.addEventListener('click', eventListenerFactory(true))

            wrongButton.classList.add("btn", "btn-danger", "mx-1");
            wrongButton.innerHTML = makeIcon("x-lg")
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

        stompClient.subscribe("/topic/on-buzzer", (_) => {
            buzzerAudio.play();
        })


        stompClient.send('/app/on-connect');
    });
}

function onSortAnswerChange() {
    submitAnswerFactory(Array.from(answerSort.rows).map((v) => v.entry))();
}

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

var states = ['HIDDEN', 'SHOW_CATEGORY', 'SHOW_QUESTION', 'SHOW_QUESTION_DATA', 'LOCK_QUESTION', 'REVEAL_ANSWERS', 'SHOW_ANSWER']

function onQuestionUpdate(msg) {
    var update = JSON.parse(msg.body);
    if (!update.question) {
        resetQuestion();
        return;
    }
    hideQuestion();

    toDisplay = [];


    var idx = states.indexOf(update.question.state);

    revealMore.innerText = states[idx + 1] || "Frage abschließen";
    revealLess.innerText = states[idx - 1] || "Frage zurücknehmen";


    const showCategory = states.indexOf('SHOW_CATEGORY') <= idx || isGameMaster;
    const showQuestion = states.indexOf('SHOW_QUESTION') <= idx || isGameMaster;
    const showQuestionData = states.indexOf('SHOW_QUESTION_DATA') <= idx || isGameMaster;
    const isLocked = states.indexOf('LOCK_QUESTION') <= idx || isGameMaster;
    const showAnswer = states.indexOf('SHOW_ANSWER') <= idx || isGameMaster;

    question_header.innerText = `${update.category.name} - ${update.question.points} Punkte`;
    question.innerText = update.question.question;

    if (showCategory) {
        toDisplay.push(question_header);
        if (update.question.type === 'NORMAL') {
            toDisplay.push(buzzer);
        }
    }

    if (showQuestion) {
        toDisplay.push(question);

        switch (update.question.type) {
            case 'NORMAL':
                toDisplay.push(buzzer);
                break;
            case 'TEXT':
                toDisplay.push(answerText);
                break;
            case 'ESTIMATE':
                toDisplay.push(answerNumber);
                break;
            case 'VIDEO':
                toDisplay.push(answerText);
                if (!isGameMaster) {
                    question_data.innerHTML = makeVideoHTML(update.question.path);
                }
                if (showQuestionData) {
                    toDisplay.push(question_data);
                }
                break;
            case 'SORT':
                if (!showQuestionData) {
                    const rowCount = answerSort.rows.length;
                    for (var i = 0; i < rowCount; i++) {
                        answerSort.deleteRow(0);
                    }

                    for (var entry of update.question.options) {
                        var row = answerSort.insertRow(-1);
                        row.id = `sort:${entry}`
                        row.entry = entry;

                        var entryCell = row.insertCell(0);
                        entryCell.innerText = entry;

                        moveCell = row.insertCell(1);
                        var upButton = document.createElement("button");
                        var downButton = document.createElement("button")

                        var eventListenerFactory = (id, up) => function () {
                            var rows = Array.from(answerSort.rows);
                            var idx = rows.findIndex((v) => v.id == id);
                            var other = idx;

                            if (up) {
                                idx--;
                            } else {
                                other++;
                            }

                            if (idx < 0 || other >= rows.length) {
                                return;
                            }

                            var rowA = answerSort.rows[idx];
                            var rowB = answerSort.rows[other]

                            answerSort.tBodies[0].insertBefore(rowB, rowA)
                            answerSort.tBodies[0].insertBefore(rowA, rowB.nextSibling);

                            onSortAnswerChange();
                        }

                        upButton.classList.add("btn", "btn-success", "mx-1");
                        upButton.innerHTML = makeIcon("caret-up-fill")
                        upButton.addEventListener('click', eventListenerFactory(row.id, true))

                        downButton.classList.add("btn", "btn-danger", "mx-1");
                        downButton.innerHTML = makeIcon("caret-down-fill")
                        downButton.addEventListener('click', eventListenerFactory(row.id, false))


                        moveCell.appendChild(upButton);
                        moveCell.appendChild(downButton);
                    }
                }
                toDisplay.push(answerSort);
                break;
        }
    }


    if (isLocked && !isGameMaster) {
        switch (update.question.type) {
            case 'NORMAL':
                updateBuzzerState(false);
                break;
            case 'TEXT':
                answerText.dispatchEvent(new Event("input"));
                break;
            case 'ESTIMATE':
                answerNumber.dispatchEvent(new Event("input"));
                break;
            case 'VIDEO':
                answerText.dispatchEvent(new Event("input"));
                break;
            case 'SORT':
                onSortAnswerChange();
                break;
        }
        updateBuzzerState(false);
    }

    if (showAnswer) {
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
            case 'SORT':
                answer.innerText = update.question.answer.join(", ");
                break;
        }
        toDisplay.push(answer);
    }

    toDisplay.forEach((v) => {
        v.style.display = null;
        v.readOnly = isLocked;
        v.style.pointerEvents = isLocked ? 'none' : 'auto';
    });

}

function reveal(more) {
    stompClient.send("/app/reveal-question", {}, more);
}

function makeIcon(name) {
    return `<i class="bi bi-${name}"></i>`
}

function makeVideoHTML(src) {
    return src ? `<div style="position:relative; width:100%; height:0px; padding-bottom:56.250%">
                <iframe allow="fullscreen;autoplay" allowfullscreen height="100%" 
                src="https://streamable.com/e/${src}?autoplay=1" width="100%" 
                style="border:none; width:100%; height:100%; position:absolute; left:0px; top:0px; overflow:hidden;">
                </iframe>
            </div>` : null;
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
        v.style.display = 'none';
    });
    Array.from(questionAnswerToolWrapper.children).forEach((v) => v.style.display = 'none');

}
function resetQuestion() {
    Array.from(questionWrapper.children).forEach((v) => {
        v.replaceChildren();
        v.innerText = '';
    });
    Array.from(questionAnswerToolWrapper.children).forEach((v) => {
        v.value = '';
        v.replaceChildren();
    });

    updateBuzzerState(true);
    answerText.value = '';
    answerNumber.value = '';

    answers.replaceChildren();

    hideQuestion();
}

function hideJoinButtons() {
    Array.from(joinButtons.children).forEach((v) => v.style.display = 'none');
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
