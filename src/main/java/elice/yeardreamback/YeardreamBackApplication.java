package elice.yeardreamback;

import elice.yeardreamback.oauth.oauth2.OAuth2Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OAuth2Properties.class)
public class YeardreamBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(YeardreamBackApplication.class, args);
	}

}
