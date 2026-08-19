package com.signia.training.a01basics;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class Identifiers {

    /*
     * ============================================================
     * Utility class
     * ============================================================
     *
     * This class contains only static utility methods.
     *
     * The private constructor prevents someone from creating
     * an object of this class.
     */
    private Identifiers() {
        throw new AssertionError(
                "Utility class should not be instantiated."
        );
    }

    /*
     * ============================================================
     * Q3 - normalizeMrn()
     * Character loop + StringBuilder implementation
     * ============================================================
     *
     * Requirements:
     *
     * 1. Trim input
     * 2. Convert to uppercase
     * 3. Remove every non-alphanumeric character
     * 4. Left-pad with zeros
     * 5. Result must be exactly 8 characters
     * 6. Reject values longer than 8 characters after cleaning
     */
    public static String normalizeMrn(String input) {

        validateInput(input, "MRN");

        String value =
                input
                        .trim()
                        .toUpperCase(Locale.ROOT);

        StringBuilder cleaned =
                new StringBuilder();

        /*
         * Process the input character by character.
         *
         * Only A-Z and 0-9 are retained.
         */
        for (int i = 0; i < value.length(); i++) {

            char current =
                    value.charAt(i);

            if ((current >= 'A' && current <= 'Z')
                    || (current >= '0' && current <= '9')) {

                cleaned.append(current);
            }
        }

        /*
         * Input may contain only special characters.
         *
         * Example:
         *
         * "@#$%"
         *
         * After cleaning:
         *
         * ""
         */
        if (cleaned.length() == 0) {

            throw new IllegalArgumentException(
                    "MRN must contain at least one "
                            + "alphanumeric character."
            );
        }

        /*
         * Reject values longer than 8 characters after
         * removing non-alphanumeric characters.
         */
        if (cleaned.length() > 8) {

            throw new IllegalArgumentException(
                    "MRN cannot be longer than 8 characters "
                            + "after cleaning: "
                            + cleaned
            );
        }

        /*
         * Left-pad with zeros until the MRN contains
         * exactly 8 characters.
         */
        while (cleaned.length() < 8) {

            cleaned.insert(0, '0');
        }

        return cleaned.toString();
    }

    /*
     * ============================================================
     * Q3 - normalizeMrnRegex()
     * Regex implementation
     * ============================================================
     *
     * The assignment requires BOTH implementations.
     *
     * We keep this implementation for comparison.
     *
     * I would ship the character-loop implementation because
     * the operation is simple character filtering and the loop
     * makes the logic explicit without regex overhead.
     */
    public static String normalizeMrnRegex(String input) {

        validateInput(input, "MRN");

        String cleaned =
                input
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replaceAll("[^A-Z0-9]", "");

        if (cleaned.length() == 0) {

            throw new IllegalArgumentException(
                    "MRN must contain at least one "
                            + "alphanumeric character."
            );
        }

        if (cleaned.length() > 8) {

            throw new IllegalArgumentException(
                    "MRN cannot be longer than 8 characters "
                            + "after cleaning: "
                            + cleaned
            );
        }

        StringBuilder result =
                new StringBuilder();

        /*
         * Add zeroes until the final result has
         * exactly 8 characters.
         */
        while (result.length() + cleaned.length() < 8) {

            result.append('0');
        }

        result.append(cleaned);

        return result.toString();
    }

    /*
     * ============================================================
     * Q3 - toIsoDate()
     * ============================================================
     *
     * Supported input formats:
     *
     * 1. dd/MM/yyyy
     * 2. dd-MM-yyyy
     * 3. MM/dd/yyyy
     * 4. yyyyMMdd
     *
     * Output format:
     *
     * yyyy-MM-dd
     *
     * Important:
     *
     * Values such as 01/02/2026 are ambiguous because they
     * can represent:
     *
     * 1 February 2026
     * OR
     * January 2nd 2026
     *
     * Therefore we reject them instead of guessing.
     */
    public static String toIsoDate(String input) {

        validateInput(input, "Date");

        String value =
                input.trim();

        /*
         * ========================================================
         * Slash formats
         * ========================================================
         *
         * Possible formats:
         *
         * dd/MM/yyyy
         * MM/dd/yyyy
         */
        if (value.matches("\\d{2}/\\d{2}/\\d{4}")) {

            boolean validDayMonth =
                    isValidDate(
                            value,
                            "dd/MM/uuuu"
                    );

            boolean validMonthDay =
                    isValidDate(
                            value,
                            "MM/dd/uuuu"
                    );

            /*
             * Both interpretations are valid.
             *
             * Example:
             *
             * 01/02/2026
             *
             * Could mean:
             *
             * 1 February
             * OR
             * January 2
             */
            if (validDayMonth && validMonthDay) {

                throw new IllegalArgumentException(
                        "Ambiguous date: "
                                + input
                                + ". It can be interpreted "
                                + "as both dd/MM/yyyy and "
                                + "MM/dd/yyyy."
                );
            }

            /*
             * Only dd/MM/yyyy is valid.
             */
            if (validDayMonth) {

                return parseDate(
                        value,
                        DateTimeFormatter.ofPattern(
                                "dd/MM/uuuu"
                        )
                );
            }

            /*
             * Only MM/dd/yyyy is valid.
             */
            if (validMonthDay) {

                return parseDate(
                        value,
                        DateTimeFormatter.ofPattern(
                                "MM/dd/uuuu"
                        )
                );
            }

            /*
             * Neither interpretation is valid.
             */
            throw new IllegalArgumentException(
                    "Invalid date: " + input
            );
        }

        /*
         * ========================================================
         * dd-MM-yyyy
         * ========================================================
         *
         * Example:
         *
         * 18-08-2026
         */
        if (value.matches("\\d{2}-\\d{2}-\\d{4}")) {

            return parseDate(
                    value,
                    DateTimeFormatter.ofPattern(
                            "dd-MM-uuuu"
                    )
            );
        }

        /*
         * ========================================================
         * yyyyMMdd
         * ========================================================
         *
         * Example:
         *
         * 20260818
         */
        if (value.matches("\\d{8}")) {

            return parseDate(
                    value,
                    DateTimeFormatter.ofPattern(
                            "uuuuMMdd"
                    )
            );
        }

        /*
         * None of the supported formats matched.
         */
        throw new IllegalArgumentException(
                "Unsupported date format: "
                        + input
                        + ". Expected dd/MM/yyyy, "
                        + "dd-MM-yyyy, MM/dd/yyyy or yyyyMMdd."
        );
    }

    /**
     * Masks a person's name while preserving the original length.
     *
     * Example:
     * "Rosa Alvarez" -> "R*** A*****"
     *
     * The first character of each name part remains visible.
     * All remaining non-space characters are replaced with '*'.
     * Spaces are preserved exactly as they appear.
     */
    public static String maskPhi(String fullName) {

        if (fullName == null) {
            throw new IllegalArgumentException(
                    "Full name cannot be null."
            );
        }

        if (fullName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Full name cannot be empty."
            );
        }

        StringBuilder masked =
                new StringBuilder(fullName.length());

        boolean firstCharacterOfWord = true;

        for (int i = 0; i < fullName.length(); i++) {

            char current = fullName.charAt(i);

            /*
             * Preserve spaces exactly.
             *
             * When we encounter a space, the next non-space
             * character starts a new name part.
             */
            if (Character.isWhitespace(current)) {

                masked.append(current);

                firstCharacterOfWord = true;

            } else if (firstCharacterOfWord) {

                /*
                 * Keep the first character of each name part.
                 */
                masked.append(current);

                firstCharacterOfWord = false;

            } else {

                /*
                 * Mask every remaining character.
                 */
                masked.append('*');
            }
        }

        return masked.toString();
    }
    /*
     * ============================================================
     * Q3 - Input validation
     * ============================================================
     *
     * The assignment specifically says:
     *
     * "Null and empty input must be handled explicitly
     * on every method."
     */
    private static void validateInput(
            String input,
            String fieldName) {

        if (input == null) {

            throw new IllegalArgumentException(
                    fieldName
                            + " cannot be null."
            );
        }

        if (input.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    fieldName
                            + " cannot be empty."
            );
        }
    }

    /*
     * ============================================================
     * Q3 - Date validation helper
     * ============================================================
     *
     * ResolverStyle.STRICT ensures invalid dates are rejected.
     *
     * Example:
     *
     * 31/02/2026
     *
     * is invalid and will return false.
     */
    private static boolean isValidDate(
            String dateText,
            String format) {

        try {

            DateTimeFormatter formatter =
                    DateTimeFormatter
                            .ofPattern(format)
                            .withResolverStyle(
                                    ResolverStyle.STRICT
                            );

            LocalDate.parse(
                    dateText,
                    formatter
            );

            return true;

        } catch (DateTimeParseException e) {

            return false;
        }
    }

    /*
     * ============================================================
     * Q3 - Date parsing helper
     * ============================================================
     *
     * We use DateTimeFormatter instead of SimpleDateFormat.
     *
     * SimpleDateFormat is mutable and not thread-safe.
     * DateTimeFormatter is immutable and thread-safe.
     */
    private static String parseDate(
            String value,
            DateTimeFormatter inputFormatter) {

        try {

            LocalDate date =
                    LocalDate.parse(
                            value,
                            inputFormatter
                    );

            return date.format(
                    DateTimeFormatter.ISO_LOCAL_DATE
            );

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Invalid date: "
                            + value,
                    e
            );
        }
    }

    /*
     * ============================================================
     * Q3 STRETCH - Levenshtein distance
     * ============================================================
     *
     * Calculates the minimum number of single-character
     * operations required to transform one string into another.
     *
     * Operations:
     *
     * 1. Insert
     * 2. Delete
     * 3. Replace
     *
     * Example:
     *
     * Smith
     * Smyth
     *
     * Distance = 1
     *
     * Time complexity:
     *
     * O(m * n)
     *
     * Space complexity:
     *
     * O(m * n)
     */
    public static int levenshtein(
            String first,
            String second) {

        if (first == null) {

            throw new IllegalArgumentException(
                    "First string cannot be null."
            );
        }

        if (second == null) {

            throw new IllegalArgumentException(
                    "Second string cannot be null."
            );
        }

        /*
         * Empty strings are valid for Levenshtein distance.
         *
         * Example:
         *
         * distance("", "Hello") = 5
         */
        int firstLength =
                first.length();

        int secondLength =
                second.length();

        /*
         * dp[i][j] represents the minimum number of edits
         * needed to transform:
         *
         * first.substring(0, i)
         *
         * into:
         *
         * second.substring(0, j)
         */
        int[][] dp =
                new int[
                        firstLength + 1
                        ][
                        secondLength + 1
                        ];

        /*
         * Transforming a string into an empty string requires
         * deleting every character.
         */
        for (int i = 0;
             i <= firstLength;
             i++) {

            dp[i][0] = i;
        }

        /*
         * Transforming an empty string into a string requires
         * inserting every character.
         */
        for (int j = 0;
             j <= secondLength;
             j++) {

            dp[0][j] = j;
        }

        /*
         * Fill the dynamic-programming table.
         */
        for (int i = 1;
             i <= firstLength;
             i++) {

            for (int j = 1;
                 j <= secondLength;
                 j++) {

                /*
                 * If the characters are the same,
                 * replacement cost is zero.
                 */
                int replacementCost =
                        first.charAt(i - 1)
                                == second.charAt(j - 1)
                                ? 0
                                : 1;

                /*
                 * Delete current character from first string.
                 */
                int deletion =
                        dp[i - 1][j] + 1;

                /*
                 * Insert current character.
                 */
                int insertion =
                        dp[i][j - 1] + 1;

                /*
                 * Replace current character.
                 */
                int replacement =
                        dp[i - 1][j - 1]
                                + replacementCost;

                /*
                 * Choose the cheapest operation.
                 */
                dp[i][j] =
                        Math.min(
                                Math.min(
                                        deletion,
                                        insertion
                                ),
                                replacement
                        );
            }
        }

        return dp[
                firstLength
                ][
                secondLength
                ];
    }

    /*
     * ============================================================
     * Q3 STRETCH - Near-duplicate surname detection
     * ============================================================
     *
     * Compares every surname with every other surname.
     *
     * If their Levenshtein distance is less than or equal to
     * the supplied threshold, they are flagged as potential
     * near-duplicates.
     *
     * Time complexity:
     *
     * O(n² * m * k)
     *
     * n = number of surnames
     * m/k = surname lengths
     *
     * We use an array because Assignment 1 specifically says
     * "No collections. Arrays only."
     */
    public static void flagNearDuplicateSurnames(
            String[] surnames,
            int threshold) {

        if (surnames == null) {

            throw new IllegalArgumentException(
                    "Surnames cannot be null."
            );
        }

        if (threshold < 0) {

            throw new IllegalArgumentException(
                    "Threshold cannot be negative."
            );
        }

        /*
         * Compare each surname with the surnames after it.
         *
         * Starting j at i + 1 prevents:
         *
         * Smith vs Smith
         * Smith vs Smyth
         * Smyth vs Smith
         *
         * We only need the second comparison.
         */
        for (int i = 0;
             i < surnames.length;
             i++) {

            if (surnames[i] == null
                    || surnames[i].trim().isEmpty()) {

                continue;
            }

            for (int j = i + 1;
                 j < surnames.length;
                 j++) {

                if (surnames[j] == null
                        || surnames[j].trim().isEmpty()) {

                    continue;
                }

                String first =
                        surnames[i]
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );

                String second =
                        surnames[j]
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );

                int distance =
                        levenshtein(
                                first,
                                second
                        );

                if (distance <= threshold) {

                    System.out.println(
                            "NEAR DUPLICATE | "
                                    + surnames[i]
                                    + " <-> "
                                    + surnames[j]
                                    + " | distance="
                                    + distance
                    );
                }
            }
        }
    }

    /*
     * ============================================================
     * MAIN
     * ============================================================
     *
     * The assignment requires:
     *
     * - At least five test cases per method
     * - Failure cases included
     * - Pass/fail output
     */
    public static void main(String[] args) {

        /*
         * ========================================================
         * normalizeMrn() tests
         * ========================================================
         */
        System.out.println(
                "========== normalizeMrn =========="
        );

        test(
                "normalizeMrn: MRN-00042",
                "MRN00042",
                () -> normalizeMrn(
                        "MRN-00042"
                )
        );

        test(
                "normalizeMrn: 42",
                "00000042",
                () -> normalizeMrn(
                        "42"
                )
        );

        test(
                "normalizeMrn: lowercase",
                "MRN00042",
                () -> normalizeMrn(
                        " mrn00042 "
                )
        );

        test(
                "normalizeMrn: special characters",
                "000ABC12",
                () -> normalizeMrn(
                        "@ABC-12"
                )
        );

        testFailure(
                "normalizeMrn: too long",
                () -> normalizeMrn(
                        "ABCDEFGHIJK"
                )
        );

        testFailure(
                "normalizeMrn: null",
                () -> normalizeMrn(
                        null
                )
        );

        testFailure(
                "normalizeMrn: empty",
                () -> normalizeMrn(
                        "   "
                )
        );

        /*
         * ========================================================
         * normalizeMrnRegex() tests
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "========== normalizeMrnRegex =========="
        );

        test(
                "normalizeMrnRegex: MRN-00042",
                "MRN00042",
                () -> normalizeMrnRegex(
                        "MRN-00042"
                )
        );

        test(
                "normalizeMrnRegex: 42",
                "00000042",
                () -> normalizeMrnRegex(
                        "42"
                )
        );

        test(
                "normalizeMrnRegex: lowercase",
                "MRN00042",
                () -> normalizeMrnRegex(
                        " mrn00042 "
                )
        );

        test(
                "normalizeMrnRegex: special characters",
                "000ABC12",
                () -> normalizeMrnRegex(
                        "@ABC-12"
                )
        );

        testFailure(
                "normalizeMrnRegex: too long",
                () -> normalizeMrnRegex(
                        "ABCDEFGHIJK"
                )
        );

        testFailure(
                "normalizeMrnRegex: null",
                () -> normalizeMrnRegex(
                        null
                )
        );

        testFailure(
                "normalizeMrnRegex: empty",
                () -> normalizeMrnRegex(
                        "   "
                )
        );

        /*
         * ========================================================
         * toIsoDate() tests
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "========== toIsoDate =========="
        );

        test(
                "toIsoDate: dd/MM/yyyy",
                "2026-08-18",
                () -> toIsoDate(
                        "18/08/2026"
                )
        );

        test(
                "toIsoDate: dd-MM-yyyy",
                "2026-08-18",
                () -> toIsoDate(
                        "18-08-2026"
                )
        );

        test(
                "toIsoDate: MM/dd/yyyy",
                "2026-08-18",
                () -> toIsoDate(
                        "08/18/2026"
                )
        );

        test(
                "toIsoDate: yyyyMMdd",
                "2026-08-18",
                () -> toIsoDate(
                        "20260818"
                )
        );

        testFailure(
                "toIsoDate: ambiguous",
                () -> toIsoDate(
                        "01/02/2026"
                )
        );

        testFailure(
                "toIsoDate: invalid date",
                () -> toIsoDate(
                        "31/02/2026"
                )
        );

        testFailure(
                "toIsoDate: unsupported format",
                () -> toIsoDate(
                        "2026.08.18"
                )
        );

        testFailure(
                "toIsoDate: null",
                () -> toIsoDate(
                        null
                )
        );

        testFailure(
                "toIsoDate: empty",
                () -> toIsoDate(
                        " "
                )
        );

        /*
         * ========================================================
         * maskPhi() tests
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "========== maskPhi =========="
        );

        test(
                "maskPhi: Rosa Alvarez",
                "R*** A******",
                () -> maskPhi(
                        "Rosa Alvarez"
                )
        );

        test(
                "maskPhi: John Smith",
                "J*** S****",
                () -> maskPhi(
                        "John Smith"
                )
        );

        test(
                "maskPhi: Bharath",
                "B******",
                () -> maskPhi(
                        "Bharath"
                )
        );

        test(
                "maskPhi: single letters",
                "A B",
                () -> maskPhi(
                        "A B"
                )
        );

        test(
                "maskPhi: multiple spaces",
                "J***  D**",
                () -> maskPhi(
                        "John  Doe"
                )
        );

        testFailure(
                "maskPhi: null",
                () -> maskPhi(
                        null
                )
        );

        testFailure(
                "maskPhi: empty",
                () -> maskPhi(
                        "   "
                )
        );

        /*
         * ========================================================
         * Levenshtein tests
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "========== levenshtein =========="
        );

        testInt(
                "levenshtein: identical strings",
                0,
                () -> levenshtein(
                        "Smith",
                        "Smith"
                )
        );

        testInt(
                "levenshtein: one replacement",
                1,
                () -> levenshtein(
                        "Smith",
                        "Smyth"
                )
        );

        testInt(
                "levenshtein: one insertion",
                1,
                () -> levenshtein(
                        "Smith",
                        "Smithh"
                )
        );

        testInt(
                "levenshtein: one deletion",
                1,
                () -> levenshtein(
                        "Smith",
                        "Smit"
                )
        );

        testInt(
                "levenshtein: multiple edits",
                3,
                () -> levenshtein(
                        "kitten",
                        "sitting"
                )
        );

        testInt(
                "levenshtein: empty string",
                5,
                () -> levenshtein(
                        "",
                        "Hello"
                )
        );

        testFailure(
                "levenshtein: null first value",
                () -> String.valueOf(levenshtein(
                        null,
                        "Smith"
                ))
        );

        testFailure(
                "levenshtein: null second value",
                () -> String.valueOf(levenshtein(
                        "Smith",
                        null
                ))
        );

        /*
         * ========================================================
         * Near-duplicate surname detection
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "========== near-duplicate surnames =========="
        );

        String[] surnames = {
                "Smith",
                "Smyth",
                "Johnson",
                "Jonson",
                "Alvarez",
                "Alverez",
                "Williams"
        };

        flagNearDuplicateSurnames(
                surnames,
                1
        );

        /*
         * ========================================================
         * Compare both MRN implementations
         * ========================================================
         */
        System.out.println();
        System.out.println(
                "========== normalizeMrn comparison =========="
        );

        String comparisonInput =
                " mrn-42 ";

        String loopResult =
                normalizeMrn(
                        comparisonInput
                );

        String regexResult =
                normalizeMrnRegex(
                        comparisonInput
                );

        if (loopResult.equals(regexResult)) {

            System.out.println(
                    "PASS | Both MRN implementations "
                            + "agree: "
                            + loopResult
            );

        } else {

            System.out.println(
                    "FAIL | Implementations disagree: "
                            + loopResult
                            + " vs "
                            + regexResult
            );
        }
    }

    /*
     * ============================================================
     * String test helper
     * ============================================================
     */
    private static void test(
            String testName,
            String expected,
            TestOperation operation) {

        try {

            String actual =
                    operation.execute();

            if (expected.equals(actual)) {

                System.out.println(
                        "PASS | "
                                + testName
                );

            } else {

                System.out.println(
                        "FAIL | "
                                + testName
                                + " | expected="
                                + expected
                                + ", actual="
                                + actual
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "FAIL | "
                            + testName
                            + " | unexpected exception: "
                            + e.getMessage()
            );
        }
    }

    /*
     * ============================================================
     * Expected failure test helper
     * ============================================================
     */
    private static void testFailure(
            String testName,
            TestOperation operation) {

        try {

            operation.execute();

            System.out.println(
                    "FAIL | "
                            + testName
                            + " | expected an exception"
            );

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "PASS | "
                            + testName
                            + " | rejected: "
                            + e.getMessage()
            );
        }
    }

    /*
     * ============================================================
     * Integer test helper
     * ============================================================
     */
    private static void testInt(
            String testName,
            int expected,
            IntTestOperation operation) {

        try {

            int actual =
                    operation.execute();

            if (expected == actual) {

                System.out.println(
                        "PASS | "
                                + testName
                );

            } else {

                System.out.println(
                        "FAIL | "
                                + testName
                                + " | expected="
                                + expected
                                + ", actual="
                                + actual
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "FAIL | "
                            + testName
                            + " | unexpected exception: "
                            + e.getMessage()
            );
        }
    }

    /*
     * ============================================================
     * Functional interface for String-returning tests
     * ============================================================
     */
    @FunctionalInterface
    private interface TestOperation {

        String execute();
    }

    /*
     * ============================================================
     * Functional interface for integer-returning tests
     * ============================================================
     */
    @FunctionalInterface
    private interface IntTestOperation {

        int execute();
    }
}