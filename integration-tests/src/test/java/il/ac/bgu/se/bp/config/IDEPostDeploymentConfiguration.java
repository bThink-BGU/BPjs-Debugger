package il.ac.bgu.se.bp.config;

import il.ac.bgu.se.bp.mocks.testService.RestServiceTestHelper;
import il.ac.bgu.se.bp.mocks.testService.TestService;
import il.ac.bgu.se.bp.service.manage.SessionHandler;
import il.ac.bgu.se.bp.service.manage.SessionHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("PDT")
public class IDEPostDeploymentConfiguration {

    @Bean
    TestService serviceTestHelper() {
        return new RestServiceTestHelper();
    }

    @Bean
    SessionHandler sessionHandler() {
        return new SessionHandlerImpl();
    }
}
