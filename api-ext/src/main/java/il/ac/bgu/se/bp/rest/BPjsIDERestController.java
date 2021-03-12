package il.ac.bgu.se.bp.rest;

import il.ac.bgu.se.bp.ExecuteBPjsResponse;
import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

import java.security.Principal;

public interface BPjsIDERestController {

    BooleanResponse subscribeUser(String sessionId, Principal principal);

    ExecuteBPjsResponse run(RunRequest code);

    ExecuteBPjsResponse debug(DebugRequest code);


}
