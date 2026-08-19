package com.signia.training.a01basics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.InputMismatchException;
import java.util.Scanner;

public class PatientIntakeConsole {

    private static final int MAX_PATIENTS = 5;

    /*
     * Fixed-size array as required by the assignment.
     * No ArrayList or other collection is used.
     */
    private static final Patient[] patients =
            new Patient[MAX_PATIENTS];

    private static int patientCount = 0;

    /*
     * Stretch:
     * Save and reload captured patients from this CSV file.
     */
    private static final Path CSV_FILE =
            Paths.get("patients.csv");

    /*
     * Strict formatter.
     *
     * We use "uuuu" instead of "yyyy" because LocalDate works
     * with a year-of-era/year representation, and STRICT parsing
     * needs the proleptic year represented by "uuuu".
     */
    private static final DateTimeFormatter DOB_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {

        /*
         * Exactly ONE Scanner for the entire program.
         *
         * We never create another Scanner for System.in and we
         * deliberately do not close this Scanner because closing
         * it would also close System.in.
         */
        Scanner scanner = new Scanner(System.in);

        /*
         * Stretch:
         * If patients.csv exists, reload previously captured records.
         */
        loadPatientsFromCsv();

        boolean running = true;

        /*
         * while requirement:
         * Continue showing the menu until the user chooses Exit.
         */
        while (running) {

            printMenu();

            int choice;

            try {

                choice = scanner.nextInt();

                /*
                 * IMPORTANT nextInt()/nextLine() newline trap:
                 *
                 * nextInt() reads the integer but leaves the
                 * newline character entered by the user in Scanner.
                 *
                 * If we immediately call nextLine(), it would
                 * consume that leftover newline instead of reading
                 * the next actual line of user input.
                 *
                 * Therefore we deliberately consume the newline here.
                 */
                scanner.nextLine();

            } catch (InputMismatchException e) {

                System.out.println(
                        "Menu choice must be a number. "
                                + "Please enter 1, 2, 3, or 4."
                );

                /*
                 * Remove the invalid token so the next loop
                 * iteration can read fresh input.
                 */
                scanner.nextLine();

                continue;
            }

            switch (choice) {

                case 1 -> capturePatient(scanner);

                case 2 -> listPatients();

                case 3 -> {
                    System.out.println(
                            "Exiting Patient Intake System."
                    );
                    running = false;
                }

                case 4 -> savePatientsToCsv();

                default -> System.out.println(
                        "Invalid option. Please enter 1, 2, 3, or 4."
                );
            }
        }

        /*
         * Do NOT call scanner.close().
         *
         * Closing Scanner(System.in) would close System.in.
         */
    }

    private static void printMenu() {

        System.out.println();
        System.out.println("=================================");
        System.out.println("      PATIENT INTAKE SYSTEM");
        System.out.println("=================================");
        System.out.println("1. New intake");
        System.out.println("2. List captured");
        System.out.println("3. Exit");
        System.out.println("4. Save to CSV");
        System.out.println("---------------------------------");
        System.out.print("Choose an option: ");
    }

    private static void capturePatient(Scanner scanner) {

        /*
         * Refuse gracefully when the fixed-size array is full.
         */
        if (patientCount >= patients.length) {

            System.out.println();
            System.out.println(
                    "Patient storage is full. "
                            + "No more patients can be captured."
            );

            return;
        }

        System.out.println();
        System.out.println("---------- NEW PATIENT INTAKE ----------");

        String mrn = readMrn(scanner);

        String firstName = readName(
                scanner,
                "Enter first name: ",
                "First name"
        );

        String lastName = readName(
                scanner,
                "Enter last name: ",
                "Last name"
        );

        String dob = readDob(scanner);

        String phone = readPhone(scanner);

        String visitType = readVisitType(scanner);

        Patient patient = new Patient(
                mrn,
                firstName,
                lastName,
                dob,
                phone,
                visitType
        );

        patients[patientCount] = patient;
        patientCount++;

        System.out.println();
        System.out.println(
                "Patient captured successfully."
        );
    }

    private static String readMrn(Scanner scanner) {

        /*
         * do-while requirement.
         *
         * We must ask for the MRN at least once.
         */
        do {

            System.out.print("Enter MRN: ");

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {

                System.out.println(
                        "MRN cannot be empty."
                );

                continue;
            }

            if (!containsOnlyAlphaNumeric(input)) {

                System.out.println(
                        "MRN must contain only letters and numbers, "
                                + "you entered: " + input
                );

                continue;
            }

            return input;

        } while (true);
    }

    private static String readName(
            Scanner scanner,
            String prompt,
            String fieldName) {

        while (true) {

            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {

                System.out.println(
                        fieldName + " cannot be empty."
                );

                continue;
            }

            if (!isValidName(input)) {

                System.out.println(
                        fieldName
                                + " must contain letters and may include "
                                + "spaces, hyphens, or apostrophes. "
                                + "You entered: "
                                + input
                );

                continue;
            }

            return input;
        }
    }

    private static String readDob(Scanner scanner) {

        while (true) {

            System.out.print(
                    "Enter date of birth (dd-MM-yyyy): "
            );

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {

                System.out.println(
                        "DOB cannot be empty."
                );

                continue;
            }

            try {

                LocalDate.parse(
                        input,
                        DOB_FORMATTER
                );

                return input;

            } catch (DateTimeParseException e) {

                System.out.println(
                        "DOB must be dd-MM-yyyy, "
                                + "you entered " + input
                );
            }
        }
    }

    private static String readPhone(Scanner scanner) {

        while (true) {

            System.out.print("Enter phone: ");

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {

                System.out.println(
                        "Phone cannot be empty."
                );

                continue;
            }

            if (!input.matches("\\d{10}")) {

                System.out.println(
                        "Phone must contain exactly 10 digits, "
                                + "you entered: " + input
                );

                continue;
            }

            return input;
        }
    }

    private static String readVisitType(Scanner scanner) {

        while (true) {

            System.out.println();
            System.out.println("Choose visit type:");
            System.out.println("1. ROUTINE");
            System.out.println("2. FOLLOW_UP");
            System.out.println("3. HOSPICE");
            System.out.println("4. EMERGENCY");
            System.out.print("Enter visit type: ");

            try {

                int choice = scanner.nextInt();

                /*
                 * nextInt() leaves the newline behind.
                 * Consume it before the next nextLine().
                 */
                scanner.nextLine();

                String visitType = switch (choice) {

                    case 1 -> "ROUTINE";
                    case 2 -> "FOLLOW_UP";
                    case 3 -> "HOSPICE";
                    case 4 -> "EMERGENCY";

                    default -> null;
                };

                /*
                 * Ternary operator requirement.
                 *
                 * If switch returned null, the choice was invalid.
                 */
                String validationMessage =
                        visitType == null
                                ? "Visit type must be a number from 1 to 4."
                                : "Visit type selected: " + visitType;

                System.out.println(validationMessage);

                if (visitType != null) {
                    return visitType;
                }

            } catch (InputMismatchException e) {

                System.out.println(
                        "Visit type must be a number from 1 to 4."
                );

                /*
                 * Remove invalid input such as "abc" so the next
                 * iteration can continue normally.
                 */
                scanner.nextLine();
            }
        }
    }

    private static void listPatients() {

        System.out.println();
        System.out.println("-------------- CAPTURED PATIENTS --------------");

        if (patientCount == 0) {

            System.out.println(
                    "No patients have been captured."
            );

            return;
        }

        /*
         * System.out.printf is specifically required by the assignment.
         */
        System.out.printf(
                "%-12s %-15s %-15s %-12s %-12s %-12s%n",
                "MRN",
                "FIRST NAME",
                "LAST NAME",
                "DOB",
                "PHONE",
                "VISIT TYPE"
        );

        System.out.println(
                "------------------------------------------------------------------------"
        );

        for (int i = 0; i < patientCount; i++) {

            Patient patient = patients[i];

            System.out.printf(
                    "%-12s %-15s %-15s %-12s %-12s %-12s%n",
                    patient.getMrn(),
                    patient.getFirstName(),
                    patient.getLastName(),
                    patient.getDob(),
                    patient.getPhone(),
                    patient.getVisitType()
            );
        }

        System.out.println(
                "Total patients: " + patientCount
        );
    }

    /*
     * ---------------------------------------------------------
     * Q1 STRETCH — SAVE TO CSV
     * ---------------------------------------------------------
     */

    private static void savePatientsToCsv() {

        // Nothing to save.
        if (patientCount == 0) {

            System.out.println(
                    "No patients to save. "
                            + "Capture at least one patient before saving to CSV."
            );

            return;
        }

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             CSV_FILE,
                             StandardCharsets.UTF_8)) {

            writer.write(
                    "mrn,firstName,lastName,dob,phone,visitType"
            );

            writer.newLine();

            for (int i = 0; i < patientCount; i++) {

                Patient patient = patients[i];

                writer.write(
                        patient.getMrn()
                                + ","
                                + patient.getFirstName()
                                + ","
                                + patient.getLastName()
                                + ","
                                + patient.getDob()
                                + ","
                                + patient.getPhone()
                                + ","
                                + patient.getVisitType()
                );

                writer.newLine();
            }

            System.out.println(
                    patientCount
                            + " patient(s) saved successfully to "
                            + CSV_FILE.toAbsolutePath()
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not save patients to CSV: "
                            + e.getMessage()
            );
        }
    }

    /*
     * ---------------------------------------------------------
     * Q1 STRETCH — RELOAD FROM CSV
     * ---------------------------------------------------------
     */

    private static void loadPatientsFromCsv() {

        /*
         * If there is no previous CSV, this is the first launch.
         * Nothing needs to be loaded.
         */
        if (!Files.exists(CSV_FILE)) {
            return;
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             CSV_FILE,
                             StandardCharsets.UTF_8)) {

            /*
             * First line is the CSV header.
             */
            String header = reader.readLine();

            if (header == null) {
                return;
            }

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                /*
                 * -1 tells split() to preserve trailing empty fields.
                 */
                String[] columns = line.split(",", -1);

                /*
                 * Our saved CSV must contain exactly six fields.
                 */
                if (columns.length != 6) {

                    System.out.println(
                            "Skipping malformed saved record: "
                                    + line
                    );

                    continue;
                }

                /*
                 * Never exceed our fixed-size array.
                 */
                if (patientCount >= patients.length) {

                    System.out.println(
                            "Patient array is full. "
                                    + "Remaining saved records were not loaded."
                    );

                    break;
                }

                Patient patient = new Patient(
                        columns[0].trim(),
                        columns[1].trim(),
                        columns[2].trim(),
                        columns[3].trim(),
                        columns[4].trim(),
                        columns[5].trim()
                );

                patients[patientCount] = patient;
                patientCount++;
            }

            System.out.println(
                    patientCount
                            + " patient(s) loaded from "
                            + CSV_FILE.toAbsolutePath()
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not load patients from CSV: "
                            + e.getMessage()
            );
        }
    }

    /*
     * ---------------------------------------------------------
     * VALIDATION HELPERS
     * ---------------------------------------------------------
     */

    private static boolean containsOnlyAlphaNumeric(
            String value) {

        for (int i = 0; i < value.length(); i++) {

            char character = value.charAt(i);

            if (!Character.isLetterOrDigit(character)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidName(String value) {

        for (int i = 0; i < value.length(); i++) {

            char character = value.charAt(i);

            if (Character.isLetter(character)) {
                continue;
            }

            if (character == ' '
                    || character == '-'
                    || character == '\'') {
                continue;
            }

            return false;
        }

        return true;
    }
}