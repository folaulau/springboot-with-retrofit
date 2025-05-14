package com.folauetau.retrofit.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import lombok.Data;

@Data
public class Asset implements Serializable {

    private static final long serialVersionUID = 1L;

//    private String createdDate;
    private String distributionUri;
//    private int height;
//    private boolean isCurrentVersion;
//    private boolean isDeleted;
//    private boolean isOriginal;
    private String language;
//    private LanguageDetails languageDetails;
//    private String lastUpdatedDate;
    private String mimeType;
//    private String versionID;
//    private int size;
    private String type;
//    private int width;
    private String assetID;
//    private String sourceType;
//    private String binaryModifyDate;
//    private String cdnModifiedDate;
    private List<Metadata> metadata;
//    private List<Collection> collections;
//    private List<Link> links;
//c2199b453b7e41a4aeff18aa3446722b,

    public String getSEOPathWithHash() {

        if (type.toLowerCase().equals("video")) {
            return getVideoSEOPath();
        }

        String seoPath = this.getSEOFileName();

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getFileName();
        }

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getPublicTitle();
        }

        seoPath = (type != null ? type.toLowerCase() + "/" : "") + seoPath;

        if (assetID != null && !assetID.isEmpty()) {
            int length = assetID.length();
            String assetHash = null;
            if (length >= 7) {
                assetHash = assetID.substring(0, 7);
            } else {
                assetHash = assetID.substring(0, length);
            }
            seoPath = seoPath + "-" + assetHash;
        }

        return seoPath;
    }

    public String getPublicTitle() {
        Optional<String> optionalMetadataPublicTitle = metadata.stream()
            .filter(m -> "publicTitle".equals(m.getKey()))
            .map(meta -> {
                String value = meta.getValue();
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return value;
            })
            .findFirst();

        if (optionalMetadataPublicTitle.isPresent()) {
            return optionalMetadataPublicTitle.get();
        }
        return null;
    }

    public String getPublicDescription() {
        Optional<String> optionalMetadataPublicDescription = metadata.stream()
            .filter(m -> "publicDescription".equals(m.getKey()))
            .map(meta -> {
                String value = meta.getValue();
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return value;
            })
            .findFirst();

        if (optionalMetadataPublicDescription.isPresent()) {
            return optionalMetadataPublicDescription.get();
        }
        return null;
    }

    public String getFileName() {
        Optional<String> optionalMetadataFileName = metadata.stream()
            .filter(m -> "fileName".equals(m.getKey()))
            .map(meta -> {
                String value = meta.getValue();
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return value;
            })
            .findFirst();

        if (optionalMetadataFileName.isPresent()) {
            return optionalMetadataFileName.get();
        }
        return null;
    }

    public String getSEOFileName() {
        Optional<String> optionalMetadataFileName = metadata.stream()
            .filter(m -> "seoFileName".equals(m.getKey()))
            .map(meta -> {
                String value = meta.getValue();
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return value;
            })
            .findFirst();

        if (optionalMetadataFileName.isPresent()) {
            return optionalMetadataFileName.get();
        }
        return null;
    }

    public String getOriginalRepositoryId() {
        Optional<String> optionalMetadataFileName = metadata.stream()
            .filter(m -> "originalRepositoryId".equals(m.getKey()))
            .map(meta -> {
                String value = meta.getValue();
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return value;
            })
            .findFirst();

        if (optionalMetadataFileName.isPresent()) {
            return optionalMetadataFileName.get();
        }
        return null;
    }

    public String getOriginalAssetId() {
        Optional<String> optionalMetadataFileName = metadata.stream()
            .filter(m -> "originalAssetId".equals(m.getKey()))
            .map(meta -> {
                String value = meta.getValue();
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return value;
            })
            .findFirst();

        if (optionalMetadataFileName.isPresent()) {
            return optionalMetadataFileName.get();
        }
        return null;
    }

    public String getTelescopeID() {
        Optional<String> optionalMetadataFileName = metadata.stream()
            .filter(m -> "telescopeID".equals(m.getKey()))
            .map(meta -> {
                String value = meta.getValue();
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return value;
            })
            .findFirst();

        if (optionalMetadataFileName.isPresent()) {
            return optionalMetadataFileName.get();
        }
        return null;
    }

    public String getSEOPath() {
        if (type.toLowerCase().equals("video")) {
            return getVideoSEOPath();
        }

        String seoPath = this.getSEOFileName();

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getFileName();
        }

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getPublicTitle();
        }

        return (type != null ? type.toLowerCase() + "/" : "") + seoPath;
    }

    private String getVideoSEOPath() {
        String seoPath = this.getFileName();

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getSEOFileName();
        }

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getOriginalRepositoryId();
        }

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getOriginalAssetId();
        }

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getTelescopeID();
        }

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getPublicTitle();
        }

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getPublicDescription();
            Optional<String> optionalMetadataVideoId = metadata.stream()
                .filter(m -> "videoId".equals(m.getKey()))
                .map(meta -> {
                    String value = meta.getValue();
                    if (value == null || value.trim().isEmpty()) {
                        return null;
                    }
                    return value;
                })
                .findFirst();
            if (optionalMetadataVideoId.isPresent()) {
                seoPath = optionalMetadataVideoId.get();
            }
        }

        if (seoPath == null || seoPath.trim().isEmpty()) {
            seoPath = this.getPublicDescription();
        }

        // remove language slug from seo path coming from titan
        if (seoPath != null && !seoPath.trim().isEmpty() && language != null
            && !language.trim().isEmpty()) {
            String seoPathLowercase = seoPath.trim().toLowerCase();
            String lowerCaseLang = language.trim().toLowerCase();
            String seoPathLangSlug = seoPathLowercase.substring(
                seoPathLowercase.length() - lowerCaseLang.length(),
                seoPathLowercase.length());
            if (seoPathLangSlug.equals(lowerCaseLang)) {
                seoPathLangSlug = seoPathLowercase.substring(
                    (seoPathLowercase.length() - lowerCaseLang.length()) - 1,
                    seoPathLowercase.length());
                if (seoPathLangSlug.equals("-" + lowerCaseLang)) {
                    seoPath = seoPath.substring(0, (seoPath.length() - lowerCaseLang.length()) - 1);
                } else {
                    seoPath = seoPath.substring(0, seoPath.length() - lowerCaseLang.length());
                }
            }
        }

        return (type != null ? type.toLowerCase() + "/" : "") + seoPath;
    }

    public String getLanguage() {
        if (language == null) {
            return "";
        }
        return language.trim();
    }
}
