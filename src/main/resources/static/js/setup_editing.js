import { registerEditing } from "./editing.js";
import { registerMain } from "./main.js";
import { connect } from "./websocket.js";

registerMain();
registerEditing();

connect();