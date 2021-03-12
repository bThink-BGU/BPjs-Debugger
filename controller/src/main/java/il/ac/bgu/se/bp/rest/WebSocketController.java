package il.ac.bgu.se.bp.rest;

//@Controller
//public class WebSocketController {
//
//    private final GreetingService greetingService;
//
//    WebSocketController(GreetingService greetingService) {
//        this.greetingService = greetingService;
//    }
//
//    @MessageMapping("/hello")
//    @SendToUser("/topic/greetings")
//    public Greeting greeting(HelloMessage message, @Header("simpSessionId") String sessionId, Principal principal) throws Exception {
//        System.out.println("Received greeting message {0} from {1} with sessionId {2}" + message+ ",," + principal.getName() +","+ sessionId);
//        greetingService.addUserName(principal.getName());
//        Thread.sleep(1000); // simulated delay
//        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
//    }

//}
