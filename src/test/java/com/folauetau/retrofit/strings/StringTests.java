package com.folauetau.retrofit.strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    @Test
    void testStringlength(){
        String title = "Thequickbrownfox jumps over the lazy dog";

        if(title.length() >= 7){
            System.out.println("title: " + title.substring(0, 7));
        } else {
            System.out.println("title: " + title.substring(0, title.length()));
        }

        title = "The12";

        if(title.length() >= 7){
            System.out.println("title: " + title.substring(0, 7));
        } else {
            System.out.println("title: " + title.substring(0, title.length()));
        }

        title = "Th";

        if(title.length() >= 7){
            System.out.println("title: " + title.substring(0, 7));
        } else {
            System.out.println("title: " + title.substring(0, title.length()));
        }
    }

    @Test
    void testStringList(){

        Map<String, Integer> pathAssetPaths = new ConcurrentHashMap<>();

        List<String> list = new ArrayList<>();
        list.add("Thequickone1");
        list.add("Thequickone1");
        list.add("Thequickone1");
        list.add("Thequickone1");
        list.add("Thequickone2");
        list.add("Thequickone2");

        List<String> newList = new ArrayList<>();
        for (int i=0; i< list.size(); i++){
            String path = list.get(i);
            System.out.println("path: " + path);
            Integer pathCount = pathAssetPaths.get(path);

            if(pathCount == null){
                pathAssetPaths.put(path, 1);
                newList.add(path);
            }else {
                pathAssetPaths.put(path, pathCount + 1);
            }
        }

        for (String path : newList) {
            System.out.println("xxpath: " + path + ", count: " + pathAssetPaths.get(path));
        }


    }
}
