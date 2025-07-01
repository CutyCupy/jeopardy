package de.ciupka.jeopardy.controller.messages;

import java.util.Arrays;

import de.ciupka.jeopardy.game.GameService;

public class LobbyUpdate {

    private PlayerUpdate[] players;

    public LobbyUpdate(GameService game) {
        this.players = Arrays.stream(game.getLobby()).map(p -> new PlayerUpdate(game, p)).toArray(PlayerUpdate[]::new);
    }

    public PlayerUpdate[] getPlayers() {
        return players;
    }

}
