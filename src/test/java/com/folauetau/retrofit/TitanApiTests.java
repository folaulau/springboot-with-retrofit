package com.folauetau.retrofit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

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

public class TitanApiTests {

    ObjectMapper objectMapper = getObjectMapper();
    TitanRestApi titanRestApi = new TitanRestApi();

    Map<String, Integer> collectionMap = new ConcurrentHashMap<>();

    private String fileStoragePath = "json_files";

    @Test
    void downloadTitanCollections() throws JsonProcessingException {

        File directory = new File(fileStoragePath);

        if (directory.exists()) {
            directory.delete();
        }

        directory.mkdirs();

        System.out.println("downloading...");

        // parent collection
        String collectionId = "22e3232e384c8650311b2940336759a89b8d6f8b";

        // track how long this takes
        long startTime = System.currentTimeMillis();
        getCollectionDetails(null, collectionId);

        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);

        long minutes = duration / 60000;
        System.out.println("Duration: " + minutes + " minutes");

    }

    CollectionDetails getCollectionDetails(CollectionDetails parent, String collectionId) {

        if(collectionId == null || collectionId.trim().isEmpty()) {
            return null;
        }

        TitanApiResponse titanApiResponse = titanRestApi.getCollection(collectionId);
//        System.out.println("titanApiResponse: " + toJson(titanApiResponse));

        if(titanApiResponse == null || titanApiResponse.getResult() == null) {
            return null;
        }

        CollectionDetails collectionDetails = titanApiResponse.getResult();

        if(collectionMap.containsKey(collectionDetails.getCollectionID())) {
            System.out.println("Collection already processed: " + collectionDetails.getCollectionID());
        }else {
            collectionMap.put(collectionDetails.getCollectionID(), 1);
        }

        if(parent != null) {
            collectionDetails.setParentCollectionID(parent.getCollectionID());
            collectionDetails.setParentCollectionPath(parent.getPath());
        }

        boolean hasChildren = collectionDetails.getChildren() != null && collectionDetails.getChildren().size() > 0;

        if(hasChildren) {
            int size = collectionDetails.getChildren().size();
            for (int i = 0; i < size; i++) {
                Child child = collectionDetails.getChildren().get(i);
                String childFileName = child.getCollectionID().toLowerCase()+".json";
//                if(Files.exists(Paths.get(fileStoragePath+"/"+childFileName))) {
//                    System.out.println("File already exists: " + childFileName+", path: "+child.getPath());
//                }

                child.setFileName(childFileName);
            }
        }

        String fileName = collectionDetails.getCollectionID().toLowerCase()+".json";

        collectionDetails.setFileName(fileName);

        try {
            // Write the object to a JSON file
            File jsonFile  = new File(fileStoragePath+"/"+fileName);
            objectMapper.writeValue(jsonFile, collectionDetails);
//            System.out.println("JSON file created successfully: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(hasChildren) {
            int size = collectionDetails.getChildren().size();
            for (int i = 0; i < size; i++) {
                Child child = collectionDetails.getChildren().get(i);
                getCollectionDetails(collectionDetails, child.getCollectionID());
            }
        }

        return collectionDetails;
    }

    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
