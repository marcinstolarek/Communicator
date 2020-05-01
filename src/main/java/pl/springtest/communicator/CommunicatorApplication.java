package pl.springtest.communicator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Version: 1.0.0
 * Author: Marcin Stolarek
 * Communicator application
 */
@SpringBootApplication
public class CommunicatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunicatorApplication.class, args);
    }
}

//TODO - create sockets list (multiple sockets immediately) - to do test
//TODO - send messages from client to others clients