package il.ac.bgu.se.bp.rest;

import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.service.BPjsIDEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@RequestMapping("/bpjs")
public class BPjsIDERestControllerImpl implements BPjsIDERestController {

    @Autowired
    private BPjsIDEService bPjsIDEService;

    @MessageMapping("/subscribe")
    public @ResponseBody
    void subscribeUser(@Header("simpSessionId") String sessionId, Principal principal) {
        System.out.println("sessionId: " + sessionId);
        System.out.println("userId: " + principal.getName());

        bPjsIDEService.subscribeUser(sessionId, principal.getName());
    }

    @Override
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public @ResponseBody
    BooleanResponse run(@Header("simpSessionId") String sessionId, @RequestBody RunRequest code) {
        return bPjsIDEService.run(code, sessionId);
    }

    @Override
    @RequestMapping(value = "/debug", method = RequestMethod.POST)
    public @ResponseBody
    BooleanResponse debug(@Header("simpSessionId") String sessionId, @RequestBody DebugRequest code) {
        return bPjsIDEService.debug(code, sessionId);
    }
}
