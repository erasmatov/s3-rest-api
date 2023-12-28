package net.erasmatov.s3restapi.exception;

public class FileValidatorException extends ApiException {
    public FileValidatorException(String message) {
        super(message, "FILE_INVALID");
    }
}
