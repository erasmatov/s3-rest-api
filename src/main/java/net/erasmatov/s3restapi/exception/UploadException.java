package net.erasmatov.s3restapi.exception;

public class UploadException extends ApiException {
    public UploadException(String message) {
        super(message, "UNLOADED");
    }
}
