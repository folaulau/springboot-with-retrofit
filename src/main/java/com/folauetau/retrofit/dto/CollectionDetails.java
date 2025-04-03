package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

// Collection details
@Data
public class CollectionDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ASSETS_COLLECTION_TYPE = "assets";
    public static final String COLLECTIONS_COLLECTION_TYPE = "collections";

    // english collection id
    private String sourceCollectionId;
    private String parentCollectionID;
    private String parentCollectionPath;
    private String collectionType;
    private String path;
    private String collectionID;
    private String language;
    private String type;
    private String lastUpdatedDate;
    private List<Link> links;
    private List<CoverImage> coverImages;
    private List<Rendition> renditions;
    private List<Related> related;
    private List<Metadata> metadata;
    private List<SecurityConstraint> securityConstraints;
    private List<Child> children;
    private List<AssetWrapper> assets;
    private String fileName;
    private boolean hasEnglishRoot;

    public String getPublicTitle() {
        return metadata.stream()
            .filter(m -> "publicTitle".equals(m.getKey()))
            .map(Metadata::getValue)
            .findFirst()
            .orElse(null);
    }

    public String getDescription() {
        return metadata.stream()
            .filter(m -> "description".equals(m.getKey()))
            .map(Metadata::getValue)
            .findFirst()
            .orElse(null);
    }

    public String getCollectionUri() {
        return metadata.stream()
            .filter(m -> "collectionUri".equals(m.getKey()))
            .map(Metadata::getValue)
            .findFirst()
            .orElse(null);
    }

    public String getName() {
        if(path == null) {
            return null;
        }
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    public boolean isEnglish() {
        return "eng".equalsIgnoreCase(language);
    }

    public String getLink(){
        return links.stream()
            .filter(l -> l!=null && l.getHref() != null)
            .map(Link::getHref)
            .findFirst()
            .orElse(null);
    }

    public void updateCollectionUri(String collectionUri, String lang) {
        metadata.add(new Metadata("collectionUri", collectionUri, lang));
    }

    public int getOrderIndex() {
        Optional<String> optionalValue = metadata.stream()
            .filter(m -> "orderIndex".equalsIgnoreCase(m.getKey()))
            .map(Metadata::getValue)
            .findFirst();

        if(optionalValue.isPresent()) {
            try {
                return Integer.parseInt(optionalValue.get());
            } catch (NumberFormatException e) {
            }
        }

        // return -1 so item is sorted to the end of the list
        return -1;
    }

    public void sortChildren() {
        if(children == null || children.size() <= 1) {
            return;
        }

        children.sort((c1, c2) -> {
            int orderIndex1 = c1.getOrderIndex();
            int orderIndex2 = c2.getOrderIndex();
            return Integer.compare(orderIndex1, orderIndex2);
        });
    }

    public String getFileName() {
        return getCollectionID().toLowerCase() + ".json";
    }

    public void sortAssets() {
        if(assets == null || assets.size() <= 1) {
            return;
        }

        assets.sort((a1, a2) -> {
            int orderIndex1 = a1.getIndex();
            int orderIndex2 = a2.getIndex();
            return Integer.compare(orderIndex1, orderIndex2);
        });
    }
}
