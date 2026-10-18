
package com.coursehub.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonFileStore<T> {

    private static final ObjectMapper MAPPER =
            new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path filePath;
    private final TypeReference<List<T>> listType;

    public JsonFileStore(
            Path filePath,
            TypeReference<List<T>> listType) {

        this.filePath = Objects.requireNonNull(filePath);
        this.listType = Objects.requireNonNull(listType);
    }

    // Read every record from the JSON file
    public synchronized List<T> readAll() {
        try {
            Path parent = filePath.toAbsolutePath().getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            // Create an empty JSON array if the file is missing
            if (Files.notExists(filePath) ||
                    Files.size(filePath) == 0) {

                writeAll(new ArrayList<>());
                return new ArrayList<>();
            }

            List<T> records =
                    MAPPER.readValue(filePath.toFile(), listType);

            return records == null
                    ? new ArrayList<>()
                    : new ArrayList<>(records);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not read JSON file: " + filePath, e);
        }
    }

    // Replace the JSON file with the supplied records
    public synchronized void writeAll(List<T> records) {
        Objects.requireNonNull(records, "Records cannot be null");

        Path temporaryFile = filePath.resolveSibling(
                filePath.getFileName().toString() + ".tmp");

        try {
            Path parent = filePath.toAbsolutePath().getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            // Write to a temporary file first
            MAPPER.writeValue(temporaryFile.toFile(), records);

            // Replace the original file
            try {
                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not write JSON file: " + filePath, e);
        }
    }
}
