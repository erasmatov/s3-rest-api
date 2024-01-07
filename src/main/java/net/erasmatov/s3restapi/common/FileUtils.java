package net.erasmatov.s3restapi.common;


import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.erasmatov.s3restapi.exception.FileValidatorException;
import net.erasmatov.s3restapi.exception.UploadException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import software.amazon.awssdk.core.SdkResponse;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@UtilityClass
@Slf4j
public class FileUtils {

    @Value("${content.types}")
    private String[] contentTypes;

    public void checkSdkResponse(SdkResponse sdkResponse) {
        if (AwsSdkUtil.isErrorSdkHttpResponse(sdkResponse)) {
            throw new UploadException(MessageFormat.format("{0} - {1}", sdkResponse.sdkHttpResponse().statusCode(), sdkResponse.sdkHttpResponse().statusText()));
        }
    }

    public ByteBuffer dataBufferToByteBuffer(List<DataBuffer> buffers) {
        log.info("Creating ByteBuffer from {} chunks", buffers.size());

        int partSize = 0;
        for (DataBuffer b : buffers) {
            partSize += b.readableByteCount();
        }

        ByteBuffer partData = ByteBuffer.allocate(partSize);
        buffers.forEach(buffer -> buffer.toByteBuffer(partData));

        partData.rewind();

        log.info("PartData: capacity={}", partData.capacity());
        return partData;
    }

    public void filePartValidator(FilePart filePart) {
        if (isEmpty(filePart)) {
            throw new FileValidatorException("File cannot be empty or null!");
        }
        if (!isValidType(filePart)) {
            throw new FileValidatorException("Invalid file type");
        }
    }

    private boolean isValidType(final FilePart filePart) {
        return isSupportedContentType(Objects.requireNonNull(filePart.headers().getContentType()).toString());
    }

    private boolean isEmpty(final FilePart filePart) {
        return StringUtils.isEmpty(filePart.filename())
                && ObjectUtils.isEmpty(filePart.headers().getContentType());
    }

    private boolean isSupportedContentType(final String contentType) {
        return Arrays.asList(contentTypes).contains(contentType);
    }
}
