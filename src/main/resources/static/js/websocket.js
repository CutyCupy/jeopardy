export let stompClient;

let subscribers = [];

const titleDiv = document.getElementById("title");

export function registerSubscription(fn) {
    subscribers.push(fn);
}


export function connect() {

    registerSubscription((client) => {
        client.subscribe("/user/topic/on-connect", (msg) => {
            var letters = [];

            for (var i = 0; i < msg.body.length; i++) {
                var span = document.createElement("span");
                span.style.setProperty("--hop-delay", `${i / 10}s`);
                span.style.setProperty("--color-delay", `${i / 10}s`);
                span.innerText = msg.body[i];
                letters.push(span);
            }

            titleDiv.replaceChildren(...letters)

        })
        client.send('/app/on-connect');

    })

    // ngrok http --host-header=localhost 8080
    // <ngrok url>/ws
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        subscribers.forEach((fn) => fn(stompClient));
    });
}



