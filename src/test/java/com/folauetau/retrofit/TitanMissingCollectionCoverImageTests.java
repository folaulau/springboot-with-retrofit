package com.folauetau.retrofit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.folauetau.retrofit.dto.CoverImage;
import com.folauetau.retrofit.dto.TitanCollectionApiResponse;
import com.folauetau.retrofit.dto.titanasset.TitanAsset;
import com.folauetau.retrofit.dto.titanasset.TitanAssetApiResponse;
import com.folauetau.retrofit.rest.TitanAssetRestApi;
import com.folauetau.retrofit.rest.TitanCollectionRestApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TitanMissingCollectionCoverImageTests {

    ObjectMapper objectMapper = getObjectMapper();
    TitanCollectionRestApi titanRestApi = new TitanCollectionRestApi();
    TitanAssetRestApi titanAssetRestApi = new TitanAssetRestApi();

    Map<String, Integer> collectionMap = new ConcurrentHashMap<>();

    private String fileStoragePath = "json_collection_cover_image_ids.txt";

    private static volatile int count = 0;

    String rootCollectionId = "22e3232e384c8650311b2940336759a89b8d6f8b";

    @Test
    void testString() {
        String seoPath = "tst-file-eng";
        String language = "eng";

        String seoPathSlug = seoPath.substring(seoPath.length() - language.length(), seoPath.length());

        log.info("seoPathSlug: {}", seoPathSlug);
        if (seoPathSlug.equals(language)) {
            seoPathSlug = seoPath.substring((seoPath.length() - language.length()) - 1, seoPath.length());
            if (seoPathSlug.equals("-" + language)) {
                seoPath = seoPath.substring(0, seoPath.length() - language.length() - 1);
            } else {
                seoPath = seoPath.substring(0, seoPath.length() - language.length());
            }

        }

        log.info("seoPath: {}", seoPath);
    }

//    @Test
//    void findTitanAssetsWithoutTitleOrDescription() throws JsonProcessingException {
//
//        String filePath = fileStoragePath; // Change this to your file path
//        Map<String, String> missingDataTitanIds = new HashMap<>();
//        Map<String, String> notFoundTitanIds = new HashMap<>();
//        log.info("Incomplete assets...");
//        try {
//            List<String> lines = Files.readAllLines(Paths.get(filePath));
//            for (String line : lines) {
//                String[] columns = line.trim().split(",");
//
//                String collectionId = columns[0];
//                String coverImageId = columns[1];
//
//                if (coverImageId == null || coverImageId.trim().isEmpty() || coverImageId.equalsIgnoreCase("NULL")) {
//                    notFoundTitanIds.put(collectionId, coverImageId);
//                    continue;
//                }
//
//                TitanAssetApiResponse titanAssetResponse = titanAssetRestApi.getTitanAsset(coverImageId);
//                if (titanAssetResponse == null || titanAssetResponse.getStatus().equalsIgnoreCase("failure")) {
//                    notFoundTitanIds.put(collectionId, coverImageId);
//                    continue;
//                }
//                TitanAsset titanAsset = titanAssetResponse.getResult();
//                if (titanAsset == null) {
//                    notFoundTitanIds.put(collectionId, coverImageId);
//                    continue;
//                }
//                //                log.info("title: {}", titanAsset.getTitle());
//                //                log.info("description: {}",titanAsset.getDescription());
//
//                String title = titanAsset.getTitle();
//                String description = titanAsset.getDescription();
//
//                if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
//                    missingDataTitanIds.put(collectionId, titanAsset.getAssetID());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        log.info("Done");
//
//        log.info("missingDataTitanIds Titan IDs count: {}", missingDataTitanIds.size());
//        for (Map.Entry<String, String> entry : missingDataTitanIds.entrySet()) {
//            System.out.println("collectionId: " + entry.getKey() + ", assetId: " + entry.getValue());
//        }
//
//        log.info("notFoundTitanIds Titan IDs count: {}", notFoundTitanIds.size());
//        for (Map.Entry<String, String> entry : notFoundTitanIds.entrySet()) {
//            System.out.println("collectionId: " + entry.getKey() + ", assetId: " + entry.getValue());
//        }
//
//    }

    @Test
    void downloadTitanCollectionCoverImageAssetIds() throws JsonProcessingException {

        File file = new File(fileStoragePath);

        if (file.exists()) {
            file.delete();
        }

        System.out.println("downloading...");

        // parent collection


        // track how long this takes
        long startTime = System.currentTimeMillis();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileStoragePath, true))) {
            getCollectionDetails(rootCollectionId, writer);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);

        long minutes = duration / 60000;
        System.out.println("Duration: " + minutes + " minutes");

        String filePath = fileStoragePath; // Change this to your file path
        Map<String, Map<String, String>> missingDataCoverImages = new HashMap<>();
        Map<String, Map<String, String>> noCoverImages = new HashMap<>();
        log.info("Incomplete assets...");
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (String line : lines) {
                String[] columns = line.trim().split(",");

                String collectionId = columns[0];
                String coverImageId = columns[1];
                String path = "";
                try {
                    path = columns[2];
                } catch (Exception e) {
                }

                String language = "";
                try {
                    language = columns[3];
                } catch (Exception e) {
                }

                Map<String, String> obj = new HashMap<>();

                obj.put("collectionId", collectionId);
                obj.put("coverImageId", coverImageId);
                obj.put("path", path);
                obj.put("language", language);

                if (coverImageId == null || coverImageId.trim().isEmpty() || coverImageId.equalsIgnoreCase("NULL")) {
                    noCoverImages.put(collectionId, obj);
                    continue;
                }

                TitanAssetApiResponse titanAssetResponse = titanAssetRestApi.getTitanAsset(coverImageId);
                if (titanAssetResponse == null || titanAssetResponse.getStatus().equalsIgnoreCase("failure")) {
                    noCoverImages.put(collectionId, obj);
                    continue;
                }
                TitanAsset titanAsset = titanAssetResponse.getResult();
                if (titanAsset == null) {
                    noCoverImages.put(collectionId, obj);
                    continue;
                }
                //                log.info("title: {}", titanAsset.getTitle());
                //                log.info("description: {}",titanAsset.getDescription());

                String title = titanAsset.getTitle();
                String description = titanAsset.getDescription();

                obj.put("assetTitle", title);
                obj.put("assetDescription", description);
                obj.put("assetLanguage", titanAsset.getLanguage());

                if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
                    obj.put("assetId", titanAsset.getAssetID());
                    missingDataCoverImages.put(collectionId, obj);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Done");


        log.info("Cover Image Not found count: {}", noCoverImages.size());
        for (Map.Entry<String, Map<String, String>> entry : noCoverImages.entrySet()) {
            Map<String, String> values = entry.getValue();
            System.out.println("collectionId: " + entry.getKey() + ", assetId: " + values.get("coverImageId")+ ", path: " + values.get("path")+ ", language: " + values.get("language"));
        }

        log.info("Cover Image with incomplete data count: {}", missingDataCoverImages.size());
        for (Map.Entry<String, Map<String, String>> entry : missingDataCoverImages.entrySet()) {
            Map<String, String> values = entry.getValue();
            System.out.println("collectionId: " + entry.getKey() + ", assetId: " + values.get("coverImageId")+  ", path: " + values.get("path")+ ", language: " + values.get("language"));
        }
    }

    CollectionDetails getCollectionDetails(String collectionId, BufferedWriter writer) {

        if (collectionId == null || collectionId.trim().isEmpty()) {
            System.out.println("collectionId is null: " + collectionId);
            return null;
        }

        TitanCollectionApiResponse titanApiResponse = titanRestApi.getCollection(collectionId);
        //        System.out.println("titanApiResponse: " + toJson(titanApiResponse));

        if (titanApiResponse == null || titanApiResponse.getResult() == null) {
            System.out.println("No collection found for collectionId: " + collectionId);
            return null;
        }

        CollectionDetails collectionDetails = titanApiResponse.getResult();

        String path = collectionDetails.getPath();

        if(!rootCollectionId.equalsIgnoreCase(collectionId) && (path ==null || path.toLowerCase().contains("unpublished") || collectionDetails.getPublicTitle() == null)){
            return null;
        }

        List<String> assetIds = new ArrayList<>();
        if (collectionMap.containsKey(collectionDetails.getCollectionID())) {
            System.out.println("Collection already processed: " + collectionDetails.getCollectionID());
        } else {
            collectionMap.put(collectionDetails.getCollectionID(), 1);

            Optional<CoverImage> optionalCoverImage = collectionDetails.getCoverImages().stream().findFirst();
            String coverImageId = null;
            if (optionalCoverImage.isPresent() && optionalCoverImage.get().getAsset() != null) {
                coverImageId = optionalCoverImage.get().getAsset().getAssetID();
            }

            processAndWriteToFile(collectionDetails.getCollectionID(), collectionDetails.getPath(), collectionDetails.getLanguage(), coverImageId, writer);
        }

        boolean hasChildren = collectionDetails.getChildren() != null && collectionDetails.getChildren().size() > 0;

        List<AssetWrapper> assetWrappers = collectionDetails.getAssets();

        boolean hasAssets = assetWrappers != null && assetWrappers.size() > 0;

        if (!hasChildren) {
            if (hasAssets) {
                collectionDetails.setCollectionType(CollectionDetails.ASSETS_COLLECTION_TYPE);
            } else {
                collectionDetails.setCollectionType(CollectionDetails.COLLECTIONS_COLLECTION_TYPE);
            }
        } else {
            collectionDetails.setCollectionType(CollectionDetails.COLLECTIONS_COLLECTION_TYPE);
        }

        if (hasChildren) {
            int size = collectionDetails.getChildren().size();
            CollectionDetails parentCollection = collectionDetails;
            for (int i = 0; i < size; i++) {
                Child child = collectionDetails.getChildren().get(i);
                getCollectionDetails(child.getCollectionID(), writer);
            }
        }

        return collectionDetails;
    }

    private boolean processAndWriteToFile(String collectionId, String collectionPath, String collectionLanguage, String coverImageId, BufferedWriter writer) {
        try {
            if (collectionId == null || collectionId.trim().length() == 0) {
                return false;
            }

            if (coverImageId == null || coverImageId.trim().length() == 0) {
                writer.write(collectionId.trim() + ",NULL"+ "," + collectionPath.trim()+ "," + collectionLanguage.trim());
            } else {
                writer.write(collectionId.trim() + "," + coverImageId.trim()+ "," + collectionPath.trim()+ "," + collectionLanguage.trim());
            }

            writer.newLine();

            writer.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Error processing API response: " + e.getMessage());
            return false;
        }
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
