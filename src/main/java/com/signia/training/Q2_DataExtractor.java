package com.signia.training;

import java.nio.file.Path;
import java.util.List;

public interface Q2_DataExtractor {

    List<String[]> extract(Path path);

    String name();

    default boolean supports(Path path) {
        return path != null;
    }

    static Q2_DataExtractor noop() {
        return new Q2_DataExtractor() {

            @Override
            public List<String[]> extract(Path path) {
                return List.of();
            }

            @Override
            public String name() {
                return "No-op extractor";
            }
        };
    }
}