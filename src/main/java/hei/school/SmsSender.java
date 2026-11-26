package hei.school;

/**
 * Sends a text message. The grading code only knows this interface, not the SMS provider.
 */
public interface SmsSender {

    void send(String phoneNumber, String message);
}
