package dev.nilswitt.webmap.api.ws;

import dev.nilswitt.webmap.entities.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> subscriptionSessions = new ConcurrentHashMap<>();

    private Logger logger = LogManager.getLogger(WebSocketSessionRegistry.class);

    public void add(WebSocketSession session) {
        sessions.put(session.getId(), session);
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


    public void notifyTopic(String topic, String message) {
        logger.info("Sending update to topic {}", topic);
        HashMap<String, WebSocketSession> notifiedSessions = new HashMap<>();
        Iterable<WebSocketSession> sessions = getSessionsForSubscription(topic);
        for (WebSocketSession session : sessions) {
            notifiedSessions.put(session.getId(), session);
        }

        List<String> fuzzyMatchedTopics = fuzzyMatch(topic);
        logger.info("Fuzzy matched topic {}", fuzzyMatchedTopics);
        for (String fuzzyTopic : fuzzyMatchedTopics) {
            Iterable<WebSocketSession> fuzzySessions = getSessionsForSubscription(fuzzyTopic);
            for (WebSocketSession session : fuzzySessions) {
                notifiedSessions.put(session.getId(), session);
            }
        }


        for (WebSocketSession session : notifiedSessions.values()) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {

            }
        }
    }


    private List<String> fuzzyMatch(String destinationTopic) {
        logger.info("Fuzzy matching topic pattern {}", destinationTopic);

        return subscriptionSessions.keySet().stream().filter(topic -> {
            String regex = createRegexFromGlob(topic);
            return destinationTopic.matches(regex);
        }).toList();

    }

    private static String createRegexFromGlob(String glob) {
        StringBuilder out = new StringBuilder("^");
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*':
                    out.append(".*");
                    break;
                case '?':
                    out.append('.');
                    break;
                case '.':
                    out.append("\\.");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '/':
                    out.append("\\/");
                    break;
                default:
                    out.append(c);
            }
        }
        out.append('$');
        return out.toString();
    }
}

