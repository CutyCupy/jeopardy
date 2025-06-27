
export let stompClient;

let subscribers = [];

export function registerSubscription(fn) {
    subscribers.push(fn);
}


export function connect() {

    registerSubscription((client) => {
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



