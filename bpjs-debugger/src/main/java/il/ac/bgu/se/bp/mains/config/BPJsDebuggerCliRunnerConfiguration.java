package il.ac.bgu.se.bp.mains.config;

import il.ac.bgu.se.bp.mains.BPJsDebuggerCliRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BPJsDebuggerCliRunnerConfiguration {

    @Bean
    public BPJsDebuggerCliRunner bpJsDebuggerCliRunner() {
        return new BPJsDebuggerCliRunner();
    }
}
