package il.ac.bgu.se.bp.rest.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import il.ac.bgu.se.bp.debugger.runner.OnStateChangedHandler;
import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;

@Service
@Qualifier("OnStateChangedHandlerImpl")
public class OnStateChangedHandlerImpl implements OnStateChangedHandler {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private static final String SIMP_SESSION_ID = "simpSessionId";
    private static final String WS_MESSAGE_TRANSFER_DESTINATION = "/state/update";
    private List<String> userNames = new ArrayList<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles WebSocket connection events
     */
    @EventListener(SessionConnectEvent.class)
    public void handleWebsocketConnectListener(SessionConnectEvent event) {
        System.out.println(String.format("WebSocket connection established for sessionID %s",
                getSessionIdFromMessageHeaders(event)));
    }

    /**
     * Handles WebSocket disconnection events
     */
    @EventListener(SessionDisconnectEvent.class)
    public void handleWebsocketDisconnectListener(SessionDisconnectEvent event) {
        System.out.println(String.format("WebSocket connection closed for sessionID %s",
                getSessionIdFromMessageHeaders(event)));
    }

    OnStateChangedHandlerImpl(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void sendMessages() {
        for (String userName : userNames) {
            System.out.println("sendMessages(): userName = " + userName);
            simpMessagingTemplate.convertAndSendToUser(userName, WS_MESSAGE_TRANSFER_DESTINATION,
                    "Hello " + userName + " at " + new Date().toString());
        }
    }

    @Override
    public void sendMessage(String sessionId, BPDebuggerState debuggerState) {
        try {
            simpMessagingTemplate.convertAndSendToUser(sessionId, WS_MESSAGE_TRANSFER_DESTINATION,
                    objectMapper.writeValueAsString(debuggerState));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void addUser(String sessionId, String userId) {
        userNames.add(userId);
    }

    private String getSessionIdFromMessageHeaders(SessionDisconnectEvent event) {
        Map<String, Object> headers = event.getMessage().getHeaders();
        return Objects.requireNonNull(headers.get(SIMP_SESSION_ID)).toString();
    }

    private String getSessionIdFromMessageHeaders(SessionConnectEvent event) {
        Map<String, Object> headers = event.getMessage().getHeaders();
        return Objects.requireNonNull(headers.get(SIMP_SESSION_ID)).toString();
    }

}
