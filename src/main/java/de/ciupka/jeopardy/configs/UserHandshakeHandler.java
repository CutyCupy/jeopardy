package de.ciupka.jeopardy.configs;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * This class is a custom HandshakeHandler that is used to create a
 * {@code UserPrincipal} object whenever a user is connecting via WebSocket.
 * Apart from {@code determineUser} every other Method is extended from
 * {@link org.springframework.web.socket.server.support.DefaultHandShakeHandler}
 * 
 * @author Alexander Ciupka
 */
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UserHandshakeHandler.class);

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        final UUID id = UUID.randomUUID();

        LOG.info("New user with ID '{}' connected", id);

        return new UserPrincipal(id);
    }
}
