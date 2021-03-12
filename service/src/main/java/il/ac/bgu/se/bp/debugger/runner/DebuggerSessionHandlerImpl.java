package il.ac.bgu.se.bp.debugger.runner;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableScheduling
@Qualifier("DebuggerSessionHandlerImpl")
public class DebuggerSessionHandlerImpl implements DebuggerSessionHandler<BProgramRunner> {

    private static final int ONE_HOUR = 60 * 60 * 1000;
    private static final int BP_JS_PROGRAM_TTL = 3;     // hours     // todo: ask gera/achia/michael for threshold

    private static final Map<String, BPJsDebugger> bpDebugProgramsByUsers = new ConcurrentHashMap<>();
    private static final Map<String, BProgramRunner> bpRunProgramsByUsers = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> bpUsersLastOperationTime = new ConcurrentHashMap<>();
    private static final Map<String, String> userIdBySessionId = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("OnStateChangedHandlerImpl")
    private OnStateChangedHandler onStateChangedHandler;

    @Override
    public void addNewRunExecution(String userId, BProgramRunner bProgramRunner) {
        bpRunProgramsByUsers.put(userId, bProgramRunner);
    }

    @Override
    public void addNewDebugExecution(String userId, BPJsDebugger bpProgramDebugger) {
        bpDebugProgramsByUsers.put(userId, bpProgramDebugger);
    }

    @Override
    public boolean validateSessionId(String sessionId) {
        return !StringUtils.isEmpty(getUserIdBySessionId(sessionId));
    }

    @Override
    public void updateLastOperationTime(String userId) {
        bpUsersLastOperationTime.put(userId, getCurrentLocalDateTime());
    }

    @Override
    public boolean removeSession(String sessionId) {
        userIdBySessionId.remove(sessionId);
        return true;
    }

    @Override
    public Void updateUserStateChange(String sessionId, BPDebuggerState debuggerState) {
        onStateChangedHandler.sendMessage(sessionId, debuggerState);
        return null;
    }

    private String getUserIdBySessionId(String sessionId) {
        return userIdBySessionId.get(sessionId);
    }

    private LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }

    @Scheduled(fixedRate = ONE_HOUR)
    private void cleanProgramsReachedThreshold() {
        LocalDateTime currentTime = getCurrentLocalDateTime();
        List<String> userIdsToRemove = new LinkedList<>();

        bpUsersLastOperationTime.forEach((userId, lastUpdateTime) -> {
            if (!lastUpdateTime.plusHours(BP_JS_PROGRAM_TTL).isAfter(currentTime))
                userIdsToRemove.add(userId);
        });

        userIdsToRemove.forEach(userId -> {
            bpUsersLastOperationTime.remove(userId);
            bpDebugProgramsByUsers.remove(userId);
            bpRunProgramsByUsers.remove(userId);
        });
    }

    @Scheduled(fixedRateString = "6000", initialDelayString = "0")
    public void schedulingTask() {
        onStateChangedHandler.sendMessages();
        userIdBySessionId.values().forEach(userId -> onStateChangedHandler.sendMessage(userId, new BPDebuggerState()));
    }

    @Override
    public void addUser(String sessionId, String userId) {
        userIdBySessionId.put(sessionId, userId);
        onStateChangedHandler.addUser(sessionId, userId);
    }

    @Override
    public void sendMessages() {
        onStateChangedHandler.sendMessages();
    }

    @Override
    public void sendMessage(String sessionId, BPDebuggerState debuggerState) {
        if (!validateSessionId(sessionId)) {
            return;
        }
        String userId = getUserIdBySessionId(sessionId);
        onStateChangedHandler.sendMessage(userId, debuggerState);
    }
}
