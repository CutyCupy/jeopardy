package de.ciupka.jeopardy.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class LobbyService {

    private List<Player> players;

    public LobbyService() {
        this.players = new ArrayList<>();
    }

    /**
     * addPlayer either adds a {@code Player} to the lobby or updates the
     * {@code uuid} of the existing {@code Player} when someone with {@code name}
     * already exists.
     * 
     * @param uuid The {@code UUID} of the Websocket Client that tries to be added
     *             to the lobby.
     * @param name The name that the new player wants to have.
     * @return True if a player was added to the lobby. If a player already exists
     *         with {@code name}, it will return false.
     */
    public boolean addPlayer(UUID uuid, String name) {
        Optional<Player> player = this.players.stream().filter((p) -> p.getName().equals(name))
                .findFirst();
        if (player.isPresent()) {
            // TODO: Think about a better solution for returning players.
            // This Method relies heavily on trust, that noone hijacks another player by
            // simply using their name.
            player.get().setUuid(uuid);
            return false;
        }
        this.players.add(new Player(uuid, name));

        return true;
    }

    public Player[] getPlayers() {
        return this.players.toArray(new Player[this.players.size()]);
    }

}
