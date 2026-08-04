package com.maahish.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * Prevents accidental production deployments with the local profile or missing profile in packaged JARs.
 */
@Slf4j
@Component
public class DeploymentProfileValidator implements ApplicationRunner {

    private final Environment environment;

    public DeploymentProfileValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String[] profiles = environment.getActiveProfiles();
        boolean prodProfile = Arrays.asList(profiles).contains("prod");
        boolean localProfile = Arrays.asList(profiles).contains("local");
        String razorpayKeyId = environment.getProperty("maahish.razorpay.key-id", "");

        if (isRunningFromJar() && !prodProfile) {
            throw new IllegalStateException(
                    "Packaged deployment requires SPRING_PROFILES_ACTIVE=prod. Active profiles: "
                            + Arrays.toString(profiles));
        }

        if (StringUtils.hasText(razorpayKeyId) && razorpayKeyId.startsWith("rzp_live_") && !prodProfile) {
            throw new IllegalStateException(
                    "Live Razorpay keys require spring.profiles.active=prod");
        }

        if (localProfile && prodProfile) {
            throw new IllegalStateException("Cannot activate both 'local' and 'prod' profiles simultaneously");
        }

        log.debug("Active profiles: {}", Arrays.toString(profiles));
    }

    private boolean isRunningFromJar() {
        String resource = DeploymentProfileValidator.class.getName().replace('.', '/') + ".class";
        return getClass().getClassLoader().getResource(resource) != null
                && "jar".equals(getClass().getClassLoader().getResource(resource).getProtocol());
    }
}
