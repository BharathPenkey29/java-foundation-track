package com.signia.training.a03oop;

import java.nio.file.Path;
import java.util.List;

public class Q2_ExtractorDemo {

    public static void main(String[] args) {

        List<Path> files = List.of(
                Path.of(
                        "src/main/resources/a01/patients.csv"
                ),
                Path.of(
                        "src/main/resources/a01/fixedwidth/roster.txt"
                ),
                Path.of(
                        "src/main/resources/a03/roster.psv"
                ),
                Path.of(
                        "src/main/resources/a03/sample.tsv"
                )
        );

        System.out.println("==========================================");
        System.out.println("Q2 - EXTRACTOR DRIVER");
        System.out.println("==========================================");

        for (Q2_DataExtractor extractor :
                Q2_ExtractorFactory.all()) {

            System.out.println();
            System.out.println(
                    "Extractor: " + extractor.name()
            );

            for (Path file : files) {

                if (!extractor.supports(file)) {
                    continue;
                }

                List<String[]> rows =
                        extractor.extract(file);

                System.out.println(
                        "File: " + file
                );

                System.out.println(
                        "Rows extracted: " + rows.size()
                );
            }
        }

        System.out.println();
        System.out.println("==========================================");
        System.out.println("Q2 - FACTORY DEMONSTRATION");
        System.out.println("==========================================");

        for (Path file : files) {

            Q2_DataExtractor extractor =
                    Q2_ExtractorFactory.forPath(file);

            System.out.println(
                    file
                            + " -> "
                            + extractor.name()
            );
        }

        System.out.println();
        System.out.println("==========================================");
        System.out.println("Q2 - NO-OP DEMONSTRATION");
        System.out.println("==========================================");

        Q2_DataExtractor noop =
                Q2_DataExtractor.noop();

        System.out.println(
                "Name: " + noop.name()
        );

        System.out.println(
                "Rows: "
                        + noop.extract(
                        Path.of("does-not-exist.txt")
                ).size()
        );
    }
}