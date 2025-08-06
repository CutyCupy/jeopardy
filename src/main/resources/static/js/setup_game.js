import { registerBoard } from "./board.js";
import { registerGamemaster } from "./gamemaster.js";
import { registerLobby } from "./lobby.js";
import { registerMain } from "./main.js";
import { registerQuestion } from "./question.js";
import { connect } from "./websocket.js";

registerBoard();
registerGamemaster();
registerLobby();
registerQuestion();
registerMain();

connect();