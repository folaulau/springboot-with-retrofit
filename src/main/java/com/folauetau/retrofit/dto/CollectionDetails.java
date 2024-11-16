package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

// Collection details
@Data
public class CollectionDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private String parentCollectionID;
    private String parentCollectionPath;
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

    public String getName() {
        if(path == null) {
            return null;
        }
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
