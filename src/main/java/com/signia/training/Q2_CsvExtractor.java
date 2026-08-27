package com.signia.training;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Q2_CsvExtractor implements Q2_DataExtractor {

    @Override
    public List<String[]> extract(Path path) {

        List<String[]> rows = new ArrayList<>();

        try {
            List<String> lines =
                    Files.readAllLines(
                            path,
                            StandardCharsets.UTF_8
                    );

            for (String line : lines) {

                if (line.isBlank()) {
                    continue;
                }

                rows.add(line.split(",", -1));
            }

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Unable to read CSV file: " + path,
                    exception
            );
        }

        return rows;
    }

    @Override
    public String name() {
        return "CSV";
    }

    @Override
    public boolean supports(Path path) {
        return path != null
                && path.toString()
                .toLowerCase()
                .endsWith(".csv");
    }
}