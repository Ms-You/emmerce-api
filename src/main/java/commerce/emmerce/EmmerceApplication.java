package commerce.emmerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling	// 스케줄링 활성화
@SpringBootApplication
public class EmmerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmmerceApplication.class, args);
	}

}
