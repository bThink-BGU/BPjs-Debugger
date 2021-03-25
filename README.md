# BP JS IDE
The project contains 5 Modules:
- api-ext - introduces the projects interfaces
- api-int - introduces the projects internal components
- bpjs-debugger - the debugger engine
- service - manipulates clients and their running sessions - run/debug sessions
- controller - responsible for the incoming HTTP/WS requests and messages


## Run server
IntelliJ:
- choose both clean and install on maven tool window and run
- under controller module:
    - run il.ac.bgu.se.bp.app.Application 

Terminal:
- cd controller
- mvn spring-boot:run

## Run CLI Debugger
- under bpjs-debugger:
    - run il.ac.bgu.se.bp.mains.BPJsDebuggerCliRunner 



---

[BP JS Framework](http://wwww.bpjside.tk)
