package net.erasmatov.s3restapi.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class UploadState {

    private final String fileKey;
    private final String contentType;
    private String uploadId;
    private int partCounter;
    private int buffered = 0;

    public void addBuffered(int buffered) {
        this.buffered += buffered;
    }

    public int getAddedPartCounter() {
        return ++this.partCounter;
    }
}
