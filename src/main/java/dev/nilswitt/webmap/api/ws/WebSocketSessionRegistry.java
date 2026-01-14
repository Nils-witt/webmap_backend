package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.entities.User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> subscriptionSessions = new ConcurrentHashMap<>();

    public void add(WebSocketSession session) {
        sessions.put(session.getId(),session);
        Object userObj = session.getAttributes().get("user");
        if (userObj instanceof User user) {
            userSessions.computeIfAbsent(user.getId(), k -> ConcurrentHashMap.newKeySet()).add(session.getId());
        }
    }

    public void remove(WebSocketSession session) {
        if (session == null) {
            return;
        }
        sessions.remove(session.getId());
        Object userObj = session.getAttributes().get("user");
        if (userObj instanceof User user) {
            userSessions.computeIfAbsent(user.getId(), k -> ConcurrentHashMap.newKeySet()).remove(session.getId());
        }
    }
    public void subscribe(WebSocketSession session, String topic) {
        subscriptionSessions.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(session.getId());
    }
    public Iterable<WebSocketSession> getSessions() {
        return sessions.values();
    }

    public Iterable<WebSocketSession> getSessionsForUser(UUID userId) {
        Set<String> sessionIds = userSessions.get(userId);
        if (sessionIds == null) {
            return Set.of();
        }
        return sessionIds.stream()
                .map(sessions::get)
                .filter(session -> session != null)
                .toList();
    }

    public Iterable<WebSocketSession> getSessionsForSubscription(String subscriptionId) {
        Set<String> sessionIds = subscriptionSessions.get(subscriptionId);
        if (sessionIds == null) {
            return Set.of();
        }
        return sessionIds.stream()
                .map(sessions::get)
                .filter(session -> session != null)
                .toList();
    }
    public Iterable<String> getAvailableSubscriptions() {
        return subscriptionSessions.keySet();
    }
}

