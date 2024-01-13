package net.erasmatov.s3restapi.exception;

public class UnloadedException extends ApiException {
    public UnloadedException(String message) {
        super(message, "UNLOADED");
    }
}
