package il.ac.bgu.se.bp.rest;

import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.service.BPjsIDEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static il.ac.bgu.se.bp.rest.utils.Constants.SIMP_SESSION_ID;
import static il.ac.bgu.se.bp.rest.utils.Endpoints.*;


@Controller
@RequestMapping(BASE_URI)
public class BPjsIDERestControllerImpl implements BPjsIDERestController {

    @Autowired
    private BPjsIDEService bPjsIDEService;

    @MessageMapping(SUBSCRIBE)
    public @ResponseBody
    void subscribeUser(@Header(SIMP_SESSION_ID) String sessionId, Principal principal) {
        System.out.println("userId: " + principal.getName());
        bPjsIDEService.subscribeUser(sessionId, principal.getName());
    }

    @Override
    @RequestMapping(value = RUN, method = RequestMethod.POST)
    public @ResponseBody
    BooleanResponse run(@RequestHeader("userId") String userId, @RequestBody RunRequest code) {
        return bPjsIDEService.run(code, userId);
    }

    @Override
    @RequestMapping(value = DEBUG, method = RequestMethod.POST)
    public @ResponseBody
    BooleanResponse debug(@RequestHeader("userId") String userId, @RequestBody DebugRequest code) {
        return bPjsIDEService.debug(code, userId);
    }
}
