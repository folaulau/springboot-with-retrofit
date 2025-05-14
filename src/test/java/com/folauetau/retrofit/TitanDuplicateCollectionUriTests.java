package com.folauetau.retrofit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.folauetau.retrofit.dto.AssetCollectionDTO;
import com.folauetau.retrofit.dto.AssetWrapper;
import com.folauetau.retrofit.dto.Child;
import com.folauetau.retrofit.dto.CollectionDTO;
import com.folauetau.retrofit.dto.CollectionDetails;
import com.folauetau.retrofit.dto.TitanCollectionApiResponse;
import com.folauetau.retrofit.rest.TitanCollectionRestApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TitanDuplicateCollectionUriTests {

    ObjectMapper objectMapper = getObjectMapper();
    TitanCollectionRestApi titanRestApi = new TitanCollectionRestApi();

    Map<String, List<CollectionDetails>> pathCollectionPaths = new ConcurrentHashMap<>();

    private String collectionDupFileName = "media_collection_dups.txt";

    private static volatile int count = 0;

    String rootCollectionId = "22e3232e384c8650311b2940336759a89b8d6f8b";

    @Test
    void getCollectionsWithDuplicatePath() throws JsonProcessingException {

        log.info("downloading...");

        // parent collection
        String collectionId = rootCollectionId;

        // track how long this takes
        long startTime = System.currentTimeMillis();
        getCollectionDetails(null, collectionId);

        long endTime = System.currentTimeMillis();

        long duration = (endTime - startTime);

        long minutes = duration / 60000;
        System.out.println("Duration: " + minutes + " minutes");

        File file = new File(collectionDupFileName);

        if (file.exists()) {
            file.delete();
        }

        System.out.println("generating report from "+pathCollectionPaths.keySet().size()+" collection paths...");
        int uniqueCount = 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(collectionDupFileName, true))) {

            processAndWriteToDuplicateFile("Collection Path","Collection Id", "Collection Language","Titan Path","Public Title", "Last UpdatedAt", writer);
            //loop through assetPathToAssetId
            for (Map.Entry<String, List<CollectionDetails>> entry : pathCollectionPaths.entrySet()) {
                String assetPathLanguage = entry.getKey();
                List<CollectionDetails> collectionDTOS = entry.getValue();

//                System.out.println(assetPathLanguage + " --- " + collectionDTOS.size());

                if(collectionDTOS == null || collectionDTOS.size() <= 1) {
                    continue;
                }
                uniqueCount++;
                for (CollectionDetails collection : collectionDTOS) {
                    processAndWriteToDuplicateFile(collection.getInternalName(),collection.getCollectionID(), collection.getLanguage(), collection.getPath(), collection.getPublicTitle(), collection.getLastUpdatedDate(),  writer);
                }
            }

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

        System.out.println("Total unique collection paths: " + uniqueCount);

        log.info("Done");

    }

    CollectionDetails getCollectionDetails(CollectionDetails parent, String collectionId) {

        if (collectionId == null || collectionId.trim().isEmpty()) {
            log.info("collectionId is null: " + collectionId);
            return null;
        }

        TitanCollectionApiResponse titanApiResponse = titanRestApi.getCollection(collectionId);
        //        System.out.println("titanApiResponse: " + toJson(titanApiResponse));

        if (titanApiResponse == null || titanApiResponse.getResult() == null) {
            log.warn("No collection found for collectionId: " + collectionId);
            return null;
        }

        CollectionDetails collectionDetails = titanApiResponse.getResult();

        boolean rootCollection = rootCollectionId.equals(collectionId);

        String path = collectionDetails.getPath();

//        if(path==null || path.toLowerCase().contains("unpublished") || collectionDetails.getPublicTitle() == null){
//            return null;
//        }

        String language = collectionDetails.getLanguage();
        String internalName = collectionDetails.getInternalName();
        String pathLanguage = internalName.trim().toLowerCase()+"-"+language.trim().toLowerCase();

        if(path!=null && !path.toLowerCase().contains("unpublished") && collectionDetails.getPublicTitle() != null){
            pathCollectionPaths.compute(pathLanguage, (key, collectionDTOS) -> {
                if (collectionDTOS != null && !collectionDTOS.isEmpty()) {
                    boolean foundCollectionId = collectionDTOS.stream()
                        .anyMatch(coll -> coll.getCollectionID().equalsIgnoreCase(collectionId));
                    if (!foundCollectionId && !collectionDetails.isPublicTitleEmpty()) {
                        collectionDTOS.add(collectionDetails);
                    }
                } else {
                    if(!collectionDetails.isPublicTitleEmpty()){
                        collectionDTOS = new ArrayList<>();
                        collectionDTOS.add(collectionDetails);
                    }
                }
                return collectionDTOS;
            });
        }


        if (parent != null) {
            collectionDetails.setParentCollectionID(parent.getCollectionID());
            collectionDetails.setParentCollectionPath(parent.getPath());
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
        }

//        if (count >= 20) {
//            return null;
//        }
//
//        count++;

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
                getCollectionDetails(parentCollection, child.getCollectionID());
            }
        }

        return collectionDetails;
    }

    private boolean processAndWriteToDuplicateFile(String collectionUri, String collectionId, String collectionLanguage,String path, String publicTitle, String lastUpdatedDate, BufferedWriter writer) {
        try {
            writer.write(collectionUri+"," + collectionId+"," + collectionLanguage+","+path+","+publicTitle+","+lastUpdatedDate);
            writer.newLine();

            writer.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Error processing API response: " + e.getMessage());
            return false;
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
