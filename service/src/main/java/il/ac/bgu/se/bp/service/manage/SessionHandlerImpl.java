package il.ac.bgu.se.bp.service.manage;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.logger.Logger;
import il.ac.bgu.se.bp.service.code.SourceCodeHelper;
import il.ac.bgu.se.bp.service.notification.NotificationHandler;
import il.ac.bgu.se.bp.socket.console.ConsoleMessage;
import il.ac.bgu.se.bp.socket.exit.ProgramExit;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableScheduling
public class SessionHandlerImpl implements SessionHandler<BProgramRunner> {

    private static final Logger logger = new Logger(SessionHandlerImpl.class);

    private static final int ONE_HOUR = 60 * 60 * 1000;
    private static final int BP_JS_PROGRAM_TTL = 3 * ONE_HOUR;

    private static final Map<String, UserProgramSession<BPJsDebugger>> bpDebugProgramsByUsers = new ConcurrentHashMap<>();
    private static final Map<String, UserProgramSession<BProgramRunner>> bpRunProgramsByUsers = new ConcurrentHashMap<>();
    private static final Map<String, UserSession> unknownSessions = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("stateNotificationHandlerImpl")
    private NotificationHandler stateNotificationHandler;

    @Autowired
    @Qualifier("consoleNotificationHandlerImpl")
    private NotificationHandler consoleNotificationHandler;

    @Autowired
    private SourceCodeHelper sourceCodeHelper;

    @Override
    public void addUser(String sessionId, String userId) {
        logger.info("adding user: {0}", userId);
        unknownSessions.put(userId, new UserSession(sessionId, userId, getCurrentLocalDateTime()));
    }

    @Override
    public void addNewRunExecution(String userId, BProgramRunner bProgramRunner, String filename) {
        UserSession existingUserSession = unknownSessions.get(userId);
        if (existingUserSession == null) {
            return;
        }

        bpRunProgramsByUsers.put(userId, existingUserSession.withProgram(bProgramRunner).withFilename(filename));
        updateLastOperationTime(userId);
    }

    @Override
    public BProgramRunner getBPjsRunnerByUser(String userId) {
        UserProgramSession<BProgramRunner> userSession = bpRunProgramsByUsers.get(userId);
        if (userSession == null) {
            return null;
        }

        return userSession.getProgram();
    }

    @Override
    public void addNewDebugExecution(String userId, BPJsDebugger bpProgramDebugger, String filename) {
        UserSession existingUserSession = unknownSessions.get(userId);
        if (existingUserSession == null) {
            return;
        }

        bpDebugProgramsByUsers.put(userId, existingUserSession.withProgram(bpProgramDebugger).withFilename(filename));
        updateLastOperationTime(userId);
    }

    @Override
    public BPJsDebugger getBPjsDebuggerByUser(String userId) {
        UserProgramSession<BPJsDebugger> userSession = bpDebugProgramsByUsers.get(userId);
        if (userSession == null) {
            return null;
        }

        return userSession.getProgram();
    }

    @Override
    public boolean validateUserId(String userId) {
        return !StringUtils.isEmpty(userId) &&
                getUserSession(userId) != null;
    }

    @Override
    public UserSession getUserSession(String userId) {
        UserSession userSession = unknownSessions.get(userId);
        userSession = userSession == null ? bpRunProgramsByUsers.get(userId) : userSession;
        return userSession == null ? bpDebugProgramsByUsers.get(userId) : userSession;
    }

    @Override
    public void updateLastOperationTime(String userId) {
        updateLastOperationTime(unknownSessions, userId);
        updateLastOperationTime(bpRunProgramsByUsers, userId);
        updateLastOperationTime(bpDebugProgramsByUsers, userId);
    }

    private void updateLastOperationTime(Map<String, ? extends UserSession> sessions, String userId) {
        UserSession userSession = sessions.get(userId);
        if (userSession == null) {
            return;
        }

        userSession.setLastOperationTime(getCurrentLocalDateTime());
    }


    @Override
    public void removeUser(String userId) {
        logger.info("removing user: {0}", userId);
        unknownSessions.remove(userId);
        removeUserPrograms(userId);
    }

    @Scheduled(fixedRate = BP_JS_PROGRAM_TTL)
    private void cleanProgramsReachedThreshold() {
        verifySessionsThreshold(unknownSessions, BP_JS_PROGRAM_TTL);
        verifySessionsThreshold(bpRunProgramsByUsers, BP_JS_PROGRAM_TTL);
        verifySessionsThreshold(bpDebugProgramsByUsers, BP_JS_PROGRAM_TTL);
    }

    private <T> void verifySessionsThreshold(Map<String, ? extends UserSession> userSessionsByIds, int threshold) {
        LocalDateTime currentTime = getCurrentLocalDateTime();
        userSessionsByIds.forEach((userId, userSession) -> {
            if (!userSession.getLastOperationTime().plusHours(threshold).isAfter(currentTime)) {
                removeUser(userId);
            }
        });
    }

    @Override
    public void visit(String userId, BPDebuggerState debuggerState) {
        if (!validateUserId(userId)) {
            return;
        }
        logger.debug("sending BPDebuggerState update");
        stateNotificationHandler.sendNotification(userId, debuggerState);
    }

    @Override
    public void visit(String userId, ConsoleMessage consoleMessage) {
        if (!validateUserId(userId)) {
            return;
        }
        logger.debug("sending ConsoleMessage update");
        consoleNotificationHandler.sendNotification(userId, consoleMessage);
    }

    @Override
    public void visit(String userId, ProgramExit programExit) {
        removeUserPrograms(userId);
    }

    @Override
    public void update(BPEvent event) {
        event.accept(this);
    }

    private void removeUserPrograms(String userId) {
        logger.info("removing programs associated with user: {0}", userId);
        removeUserProgramFrom(userId, bpDebugProgramsByUsers);
        removeUserProgramFrom(userId, bpRunProgramsByUsers);
    }

    private void removeUserProgramFrom(String userId, Map<String, ? extends UserProgramSession> programByUserId) {
        UserProgramSession userProgramSession = programByUserId.remove(userId);
        if (userProgramSession != null) {
            sourceCodeHelper.removeCodeFile(userProgramSession.getFilename());
        }
    }

    private LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }
}
