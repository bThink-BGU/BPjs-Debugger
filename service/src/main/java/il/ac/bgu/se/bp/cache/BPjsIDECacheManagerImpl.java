package il.ac.bgu.se.bp.cache;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@EnableScheduling
public class BPjsIDECacheManagerImpl implements BPjsIDECacheManager {

    private static final int ONE_HOUR = 60 * 60 * 1000;
    private static final int BP_JS_PROGRAM_TTL = 3;     // hours     // todo: ask gera/achia/michael for threshold

    private final Map<String, BPJsDebugger> bpDebugProgramsByUsers = new ConcurrentHashMap<>();
    private static final Map<String, BProgramRunner> bpRunProgramsByUsers = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> bpUsersLastOperationTime = new ConcurrentHashMap<>();

    @Override
    public void addNewRunExecution(String userId, BProgramRunner bProgramRunner) {
        bpRunProgramsByUsers.put(userId, bProgramRunner);
    }

    @Override
    public void addNewDebugExecution(String userId, BPJsDebugger bpProgramDebugger) {
        bpDebugProgramsByUsers.put(userId, bpProgramDebugger);
    }

    @Override
    public void updateLastOperationTime(String userId) {
        bpUsersLastOperationTime.put(userId, getCurrentLocalDateTime());
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
}
