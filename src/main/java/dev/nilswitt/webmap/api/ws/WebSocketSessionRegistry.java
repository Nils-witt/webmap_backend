package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.api.exceptions.ForbiddenException;
import dev.nilswitt.webmap.entities.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<User, Set<String>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> topicSubscriptions = new ConcurrentHashMap<>();
    private Logger logger = LogManager.getLogger(WebSocketSessionRegistry.class);


    public void add(WebSocketSession session) {
        sessions.put(session.getId(), session);
        Object userObj = session.getAttributes().get("user");
        if (userObj instanceof User user) {
            userSessions.computeIfAbsent(user, k -> ConcurrentHashMap.newKeySet()).add(session.getId());
        }
    }
    @Scheduled(fixedRate = 30000)
    public void keepalive(){
        logger.debug("Keep alive sessions");
        for(WebSocketSession session : sessions.values()){
            try {
                if(session.isOpen()) {
                    session.sendMessage(new TextMessage("ping"));
                }
            } catch (Exception e) {
                logger.warn("Failed to send keepalive ping to session {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    public void remove(WebSocketSession session) {
        if (session == null) {
            return;
        }
        sessions.remove(session.getId());
        Object userObj = session.getAttributes().get("user");
        if (userObj instanceof User user) {
            userSessions.computeIfAbsent(user, k -> ConcurrentHashMap.newKeySet()).remove(session.getId());
        }
    }

    public void subscribe(WebSocketSession session, String topic) throws ForbiddenException {
        topicSubscriptions.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(session.getId());
        logger.info("Session {} subscribed to topic {}", session.getId(), topic);

    }

    public Iterable<WebSocketSession> getSessions() {
        return sessions.values();
    }

    public WebSocketSession getSessionById(String sessionId) {
        return sessions.get(sessionId);
    }

    public Iterable<WebSocketSession> getSessionsForUser(UUID userId) {
        Set<String> sessionIds = userSessions.get(userId);
        if (sessionIds == null) {
            return Set.of();
        }
        return sessionIds.stream()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public Iterable<WebSocketSession> getSessionsForTopic(String topic) {
        Set<String> sessionIds = topicSubscriptions.get(topic);
        if (sessionIds == null) {
            return Set.of();
        }
        return sessionIds.stream()
                .map(sessions::get)
                .filter(session -> session != null)
                .toList();
    }

}

