package il.ac.bgu.se.bp.rest;

import il.ac.bgu.se.bp.ExecuteBPjsResponse;
import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.RunRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.socket.GreetingService;
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

    @Autowired
    private GreetingService greetingService;

    @MessageMapping("/subscribe")
    public BooleanResponse subscribeUser(@Header("simpSessionId") String sessionId, Principal principal) {
        greetingService.addUserName(principal.getName());
        return bPjsIDEService.subscribeUser(sessionId, principal.getName());
    }

    private void sleep() {
        try {
            Thread.sleep(1000); // simulated delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    public @ResponseBody
    ExecuteBPjsResponse run(@RequestBody RunRequest code) {
        return bPjsIDEService.run(code);
    }

    @Override
    @RequestMapping(value = "/debug", method = RequestMethod.POST)
    public @ResponseBody
    ExecuteBPjsResponse debug(@RequestBody DebugRequest code) {
        return bPjsIDEService.debug(code);
    }
}
