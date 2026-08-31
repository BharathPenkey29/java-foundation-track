package com.signia.training.a03oop;

import java.nio.file.Path;
import java.util.List;

public final class Q2_ExtractorFactory {

    private static final List<Q2_DataExtractor> EXTRACTORS =
            List.of(
                    new Q2_CsvExtractor(),
                    new Q2_FixedWidthExtractor(),
                    new Q2_PipeDelimitedExtractor(),
                    new Q2_TabDelimitedExtractor()
            );

    private Q2_ExtractorFactory() {
        // Prevent instantiation.
    }

    public static Q2_DataExtractor forPath(Path path) {

        return EXTRACTORS.stream()
                .filter(extractor -> extractor.supports(path))
                .findFirst()
                .orElseThrow(
                        () -> new Q2_UnsupportedSourceException(
                                "Unsupported source format: " + path
                        )
                );
    }

    public static List<Q2_DataExtractor> all() {
        return EXTRACTORS;
    }
}