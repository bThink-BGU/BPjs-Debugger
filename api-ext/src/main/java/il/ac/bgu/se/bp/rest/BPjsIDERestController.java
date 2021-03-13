package il.ac.bgu.se.bp.rest;

import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;

import java.security.Principal;

public interface BPjsIDERestController {

    void subscribeUser(String sessionId, Principal principal);

    BooleanResponse run(String sessionId, RunRequest code);

    BooleanResponse debug(String sessionId, DebugRequest code);


}
