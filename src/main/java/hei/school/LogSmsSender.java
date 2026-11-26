package hei.school;

import org.springframework.stereotype.Component;

/**
 * Simulated SMS provider: writes the message in the logs. Replace this class once a provider is chosen.
 */
@Component
public class LogSmsSender implements SmsSender {

    @Override
    public void send(String phoneNumber, String message) {
        System.out.println("[SMS] " + phoneNumber + " : " + message);
    }
}
