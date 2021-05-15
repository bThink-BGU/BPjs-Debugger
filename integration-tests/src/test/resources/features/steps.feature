Feature: Steps

  Scenario: step into, step over, step out
    Given user alex has connected with userId 4655ae8e-cdfe-4ce3-ac2b-dc03743a780f
    And alex has connected to websocket with alex-session

    When alex asks to debug with filename testFile3 and toggleMuteBreakpoints false and toggleMuteSyncPoints true and toggleWaitForExternalEvent false and breakpoints 3,10,-1,50
    Then The debug response should be true with errorCode null and breakpoints 3,10 for user alex
    And wait until user alex has reached status breakpoint
    And alex should get notification with BThread bt-world on line 3, envs [{FUNCNAME:BTMain, LINENUMBER:3}] and breakpoint lines 3,10

    When alex clicks on step into
    Then The response should be true with errorCode null
    And wait until user alex has reached status breakpoint
    And alex should get notification with BThread bt-world on line 6, envs [{FUNCNAME:foo, LINENUMBER:6, bt:1.0, m:null, n:null, p:null, t:null}, {FUNCNAME:BTMain,LINENUMBER:3}] and breakpoint lines 3,10

    When alex clicks on step over
    Then The response should be true with errorCode null
    And wait until user alex has reached status breakpoint
    And alex should get notification with BThread bt-world on line 7, envs [{FUNCNAME:foo, LINENUMBER:7, bt:1.0, m:null, n:null, p:null, t:null},{FUNCNAME:BTMain,LINENUMBER:3}] and breakpoint lines 3,10

    When alex clicks on step over
    Then The response should be true with errorCode null
    And wait until user alex has reached status breakpoint
    And alex should get notification with BThread bt-world on line 8, envs [{FUNCNAME:foo, LINENUMBER:8, bt:1.0, m:50.0, n:null, p:null, t:null},{FUNCNAME:BTMain,LINENUMBER:3}] and breakpoint lines 3,10

    When alex clicks on continue
    Then The response should be true with errorCode null
    And wait until user alex has reached status breakpoint
    And alex should get notification with BThread bt-world on line 10, envs [{FUNCNAME:foo, LINENUMBER:10, bt:1.0, m:50.0, n:100.0, p:150.0, t:null},{FUNCNAME:BTMain,LINENUMBER:3}] and breakpoint lines 3,10

    When alex clicks on step into
    Then The response should be true with errorCode null
    And wait until user alex has reached status breakpoint
    And alex should get notification with BThread bt-world on line 14, envs [{FUNCNAME:goo, LINENUMBER:14, g1:null, g2:null, g3: null},{FUNCNAME:foo, LINENUMBER:10, bt:1.0, m:50.0, n:100.0, p:150.0, t:null},{FUNCNAME:BTMain,LINENUMBER:3}] and breakpoint lines 3,10

    When alex clicks on step over
    Then The response should be true with errorCode null
    And wait until user alex has reached status breakpoint
    And alex should get notification with BThread bt-world on line 15, envs [{FUNCNAME:goo,LINENUMBER:15 , g1:null, g2:null, g3: null},{FUNCNAME:foo, LINENUMBER:10, bt:1.0, m:50.0, n:100.0, p:150.0, t:null},{FUNCNAME:BTMain,LINENUMBER:3}] and breakpoint lines 3,10

    When alex clicks on step over
    Then The response should be true with errorCode null
    And wait until user alex has reached status breakpoint
    And alex should get notification with BThread bt-world on line 16, envs [{FUNCNAME:goo,LINENUMBER:16, g1:50.0, g2:null, g3: null},{FUNCNAME:foo, LINENUMBER:10,bt:1.0, m:50.0, n:100.0, p:150.0, t:null},{FUNCNAME:BTMain,LINENUMBER:3}] and breakpoint lines 3,10

    When alex clicks on step out
    Then The response should be true with errorCode null
    And wait until user alex has reached status breakpoint
    And alex should get notification with BThread bt-world on line 11, envs [{FUNCNAME:foo, LINENUMBER:11, bt:1.0, m:50.0, n:100.0, p:150.0, t:null},{FUNCNAME:BTMain,LINENUMBER:3}] and breakpoint lines 3,10

    When alex clicks on step out
    Then The response should be true with errorCode null
    And wait until program of user alex is over