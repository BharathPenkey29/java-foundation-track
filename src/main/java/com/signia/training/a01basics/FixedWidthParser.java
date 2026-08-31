package com.signia.training.a01basics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
// assignment 3 q 2 is using
import java.util.ArrayList;
import java.util.List;

/**
 * Assignment 1 - Q6
 *
 * Fixed-Width Record Parser with Checksum.
 *
 * Reads a fixed-width clinical roster line by line,
 * extracts fields using substring positions, and verifies
 * the trailing checksum.
 */
public class FixedWidthParser {

    /*
     * ============================================================
     * File configuration
     * ============================================================
     */

    private static final String INPUT_FILE =
            "src/main/resources/a01/fixedwidth/roster.txt";


    /*
     * ============================================================
     * Fixed-width layout
     * ============================================================
     *
     * Column boundaries are declared ONCE.
     *
     * If a field width changes, these are the values to change.
     */

    private static final int MRN_START = 0;
    private static final int MRN_END = 8;

    private static final int LAST_NAME_START = 8;
    private static final int LAST_NAME_END = 28;

    private static final int FIRST_NAME_START = 28;
    private static final int FIRST_NAME_END = 43;

    private static final int DOB_START = 43;
    private static final int DOB_END = 51;

    private static final int CHECKSUM_START = 51;
    private static final int CHECKSUM_END = 53;

    private static final int EXPECTED_LINE_LENGTH = 53;


    /*
     * Only the first 10 failures should be printed in detail.
     */
    private static final int MAX_DETAILED_FAILURES = 10;


    public static void main(String[] args) {

        Path inputPath =
                Paths.get(INPUT_FILE);

        /*
         * ========================================================
         * Check file
         * ========================================================
         */

        if (!Files.exists(inputPath)) {

            System.out.println(
                    "Input file not found: "
                            + inputPath
            );

            System.exit(1);
        }

        if (!Files.isReadable(inputPath)) {

            System.out.println(
                    "Input file is not readable: "
                            + inputPath
            );

            System.exit(1);
        }


        /*
         * ========================================================
         * Counters
         * ========================================================
         */

        int totalLines = 0;

        int validLines = 0;

        int invalidLengthLines = 0;

        int checksumFailures = 0;

        int detailedFailuresPrinted = 0;

        int additionalFailures = 0;


        /*
         * ========================================================
         * Read line by line
         * ========================================================
         *
         * try-with-resources automatically closes the reader.
         *
         * We DO NOT use Files.readAllLines() because the assignment
         * says the file could be 2 GB.
         */
        try (BufferedReader reader =
                     Files.newBufferedReader(
                             inputPath,
                             StandardCharsets.UTF_8
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {

                totalLines++;

                /*
                 * =================================================
                 * Length validation
                 * =================================================
                 */

                if (line.length()
                        != EXPECTED_LINE_LENGTH) {

                    invalidLengthLines++;

                    String message;

                    if (line.length()
                            < EXPECTED_LINE_LENGTH) {

                        message =
                                "Line is too short. "
                                        + "Expected "
                                        + EXPECTED_LINE_LENGTH
                                        + " characters but found "
                                        + line.length()
                                        + ".";

                    } else {

                        message =
                                "Line is too long. "
                                        + "Expected "
                                        + EXPECTED_LINE_LENGTH
                                        + " characters but found "
                                        + line.length()
                                        + ".";
                    }

                    /*
                     * Important:
                     *
                     * DO NOT call substring() on this line.
                     *
                     * The assignment specifically says malformed
                     * length lines must not cause substring() to throw.
                     */
                    if (detailedFailuresPrinted
                            < MAX_DETAILED_FAILURES) {

                        printFailure(
                                totalLines,
                                message
                        );

                        detailedFailuresPrinted++;

                    } else {

                        additionalFailures++;
                    }

                    continue;
                }


                /*
                 * =================================================
                 * Extract fields
                 * =================================================
                 *
                 * substring() is safe here because we already
                 * verified that the line has exactly 53 chars.
                 */

                String mrn =
                        line.substring(
                                MRN_START,
                                MRN_END
                        ).trim();

                String lastName =
                        line.substring(
                                LAST_NAME_START,
                                LAST_NAME_END
                        ).trim();

                String firstName =
                        line.substring(
                                FIRST_NAME_START,
                                FIRST_NAME_END
                        ).trim();

                String dob =
                        line.substring(
                                DOB_START,
                                DOB_END
                        ).trim();

                String checksum =
                        line.substring(
                                CHECKSUM_START,
                                CHECKSUM_END
                        ).trim();


                /*
                 * =================================================
                 * Checksum validation
                 * =================================================
                 *
                 * Checksum rule:
                 *
                 * 1. Sum ASCII values of characters 0-50.
                 * 2. Take % 97.
                 * 3. Compare with the two-character checksum.
                 */

                int calculatedChecksum =
                        calculateChecksum(line);

                String expectedChecksum =
                        String.format(
                                "%02d",
                                calculatedChecksum
                        );


                if (!expectedChecksum.equals(
                        checksum)) {

                    checksumFailures++;

                    String message =
                            "Checksum mismatch. "
                                    + "Expected "
                                    + expectedChecksum
                                    + " but found "
                                    + checksum
                                    + ".";

                    if (detailedFailuresPrinted
                            < MAX_DETAILED_FAILURES) {

                        printFailure(
                                totalLines,
                                message
                                        + " MRN="
                                        + mrn
                                        + ", lastName="
                                        + lastName
                                        + ", firstName="
                                        + firstName
                                        + ", dob="
                                        + dob
                        );

                        detailedFailuresPrinted++;

                    } else {

                        additionalFailures++;
                    }

                    continue;
                }


                /*
                 * =================================================
                 * Valid record
                 * =================================================
                 */

                validLines++;
            }


        } catch (IOException e) {

            /*
             * Do not print a stack trace.
             */
            System.out.println(
                    "Unable to read roster file: "
                            + e.getMessage()
            );

            System.exit(1);
        }


        /*
         * ========================================================
         * Final report
         * ========================================================
         */

        System.out.println();

        System.out.println(
                "============================================================"
        );

        System.out.println(
                "FIXED-WIDTH ROSTER VALIDATION SUMMARY"
        );

        System.out.println(
                "============================================================"
        );

        System.out.println(
                "Input file              : "
                        + inputPath
        );

        System.out.println(
                "Total lines             : "
                        + totalLines
        );

        System.out.println(
                "Valid lines             : "
                        + validLines
        );

        System.out.println(
                "Wrong-length lines      : "
                        + invalidLengthLines
        );

        System.out.println(
                "Checksum failures       : "
                        + checksumFailures
        );

        System.out.println(
                "Detailed failures shown : "
                        + detailedFailuresPrinted
        );

        if (additionalFailures > 0) {

            System.out.println(
                    "Additional failures     : "
                            + additionalFailures
            );
        }

        System.out.println(
                "============================================================"
        );
    }


    /*
     * ============================================================
     * Calculate checksum
     * ============================================================
     *
     * The checksum is calculated from characters 0 through 50
     * inclusive.
     *
     * That means substring(0, 51).
     *
     * ASCII values are represented by the char numeric values
     * for the characters used by this fixed-width format.
     */
    private static int calculateChecksum(
            String line) {

        int sum = 0;

        /*
         * Characters 0-50 inclusive.
         */
        for (int i = 0;
             i <= 50;
             i++) {

            sum += line.charAt(i);
        }

        return sum % 97;
    }


    /*
     * ============================================================
     * Print failure
     * ============================================================
     */

    private static void printFailure(
            int lineNumber,
            String message) {

        System.out.println(
                "FAIL | line "
                        + lineNumber
                        + " | "
                        + message
        );
    }
    public static List<String[]> parse(Path inputPath) {

        List<String[]> records = new ArrayList<>();

        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException(
                    "Input file not found: " + inputPath
            );
        }

        if (!Files.isReadable(inputPath)) {
            throw new IllegalArgumentException(
                    "Input file is not readable: " + inputPath
            );
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             inputPath,
                             StandardCharsets.UTF_8
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {

                /*
                 * Reuse the same Q6 validation rule:
                 * the record must contain exactly 53 characters.
                 */
                if (line.length() != EXPECTED_LINE_LENGTH) {
                    continue;
                }

                /*
                 * Reuse the same Q6 field positions.
                 */
                String mrn =
                        line.substring(
                                MRN_START,
                                MRN_END
                        ).trim();

                String lastName =
                        line.substring(
                                LAST_NAME_START,
                                LAST_NAME_END
                        ).trim();

                String firstName =
                        line.substring(
                                FIRST_NAME_START,
                                FIRST_NAME_END
                        ).trim();

                String dob =
                        line.substring(
                                DOB_START,
                                DOB_END
                        ).trim();

                String checksum =
                        line.substring(
                                CHECKSUM_START,
                                CHECKSUM_END
                        ).trim();

                /*
                 * Reuse the Q6 checksum method.
                 */
                int calculatedChecksum =
                        calculateChecksum(line);

                String expectedChecksum =
                        String.format(
                                "%02d",
                                calculatedChecksum
                        );

                /*
                 * Only valid records are returned.
                 */
                if (!expectedChecksum.equals(checksum)) {
                    continue;
                }

                records.add(
                        new String[]{
                                mrn,
                                lastName,
                                firstName,
                                dob
                        }
                );
            }

        } catch (IOException exception) {
            throw new RuntimeException(
                    "Unable to read roster file: "
                            + inputPath,
                    exception
            );
        }

        return records;
    }
}