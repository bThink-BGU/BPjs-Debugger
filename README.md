# BP JS IDE
The project contains 5 Modules:
- api-ext - introduces the projects interfaces
- api-int - introduces the projects internal components
- bpjs-debugger - the debugger engine
- service - manipulates clients and their running sessions - run/debug sessions
- controller - responsible for the incoming HTTP/WS requests and messages

## Running the CLI Debugger
- under bpjs-debugger:
    - run il.ac.bgu.se.bp.mains.BPJsDebuggerCliRunner 

## Running the server
IntelliJ:
- choose both clean and install on maven tool window and run
- under controller module:
    - run il.ac.bgu.se.bp.app.Application 

Terminal:
- cd controller
- mvn spring-boot:run


---

## Using the server

Base URL is localhost:8080/bpjs

Supported endpoints:

| Command                     | Endpoint           | Method | Parameters                                                                                             |
| --------------------------- | ------------------ | ------ | ------------------------------------------------------------------------------------------------------ |
| Run                         | bpjs/run           | POST   | {sourceCode: String}                                                                                   |
| Debug                       | bpjs/debug         | POST   | {sourceCode: String, breakpoints: int[], skipBreakpointsToggle: boolean, skipSyncStateToggle: boolean} |
| Add / remove Breakpoint     | bpjs/breakpoint    | POST   | {lineNumber: number, stopOnBreakpoint: boolean}                                                        |
| Toggle Mute Breakpoints     | bpjs/breakpoint    | PUT    | {skipBreakpoints: boolean}                                                                             |
| Toggle Mute Sync States     | bpjs/syncStates    | PUT    | {skipSyncStates: boolean}                                                                              |
| Stop                        | bpjs/stop          | GET    | None                                                                                                   |
| Step Out                    | bpjs/stepOut       | GET    | None                                                                                                   |
| Step Into                   | bpjs/stepInto      | GET    | None                                                                                                   |
| Step Over                   | bpjs/stepOver      | GET    | None                                                                                                   |
| Continue                    | bpjs/continue      | GET    | None                                                                                                   |
| Next Sync                   | bpjs/nextSync      | GET    | None                                                                                                   |
| Add / Remove External Event | bpjs/externalEvent | POST   | {externalEvent: String, addEvent: boolean}                                                             |
| Set Sync Snapshot           | bpjs/syncSnapshot  | POST   | {snapShotTime: long}                                                                                   |


[BP JS Framework](http://wwww.bpjside.tk)
