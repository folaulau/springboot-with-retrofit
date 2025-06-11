package com.folauetau.retrofit.dto.titanasset;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.folauetau.retrofit.dto.Asset;
import com.folauetau.retrofit.dto.CollectionDetails;
import com.folauetau.retrofit.dto.LanguageDetails;
import com.folauetau.retrofit.dto.Link;
import com.folauetau.retrofit.dto.Metadata;
import com.folauetau.retrofit.dto.Related;
import io.micrometer.common.util.StringUtils;
import lombok.Data;

@Data
public class TitanAsset implements Serializable {

    private static final long serialVersionUID = 1L;

    private int bitrate;
    private String createdDate;
    private String distributionUri;
    private String hlsDistributionUri;
    private Long durationMilliseconds;
    private Integer height;
    private Boolean isCurrentVersion;
    private Boolean isDeleted;
    private Boolean isOriginal;
    private String language;
    private LanguageDetails languageDetails;
    private String lastUpdatedDate;
    private String mimeType;
    private String versionID;
    private Long size;
    private String type;
    private String version;
    private Integer width;
    private String assetID;
    private String sourceType;
    private List<Metadata> metadata;
    private List<Related> related;

    public String getMetadataValueByKey(String key, boolean includeLanguageCheck) {
        return metadata.stream()
            .filter(m -> m.getKey().equals(key))
            .filter(m -> !includeLanguageCheck || metadataMatchesLanguage(m))
            .map(Metadata::getValue).findFirst().orElse(null);
    }

    public String getFirstMetaDataValue(String key) {
        return metadata.stream()
            .filter(m -> m.getKey().equals(key))
            .map(Metadata::getValue).findFirst().orElse(null);
    }

    private boolean metadataMatchesLanguage(Metadata metadata) {
        String language = getFirstMetaDataValue("languageName");
        if (StringUtils.isBlank(language)) {
            language = getLanguage();
        }
        return StringUtils.isBlank(language)
            || (StringUtils.isBlank(metadata.getLanguage()) && language.equalsIgnoreCase("eng"))
            || (!StringUtils.isBlank(metadata.getLanguage()) && metadata.getLanguage().equalsIgnoreCase(language));
    }

    /**
     * //publicDescription String publicDescription = getMetadataValueByKey(titanHit, "publicDescription", true); if
     * (StringUtils.isBlank(publicDescription)) { titanAssetImportable.setDescription(publicTitle); } else {
     * titanAssetImportable.setDescription(publicDescription); }
     *
     *
     * protected static String getMetadataValueByKey(TitanHit titanHit, String key, boolean includeLanguageCheck) {
     * return titanHit.getMetadata().stream() .filter(m -> m.getKey().equals(key)) .filter(m -> !includeLanguageCheck ||
     * metadataMatchesLanguage(titanHit, m)) .map(TitanMetadata::getValue).findFirst().orElse(null); }
     *
     *
     * //publicTitle, fallback to not include the language check if the public title is null String publicTitle =
     * ObjectUtils.firstNonBlank( getMetadataValueByKey(titanHit, PUBLIC_TITLE, true), getMetadataValueByKey(titanHit,
     * PUBLIC_TITLE, false));
     *
     * titanAssetImportable.setTitle(publicTitle);
     *
     * @throws JsonProcessingException
     */

    public String getTitle() {
        return firstNonBlank(
            getMetadataValueByKey("publicTitle", true),
            getMetadataValueByKey("publicTitle", false));
    }

    public String getDescription() {
        String publicDescription = getMetadataValueByKey("publicDescription", true);
        if (!StringUtils.isBlank(publicDescription)) {
            return publicDescription;
        } else {
            return getTitle();
        }
    }

    public static <T> T firstNonBlank(T... values) {
        if (values != null) {
            for (T value : values) {
                if (!isBlank(value)) {
                    return value;
                }
            }
        }

        return null;
    }

    public static boolean isBlank(Object object) {
        if (object == null) {
            return true;
        } else if (object instanceof String) {
            String string = (String) object;
            int i = 0;

            for (int length = string.length(); i < length; ++i) {
                if (!Character.isWhitespace(string.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else if (object instanceof Collection) {
            return ((Collection) object).isEmpty();
        } else if (object instanceof Map) {
            return ((Map) object).isEmpty();
        } else if (object.getClass().isArray()) {
            return Array.getLength(object) == 0;
        } else if (object instanceof Iterable) {
            return !((Iterable) object).iterator().hasNext();
        } else {
            return false;
        }
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

    public String getPath() {
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
}
