let stompClient;
let isGameMaster;

const gamemasterButton = document.getElementById("gamemaster");
const joinButtons = document.getElementById("join")
const lobby = document.getElementById("lobby");
const board = document.getElementById("board");

const question_title = document.getElementById("question-title");
const question_subtitle = document.getElementById("question-subtitle");
const question = document.getElementById("question");
const question_data = document.getElementById("question-data");

var buzzer = document.getElementById("buzzer");
const answerText = document.getElementById("answer-textfield");
const answerNumber = document.getElementById("answer-numberfield");
const answerSort = document.getElementById("answer-sort");
const answer = document.getElementById("answer");
const playerAnswers = document.getElementById("player-answers");
const gamemasterAnswers = document.getElementById("gamemaster-answers");

const playerArea = document.getElementById("player-area")
const gamemasterArea = document.getElementById("gamemaster-area")

const questionWrapper = document.getElementById("question-wrapper");
const questionAnswerToolWrapper = document.getElementById("question-answer-tool-wrapper");
var submitButton = document.getElementById("submit-button");

const alertPlaceholder = document.getElementById('alert');

const buzzerAudio = document.getElementById("buzzer-audio");
const countdownBeepAudio = document.getElementById("countdown-beep-audio");
const countdownEndAudio = document.getElementById("countdown-end-audio");

const revealMore = document.getElementById("reveal-more");
const revealLess = document.getElementById("reveal-less");

const countdownWrapper = document.getElementById("countdown");
const countdownText = document.getElementById("countdown-text");
const countdownCircle = document.getElementById("countdown-circle");

const COUNTDOWN_LENGTH = 0;

var countdown;
var countdownInterval;

const connect = () => {
    // ngrok http --host-header=localhost 8080
    // <ngrok url>/ws
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        // TODO: Add needed subscriptions to updates when implemented
        resetQuestion();
        // subscribes to user specific messages about lobby join information. Displays a message when joined successfully.
        stompClient.subscribe("/user/topic/join", (msg) => {
            hideJoinButtons();

            showAlert('success', `${JSON.parse(msg.body) ? 'Herzlich Willkommen' : 'Willkommen zurück'}! Jeden Moment sollte das Spiel starten!`);
        });

        // subscribes to lobby update messages and rebuilds the lobby table according to the given informations about the lobby.
        stompClient.subscribe("/topic/lobby-update", onLobbyUpdate);
        stompClient.subscribe("/user/topic/lobby-update", onLobbyUpdate);

        stompClient.subscribe("/user/topic/active-player-update", (msg) => {
            const isActive = JSON.parse(msg.body);

            if (isActive) {
                board.classList.add("active");
            } else {
                board.classList.remove("active");
            }
        })

        const boardUpdate = (msg) => {
            const update = JSON.parse(msg.body);

            var cols = [];
            for (var category of update.categories) {
                const column = document.createElement("div");
                column.classList.add("col", "board-column");

                const label = document.createElement("div");
                label.classList.add("category");
                label.innerText = category.title;

                column.appendChild(label);


                label.style.setProperty('--background', category.color);
                label.style.setProperty('--color', getTextColorForBackground(hexToHSL(category.color).l));

                if (update.selected) {
                    label.style.filter = 'brightness(0.6)';
                }
                const { buttonNormal, buttonHover, textColorNormal, textColorHover } = deriveButtonColors(category.color);

                for (var question of category.questions) {
                    const button = document.createElement("button");

                    button.style.setProperty('--background', buttonNormal);
                    button.style.setProperty('--color', textColorNormal);
                    button.style.setProperty('--hover-background', buttonHover);
                    button.style.setProperty('--hover-color', textColorHover);

                    button.classList.add("col", "question-btn");

                    const selected = _.isEqual(update.selected, question.id);

                    button.disabled = question.answered || (update.selected && !selected);
                    if (question.answered) {
                        if (question.correct === null) {
                            button.classList.add("unanswered");
                        } else if (question.correct === false) {
                            button.classList.add("incorrect");
                        } else if (question.correct === true) {
                            button.classList.add("correct")
                        }
                    }

                    if (selected) {
                        button.classList.add("selected");

                        label.style.filter = 'brightness(1.2)';
                        label.style.transition = 'all 0.3s ease';
                    }

                    button.innerText = question.points;

                    if (!update.selected) {
                        button.addEventListener('click', callbackClosure(question.id, (id) => {
                            stompClient.send(
                                "/app/question", {},
                                JSON.stringify(id),
                            );
                        }),
                        );
                    }

                    column.appendChild(button);
                }

                cols.push(column)
            }

            board.replaceChildren(...cols);
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

        stompClient.subscribe("/topic/answer", onAnswerUpdate)
        stompClient.subscribe("/user/topic/answer", onAnswerUpdate)

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

function onAnswerUpdate(msg) {
    var answers = JSON.parse(msg.body);

    var myAnswers = isGameMaster ? gamemasterAnswers : playerAnswers;
    if (!answers.length) {
        myAnswers.replaceChildren();
        return;
    }

    for (var answer of answers) {
        const rowID = `row:${answer.player}`;
        const nameID = `name:${answer.player}`;
        const answerID = `answer:${answer.player}`;
        const judgeID = `judge:${answer.player}`;

        var row = document.getElementById(rowID) || myAnswers.insertRow(-1);
        row.classList.remove("table-success");
        row.classList.remove("table-danger");

        row.id = rowID;
        if (answer.correct === false) {
            row.classList.add("table-danger");
        } else if (answer.correct === true) {
            row.classList.add("table-success");
        }

        var nameCell = document.getElementById(nameID) || row.insertCell(-1);
        nameCell.id = nameID
        nameCell.innerText = answer.player;

        if (answers.find((v) => !!v.answer)) {
            var answerCell = document.getElementById(answerID) || row.insertCell(1);
            answerCell.id = answerID;
            answerCell.innerText = answer.answer;
        }

        if (!isGameMaster) {
            continue;
        }

        if (!document.getElementById(judgeID)) {
            var judgeCell = row.insertCell(-1);
            judgeCell.id = judgeID;
            row.classList.add("text-center");

            var correctButton = document.createElement("button");
            var wrongButton = document.createElement("button");
            var revealButton = document.createElement("button");

            var eventListenerFactory = (correct) => function () {
                stompClient.send("/app/answer", {}, JSON.stringify({
                    playerName: answer.player,
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

            revealButton.classList.add("btn", "btn-warning", "mx-1");
            revealButton.innerHTML = makeIcon("search")
            revealButton.addEventListener('click', function () {
                stompClient.send("/app/reveal-answer", {}, answer.player);
            })

            judgeCell.appendChild(correctButton);
            judgeCell.appendChild(wrongButton);
            judgeCell.appendChild(revealButton);
        }
    }
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
        board.style.display = null;
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
    const isLocked = states.indexOf('LOCK_QUESTION') <= idx;
    const showAnswer = states.indexOf('SHOW_ANSWER') <= idx;

    const { buttonNormal } = deriveButtonColors(update.category.colorCode);


    question_title.innerText = `${update.category.name} - ${update.question.points} Punkte`;
    question_title.style.setProperty("--color", update.category.colorCode)
    question_subtitle.innerText = `${update.question.type.title} - ${update.question.type.penalty ? '' : 'keine '}Minuspunkte`
    question_subtitle.style.setProperty("--color", buttonNormal)
    question.innerText = update.question.question;

    if (showCategory) {
        board.style.display = 'none';

        toDisplay.push(question_title);
        toDisplay.push(question_subtitle);
        if (update.question.type.name === 'NORMAL') {
            toDisplay.push(buzzer);
        }
    }

    if (showQuestion) {
        toDisplay.push(question);

        switch (update.question.type.name) {
            case 'NORMAL':
                toDisplay.push(buzzer);
                break;
            case 'TEXT':
                updateSubmitButton(() => {
                    submitAnswer(answerText.value);
                });

                toDisplay.push(answerText);
                toDisplay.push(submitButton);
                break;
            case 'ESTIMATE':
                updateSubmitButton(() => {
                    submitAnswer(answerNumber.value);
                });


                toDisplay.push(answerNumber);
                toDisplay.push(submitButton);
                break;
            case 'VIDEO':
                toDisplay.push(answerText);
                if (showQuestionData) {
                    if (!isGameMaster) {
                        if (!question_data.innerHTML) {
                            question_data.innerHTML = makeVideoHTML(update.question.path);
                        }
                    }
                    toDisplay.push(question_data);
                }
                updateSubmitButton(() => {
                    submitAnswer(answerText.value);
                });

                toDisplay.push(submitButton);
                break;
            case 'SORT':
                if (!showQuestionData) {
                    answerSort.replaceChildren();

                    for (var entry of update.question.options) {
                        const row = document.createElement("div");
                        row.classList.add("bg-white", "border", "border-4", "border-secondary", "p-2", "m-1", "text-center");

                        row.style.color = 'black';

                        row.id = `sort:${entry}`
                        row.innerText = entry;

                        row.addEventListener('drop', dropHandler);
                        row.addEventListener('dragover', dragoverHandler);
                        row.addEventListener('dragstart', dragstartHandler);

                        row.draggable = true;

                        answerSort.appendChild(row);
                    }
                }

                updateSubmitButton(() => {
                    submitAnswer(Array.from(answerSort.children).map((v) => {
                        return { name: v.innerText, value: 0 }
                    }));
                });

                toDisplay.push(answerSort);
                toDisplay.push(submitButton);
                break;
        }
    }


    if (isLocked && !showAnswer) {
        countdown = COUNTDOWN_LENGTH;
        const diameter = 2 * Math.PI * 90; // TODO: Maybe hardcode the radius?

        var cb = function () {
            countdownWrapper.style.display = null;

            countdownText.innerHTML = `${countdown}s`;
            countdownCircle.style.strokeDashoffset = (1 - (countdown / COUNTDOWN_LENGTH)) * diameter;
            if (countdown < 0) {
                // countdownEndAudio.play();
                countdownWrapper.style.display = 'none';
                clearInterval(countdownInterval);

                switch (update.question.type.name) {
                    case 'NORMAL':
                        updateBuzzerState(false);
                        break;
                    case 'VIDEO':
                        question_data.innerHTML = '';
                        question_data.replaceChildren();
                    default:
                        submitButton.click();
                        break;
                }


                toDisplay.forEach((v) => {
                    v.readOnly = true;
                    v.style.pointerEvents = 'none';
                });

                return;
            }
            countdown--;
            countdownBeepAudio.play();
        }
        cb();
        countdownInterval = setInterval(cb, 1000);
    } else if (!showAnswer) {
        toDisplay.forEach((v) => {
            v.readOnly = false;
            v.style.pointerEvents = 'auto';
        });
    }


    if (showAnswer || isGameMaster) {
        switch (update.question.type.name) {
            case 'NORMAL':
            case 'TEXT':
            case 'ESTIMATE':
                const span = document.createElement("span");
                span.innerText = update.question.answer;

                answer.replaceChildren(span);
                break;
            case 'VIDEO':
                question_data.innerHTML = '';
                if (!answer.innerHTML) {
                    answer.innerHTML = makeVideoHTML(update.question.answer);
                }
                break;
            case 'SORT':
                answer.innerText = update.question.answer.map((v) => `${v.name} (${v.value})`).join(", ");
                break;
        }
        toDisplay.push(answer);
    }

    toDisplay.forEach((v) => {
        v.style.display = null;
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
    Array.from(questionAnswerToolWrapper.children).forEach((v) => {
        v.style.display = 'none';
    });

}
function resetQuestion() {
    Array.from(questionWrapper.children).forEach((v) => {
        v.replaceChildren();
        v.innerText = '';
    });
    Array.from(questionAnswerToolWrapper.children).forEach((v) => {
        if (v.id == submitButton.id) {
            return;
        }
        v.value = '';
        v.replaceChildren();
    });

    updateBuzzerState(true);
    answerText.value = '';
    answerNumber.value = '';

    gamemasterAnswers.replaceChildren();
    playerAnswers.replaceChildren();
    question_data.replaceChildren();
    question_data.innerHTML = '';

    answer.replaceChildren();
    answer.innerHTML = '';

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
        newBuzzer.addEventListener('click', () => {
            submitAnswer("");
        });
        newBuzzer.src = "./img/enabutton.png"
    } else {
        newBuzzer.src = "./img/disbutton.png"
    }

    buzzer.parentNode.replaceChild(newBuzzer, buzzer);
    buzzer = newBuzzer;
}

function updateSubmitButton(listener) {
    const newSubmit = submitButton.cloneNode(true);
    newSubmit.addEventListener('click', listener);

    submitButton.parentNode.replaceChild(newSubmit, submitButton);
    submitButton = newSubmit;
}

function submitAnswer(answer) {
    stompClient.send("/app/submit-answer", {}, JSON.stringify({ answer }));
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


function dragstartHandler(ev) {
    ev.dataTransfer.setData("text", ev.target.id);
}

function dragoverHandler(ev) {
    ev.preventDefault();
}

function dropHandler(ev) {
    ev.preventDefault();
    const data = ev.dataTransfer.getData("text");
    swapElements(document.getElementById(data), ev.target)
}

function swapElements(el1, el2) {
    const parent1 = el1.parentNode;
    const parent2 = el2.parentNode;

    const next1 = el1.nextSibling;
    const next2 = el2.nextSibling;

    parent1.insertBefore(el2, next1);
    parent2.insertBefore(el1, next2);
}


function hexToHSL(H) {
    let r = 0, g = 0, b = 0;
    if (H.length === 7) {
        r = parseInt(H.substring(1, 3), 16) / 255;
        g = parseInt(H.substring(3, 5), 16) / 255;
        b = parseInt(H.substring(5, 7), 16) / 255;
    }
    let max = Math.max(r, g, b), min = Math.min(r, g, b);
    let h = 0, s = 0, l = (max + min) / 2;

    if (max !== min) {
        let d = max - min;
        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
        switch (max) {
            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
            case g: h = (b - r) / d + 2; break;
            case b: h = (r - g) / d + 4; break;
        }
        h /= 6;
    }

    h = Math.round(h * 360);
    s = Math.round(s * 100);
    l = Math.round(l * 100);

    return { h, s, l };
}

function hslToHex(h, s, l) {
    s /= 100;
    l /= 100;

    function hue2rgb(p, q, t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1 / 6) return p + (q - p) * 6 * t;
        if (t < 1 / 2) return q;
        if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
        return p;
    }

    let r, g, b;

    if (s === 0) {
        r = g = b = l;
    } else {
        let q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        let p = 2 * l - q;
        r = hue2rgb(p, q, h / 360 + 1 / 3);
        g = hue2rgb(p, q, h / 360);
        b = hue2rgb(p, q, h / 360 - 1 / 3);
    }

    let toHex = x => {
        let hex = Math.round(x * 255).toString(16);
        return hex.length === 1 ? "0" + hex : hex;
    };

    return "#" + toHex(r) + toHex(g) + toHex(b);
}

// --- Textfarbe basierend auf Helligkeit (L) ---

function getTextColorForBackground(l) {
    // Wenn Lichtstärke unter 50%, Text weiß, sonst dunkel
    return l < 50 ? '#fff' : '#2d2d2d';
}

// --- Farben ableiten ---

function deriveButtonColors(categoryHex) {
    const hsl = hexToHSL(categoryHex);

    // Dunklerer Ton für Button normal
    let normalL = Math.max(0, hsl.l + 15);
    // Hellerer Ton für Hover
    let hoverL = Math.min(100, hsl.l + 30);

    return {
        buttonNormal: hslToHex(hsl.h, hsl.s, normalL),
        buttonHover: hslToHex(hsl.h, hsl.s, hoverL),
        textColorNormal: getTextColorForBackground(normalL),
        textColorHover: getTextColorForBackground(hoverL)
    };
}