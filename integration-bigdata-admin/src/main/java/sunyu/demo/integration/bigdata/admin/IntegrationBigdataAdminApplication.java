package sunyu.demo.integration.bigdata.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
public class IntegrationBigdataAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationBigdataAdminApplication.class, args);
    }

}
