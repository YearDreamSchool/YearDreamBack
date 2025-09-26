package elice.yeardreamback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class YeardreamBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(YeardreamBackApplication.class, args);
	}

}
