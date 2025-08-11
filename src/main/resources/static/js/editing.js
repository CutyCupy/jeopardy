import { registerSubscription, stompClient } from "./websocket.js";
import { callbackClosure, deriveButtonColors, getTextColorForBackground, hexToHSL } from "./main.js";

const boardDiv = document.getElementById("board");
const questionDiv = document.getElementById("question-form");
const categoryDiv = document.getElementById("category-form");

const questionFields = document.getElementById("question-fields");

const typeOptions = ["NORMAL", "ESTIMATE", "SORT", "TEXT", "VIDEO", "HINT"];
const toolOptions = ["BUZZER", "TEXT", "NUMBER", "SORT"];

export function registerEditing() {
    registerSubscription((client) => {

        client.subscribe("/user/topic/editing-board", (msg) => {

            const categories = JSON.parse(msg.body);

            var cols = [];
            for (var cIdx = 0; cIdx < (categories || []).length; cIdx++) {
                const category = categories[cIdx];
                const column = document.createElement("div");
                column.classList.add("col", "board-column");

                const categoryButton = document.createElement("div");
                categoryButton.classList.add("category", "question-btn");
                categoryButton.innerText = category.name;

                categoryButton.addEventListener('click', callbackClosure({ category, catIdx: cIdx }, (cat) => {
                    showCategory(cat.category, cat.catIdx);
                }));

                column.appendChild(categoryButton);

                categoryButton.style.setProperty('--background', category.color);
                categoryButton.style.setProperty('--color', getTextColorForBackground(hexToHSL(category.color).l));

                const { buttonNormal, buttonHover, textColorNormal, textColorHover } = deriveButtonColors(category.color);

                categoryButton.style.setProperty('--hover-background', buttonNormal);
                categoryButton.style.setProperty('--hover-color', textColorNormal);

                for (var qIdx = 0; qIdx < (category.questions || []).length; qIdx++) {
                    const question = category.questions[qIdx];
                    const button = document.createElement("button");

                    const icon = document.createElement("i");
                    icon.classList.add(getBootstrapIcon(question.type), "me-2");

                    const label = document.createElement("span");
                    label.innerText = question.points;


                    button.style.setProperty('--background', buttonNormal);
                    button.style.setProperty('--color', textColorNormal);
                    button.style.setProperty('--hover-background', buttonHover);
                    button.style.setProperty('--hover-color', textColorHover);

                    button.classList.add("col", "question-btn");

                    button.addEventListener('click', callbackClosure({ question, cIdx, qIdx }, (q) => {
                        showQuestion(q.question, q.cIdx, q.qIdx);
                    }),
                    );

                    button.appendChild(icon);
                    button.appendChild(label);

                    column.appendChild(button);
                }

                const addQuestion = document.createElement("button");

                addQuestion.style.setProperty('--background', buttonNormal);
                addQuestion.style.setProperty('--color', textColorNormal);
                addQuestion.style.setProperty('--hover-background', buttonHover);
                addQuestion.style.setProperty('--hover-color', textColorHover);

                addQuestion.classList.add("col", "question-btn");

                addQuestion.innerText = "+";

                addQuestion.addEventListener('click', callbackClosure(cIdx, (cat) => {
                    showQuestion(null, cat, -1);
                }),
                );

                column.appendChild(addQuestion);

                cols.push(column)
            }

            const column = document.createElement("div");
            column.classList.add("col", "board-column");

            const button = document.createElement("button");
            button.classList.add("category", "question-btn");
            button.innerText = "+";
            button.addEventListener('click', () => {
                showCategory(null, -1);
            })

            const { buttonNormal, buttonHover, textColorNormal, textColorHover } = deriveButtonColors("#28a745");

            button.style.setProperty('--background', buttonNormal);
            button.style.setProperty('--color', textColorNormal);
            button.style.setProperty('--hover-background', buttonHover);
            button.style.setProperty('--hover-color', textColorHover);



            column.appendChild(button);

            cols.push(column)
            // label.style.setProperty('--background', category.color);
            // label.style.setProperty('--color', getTextColorForBackground(hexToHSL(category.color).l));

            boardDiv.replaceChildren(...cols);

            displayOnly(boardDiv);
        });

        var password = prompt("Bitte geben Sie das Master-Passwort ein:")
        client.send("/app/start-editing", {}, password);
    });

}

function getBootstrapIcon(type) {
    switch (type) {
        case "NORMAL":
            return "bi-question-circle-fill";
        case "ESTIMATE":
            return "bi-calculator-fill";
        case "SORT":
            return "bi-funnel-fill";
        case "TEXT":
            return "bi-chat-text-fill";
        case "VIDEO":
            return "bi-camera-video-fill";
        case "HINT":
            return "bi-lightbulb-fill";
        default:
            return "bi-question-circle-fill"; // Fallback
    }
}

function questionSubmitCallback(cIdx, qIdx) {
    return (ev) => {
        ev.preventDefault();

        const fields = Array.from(questionDiv.getElementsByTagName("*")).filter((v) => v.id && v.value);

        const result = {};

        for (var field of fields) {
            result[field.id] = field.type == 'checkbox' ? field.checked : field.value; // TODO: Sicherstellen, dass hier immer die richtigen Values drin stehen
        }

        stompClient.send("/app/edit-question", {
            question: qIdx,
            category: cIdx,
        }, JSON.stringify(result));
    }
}

function categorySubmitCallback(index) {
    return (ev) => {
        ev.preventDefault();

        const fields = Array.from(categoryDiv.getElementsByTagName("*")).filter((v) => v.id && v.value);

        const result = {};

        for (var field of fields) {
            result[field.id] = field.value; // TODO: Sicherstellen, dass hier immer die richtigen Values drin stehen
        }

        stompClient.send("/app/edit-category", {
            category: index,
        }, JSON.stringify(result));
    }
}

export function displayOnly(display) {
    boardDiv.style.display = null;
    questionDiv.style.display = display == questionDiv ? null : 'none';
    categoryDiv.style.display = display == categoryDiv ? null : 'none';
}

function showQuestion(question, cIdx, qIdx) {
    console.log(question)
    const typeSelect = document.getElementById('type');

    typeSelect.replaceChildren(...typeOptions.map((type) => {
        const option = document.createElement('option');
        option.value = type;
        option.innerText = type;

        return option;
    }));

    typeSelect.value = question?.type;
    typeSelect.onchange = onTypeChange;

    onTypeChange();

    for (const key of Object.keys(question || {})) {
        const element = document.getElementById(key);
        if (!element || key == 'type') {
            continue;
        }
        switch (key) {
            case "hints":
                for (const hint of question[key] || []) {
                    insertHint(hint);
                }
                onHintChange();
                break;
            case "answer":
                if (question.type == "SORT") {
                    for (const option of question[key] || []) {
                        insertOption(option);
                    }
                    onOptionChange();
                    continue;
                }
        }
        element.value = question[key];
        element.selected = !!question[key];
    }


    const saveButton = document.getElementById('question-save');
    saveButton.onclick = questionSubmitCallback(cIdx, qIdx);


    const deleteButton = document.getElementById('question-delete');
    deleteButton.onclick = callbackClosure({ cIdx, qIdx }, (id) => stompClient.send("/app/delete-question", {
        question: id.qIdx, category: id.cIdx,
    }));


    const cancelButton = document.getElementById('question-cancel');
    cancelButton.onclick = () => displayOnly(boardDiv);

    displayOnly(questionDiv);
}

function onTypeChange() {
    const typeField = document.getElementById("type");

    questionFields.replaceChildren();

    const questionField = document.getElementById("question") || document.createElement('input');
    questionField.id = "question";

    questionFields.appendChild(asFormGroupItem(questionField, "Frage"));

    const pointsField = document.getElementById("points") || document.createElement('input');
    pointsField.id = "points";
    pointsField.type = 'number';

    questionFields.appendChild(asFormGroupItem(pointsField, "Punkte"));

    switch (typeField.value) {
        case "ESTIMATE": {
            console.log("estimate");
            const answerField = document.createElement('input');
            answerField.id = "answer";
            answerField.type = 'number';

            questionFields.appendChild(asFormGroupItem(answerField, "Antwort"));
            break;
        }
        case "SORT": {
            console.log("sort");
            const descendingField = document.createElement('input');
            descendingField.id = "descending";
            descendingField.type = 'checkbox';

            questionFields.appendChild(asFormGroupItem(descendingField, "Absteigend?"));

            const optionsDiv = document.createElement('div')
            optionsDiv.id = "answer";


            const addButton = document.createElement('button')
            addButton.classList.add("btn", "btn-success")
            addButton.innerText = "+"
            addButton.id = "options-add"
            addButton.onclick = () => insertOption();

            optionsDiv.appendChild(addButton);

            questionFields.appendChild(optionsDiv)
            break;
        }
        case "NORMAL": {
            console.log("normal or text");
            const answerField = document.createElement('textarea');
            answerField.id = "answer";

            questionFields.appendChild(asFormGroupItem(answerField, "Antwort"));
            questionFields.appendChild(getAnswerTool());
            break;
        }
        case "HINT":
            const answerField = document.createElement('textarea');
            answerField.id = "answer";

            questionFields.appendChild(asFormGroupItem(answerField, "Antwort"));

            const hintsDiv = document.createElement('div')
            hintsDiv.id = "hints";


            const addButton = document.createElement('button')
            addButton.classList.add("btn", "btn-success")
            addButton.innerText = "+"
            addButton.id = "hints-add"
            addButton.onclick = () => insertHint();

            hintsDiv.appendChild(addButton);

            questionFields.appendChild(hintsDiv);

            break;
        case "VIDEO": {
            console.log("video");
            const questionVideo = document.createElement('input');
            questionVideo.id = "questionVideo";
            addOnlyStreamableCheck(questionVideo);

            questionFields.appendChild(asFormGroupItem(questionVideo, "Frage-Video"));
            const answerVideo = document.createElement('input');
            answerVideo.id = "answerVideo";
            addOnlyStreamableCheck(answerVideo);

            questionFields.appendChild(asFormGroupItem(answerVideo, "Antwort-Video"));
            const answerField = document.createElement('textarea');
            answerField.id = "answer";

            questionFields.appendChild(asFormGroupItem(answerField, "Antwort"));
            questionFields.appendChild(getAnswerTool());

            break;
        }
    }
}

function addOnlyStreamableCheck(input) {
    input.addEventListener('keydown', function (e) {
        const allowedKeys = [
            'Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown',
            'Tab', 'Home', 'End'
        ];
        if (!allowedKeys.includes(e.key) && !(e.ctrlKey && e.key.toLowerCase() === 'v')) {
            e.preventDefault();
        }
    });

    input.addEventListener('paste', function (e) {
        e.preventDefault(); // Verhindert das Standard-Einfügen

        const pastedText = (e.clipboardData || window.clipboardData).getData('text').trim();

        try {
            const url = new URL(pastedText);

            if (url.hostname.includes('streamable.com')) {
                // Hol den letzten Teil nach dem letzten Slash
                const parts = url.pathname.split('/').filter(Boolean);
                if (parts.length > 0) {
                    const lastPart = parts[parts.length - 1];
                    input.value = lastPart;
                    errorMsg.textContent = '';
                } else {
                    errorMsg.textContent = 'Ungültiger Streamable-Link.';
                }
            } else {
                errorMsg.textContent = 'Nur Links von streamable.com sind erlaubt.';
            }
        } catch {
            errorMsg.textContent = 'Kein gültiger Link.';
        }
    });
}

function onOptionChange() {
    const optionsDiv = document.getElementById('answer');

    optionsDiv.value = Array.from(optionsDiv.getElementsByTagName("div")).map((div) => {
        const [name, value, _] = div.children;
        return {
            name: name.value, value: value.value,
        };
    })
}

function insertOption(option) {
    const optionsDiv = document.getElementById('answer');

    const wrapper = document.createElement('div');
    wrapper.id = `option-${optionsDiv.children.length - 1}`;
    wrapper.classList.add("row");

    const label = document.createElement('input');
    label.value = option?.name || "";
    label.classList.add("col");
    label.onchange = onOptionChange;

    const value = document.createElement('input');
    value.value = option?.value || 0;
    value.type = 'number';
    value.classList.add("col");
    value.onchange = onOptionChange;

    const remove = document.createElement('button');
    remove.innerText = "x"
    remove.classList.add("btn", "btn-danger");
    remove.classList.add("col");
    remove.onclick = () => {
        optionsDiv.removeChild(wrapper);
        onOptionChange();
    }

    wrapper.appendChild(label);
    wrapper.appendChild(value);
    wrapper.appendChild(remove);

    optionsDiv.insertBefore(wrapper, document.getElementById("options-add"))
}

function onHintChange() {
    const hintsDiv = document.getElementById('hints');

    hintsDiv.value = Array.from(hintsDiv.getElementsByTagName("input")).map((v) => {
        return v.value
    })
}

function insertHint(hint) {
    const hintsDiv = document.getElementById('hints');


    const wrapper = document.createElement('div');
    wrapper.id = `option-${hintsDiv.children.length - 1}`;
    wrapper.classList.add("row");

    const hintEle = document.createElement('input');
    hintEle.value = hint || "";
    hintEle.classList.add("col");
    hintEle.onchange = onHintChange;

    const remove = document.createElement('button');
    remove.innerText = "x"
    remove.classList.add("btn", "btn-danger");
    remove.classList.add("col");
    remove.onclick = () => {
        hintsDiv.removeChild(hintEle);
        onHintChange();
    }

    wrapper.appendChild(hintEle);
    wrapper.appendChild(remove);

    hintsDiv.insertBefore(wrapper, document.getElementById("hints-add"))
}

function asFormGroupItem(item, label) {
    const group = document.createElement('div');
    group.classList.add('form-group');

    const labelElement = document.createElement('label');
    labelElement.htmlFor = item.id;
    labelElement.innerText = label;

    if (item.type == "checkbox") {
        item.classList.add("form-check-input");
    } else {
        item.classList.add("form-control")
    }

    group.appendChild(labelElement);
    group.appendChild(item);

    return group;
}

function showCategory(category, index) {
    const name = document.getElementById("name");
    name.value = category?.name || "";
    const color = document.getElementById("color");
    color.value = category?.color;


    const saveButton = document.getElementById('category-save');
    saveButton.onclick = categorySubmitCallback(index);

    const deleteButton = document.getElementById('category-delete');
    deleteButton.onclick = callbackClosure(index, (id) => stompClient.send("/app/delete-category", {
        category: id,
    }));

    const cancelButton = document.getElementById('category-cancel');
    cancelButton.onclick = () => displayOnly(boardDiv);


    displayOnly(categoryDiv);
}

function getAnswerTool() {
    const answerTool = document.createElement('select')
    answerTool.id = "answerTool";


    answerTool.replaceChildren(...toolOptions.map((type) => {
        const option = document.createElement('option');
        option.value = type;
        option.innerText = type;

        return option;
    }));

    return asFormGroupItem(answerTool, "Art der Antwort");
}