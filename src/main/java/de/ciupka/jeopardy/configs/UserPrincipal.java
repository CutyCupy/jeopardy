package de.ciupka.jeopardy.configs;

import java.security.Principal;
import java.util.UUID;

/**
 * This class is used to provide a unique identifier in order to identify a connected WebSocket-Client and enable simple direct messages towards given client.
 * It is only instantiated whenever a new WebSocket Handshake is done and can be provided to any WebSocket MessageMapping if necessary.
 * @author Alexander Ciupka
 */
public class UserPrincipal implements Principal {

    private UUID uuid;

    public UserPrincipal(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getID() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.uuid.toString();
    }

}