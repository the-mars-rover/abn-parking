package abn.parking.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tech.ailef.snapadmin.external.SnapAdminAutoConfiguration;

@SpringBootApplication
@ImportAutoConfiguration(SnapAdminAutoConfiguration.class)
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

}
