package il.ac.bgu.se.bp.debugger.runner;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.logger.Logger;
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

    private static final Map<String, BPJsDebugger> bpDebugProgramsByUsers = new ConcurrentHashMap<>();
    private static final Map<String, BProgramRunner> bpRunProgramsByUsers = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> bpUsersLastOperationTime = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("stateNotificationHandlerImpl")
    private NotificationHandler stateNotificationHandler;

    @Autowired
    @Qualifier("consoleNotificationHandlerImpl")
    private NotificationHandler consoleNotificationHandler;


    @Override
    public void addNewRunExecution(String userId, BProgramRunner bProgramRunner) {
        bpRunProgramsByUsers.put(userId, bProgramRunner);
        updateLastOperationTime(userId);
    }

    @Override
    public BProgramRunner getBPjsRunnerByUser(String userId) {
        return bpRunProgramsByUsers.get(userId);
    }

    @Override
    public void addNewDebugExecution(String userId, BPJsDebugger bpProgramDebugger) {
        bpDebugProgramsByUsers.put(userId, bpProgramDebugger);
        updateLastOperationTime(userId);
    }

    @Override
    public BPJsDebugger getBPjsDebuggerByUser(String userId) {
        return bpDebugProgramsByUsers.get(userId);
    }

    @Override
    public boolean validateUserId(String userId) {
        return !StringUtils.isEmpty(userId) &&
                bpUsersLastOperationTime.containsKey(userId);
    }

    @Override
    public void addUser(String sessionId, String userId) {
        logger.info("adding user: {0}", userId);
        updateLastOperationTime(userId);
    }

    @Override
    public void updateLastOperationTime(String userId) {
        bpUsersLastOperationTime.put(userId, getCurrentLocalDateTime());
    }

    @Override
    public void removeUser(String userId) {
        bpUsersLastOperationTime.remove(userId);
        removeUserPrograms(userId);
    }

    @Scheduled(fixedRate = BP_JS_PROGRAM_TTL)
    private void cleanProgramsReachedThreshold() {
        LocalDateTime currentTime = getCurrentLocalDateTime();

        bpUsersLastOperationTime.forEach((userId, lastUpdateTime) -> {
            if (!lastUpdateTime.plusHours(BP_JS_PROGRAM_TTL).isAfter(currentTime)) {
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
        visit(userId, new ConsoleMessage("console.log..... what a message"));      //temp
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
        logger.info("removing user: {0}", userId);
        bpDebugProgramsByUsers.remove(userId);
        bpRunProgramsByUsers.remove(userId);
    }

    private LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }
}
