Feature: Breakpoints
  Scenario Outline: multiple valid breakpoints
    Given I have connected to websocket with <sessionId> and <userId>
    When I ask to debug with filename <filename> and toggleMuteBreakpoints <toggleMuteBreakpoints> and toggleMuteSyncPoints <toggleMuteSyncPoints> and breakpoints <breakpoints>
    Then wait until breakpoint reached
    And The response should be true with errorCode null
    And I should get notification with doubles <doubleVariables> and strings <stringVariables>

    Examples:
      | sessionId  | userId                               | filename  | toggleMuteBreakpoints | toggleMuteSyncPoints | breakpoints | doubleVariables | stringVariables |
      | sessionId1 | 4655ae8e-cdfe-4ce3-ac2b-dc03743a780f | testFile1 | false                 | true                 | 10          | x=5.0,y=16.7    | z=alex          |
      | sessionId2 | d1b10451-bbb0-43e2-b5ad-d10afa73449e | testFile2 | false                 | true                 | 4           | t=6.0           |                 |
