package org.jboss.as.paas.controller.dmr;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Message {

    private String message;
    private FireOn fireOn;
    private String[] required;

    public Message(String message, FireOn fireOn, String[] required) {
        this.message = message;
        this.fireOn = fireOn;
        this.required = required;
    }

    public String[] getRequired() {
        return required;
    }

    public String getMessage() {
        return message;
    }

    public boolean fireOnExecute() {
        return FireOn.EXECUTED.equals(fireOn);
    }

    public boolean fireOnFailed() {
        return FireOn.FAILED.equals(fireOn);
    }

    public enum FireOn {
        EXECUTED, FAILED;
    }

}
