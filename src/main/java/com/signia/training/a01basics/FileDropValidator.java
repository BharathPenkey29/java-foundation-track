package com.signia.training.a01basics;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Assignment 1 - Q4
 *
 * File Drop Validator with Error Codes.
 *
 * Validates every CSV file in a supplied directory and reports
 * machine-readable validation findings.
 *
 * Q4 does not prohibit collections.
 * Q1 specifically requires arrays only.
 */
public class FileDropValidator {

    /*
     * ============================================================
     * Configuration
     * ============================================================
     */

    private static final String EXPECTED_HEADER =
            "mrn,lastName,firstName,dob,visitType,provider";

    private static final int EXPECTED_COLUMN_COUNT = 6;

    private static final String JSON_OUTPUT_FILE =
            "file-drop-findings.json";


    /*
     * ============================================================
     * Main
     * ============================================================
     */

    public static void main(String[] args) {

        if (args.length != 1) {

            System.out.println(
                    "Usage: FileDropValidator <directory>"
            );

            System.exit(1);
        }

        Path directory =
                Paths.get(args[0]);

        if (!Files.exists(directory)) {

            System.out.println(
                    "Directory does not exist: "
                            + directory
            );

            System.exit(1);
        }

        if (!Files.isDirectory(directory)) {

            System.out.println(
                    "Path is not a directory: "
                            + directory
            );

            System.exit(1);
        }

        List<FileValidationResult> results =
                new ArrayList<>();

        /*
         * Process every CSV file.
         *
         * Files.list() is used instead of the legacy File API.
         */
        try (Stream<Path> paths =
                     Files.list(directory)) {

            paths
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.getFileName()
                                    .toString()
                                    .toLowerCase()
                                    .endsWith(".csv")
                    )
                    .sorted(
                            Comparator.comparing(
                                    path ->
                                            path.getFileName()
                                                    .toString()
                            )
                    )
                    .forEach(path ->
                            results.add(
                                    validateFile(path)
                            )
                    );

        } catch (IOException e) {

            System.out.println(
                    "Unable to read directory: "
                            + directory
                            + " | "
                            + e.getMessage()
            );

            System.exit(1);
        }

        /*
         * Keep final output deterministic.
         */
        results.sort(
                Comparator.comparing(
                        FileValidationResult::getFileName
                )
        );

        printSummary(results);

        /*
         * Stretch requirement:
         * hand-written JSON output.
         */
        writeJsonReport(
                results,
                directory.resolve(JSON_OUTPUT_FILE)
        );

        boolean hasFailures =
                results.stream()
                        .anyMatch(
                                result ->
                                        !result.isClean()
                        );

        /*
         * 0 = all files clean
         * 1 = one or more files failed
         */
        System.exit(
                hasFailures ? 1 : 0
        );
    }


    /*
     * ============================================================
     * Validate one file
     * ============================================================
     */

    private static FileValidationResult validateFile(
            Path path) {

        String fileName =
                path.getFileName()
                        .toString();

        FileValidationResult result =
                new FileValidationResult(fileName);

        /*
         * Check file exists.
         *
         * This is an operational problem, not FILE_EMPTY.
         */
        if (!Files.exists(path)) {

            result.addOperationalError(
                    "File does not exist."
            );

            return result;
        }

        /*
         * Check readability.
         */
        if (!Files.isReadable(path)) {

            result.addOperationalError(
                    "File is not readable."
            );

            return result;
        }

        /*
         * TRN.1.1 is specifically for a zero-byte file.
         */
        try {

            if (Files.size(path) == 0) {

                result.addFinding(
                        new ValidationException(
                                ValidationCode.FILE_EMPTY,
                                fileName,
                                0,
                                "File is empty."
                        )
                );

                return result;
            }

        } catch (IOException e) {

            result.addOperationalError(
                    "Unable to determine file size: "
                            + e.getMessage()
            );

            return result;
        }


        /*
         * ========================================================
         * Read the file
         * ========================================================
         *
         * UTF-8 is explicit.
         *
         * try-with-resources automatically closes the reader.
         */
        try (BufferedReader reader =
                     Files.newBufferedReader(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            /*
             * First line = header.
             */
            String header =
                    reader.readLine();

            if (header == null) {

                result.addFinding(
                        new ValidationException(
                                ValidationCode.HEADER_MISMATCH,
                                fileName,
                                1,
                                "Header is missing."
                        )
                );

                return result;
            }

            /*
             * Header must match EXACTLY.
             */
            if (!EXPECTED_HEADER.equals(header)) {

                result.addFinding(
                        new ValidationException(
                                ValidationCode.HEADER_MISMATCH,
                                fileName,
                                1,
                                "Expected header '"
                                        + EXPECTED_HEADER
                                        + "' but found '"
                                        + header
                                        + "'."
                        )
                );
            }

            /*
             * HashSet gives average O(1) lookup.
             *
             * We need this to detect duplicate MRNs within
             * this particular file.
             */
            Set<String> seenMrns =
                    new HashSet<>();

            String line;

            /*
             * Header is row 1.
             * Therefore first data row is row 2.
             */
            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {

                rowNumber++;

                result.incrementRowsRead();

                /*
                 * IMPORTANT:
                 *
                 * -1 keeps trailing empty fields.
                 *
                 * Example:
                 *
                 * MRN001,Smith,John,2020-01-01,ROUTINE,
                 *
                 * The provider field is still counted as column 6.
                 */
                String[] columns =
                        line.split(",", -1);

                /*
                 * =================================================
                 * Check column count
                 * =================================================
                 */

                if (columns.length
                        != EXPECTED_COLUMN_COUNT) {

                    result.addFinding(
                            new ValidationException(
                                    ValidationCode.COLUMN_COUNT_MISMATCH,
                                    fileName,
                                    rowNumber,
                                    "Expected "
                                            + EXPECTED_COLUMN_COUNT
                                            + " columns but found "
                                            + columns.length
                            )
                    );

                    /*
                     * We cannot safely inspect mrn/dob because
                     * their indexes may not exist.
                     */
                    continue;
                }


                /*
                 * =================================================
                 * Mandatory MRN
                 * =================================================
                 */

                String mrn =
                        columns[0].trim();

                if (mrn.isBlank()) {

                    result.addFinding(
                            new ValidationException(
                                    ValidationCode.MANDATORY_FIELD_BLANK,
                                    fileName,
                                    rowNumber,
                                    "Mandatory field 'mrn' is blank."
                            )
                    );
                }


                /*
                 * =================================================
                 * Mandatory DOB
                 * =================================================
                 */

                String dob =
                        columns[3].trim();

                if (dob.isBlank()) {

                    result.addFinding(
                            new ValidationException(
                                    ValidationCode.MANDATORY_FIELD_BLANK,
                                    fileName,
                                    rowNumber,
                                    "Mandatory field 'dob' is blank."
                            )
                    );
                }


                /*
                 * =================================================
                 * Duplicate MRN
                 * =================================================
                 *
                 * Don't treat a blank MRN as a duplicate key.
                 * It already has the mandatory-field error.
                 */
                if (!mrn.isBlank()) {

                    if (!seenMrns.add(mrn)) {

                        result.addFinding(
                                new ValidationException(
                                        ValidationCode.DUPLICATE_KEY,
                                        fileName,
                                        rowNumber,
                                        "Duplicate MRN: "
                                                + mrn
                                )
                        );
                    }
                }
            }

        } catch (IOException e) {

            /*
             * One bad file must not stop other files.
             */
            result.addOperationalError(
                    "Unable to read file: "
                            + e.getMessage()
            );
        }

        return result;
    }


    /*
     * ============================================================
     * Summary
     * ============================================================
     */

    private static void printSummary(
            List<FileValidationResult> results) {

        System.out.println();

        System.out.println(
                "============================================================"
        );

        System.out.println(
                "FILE DROP VALIDATION SUMMARY"
        );

        System.out.println(
                "============================================================"
        );

        System.out.printf(
                "%-28s %-10s %-10s%n",
                "FILE",
                "ROWS READ",
                "STATUS"
        );

        System.out.println(
                "------------------------------------------------------------"
        );

        for (FileValidationResult result :
                results) {

            String status =
                    result.isClean()
                            ? "CLEAN"
                            : "FAILED";

            System.out.printf(
                    "%-28s %-10d %-10s%n",
                    result.getFileName(),
                    result.getRowsRead(),
                    status
            );

            /*
             * Print counts by error code.
             */
            printFindingsByCode(result);

            /*
             * Print detailed findings in stable order.
             */
            List<ValidationException> findings =
                    new ArrayList<>(
                            result.getFindings()
                    );

            findings.sort(
                    Comparator
                            .comparingInt(
                                    ValidationException::getRowNumber
                            )
                            .thenComparing(
                                    exception ->
                                            exception
                                                    .getCode()
                                                    .getCode()
                            )
                            .thenComparing(
                                    ValidationException::getMessage
                            )
            );

            for (ValidationException finding :
                    findings) {

                System.out.printf(
                        "    %-8s row %-4d %s%n",
                        finding.getCode().getCode(),
                        finding.getRowNumber(),
                        finding.getMessage()
                );
            }

            /*
             * Operational errors are separate from the
             * assignment's validation codes.
             */
            for (String error :
                    result.getOperationalErrors()) {

                System.out.println(
                        "    OPERATIONAL ERROR: "
                                + error
                );
            }
        }

        System.out.println(
                "============================================================"
        );

        boolean allClean =
                results.stream()
                        .allMatch(
                                FileValidationResult::isClean
                        );

        System.out.println(
                "Overall status: "
                        + (allClean
                        ? "CLEAN"
                        : "FAILED")
        );

        System.out.println(
                "============================================================"
        );
    }


    /*
     * ============================================================
     * Findings by code
     * ============================================================
     */

    private static void printFindingsByCode(
            FileValidationResult result) {

        if (result.getFindings().isEmpty()) {
            return;
        }

        /*
         * One count for each enum value.
         */
        int[] counts =
                new int[ValidationCode.values().length];

        for (ValidationException finding :
                result.getFindings()) {

            counts[
                    finding.getCode().ordinal()
                    ]++;
        }

        System.out.println(
                "    Findings by code:"
        );

        for (ValidationCode code :
                ValidationCode.values()) {

            int count =
                    counts[code.ordinal()];

            if (count > 0) {

                System.out.printf(
                        "        %-8s = %d%n",
                        code.getCode(),
                        count
                );
            }
        }
    }


    /*
     * ============================================================
     * JSON Stretch
     * ============================================================
     */

    private static void writeJsonReport(
            List<FileValidationResult> results,
            Path outputPath) {

        StringBuilder json =
                new StringBuilder();

        json.append("{\n");
        json.append("  \"files\": [\n");

        for (int i = 0;
             i < results.size();
             i++) {

            FileValidationResult result =
                    results.get(i);

            json.append("    {\n");

            json.append("      \"file\": \"")
                    .append(
                            escapeJson(
                                    result.getFileName()
                            )
                    )
                    .append("\",\n");

            json.append("      \"rowsRead\": ")
                    .append(
                            result.getRowsRead()
                    )
                    .append(",\n");

            json.append("      \"status\": \"")
                    .append(
                            result.isClean()
                                    ? "CLEAN"
                                    : "FAILED"
                    )
                    .append("\",\n");

            json.append(
                    "      \"findings\": [\n"
            );

            List<ValidationException> findings =
                    new ArrayList<>(
                            result.getFindings()
                    );

            findings.sort(
                    Comparator
                            .comparingInt(
                                    ValidationException::getRowNumber
                            )
                            .thenComparing(
                                    exception ->
                                            exception
                                                    .getCode()
                                                    .getCode()
                            )
                            .thenComparing(
                                    ValidationException::getMessage
                            )
            );

            for (int j = 0;
                 j < findings.size();
                 j++) {

                ValidationException finding =
                        findings.get(j);

                json.append("        {\n");

                json.append(
                                "          \"code\": \""
                        )
                        .append(
                                finding.getCode().getCode()
                        )
                        .append("\",\n");

                json.append(
                                "          \"type\": \""
                        )
                        .append(
                                finding.getCode().name()
                        )
                        .append("\",\n");

                json.append(
                                "          \"file\": \""
                        )
                        .append(
                                escapeJson(
                                        finding.getFileName()
                                )
                        )
                        .append("\",\n");

                json.append(
                                "          \"row\": "
                        )
                        .append(
                                finding.getRowNumber()
                        )
                        .append(",\n");

                json.append(
                                "          \"message\": \""
                        )
                        .append(
                                escapeJson(
                                        finding.getMessage()
                                )
                        )
                        .append("\"\n");

                json.append("        }");

                if (j < findings.size() - 1) {
                    json.append(",");
                }

                json.append("\n");
            }

            json.append("      ]\n");
            json.append("    }");

            if (i < results.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}\n");

        /*
         * try-with-resources for the writer.
         */
        try (var writer =
                     Files.newBufferedWriter(
                             outputPath,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    json.toString()
            );

            System.out.println(
                    "JSON report created: "
                            + outputPath
            );

        } catch (IOException e) {

            System.out.println(
                    "Unable to write JSON report: "
                            + e.getMessage()
            );
        }
    }


    /*
     * ============================================================
     * JSON escaping
     * ============================================================
     */

    private static String escapeJson(
            String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    /*
     * ============================================================
     * Validation codes
     * ============================================================
     *
     * All assignment error codes live in one enum.
     */
    private enum ValidationCode {

        FILE_EMPTY(
                "TRN.1.1",
                "File is empty."
        ),

        HEADER_MISMATCH(
                "TRN.1.2",
                "Header does not match expected columns."
        ),

        COLUMN_COUNT_MISMATCH(
                "TRN.2.1",
                "Column count does not match expected count."
        ),

        MANDATORY_FIELD_BLANK(
                "TRN.2.2",
                "Mandatory field is blank."
        ),

        DUPLICATE_KEY(
                "TRN.2.3",
                "Duplicate MRN found."
        );

        private final String code;

        private final String messageTemplate;

        ValidationCode(
                String code,
                String messageTemplate) {

            this.code = code;
            this.messageTemplate = messageTemplate;
        }

        public String getCode() {
            return code;
        }

        public String getMessageTemplate() {
            return messageTemplate;
        }
    }


    /*
     * ============================================================
     * Checked ValidationException
     * ============================================================
     *
     * Extends Exception, therefore it is CHECKED.
     */
    private static class ValidationException
            extends Exception {

        private final ValidationCode code;

        private final String fileName;

        private final int rowNumber;

        public ValidationException(
                ValidationCode code,
                String fileName,
                int rowNumber,
                String message) {

            super(message);

            this.code = code;
            this.fileName = fileName;
            this.rowNumber = rowNumber;
        }

        public ValidationCode getCode() {
            return code;
        }

        public String getFileName() {
            return fileName;
        }

        public int getRowNumber() {
            return rowNumber;
        }
    }


    /*
     * ============================================================
     * Result for one file
     * ============================================================
     */

    private static class FileValidationResult {

        private final String fileName;

        private int rowsRead;

        private final List<ValidationException> findings;

        private final List<String> operationalErrors;

        public FileValidationResult(
                String fileName) {

            this.fileName = fileName;

            this.rowsRead = 0;

            this.findings =
                    new ArrayList<>();

            this.operationalErrors =
                    new ArrayList<>();
        }

        public void incrementRowsRead() {
            rowsRead++;
        }

        public void addFinding(
                ValidationException finding) {

            findings.add(finding);
        }

        public void addOperationalError(
                String error) {

            operationalErrors.add(error);
        }

        public String getFileName() {
            return fileName;
        }

        public int getRowsRead() {
            return rowsRead;
        }

        public List<ValidationException> getFindings() {
            return findings;
        }

        public List<String> getOperationalErrors() {
            return operationalErrors;
        }

        public boolean isClean() {

            return findings.isEmpty()
                    && operationalErrors.isEmpty();
        }
    }
}