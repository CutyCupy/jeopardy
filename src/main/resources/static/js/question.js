import { gamemasterAnswers, isGameMaster, revealLess, revealMore } from "./gamemaster.js";
import { deriveButtonColors } from "./main.js";
import { playerAnswers } from "./player.js";
import { registerSubscription, stompClient } from "./websocket.js";

var states = ['HIDDEN', 'SHOW_CATEGORY', 'SHOW_QUESTION', 'SHOW_QUESTION_DATA', 'LOCK_QUESTION', 'REVEAL_ANSWERS', 'SHOW_ANSWER']

const question_title = document.getElementById("question-title");
const question_subtitle = document.getElementById("question-subtitle");
const question = document.getElementById("question");
const question_data = document.getElementById("question-data");

var buzzer = document.getElementById("buzzer");

const answerText = document.getElementById("answer-textfield");
const answerNumber = document.getElementById("answer-numberfield");
const answerSort = document.getElementById("answer-sort");
const answer = document.getElementById("answer");

const questionWrapper = document.getElementById("question-wrapper");
const questionAnswerToolWrapper = document.getElementById("question-answer-tool-wrapper");
var submitButton = document.getElementById("submit-button");

const buzzerAudio = document.getElementById("buzzer-audio");

const countdownWrapper = document.getElementById("countdown");
const countdownText = document.getElementById("countdown-text");
const countdownCircle = document.getElementById("countdown-circle");
const countdownBeepAudio = document.getElementById("countdown-beep-audio");
const countdownEndAudio = document.getElementById("countdown-end-audio");

var countdown;
var countdownInterval;

const COUNTDOWN_LENGTH = 0;




export function registerQuestion() {
    registerSubscription((client) => {
        function onQuestionUpdate(msg) {
            var update = JSON.parse(msg.body);
            if (!update.question) {
                resetQuestion();
                board.style.display = null;
                return;
            }
            hideQuestion();

            var toDisplay = [];


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
                    // countdownBeepAudio.play();
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
                        client.send("/app/answer", {}, JSON.stringify({
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
                        client.send("/app/reveal-answer", {}, answer.player);
                    })

                    judgeCell.appendChild(correctButton);
                    judgeCell.appendChild(wrongButton);
                    judgeCell.appendChild(revealButton);
                }
            }
        }




        client.subscribe("/topic/question-update", onQuestionUpdate);
        client.subscribe("/user/topic/question-update", onQuestionUpdate);

        client.subscribe("/topic/answer", onAnswerUpdate)
        client.subscribe("/user/topic/answer", onAnswerUpdate)

        client.subscribe("/topic/buzzer-update", (msg) => {
            var state = JSON.parse(msg.body);

            updateBuzzerState(state);
        });

        client.subscribe("/user/topic/buzzer-update", (msg) => {
            var state = JSON.parse(msg.body);

            updateBuzzerState(state);
        });



        client.subscribe("/topic/on-buzzer", (_) => {
            buzzerAudio.play();
        })

        resetQuestion();


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

function makeVideoHTML(src) {
    return src ? `<div style="position:relative; width:100%; height:0px; padding-bottom:56.250%">
                <iframe allow="fullscreen;autoplay" allowfullscreen height="100%" 
                src="https://streamable.com/e/${src}?autoplay=1" width="100%" 
                style="border:none; width:100%; height:100%; position:absolute; left:0px; top:0px; overflow:hidden;">
                </iframe>
            </div>` : null;
}

function makeIcon(name) {
    return `<i class="bi bi-${name}"></i>`
}




