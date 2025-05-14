package com.folauetau.retrofit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.folauetau.retrofit.dto.AssetCollectionDTO;
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
public class TitanAssetDuplicatePathApiTests {

    ObjectMapper objectMapper = getObjectMapper();
    TitanCollectionRestApi titanRestApi = new TitanCollectionRestApi();
    TitanAssetRestApi titanAssetRestApi = new TitanAssetRestApi();

    Map<String, Integer> collectionMap = new ConcurrentHashMap<>();

    private String fileStoragePath = "json_asset_dup_ids.txt";

    private String assetDupFileName = "media_asset_dups.txt";

    private static volatile int count = 0;

    @Test
    void downloadTitanAssetIds() throws JsonProcessingException {

        File file = new File(fileStoragePath);

        if (file.exists()) {
            file.delete();
        }

        System.out.println("downloading...");

        // parent collection
        String collectionId = "22e3232e384c8650311b2940336759a89b8d6f8b";

        // track how long this takes
        long startTime = System.currentTimeMillis();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileStoragePath, true))) {
            getCollectionDetails(collectionId, writer);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }


        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);

        long minutes = duration / 60000;
        System.out.println("Duration: " + minutes + " minutes");

        findTitanAssetsWithoutTitleOrDescription();

    }

    void findTitanAssetsWithoutTitleOrDescription() throws JsonProcessingException {
        System.out.println("findTitanAssetsWithoutTitleOrDescription...");
        String filePath = fileStoragePath;
        Set<String> notFoundTitanIds = new HashSet<>();

        Map<String, List<AssetCollectionDTO>> assetIdCollections = new ConcurrentHashMap<>();

        log.info("getting assets...");
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (String line : lines) {
                String assetIdCollectionId = line.trim();

                String[] row = assetIdCollectionId.split(",");

                String assetId = row[0];
                String collectionId = row[1];
                String collectionPath = row[2];

                TitanAssetApiResponse titanAssetResponse = titanAssetRestApi.getTitanAsset(assetId);
                if(titanAssetResponse == null || titanAssetResponse.getStatus().equalsIgnoreCase("failure")) {
                    notFoundTitanIds.add(assetId);
                    continue;
                }
                TitanAsset titanAsset = titanAssetResponse.getResult();
                if(titanAsset == null) {
                    notFoundTitanIds.add(assetId);
                    continue;
                }
                //                log.info("title: {}", titanAsset.getTitle());
                //                log.info("description: {}",titanAsset.getDescription());

                String path = titanAsset.getPath();

                String pathWithLanguage = (path + "-" + titanAsset.getLanguage()).trim().toLowerCase();

                AssetCollectionDTO assetCollectionDTO = new AssetCollectionDTO();
                assetCollectionDTO.setCollectionId(collectionId);
                assetCollectionDTO.setCollectionPath(collectionPath);
                assetCollectionDTO.setAssetId(assetId);
                assetCollectionDTO.setAssetLanguage(titanAsset.getLanguage());
                assetCollectionDTO.setAssetPath(path);

                if(assetIdCollections.containsKey(pathWithLanguage)) {
                    List<AssetCollectionDTO> collections = assetIdCollections.get(pathWithLanguage);

                    if(collections == null){
                        collections = new ArrayList<>();
                    }

                    boolean assetIdFound = collections.stream().filter(assetColl -> assetColl.getAssetId()!=null && assetColl.getAssetId().equalsIgnoreCase(assetId)).count() > 0;

                    if(!assetIdFound){
                        collections.add(assetCollectionDTO);
                    }

                    assetIdCollections.put(pathWithLanguage, collections);

                }else{
                    List<AssetCollectionDTO> collections = new ArrayList<>();
                    collections.add(assetCollectionDTO);
                    assetIdCollections.put(pathWithLanguage, collections);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //processAndWriteToDuplicateFile(String assetId, String assetPath, String collectionId, String collectionPath, BufferedWriter writer)

        File file = new File(assetDupFileName);

        if (file.exists()) {
            file.delete();
        }

        System.out.println("generating report...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(assetDupFileName, true))) {

            processAndWriteToDuplicateFile("Asset Path","Asset Id", "Asset Language",  "Collection Id", "Collection Path", writer);
            //loop through assetPathToAssetId
            for (Map.Entry<String, List<AssetCollectionDTO>> entry : assetIdCollections.entrySet()) {
                String assetPathLanguage = entry.getKey();
                List<AssetCollectionDTO> collectionDTOS = entry.getValue();

                if(collectionDTOS == null || collectionDTOS.size() <= 1) {
                    continue;
                }

                for (AssetCollectionDTO collection : collectionDTOS) {
                    processAndWriteToDuplicateFile(collection.getAssetPath(), collection.getAssetId(), collection.getAssetLanguage(), collection.getCollectionId(), collection.getCollectionPath(), writer);
                }
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        log.info("Done");

    }

    CollectionDetails getCollectionDetails(String collectionId, BufferedWriter writer) {

        if(collectionId == null || collectionId.trim().isEmpty()) {
            System.out.println("collectionId is null: " + collectionId);
            return null;
        }

        TitanCollectionApiResponse titanApiResponse = titanRestApi.getCollection(collectionId);
//        System.out.println("titanApiResponse: " + toJson(titanApiResponse));

        if(titanApiResponse == null || titanApiResponse.getResult() == null) {
           System.out.println("No collection found for collectionId: " + collectionId);
            return null;
        }

        CollectionDetails collectionDetails = titanApiResponse.getResult();

        String path = collectionDetails.getPath();

        List<String> assetIds = new ArrayList<>();
        if(collectionMap.containsKey(collectionDetails.getCollectionID())) {
            System.out.println("Collection already processed: " + collectionDetails.getCollectionID());
        }else {
            collectionMap.put(collectionDetails.getCollectionID(), 1);

            Optional<CoverImage> optionalCoverImage = collectionDetails.getCoverImages().stream().findFirst();
            if (optionalCoverImage.isPresent() && optionalCoverImage.get().getAsset() != null) {
                String assetId = optionalCoverImage.get().getAsset().getAssetID();
                assetIds.add(assetId);
            }
        }

        boolean hasChildren = collectionDetails.getChildren() != null && collectionDetails.getChildren().size() > 0;

        List<AssetWrapper> assetWrappers = collectionDetails.getAssets();

        boolean hasAssets = assetWrappers != null && assetWrappers.size() > 0;

        if(!hasChildren) {
            if (hasAssets) {
                collectionDetails.setCollectionType(CollectionDetails.ASSETS_COLLECTION_TYPE);
                for (AssetWrapper assetWrapper : assetWrappers) {
                    if (assetWrapper == null || assetWrapper.getAsset() == null) {
                        continue;
                    }
                    String assetId = assetWrapper.getAsset().getAssetID();
                    assetIds.add(assetId);
                }
            }else {
                collectionDetails.setCollectionType(CollectionDetails.COLLECTIONS_COLLECTION_TYPE);
            }
        }

        processAndWriteToFile(collectionDetails.getCollectionID(), collectionDetails.getPath(), assetIds, writer);

//        if (count >= 50) {
//            return null;
//        }

        count++;

        if(hasChildren) {
            int size = collectionDetails.getChildren().size();
            CollectionDetails parentCollection = collectionDetails;
            for (int i = 0; i < size; i++) {
                Child child = collectionDetails.getChildren().get(i);
                getCollectionDetails(child.getCollectionID(), writer);
            }
        }

        return collectionDetails;
    }

    private boolean processAndWriteToFile(String collectionId, String collectionPath, List<String> titanIds, BufferedWriter writer) {
        try {
            if (titanIds == null || titanIds.size() == 0) {
                return false; // No more data to process
            }

            if(collectionPath == null || collectionPath.trim().isEmpty()) {
                collectionPath = "";
            }

            for (String titanId : titanIds) {
                writer.write(titanId+"," + collectionId+"," + collectionPath);
                writer.newLine();
            }

            writer.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Error processing API response: " + e.getMessage());
            return false;
        }
    }

    private boolean processAndWriteToDuplicateFile(String assetPath, String assetId, String assetLanguage, String collectionId, String collectionPath, BufferedWriter writer) {
        try {
            writer.write(assetPath+"," + assetId+"," + assetLanguage+"," + collectionId+"," + collectionPath);
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
