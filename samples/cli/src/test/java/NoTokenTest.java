import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

import org.junit.jupiter.api.Test;

@QuarkusMainTest
@TestProfile(NoTokenTestProfile.class)
public class NoTokenTest {

    @Test
    @Launch(value = {}, exitCode = 0)
    public void testLaunchShowsUsage(LaunchResult result) {
        assertThat(result.getOutput()).contains("Usage: hassq");
    }
}
