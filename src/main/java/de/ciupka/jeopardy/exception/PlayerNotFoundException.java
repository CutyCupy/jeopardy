package de.ciupka.jeopardy.exception;

import java.util.UUID;

public class PlayerNotFoundException extends Exception {

    private UUID uuid;
    private String name;

    public PlayerNotFoundException(UUID uuid) {
        this.uuid = uuid;
    }

    public PlayerNotFoundException(String name) {
        this.name = name;
    }

    @Override
    public String getMessage() {
        if (uuid != null) {
            return String.format("Es wurde kein Spieler mit dem Namen '%s' gefunden!", name);
        }
        return String.format("Es wurde kein Spieler mit der ID '%s' gefunden!", uuid.toString());
    }

}
