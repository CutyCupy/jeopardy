import { callbackClosure, deriveButtonColors, getTextColorForBackground, hexToHSL } from "./main.js";
import { registerSubscription } from "./websocket.js";

export const board = document.getElementById("board");


export function registerBoard() {
    registerSubscription((client) => {
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
                    label.style.filter = 'brightness(0.6#)';
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
                            client.send(
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


        client.subscribe("/user/topic/board-update", boardUpdate);
        client.subscribe("/topic/board-update", boardUpdate);

        client.subscribe("/user/topic/active-player-update", (msg) => {
            const isActive = JSON.parse(msg.body);

            if (isActive) {
                board.classList.add("active");
            } else {
                board.classList.remove("active");
            }
        })
    });
}