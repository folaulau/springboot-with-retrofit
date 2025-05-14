package com.folauetau.retrofit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UniqueAssetPathCounter {
    public static void main(String[] args) {
        String csvFile = "media_asset_dups.txt"; // Update this path
        String line;
        String cvsSplitBy = ",";
        Set<String> uniquePaths = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) { // Skip header
                    isFirstLine = false;
                    continue;
                }

                String[] columns = line.split(cvsSplitBy, -1); // -1 to include trailing empty strings
                if (columns.length >= 3) {
                    String assetPath = columns[0].trim();
                    String assetLanguage = columns[2].trim();
                    uniquePaths.add(assetPath + "|" + assetLanguage);
                }
            }

            System.out.println("Total unique (Asset Path + Asset Language) combinations: " + uniquePaths.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

