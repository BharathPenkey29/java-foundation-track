package com.signia.training.a01basics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

public class EligibilityReport {

    /*
     * Input and output paths are relative.
     *
     * This means there are no machine-specific absolute paths
     * such as C:\Users\...
     */
    private static final Path INPUT_FILE =
            Paths.get(
                    "src",
                    "main",
                    "resources",
                    "a01",
                    "patients.csv"
            );

    private static final Path OUTPUT_FILE =
            Paths.get("eligible-patients.txt");

    /*
     * Expected CSV structure.
     *
     * Keep this in one place rather than repeating the expected
     * header in multiple methods.
     */
    private static final String EXPECTED_HEADER =
            "mrn,lastName,firstName,dob,visitType,provider";

    private static final int EXPECTED_COLUMN_COUNT = 6;

    /*
     * The reference date is captured once.
     *
     * Every patient's age is calculated against the same date.
     */
    private static final LocalDate REFERENCE_DATE =
            LocalDate.now();

    public static void main(String[] args) {

        /*
         * Check the input file before trying to read it.
         *
         * The assignment requires one clear message and exit code 1
         * if the input file is missing.
         */
        if (!Files.exists(INPUT_FILE)) {

            System.out.println(
                    "Input file not found: "
                            + INPUT_FILE
            );

            System.exit(1);
        }

        /*
         * Arrays only.
         *
         * The assignment does not require collections for Q2.
         *
         * We know the fixture has 28 data rows, but using a larger
         * fixed-size array makes this program safer if the input grows.
         */
        Patient[] eligiblePatients =
                new Patient[1000];

        int eligibleCount = 0;
        int rowsRead = 0;
        int rowsSkipped = 0;

        System.out.println(
                "Reading patient roster: "
                        + INPUT_FILE
        );

        /*
         * try-with-resources:
         *
         * The BufferedReader is automatically closed when this
         * block finishes.
         */
        try (BufferedReader reader =
                     Files.newBufferedReader(
                             INPUT_FILE,
                             StandardCharsets.UTF_8)) {

            /*
             * The first line is the CSV header.
             */
            String header = reader.readLine();

            if (header == null) {

                System.out.println(
                        "Input file is empty."
                );

                writeReport(
                        eligiblePatients,
                        eligibleCount,
                        rowsRead,
                        rowsSkipped
                );

                return;
            }

            /*
             * Check that the header is exactly what we expect.
             */
            if (!EXPECTED_HEADER.equals(header)) {

                System.out.println(
                        "Invalid CSV header. Expected: "
                                + EXPECTED_HEADER
                                + ", but found: "
                                + header
                );

                return;
            }

            String line;

            /*
             * Read the file exactly once.
             *
             * Each line is processed here and is never reread.
             */
            while ((line = reader.readLine()) != null) {

                rowsRead++;

                /*
                 * Blank line is one of the deliberately malformed
                 * rows in the assignment.
                 */
                if (line.isBlank()) {

                    rowsSkipped++;

                    System.out.println(
                            "Row "
                                    + rowsRead
                                    + " skipped: blank row."
                    );

                    continue;
                }

                /*
                 * -1 is important here.
                 *
                 * String.split() normally discards trailing empty
                 * strings. For example:
                 *
                 * "A,B,"
                 *
                 * without -1 can lose the final empty field.
                 *
                 * split(",", -1) preserves trailing empty fields,
                 * allowing us to validate them correctly.
                 */
                String[] columns =
                        line.split(",", -1);

                /*
                 * A valid patient row must contain exactly
                 * six columns.
                 */
                if (columns.length != EXPECTED_COLUMN_COUNT) {

                    rowsSkipped++;

                    System.out.println(
                            "Row "
                                    + rowsRead
                                    + " skipped: expected "
                                    + EXPECTED_COLUMN_COUNT
                                    + " columns but found "
                                    + columns.length
                    );

                    continue;
                }

                /*
                 * Trim each field before validation.
                 */
                String mrn = columns[0].trim();
                String lastName = columns[1].trim();
                String firstName = columns[2].trim();
                String dobText = columns[3].trim();
                String visitType = columns[4].trim();
                String provider = columns[5].trim();

                /*
                 * Validate mandatory/basic fields.
                 */
                if (mrn.isEmpty()) {

                    rowsSkipped++;

                    System.out.println(
                            "Row "
                                    + rowsRead
                                    + " skipped: MRN is blank."
                    );

                    continue;
                }

                if (lastName.isEmpty()) {

                    rowsSkipped++;

                    System.out.println(
                            "Row "
                                    + rowsRead
                                    + " skipped: last name is blank."
                    );

                    continue;
                }

                if (firstName.isEmpty()) {

                    rowsSkipped++;

                    System.out.println(
                            "Row "
                                    + rowsRead
                                    + " skipped: first name is blank."
                    );

                    continue;
                }

                if (dobText.isEmpty()) {

                    rowsSkipped++;

                    System.out.println(
                            "Row "
                                    + rowsRead
                                    + " skipped: DOB is blank."
                    );

                    continue;
                }

                /*
                 * Q2 requires dates in the CSV to be yyyy-MM-dd.
                 *
                 * LocalDate.parse() uses ISO yyyy-MM-dd by default.
                 */
                LocalDate dateOfBirth;

                try {

                    dateOfBirth =
                            LocalDate.parse(dobText);

                } catch (DateTimeParseException e) {

                    rowsSkipped++;

                    System.out.println(
                            "Row "
                                    + rowsRead
                                    + " skipped: "
                                    + "DOB is not a valid yyyy-MM-dd date: "
                                    + dobText
                    );

                    continue;
                }

                /*
                 * Convert the valid row into a Patient object.
                 *
                 * Q2 does not contain phone, so we use an empty
                 * string for phone.
                 */
                Patient patient = new Patient(
                        mrn,
                        firstName,
                        lastName,
                        dobText,
                        "",
                        visitType,
                        provider
                );

                /*
                 * Calculate age using Period.
                 *
                 * DO NOT use:
                 *
                 * currentYear - birthYear
                 *
                 * because that gives the wrong result before
                 * the patient's birthday.
                 */
                Period agePeriod =
                        Period.between(
                                dateOfBirth,
                                REFERENCE_DATE
                        );

                int age = agePeriod.getYears();

                /*
                 * Eligibility rule:
                 *
                 * age >= 65
                 * OR
                 * visit type is HOSPICE
                 */
                boolean eligible =
                        age >= 65
                                || "HOSPICE".equalsIgnoreCase(
                                visitType
                        );

                if (eligible) {

                    if (eligibleCount
                            >= eligiblePatients.length) {

                        System.out.println(
                                "Eligible patient array is full. "
                                        + "Remaining eligible rows "
                                        + "cannot be stored."
                        );

                        break;
                    }

                    eligiblePatients[eligibleCount] =
                            patient;

                    eligibleCount++;
                }
            }

        } catch (IOException e) {

            /*
             * Do not print a stack trace.
             * Give the user a useful message instead.
             */
            System.out.println(
                    "Could not read input file: "
                            + e.getMessage()
            );

            System.exit(1);
        }

        /*
         * Now create the human-readable report.
         */
        writeReport(
                eligiblePatients,
                eligibleCount,
                rowsRead,
                rowsSkipped
        );
    }

    private static void writeReport(
            Patient[] eligiblePatients,
            int eligibleCount,
            int rowsRead,
            int rowsSkipped) {

        /*
         * try-with-resources for the writer.
         *
         * UTF-8 is explicitly specified so non-ASCII names are
         * written correctly on every operating system.
         */
        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             OUTPUT_FILE,
                             StandardCharsets.UTF_8)) {

            writer.write(
                    "ELIGIBLE PATIENT REPORT"
            );

            writer.newLine();

            writer.write(
                    "Reference date: "
                            + REFERENCE_DATE
            );

            writer.newLine();
            writer.newLine();

            /*
             * Aligned columns.
             */
            writer.write(
                    String.format(
                            "%-12s %-20s %-20s %-12s %-12s %-25s",
                            "MRN",
                            "LAST NAME",
                            "FIRST NAME",
                            "DOB",
                            "VISIT TYPE",
                            "PROVIDER"
                    )
            );

            writer.newLine();

            writer.write(
                    "------------------------------------------------------------------------------------------------"
            );

            writer.newLine();

            /*
             * Write eligible patients.
             */
            for (int i = 0; i < eligibleCount; i++) {

                Patient patient =
                        eligiblePatients[i];

                writer.write(
                        String.format(
                                "%-12s %-20s %-20s %-12s %-12s %-25s",
                                patient.getMrn(),
                                patient.getLastName(),
                                patient.getFirstName(),
                                patient.getDob(),
                                patient.getVisitType(),
                                patient.getProvider()
                        )
                );

                writer.newLine();
            }

            writer.newLine();

            /*
             * Footer.
             */
            writer.write(
                    rowsRead
                            + " rows read."
            );

            writer.newLine();
            int parsedRows = rowsRead - rowsSkipped;

            writer.write(
                    parsedRows
                            + " rows parsed successfully."
            );

            writer.newLine();

            writer.write(
                    eligibleCount
                            + " of "
                            + parsedRows
                            + " rows eligible."
            );

            writer.newLine();

            writer.write(
                    rowsSkipped
                            + " rows skipped."
            );

            writer.newLine();

            writer.write(
                    "Reference date: "
                            + REFERENCE_DATE
            );

            writer.newLine();

            /*
             * Tell the user where the report was created.
             */
            System.out.println(
                    "Report created successfully: "
                            + OUTPUT_FILE.toAbsolutePath()
            );

            System.out.println(
                    "Rows read: "
                            + rowsRead
            );

            System.out.println(
                    "Rows eligible: "
                            + eligibleCount
            );

            System.out.println(
                    "Rows skipped: "
                            + rowsSkipped
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not write report file: "
                            + e.getMessage()
            );

            System.exit(1);
        }
    }
}