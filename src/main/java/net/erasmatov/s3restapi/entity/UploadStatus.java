package net.erasmatov.s3restapi.entity;

import lombok.Data;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.HashMap;
import java.util.Map;

@Data
public class UploadStatus {

    private final String fileKey;
    private final String contentType;
    private String uploadId;
    private int partCounter;
    private int buffered;
    private Map<Integer, CompletedPart> completedParts = new HashMap<>();

    public void addBuffered(int buffered) {
        this.buffered += buffered;
    }

    public int getAddedPartCounter() {
        return ++this.partCounter;
    }
}
