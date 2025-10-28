package elice.yeardreamback.oauth.oauth2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.app.oauth2")
public class OAuth2Properties {
    private String frontendRedirectUri;
}
