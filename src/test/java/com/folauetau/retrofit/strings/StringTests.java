package com.folauetau.retrofit.strings;

import java.util.List;

import org.junit.jupiter.api.Test;

public class StringTests {

    @Test
    void testString() {

        String mimeType = "image";
        String assetType = getAssetType(mimeType);

        System.out.println("assetType: " + assetType);
    }

    private String getAssetType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return "media";
        }
        String[] strs = mimeType.split("/");
        if (strs.length == 0) {
            return mimeType;
        }
        return strs[0];
    }

    @Test
    void streamPeek(){
        List.of("a","b","c").stream().peek(letter -> {
            System.out.println("peek: " + letter);
        }).forEach(letter -> System.out.println("letter: " + letter));
    }

    @Test
    void streamsPeek(){
        String title = "The quick brown fox jumps over the lazy dog";

        String[] words = title.split("/");

        System.out.println("last: " + words[words.length-1]);

        title = "The quick brown fox jumps/ over the/ lazy dog";

        words = title.split("/");

        System.out.println("last: " + words[words.length-1]);
    }
}
