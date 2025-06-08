package de.ciupka.jeopardy.configs;

import java.security.Principal;
import java.util.UUID;

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