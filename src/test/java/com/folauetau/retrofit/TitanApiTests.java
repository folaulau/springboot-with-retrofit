package com.folauetau.retrofit;

import java.io.File;
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
import com.folauetau.retrofit.dto.Asset;
import com.folauetau.retrofit.dto.AssetWrapper;
import com.folauetau.retrofit.dto.Child;
import com.folauetau.retrofit.dto.CollectionDetails;
import com.folauetau.retrofit.dto.TitanCollectionApiResponse;
import com.folauetau.retrofit.dto.titanasset.TitanAsset;
import com.folauetau.retrofit.dto.titanasset.TitanAssetApiResponse;
import com.folauetau.retrofit.rest.TitanAssetRestApi;
import com.folauetau.retrofit.rest.TitanCollectionRestApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TitanApiTests {

    ObjectMapper objectMapper = getObjectMapper();
    TitanCollectionRestApi titanRestApi = new TitanCollectionRestApi();
    TitanAssetRestApi titanAssetRestApi = new TitanAssetRestApi();

    Map<String, Integer> collectionMap = new ConcurrentHashMap<>();
    Map<String, String> englishCollectionIds = new ConcurrentHashMap<>();

    Map<String, Integer> pathCollectionPaths = new ConcurrentHashMap<>();

    // asset path : collection id of the first collection that has this asset path
    Map<String, AssetPathDTO> pathAssetPaths = new ConcurrentHashMap<>();

    Map<String, String> englishPathToCollectionUris = new ConcurrentHashMap<>();
    Map<String, String> langToLanguage = new ConcurrentHashMap<>();
    private String fileStoragePath = "json_files_full";

    private static volatile int count = 0;

    /**
     * featured - eng - 38b9e81d44343bf4204296241568235d6e8e77c6
     *
     * featured - span - fe9b244b1ac04e0aa0cd91fb9521df2f
     *
     * Images - eng - 9469dcd2e95b0f73fd237301b21fecbd372d2e60
     *
     * Images - span - 4f8489a86d15b8824bef8775080622b95abaa78a
     */
    private List<String> rootCollectionsToImport = null;//List.of("38b9e81d44343bf4204296241568235d6e8e77c6","fe9b244b1ac04e0aa0cd91fb9521df2f","9469dcd2e95b0f73fd237301b21fecbd372d2e60","4f8489a86d15b8824bef8775080622b95abaa78a");

    private void addEnglishCollectionUri(String collectionUri, String collectionId, String path) {
        if (collectionUri == null || collectionUri.trim().isEmpty()) {
            return;
        }
        collectionUri = collectionUri.trim().toLowerCase();
        if (englishCollectionIds.containsKey(collectionUri)) {
        }
        englishCollectionIds.put(collectionUri, collectionId.trim());
        englishPathToCollectionUris.put(path.trim().toLowerCase(), collectionUri);
    }

    private String getEnglishCollectionId(String collectionUri) {
        if (collectionUri == null || collectionUri.trim().isEmpty()) {
//            log.warn("Collection URI is null or empty: " + collectionUri);
            return null;
        }
        return englishCollectionIds.get(collectionUri.toLowerCase());
    }

    String rootCollectionId = "22e3232e384c8650311b2940336759a89b8d6f8b";

    @Test
    void downloadTitanCollections() throws JsonProcessingException {

        File directory = new File(fileStoragePath);

        if (directory.exists()) {
            directory.delete();
        }

        directory.mkdirs();

        log.info("downloading...");

        // parent collection
        String collectionId = rootCollectionId;

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

        return getCollectionDetails(parent, collectionDetails, filename);
    }

    CollectionDetails getCollectionDetails(CollectionDetails parent, CollectionDetails collectionDetails, String filename) {

        if (collectionMap.containsKey(collectionDetails.getCollectionID())) {
            System.out.println("Collection already processed: " + collectionDetails.getCollectionID());
        } else {
            collectionMap.put(collectionDetails.getCollectionID(), 1);
        }

        String path = collectionDetails.getPath();

        boolean rootCollection = rootCollectionId.equals(collectionDetails.getCollectionID());

        if(!rootCollection && collectionDetails.isPublicTitleEmpty()){
            System.out.println("Empty Public title: "+collectionDetails.getPublicTitle()+", empty: "+collectionDetails.isPublicTitleEmpty()+", path: "+collectionDetails.getPath()+", id: "+collectionDetails.getCollectionID());
            return null;
        }

        if(!rootCollection && path==null){
            System.out.println("Empty path public title: "+collectionDetails.getPublicTitle()+", empty: "+collectionDetails.isPublicTitleEmpty()+", path: "+collectionDetails.getPath()+", id: "+collectionDetails.getCollectionID());
            return null;
        }

        if(!rootCollection && path.toLowerCase().contains("unpublished")){
            System.out.println("Unpublished public title: "+collectionDetails.getPublicTitle()+", empty: "+collectionDetails.isPublicTitleEmpty()+", path: "+collectionDetails.getPath()+", id: "+collectionDetails.getCollectionID());
            return null;
        }

        String internalName = collectionDetails.getInternalName();

        String pathLanguage = internalName.trim().toLowerCase()+"-"+collectionDetails.getLanguage().trim().toLowerCase();

        Integer pathCount = pathCollectionPaths.get(pathLanguage);

        if (pathCount == null) {
            pathCollectionPaths.put(pathLanguage, 1);
        }else{
            pathCollectionPaths.put(pathLanguage, pathCount + 1);
            System.out.println("Dup collection, title: "+collectionDetails.getPublicTitle()+", empty: "+collectionDetails.isPublicTitleEmpty()+", path: "+collectionDetails.getPath()+", id: "+collectionDetails.getCollectionID());
            return null;
        }

        if (collectionDetails.isEnglish()) {
            String collectionUri = collectionDetails.getCollectionUri();
            if (collectionUri == null || collectionUri.trim().isEmpty()) {
//                log.warn(
//                    "Collection URI is null for eng collection ID: {}, path: {}",
//                    collectionDetails.getCollectionID(),
//                    collectionDetails.getPath());
            } else {
                addEnglishCollectionUri(collectionUri, collectionDetails.getCollectionID(), collectionDetails.getPath());
            }
        } else {
            String englishCollectionId = getEnglishCollectionId(collectionDetails.getCollectionUri());
            if (englishCollectionId != null) {
                collectionDetails.setSourceCollectionId(englishCollectionId);
                collectionDetails.setHasEnglishRoot(true);
            } else {
                //                log.info("No English collection ID found for collection URI: " + collectionDetails.getCollectionUri()
                //                    + ", path: " + collectionDetails.getPath() + ", id: " + collectionDetails.getCollectionID()
                //                    + ", parent: " + collectionDetails.getParentCollectionID());
                //                System.out.println("No English collection ID found for collection URI: " + toJson(collectionDetails));

                path = collectionDetails.getPath().toLowerCase();
                String lang = collectionDetails.getLanguage();
                String collectionUri = englishPathToCollectionUris.get(path);

                if (collectionUri == null && (lang!=null && !lang.trim().isEmpty() && langToLanguage.get(lang.toLowerCase())!=null)){
                        String language = langToLanguage.get(lang.toLowerCase());
                        collectionUri = englishPathToCollectionUris.get(path.replaceAll(language, "english"));
                }

                if (collectionUri != null) {
                    collectionDetails.setSourceCollectionId(englishCollectionId);
                    collectionDetails.setHasEnglishRoot(true);
                    collectionDetails.updateCollectionUri(collectionUri, lang);
                }else{
                    collectionDetails.setHasEnglishRoot(false);
//                    System.out.println(
//                        collectionDetails.getCollectionID() + ", " + collectionDetails.getCollectionUri() + ", "
//                            + collectionDetails.getPath() + ", " + collectionDetails.getLink());
                }

                //                System.out.println("No English collection ID found for collection URI: " + collectionDetails.getCollectionUri());

            }
        }

        if (parent != null) {
            collectionDetails.setParentCollectionID(parent.getCollectionID());
            collectionDetails.setParentCollectionPath(parent.getPath());
        }

        boolean hasChildren = collectionDetails.getChildren() != null && collectionDetails.getChildren().size() > 0;

        List<CollectionDetails> childrenCollections = new ArrayList<>();

        if (hasChildren) {
            collectionDetails.setCollectionType(CollectionDetails.COLLECTIONS_COLLECTION_TYPE);
            int size = collectionDetails.getChildren().size();
            for (int i = 0; i < size; i++) {
                Child child = collectionDetails.getChildren().get(i);

                TitanCollectionApiResponse childCollectionTitanApiResponse = titanRestApi.getCollection(child.getCollectionID());

                if (childCollectionTitanApiResponse != null && childCollectionTitanApiResponse.getResult() != null) {
                    CollectionDetails childCollectionDetails = childCollectionTitanApiResponse.getResult();

                    // populate root children language
                    if(rootCollection){
                        child.setRootChildLanguage(childCollectionDetails.getLanguage());
                    }

                    child.setOrderIndex(childCollectionDetails.getOrderIndex());

                    childrenCollections.add(childCollectionDetails);

                    child.setFileName(childCollectionDetails.getFileName());
                }
            }
        }

//        if(rootCollection){
//            log.info("Root collection {}", toJson(collectionDetails));
//        }

        List<AssetWrapper> assetWrappers = collectionDetails.getAssets();

        boolean hasAssets = assetWrappers != null && assetWrappers.size() > 0;

        if (!hasChildren) {
            if (hasAssets) {
                collectionDetails.setCollectionType(CollectionDetails.ASSETS_COLLECTION_TYPE);
                List<AssetWrapper> newAssetWrappers = new ArrayList<>();
                for (int i = 0; i < assetWrappers.size(); i++) {
                    AssetWrapper assetWrapper = assetWrappers.get(i);
                    Asset asset = assetWrapper.getAsset();

                    if(asset == null || asset.getAssetID() == null){
                        continue;
                    }

                    String assetPath = asset.getSEOPathWithHash();
                    String assetPathWithLanguage = (assetPath + "-" + collectionDetails.getLanguage()).trim().toLowerCase();

                    if (assetPathWithLanguage != null && !assetPathWithLanguage.trim().isEmpty()) {
                        AssetPathDTO assetPathDTO = pathAssetPaths.get(assetPathWithLanguage);

                        if (assetPathDTO == null) {
                            assetPathDTO = new AssetPathDTO();
                            assetPathDTO.setCollectionId(collectionDetails.getCollectionID());
                            assetPathDTO.setCollectionPath(collectionDetails.getPath());
                            assetPathDTO.setCollectionLanguage(collectionDetails.getLanguage());
                            assetPathDTO.setAssetId(asset.getAssetID());
                            assetPathDTO.setAssetPath(assetPath);

                            pathAssetPaths.put(assetPathWithLanguage, assetPathDTO);
                            newAssetWrappers.add(assetWrapper);
                        } else {

                            if(assetPathDTO.getCollectionLanguage()!=null && collectionDetails.getLanguage() !=null
                                &&
                                assetPathDTO.getCollectionLanguage().equalsIgnoreCase(collectionDetails.getLanguage())
                                &&
                                assetPathDTO.getAssetId()!=null && !assetPathDTO.getAssetId().equalsIgnoreCase(asset.getAssetID())
                            ){
                                System.out.println("Dup asset, title: " + asset.getPublicTitle() + ", path: " + asset.getSEOPathWithHash() + ", id: " + asset.getAssetID()+ ", collection id: " + collectionDetails.getCollectionID()+", collection path: "+collectionDetails.getPath()+", collection id duplicate of: "+assetPathDTO.getCollectionId()+", asset id duplicate of: "+assetPathDTO.getAssetId()+", assetPathWithLanguage: "+assetPathWithLanguage);
                            }else {
                                newAssetWrappers.add(assetWrapper);
                            }
                        }
                    }

                    if (!collectionDetails.isEnglish()) {
                        TitanAssetApiResponse titanAssetResponse = titanAssetRestApi.getTitanAsset(asset.getAssetID());
                        if(titanAssetResponse == null || titanAssetResponse.getStatus().equalsIgnoreCase("failure")) {
                            continue;
                        }
                        TitanAsset titanAsset = titanAssetResponse.getResult();
                        if(titanAsset == null) {
                            continue;
                        }
                        asset.setRelated(titanAsset.getRelated());
                    }

                }
                collectionDetails.setAssets(newAssetWrappers);

            } else {
                collectionDetails.setCollectionType(CollectionDetails.COLLECTIONS_COLLECTION_TYPE);
            }
        }

        collectionDetails.setFileName(filename);

        // sort children
        collectionDetails.sortChildren();

        // sort assets
        collectionDetails.sortAssets();

        try {
            // Write the object to a JSON file
            File jsonFile = new File(fileStoragePath + "/" + filename);
            objectMapper.writeValue(jsonFile, collectionDetails);
            //            System.out.println("JSON file created successfully: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

//        if (count >= 50) {
//            return null;
//        }

        count++;

        if (hasChildren && childrenCollections.size() > 0) {
            List<CollectionDetails> children = childrenCollections;
            int size = children.size();

            // process english collection first so that translation can work
            children.sort((c1, c2) -> {
                boolean eng1 = c1.getLanguage().toLowerCase().contains("eng");
                boolean eng2 = c2.getLanguage().toLowerCase().contains("eng");

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
                CollectionDetails child = children.get(i);
                if(rootCollection){
                    if(rootCollectionsToImport!=null && rootCollectionsToImport.size()>0){
                        if(rootCollectionsToImport.contains(child.getCollectionID())){
                            getCollectionDetails(parentCollection, child, child.getFileName());
                        }
                    }else{
                        getCollectionDetails(parentCollection, child, child.getFileName());
                    }
                }else{
                    getCollectionDetails(parentCollection, child, child.getFileName());
                }

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
