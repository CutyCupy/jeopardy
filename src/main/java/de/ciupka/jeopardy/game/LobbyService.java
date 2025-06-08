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

    public void addPlayer(UUID uuid, String name) {
        Optional<Player> player = this.players.stream().filter((p) -> p.getName().equals(name))
                .findFirst();
        if (player.isPresent()) {
            player.get().setUuid(uuid);
            return;
        }
        this.players.add(new Player(uuid, name));
    }

    public Player[] getPlayers() {
        return this.players.toArray(new Player[this.players.size()]);
    }

}
