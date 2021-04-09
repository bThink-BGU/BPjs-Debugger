Feature: Breakpoints
  Scenario Outline: multiple valid breakpoints
    Given I have connected to websocket with <sessionId> and <userId>
    When I ask to debug with filename <filename> and toggleMuteBreakpoints <toggleMuteBreakpoints> and toggleMuteSyncPoints <toggleMuteSyncPoints> and breakpoints <breakpoints>
    Then The response should be true with errorCode null
    And wait until breakpoint reached
    And I should get notification with BThread <bThreadNameByBreakpoint>, doubles <doubleVariables>, strings <stringVariables> and breakpoint lines <breakpoints>
    When I click on continue
    Then The response should be true with errorCode null
    And wait until breakpoint reached
    And I should get notification with BThread <bThreadNameByBreakpoint>, doubles <doubleVariables>, strings <stringVariables> and breakpoint lines <breakpoints>
    When I click on continue
    Then The response should be true with errorCode null
    And wait until breakpoint reached
    And I should get notification with BThread <bThreadNameByBreakpoint>, doubles <doubleVariables>, strings <stringVariables> and breakpoint lines <breakpoints>
    And verify all breakpoints were reached

    Examples:
      | sessionId  | userId                               | filename  | toggleMuteBreakpoints | toggleMuteSyncPoints | bThreadNameByBreakpoint                     | breakpoints | stringVariables | doubleVariables                                                           |
      | sessionId1 | 4655ae8e-cdfe-4ce3-ac2b-dc03743a780f | testFile1 | false                 | true                 | {10:bt-world-son}{21:bt-world,bt-world-son} | 10,21       | {10:z=alex}     | {10:x=5.0,y=16.7}{21:m=50,n=100,p=150} |
      | sessionId2 | d1b10451-bbb0-43e2-b5ad-d10afa73449e | testFile2 | false                 | true                 | {4:Thread1}{9:Thread2}{11:Thread2}          | 4,9,11      | {11:var2=alex}  | {4:t=6.0}{9:varT2B=1}{11:varT2B=1}     |
