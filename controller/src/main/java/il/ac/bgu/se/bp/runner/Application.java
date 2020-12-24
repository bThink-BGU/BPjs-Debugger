package il.ac.bgu.se.bp.runner;


import il.ac.bgu.se.bp.logger.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages="il.ac.bgu.se.bp")
public class Application {
    private static final Logger logger = new Logger(Application.class);

    public static void main(String[] args) {
        logger.info("Spring program is starting..");
        SpringApplication.run(Application.class, args);
    }
}
