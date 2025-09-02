package ee.ng.events.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.cookie")
public class CookieConfig {
    private String name;
    private boolean secure;
    private boolean httpOnly;
    private String sameSite;
    private long maxAgeMinutes;
}
