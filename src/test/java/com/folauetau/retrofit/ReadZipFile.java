package com.folauetau.retrofit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.folauetau.retrofit.dto.Child;
import com.folauetau.retrofit.dto.CollectionDetails;
import com.folauetau.retrofit.dto.TitanApiResponse;
import com.folauetau.retrofit.rest.TitanRestApi;
import org.junit.jupiter.api.Test;

import static java.util.Collections.*;

public class ReadZipFile {


    private String zipFolder = "json_files";

    private String zipFolderFile = zipFolder+".zip";

    private static volatile int count = 0;

    private final ObjectMapper MAPPER = getObjectMapper();

    private List<CollectionDetails> collections = new ArrayList<>();


    String outputDirPath = "output";

    @Test
    void readZipFile() throws IOException {

        File directory = new File(zipFolderFile);

        Path outputDir = Paths.get(outputDirPath);

        // Create output directory if it doesn't exist
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Unzip the file
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFolderFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outputFilePath = outputDir.resolve(entry.getName());

                // Handle directories
                if (entry.isDirectory()) {
                    Files.createDirectories(outputFilePath);
                } else {
                    // Write file to the output directory
                    Files.createDirectories(outputFilePath.getParent()); // Ensure parent directories exist
                    try (OutputStream os = Files.newOutputStream(outputFilePath)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zis.closeEntry();
            }
        }

        // Process each file in the unzipped folder
        try {
            Files.walk(outputDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    System.out.println("Processing file: " + file);
                    try {
                        readFileContent(file);
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + file + " - " + e.getMessage());
                    }
                });
        } catch (IOException e) {
            throw new IOException("Error walking through output directory: " + e.getMessage(), e);
        }
    }

    public static void readFileContent(Path filePath) throws IOException {
        // Read and print the file content
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    @Test
    void readZipFileZipFiless() throws IOException {
        String targetFileName = "root.json";
        try (ZipFile zipFile = new ZipFile(zipFolderFile)) {
            ZipEntry rootJsonEntry = zipFile.getEntry(zipFolder+"/"+targetFileName);

            if (rootJsonEntry != null) {
                System.out.println("Found " + targetFileName);
                try (InputStream is = zipFile.getInputStream(rootJsonEntry)) {
                    String content = readFileContent(is);
                    System.out.println("content: " + content);
                }
            } else {
                System.out.println(targetFileName + " not found in the ZIP file.");
            }
        } catch (IOException e) {
            System.err.println("Error reading ZIP file: " + e.getMessage());
        }
    }


    @Test
    void readZipFileZipFiles() throws IOException {

        try (ZipFile zipFile = new ZipFile(zipFolderFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                System.out.println("File: " + entry.getName());

                // Check if it's not a directory
                if (!entry.isDirectory()) {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        String content = readFileContent(is);
                        System.out.println("content: " + content);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading ZIP file: " + e.getMessage());
        }
    }

    private String readFileContent(InputStream is) throws IOException {
        System.out.println("Reading file content...");
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                content.append(line).append("\n");
            }
        }
        System.out.println("Reading file content done!");

        CollectionDetails collectionDetails = MAPPER.readValue(content.toString(), CollectionDetails.class);

        System.out.println("collectionDetails: " + collectionDetails);

        return content.toString();
    }

    private void fetchAll(Path directory) {

//        try (Stream<Path> paths = Files.walk(directory)) {
//            paths.filter(Files::isRegularFile)
//                .forEach(System.out::println);
//
//        } catch (IOException e) {
//            System.err.println("Error reading directory: " + e.getMessage());
//        }

        String rootFileName = "root.json";
        System.out.println("directory: " + directory);
        fetchCollection(directory, rootFileName);

    }
    private void fetchCollection(Path directory, String fileName) {
        Path currentFile = directory.resolve(fileName);
        System.out.println("currentFile: " + currentFile);
        CollectionDetails collectionDetails = null;
        try {
            collectionDetails = MAPPER.readValue(
                Files.newBufferedReader(currentFile),
                CollectionDetails.class);

            collections.add(collectionDetails);

            System.out.println("Collection: " + collectionDetails);

        } catch (IOException e) {
            System.err.println("Error reading or processing file: " + currentFile);
            e.printStackTrace();
        }

        if (collectionDetails == null) {
            return;
        }

        try {
            List<Child> children = collectionDetails.getChildren();

            if (children != null && !children.isEmpty()) {
                for (Child child : children) {
//                    fetchCollection(directory, child.getFileName());
                }
            }

        } catch (Exception e) {
            System.err.println("Error reading or processing file: " + currentFile);
            e.printStackTrace();
        }
    }


    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        // Deserialization
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Date and Time Format
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US));
        // format LocalDate and LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }
}
