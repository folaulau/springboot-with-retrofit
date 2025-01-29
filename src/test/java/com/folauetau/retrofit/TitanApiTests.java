package com.folauetau.retrofit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
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
import com.folauetau.retrofit.dto.AssetWrapper;
import com.folauetau.retrofit.dto.Child;
import com.folauetau.retrofit.dto.CollectionDetails;
import com.folauetau.retrofit.dto.TitanApiResponse;
import com.folauetau.retrofit.rest.TitanRestApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TitanApiTests {

    ObjectMapper objectMapper = getObjectMapper();
    TitanRestApi titanRestApi = new TitanRestApi();

    Map<String, Integer> collectionMap = new ConcurrentHashMap<>();
    Map<String, String> englishCollectionIds = new ConcurrentHashMap<>();

    private String fileStoragePath = "json_files";

    private static volatile int count = 0;

    private void addCollectionUri(String collectionUri, String collectionId) {
        if (collectionUri == null || collectionUri.trim().isEmpty()) {
            log.info("Collection URI is null or empty: " + collectionUri);
            return;
        }
        collectionUri = collectionUri.trim().toLowerCase();
        if (englishCollectionIds.containsKey(collectionUri)) {
            log.info("Collection URI already exists: " + collectionUri);
        }
        englishCollectionIds.put(collectionUri, collectionId.trim());
    }

    private String getEnglishCollectionId(String collectionUri) {
        if (collectionUri == null || collectionUri.trim().isEmpty()) {
            log.warn("Collection URI is null or empty: " + collectionUri);
            return null;
        }
        String englishCollectionId = englishCollectionIds.get(collectionUri.toLowerCase());
        if (englishCollectionId == null || englishCollectionId.trim().isEmpty()) {
            //            System.out.println("No English collection ID found for collection URI: " + collectionUri);
        } else {
            log.info("English collection ID found for collection URI: " + collectionUri + " - " + englishCollectionId);
        }
        return englishCollectionId;
    }

    @Test
    void downloadTitanCollections() throws JsonProcessingException {

        File directory = new File(fileStoragePath);

        if (directory.exists()) {
            directory.delete();
        }

        directory.mkdirs();

        log.info("downloading...");

        // parent collection
        String collectionId = "22e3232e384c8650311b2940336759a89b8d6f8b";

        // track how long this takes
        long startTime = System.currentTimeMillis();
        getCollectionDetails(null, collectionId, "root.json");

        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);

        long minutes = duration / 60000;
        System.out.println("Duration: " + minutes + " minutes");

    }

    CollectionDetails getCollectionDetails(CollectionDetails parent, String collectionId, String filename) {

        if (collectionId == null || collectionId.trim().isEmpty()) {
            log.info("collectionId is null: " + collectionId);
            return null;
        }

        TitanApiResponse titanApiResponse = titanRestApi.getCollection(collectionId);
        //        System.out.println("titanApiResponse: " + toJson(titanApiResponse));

        if (titanApiResponse == null || titanApiResponse.getResult() == null) {
            log.warn("No collection found for collectionId: " + collectionId);
            return null;
        }

        CollectionDetails collectionDetails = titanApiResponse.getResult();

        //        System.out.println("collectionDetails path: " + collectionDetails.getPath());

        if (collectionMap.containsKey(collectionDetails.getCollectionID())) {
            System.out.println("Collection already processed: " + collectionDetails.getCollectionID());
        } else {
            collectionMap.put(collectionDetails.getCollectionID(), 1);
        }

        if (collectionDetails.isEnglish()) {
            String collectionUri = collectionDetails.getCollectionUri();
            if (collectionUri == null || collectionUri.trim().isEmpty()) {
                log.warn(
                    "Collection URI is null for eng collection ID: {}, path: {}",
                    collectionDetails.getCollectionID(),
                    collectionDetails.getPath());
            } else {
                addCollectionUri(collectionUri, collectionDetails.getCollectionID());
            }
        } else {
            String englishCollectionId = getEnglishCollectionId(collectionDetails.getCollectionUri());
            if (englishCollectionId != null) {
                collectionDetails.setSourceCollectionId(englishCollectionId);
            } else {
                log.info("No English collection ID found for collection URI: " + collectionDetails.getCollectionUri()
                    + ", path: " + collectionDetails.getPath() + ", id: " + collectionDetails.getCollectionID()
                    + ", parent: " + collectionDetails.getParentCollectionID());
                //                System.out.println("No English collection ID found for collection URI: " + toJson(collectionDetails));
            }
        }

        if (parent != null) {
            collectionDetails.setParentCollectionID(parent.getCollectionID());
            collectionDetails.setParentCollectionPath(parent.getPath());
        }

        boolean hasChildren = collectionDetails.getChildren() != null && collectionDetails.getChildren().size() > 0;

        if (hasChildren) {
            collectionDetails.setCollectionType(CollectionDetails.COLLECTIONS_COLLECTION_TYPE);
            int size = collectionDetails.getChildren().size();
            for (int i = 0; i < size; i++) {
                Child child = collectionDetails.getChildren().get(i);
                String childFileName = child.getCollectionID().toLowerCase() + ".json";
                child.setFileName(childFileName);
            }
        }

        List<AssetWrapper> assetWrappers = collectionDetails.getAssets();

        boolean hasAssets = assetWrappers != null && assetWrappers.size() > 0;

        if (!hasChildren) {
            if (hasAssets) {
                collectionDetails.setCollectionType(CollectionDetails.ASSETS_COLLECTION_TYPE);
            } else {
                collectionDetails.setCollectionType(CollectionDetails.COLLECTIONS_COLLECTION_TYPE);
            }
        }

        collectionDetails.setFileName(filename);

        try {
            // Write the object to a JSON file
            File jsonFile = new File(fileStoragePath + "/" + filename);
            objectMapper.writeValue(jsonFile, collectionDetails);
            //            System.out.println("JSON file created successfully: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (count >= 1000) {
            return null;
        }

        count++;

        if (hasChildren) {
            List<Child> children = collectionDetails.getChildren();
            int size = children.size();

            // process english collection first so that translation can work
            children.sort((c1, c2) -> {
                boolean eng1 = c1.getPath().toLowerCase().contains("english");
                boolean eng2 = c2.getPath().toLowerCase().contains("english");

                if (eng1 && !eng2) {
                    return -1; // eng1 comes first
                } else if (!eng1 && eng2) {
                    return 1; // eng2 comes first
                } else {
                    return 0; // otherwise, keep the same order
                }
            });
            CollectionDetails parentCollection = collectionDetails;
            for (int i = 0; i < size; i++) {
                Child child = children.get(i);
                getCollectionDetails(parentCollection, child.getCollectionID(), child.getFileName());
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
