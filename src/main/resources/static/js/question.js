import { gamemasterAnswers, isGameMaster, reveal } from "./gamemaster.js";
import { playerAnswers } from "./player.js";
import { registerSubscription, stompClient } from "./websocket.js";

var states = ['HIDDEN', 'SHOW_CATEGORY', 'SHOW_QUESTION', 'SHOW_QUESTION_DATA', 'LOCK_QUESTION', 'REVEAL_ANSWERS', 'SHOW_ANSWER']

const metadataDiv = document.getElementById("question-metadata");
const questionDiv = document.getElementById("question-question");
const hintDiv = document.getElementById("question-hint");
const answerDiv = document.getElementById("question-answer");
const closeDiv = document.getElementById("question-close");

var buzzer = document.getElementById("buzzer");

const answerText = document.getElementById("answer-textfield");
const answerNumber = document.getElementById("answer-numberfield");
const answerSort = document.getElementById("answer-sort");

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


function makeStep(step) {
    var blueprint;
    switch (step.type) {
        case "TEXT":
            blueprint = document.createElement('p');
            blueprint.innerText = step.content;
            break;
        case "IMAGE":
            break;
        case "VIDEO":
            blueprint = document.createElement('div');
            blueprint.innerHTML = makeVideoHTML(step.content);
            break;
        case "AUDIO":
            break;
        case "LIST":
            break;
    }

    return blueprint;
}

export function registerQuestion() {
    registerSubscription((client) => {
        function onQuestionUpdate(msg) {
            var update = JSON.parse(msg.body);
            if (!update.question) {
                resetQuestion();
                board.style.display = null;
                return;
            }
            hideAnswer();

            var metadataGrp = update.question.groups['METADATA'];
            var questionGrp = update.question.groups['QUESTION'];
            var hintGrp = update.question.groups['HINT'];
            var answerGrp = update.question.groups['ANSWER'];

            metadataDiv.style.setProperty("--color", update.color);

            const addRevealButton = (more, child) => {

                var typ = more ? 'reveal' : 'hide';

                var button = document.createElement("button");
                button.classList.add(`${typ}-btn`, "btn", `btn-outline-${more ? 'warning' : 'danger'}`, "d-flex", "align-items-center", "gap-2");
                button.innerHTML = makeIcon(`${more ? 'eye' : 'eye-slash'}`);
                button.addEventListener('click', () => reveal(more));


                var wrapper = document.createElement("div");
                wrapper.classList.add(`${typ}-wrapper`);
                wrapper.append(button, child);

                return wrapper;
            }

            var lastRevealed = true;
            var lastElement;
            for (var grp of [{ grp: metadataGrp, div: metadataDiv }, { grp: questionGrp, div: questionDiv }, { grp: hintGrp, div: hintDiv }, { grp: answerGrp, div: answerDiv }]) {
                grp.div.replaceChildren();
                for (var step of grp.grp.steps) {
                    if (step.revealed || isGameMaster) {
                        var child = makeStep(step);
                        if (!step.revealed && lastRevealed) {
                            child = addRevealButton(true, child);

                            if (lastElement) {
                                var parent = lastElement.parentNode;

                                lastElement.remove();

                                parent.appendChild(addRevealButton(false, lastElement));
                            }
                        }
                        lastElement = child;
                        grp.div.appendChild(child);
                    }
                    lastRevealed = step.revealed;
                }
            }

            if (lastRevealed && isGameMaster) {
                var closeQuestionButton = document.createElement("button");
                closeQuestionButton.classList.add("btn-end-question", "btn", "btn-outline-warning", "d-flex", "align-items-center", "gap-2");
                closeQuestionButton.innerHTML = makeIcon("check-circle-fill");
                closeQuestionButton.innerText = "Frage abschließen";
                closeQuestionButton.addEventListener('click', () => reveal(true));
                closeDiv.replaceChildren(closeQuestionButton);

                if (lastElement) {
                    var parent = lastElement.parentNode;

                    lastElement.remove();

                    parent.appendChild(addRevealButton(false, lastElement));
                }
            }


            if (metadataGrp.started) {
                board.style.display = 'none';
            }
            if (metadataGrp.complete) {
                switch (update.question.type.name) {
                    case 'NORMAL':
                        buzzer.style.display = null;
                        break;
                    case 'TEXT':
                        updateSubmitButton(() => {
                            submitAnswer(answerText.value);
                        });

                        answerText.style.display = null;
                        submitButton.style.display = null;
                        break;
                    case 'ESTIMATE':
                        updateSubmitButton(() => {
                            submitAnswer(answerNumber.value);
                        });


                        answerNumber.style.display = null;
                        submitButton.style.display = null;
                        break;
                    case 'VIDEO':
                        updateSubmitButton(() => {
                            submitAnswer(answerText.value);
                        });

                        answerText.style.display = null;
                        submitButton.style.display = null;
                        break;
                    case 'SORT':
                        if (questionGrp.started && !answerSort.children.length) {
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
                            updateSubmitButton(() => {
                                submitAnswer(Array.from(answerSort.children).map((v) => {
                                    return { name: v.innerText, value: 0 }
                                }));
                            });
                        }


                        answerSort.style.display = null;
                        submitButton.style.display = null;
                        break;
                }
            }


            // if (isLocked && !showAnswer) {
            //     countdown = COUNTDOWN_LENGTH;
            //     const diameter = 2 * Math.PI * 90; // TODO: Maybe hardcode the radius?

            //     var cb = function () {
            //         countdownWrapper.style.display = null;

            //         countdownText.innerHTML = `${countdown}s`;
            //         countdownCircle.style.strokeDashoffset = (1 - (countdown / COUNTDOWN_LENGTH)) * diameter;
            //         if (countdown < 0) {
            //             // countdownEndAudio.play();
            //             countdownWrapper.style.display = 'none';
            //             clearInterval(countdownInterval);

            //             switch (update.question.type.name) {
            //                 case 'NORMAL':
            //                     updateBuzzerState(false);
            //                     break;
            //                 case 'VIDEO':
            //                     question_data.innerHTML = '';
            //                     question_data.replaceChildren();
            //                 default:
            //                     submitButton.click();
            //                     break;
            //             }


            //             toDisplay.forEach((v) => {
            //                 v.readOnly = true;
            //                 v.style.pointerEvents = 'none';
            //             });

            //             return;
            //         }
            //         countdown--;
            //         // countdownBeepAudio.play();
            //     }
            //     cb();
            //     countdownInterval = setInterval(cb, 1000);
            // } else if (!showAnswer) {
            //     toDisplay.forEach((v) => {
            //         v.readOnly = false;
            //         v.style.pointerEvents = 'auto';
            //     });
            // }

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
                const wrongID = `wrong:${answer.player}`;
                const correctID = `correct:${answer.player}`;
                const revealID = `reveal:${answer.player}`;

                var row = document.getElementById(rowID) || myAnswers.insertRow(-1);
                row.classList.remove("table-success");
                row.classList.remove("table-danger");
                row.classList.add("text-center");

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

                var judgeCell = document.getElementById(judgeID) || row.insertCell(-1);
                judgeCell.id = judgeID;

                var correctButton = document.getElementById(correctID) || document.createElement("button");
                correctButton.id = correctID;

                var wrongButton = document.getElementById(wrongID) || document.createElement("button");
                wrongButton.id = wrongID;

                if (answer.evaluatable) {
                    correctButton.remove();
                    wrongButton.remove();
                } else {
                    var eventListenerFactory = (correct) => function () {
                        client.send("/app/answer", {}, JSON.stringify({
                            playerName: answer.player,
                            isCorrect: correct,
                        }))
                    }
                    correctButton.classList.add("btn", "btn-success", "mx-1");
                    correctButton.innerHTML = makeIcon("check-lg");
                    correctButton.disabled = answer.correct != null;
                    correctButton.addEventListener('click', eventListenerFactory(true))


                    wrongButton.classList.add("btn", "btn-danger", "mx-1");
                    wrongButton.innerHTML = makeIcon("x-lg");
                    wrongButton.disabled = answer.correct != null;
                    wrongButton.addEventListener('click', eventListenerFactory(false));


                    if (!correctButton.parentNode && !wrongButton.parentNode) {
                        judgeCell.appendChild(correctButton);
                        judgeCell.appendChild(wrongButton);
                    }
                }

                var revealButton = document.getElementById(revealID) || document.createElement("button");
                revealButton.id = revealID;

                if (answer.revealed) {
                    revealButton.remove();
                } else {
                    revealButton.classList.add("btn", "btn-warning", "mx-1");
                    revealButton.innerHTML = makeIcon("search")
                    revealButton.addEventListener('click', function () {
                        client.send("/app/reveal-answer", {}, answer.player);
                    })
                    if (!revealButton.parentNode) {
                        judgeCell.appendChild(revealButton);
                    }
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

function hideAnswer() {
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
    answerSort.replaceChildren();

    metadataDiv.replaceChildren();
    questionDiv.replaceChildren();
    hintDiv.replaceChildren();
    answerDiv.replaceChildren();

    gamemasterAnswers.replaceChildren();
    playerAnswers.replaceChildren();

    hideAnswer();
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




