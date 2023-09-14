package dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions;

public class ProxyErrorException extends Exception {

    public ProxyErrorException(Throwable cause) {
        super(cause);
    }

    public ProxyErrorException(String message) {
        super(message);
    }
}
