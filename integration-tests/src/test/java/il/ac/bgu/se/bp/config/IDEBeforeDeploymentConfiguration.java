package il.ac.bgu.se.bp.config;

import il.ac.bgu.se.bp.mocks.MockSessionHandler;
import il.ac.bgu.se.bp.mocks.testService.ControllerTestHelper;
import il.ac.bgu.se.bp.mocks.testService.TestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("BDT")
public class IDEBeforeDeploymentConfiguration {
    @Bean
    TestService testService() {
        return new ControllerTestHelper();
    }

    @Bean
    MockSessionHandler sessionHandler() {
        return new MockSessionHandler();
    }

}
