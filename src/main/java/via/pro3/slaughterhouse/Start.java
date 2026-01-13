package via.pro3.slaughterhouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Start {

  private static final Logger log = LoggerFactory.getLogger(Start.class);

  public static void main(String[] args) {
    SpringApplication.run(Start.class, args);
    }
}
