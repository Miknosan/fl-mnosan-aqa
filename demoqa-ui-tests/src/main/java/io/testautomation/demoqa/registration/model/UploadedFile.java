package io.testautomation.demoqa.registration.model;

import java.util.Arrays;
import java.util.Objects;

public record UploadedFile(String name, String mimeType, byte[] content) {
    public UploadedFile {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Uploaded file name must not be blank");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("Uploaded file MIME type must not be blank");
        }
        Objects.requireNonNull(content, "content");
        if (content.length == 0) {
            throw new IllegalArgumentException("Uploaded file content must not be empty");
        }
        content = Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }
}
