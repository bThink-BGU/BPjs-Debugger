package il.ac.bgu.se.bp;

import il.ac.bgu.se.bp.config.IDECommonTestConfiguration;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features")
@ContextConfiguration(classes = IDECommonTestConfiguration.class)
public class CucumberRunnerTest {

}
