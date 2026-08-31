package com.signia.training.a04advanced;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates Q1 Result, repository, PECS and stretch combine.
 */
public class Q1_ResultDemo {

    public static void main(String[] args) {

        System.out.println("==========================================");

        System.out.println("A4 Q1 - GENERIC RESULT AND REPOSITORY");

        System.out.println("==========================================");

        demonstrateResult();

        demonstrateRepository();

        demonstratePecs();

        demonstrateCombine();

        demonstrateTypeErasure();
    }

    private static void demonstrateResult() {

        System.out.println();
        System.out.println("--- RESULT CHAIN ---");

        Q1_Result<String> result = Q1_Result.ok("  MRN001  ");

        /*
         * MAP #1
         *
         * Successful transformation.
         */
        result = result.map(String::trim);

        /*
         * MIDDLE STEP
         *
         * This operation can fail, so flatMap is the correct
         * Result operation.
         */
        result = result.flatMap(
                        value -> {
                            System.out.println("Middle step executing for: " + value);
                            return Q1_Result.fail("PATIENT_NOT_FOUND", "Patient was not found");
                        }
                );

        /*
         * MAP #3
         *
         * Because result is already Fail, this function is
         * NOT executed.
         */
        result = result.map(
                        value -> {

                            System.out.println("ERROR: third map should not execute");
                            return value.toUpperCase();
                        }
                );

        System.out.println("Result successful: " + result.isOk());

        System.out.println("Fallback: " + result.orElse("NO PATIENT"));

        /*
         * Print the failure code explicitly.
         */
        switch (result) {

            case Q1_Result.Ok<String>(String value) -> {
                System.out.println("Unexpected success: " + value);
            }

            case Q1_Result.Fail<String>(
                    String code, String message) -> {
                System.out.println("Error code: " + code);
                System.out.println("Error message: " + message);
            }
        }

        /*
         * ifOk() should not execute because the result is Fail.
         */
        result.ifOk(value -> System.out.println("This must not print: " + value));
    }

    private static void demonstrateRepository() {

        System.out.println();
        System.out.println("--- GENERIC REPOSITORY ---");

        Q1_MapRepository<
                Q1_Patient,
                String> repository = new Q1_MapRepository<>();

        Q1_Patient patient1 = new Q1_Patient("MRN001", "John Smith");

        Q1_Patient patient2 = new Q1_Patient("MRN002", "Jane Doe");

        repository.save(patient1);
        repository.save(patient2);

        System.out.println("Repository size: " + repository.size());

        System.out.println("Find MRN001: " + repository.findById("MRN001"));

        System.out.println("All patients: " + repository.findAll());

        System.out.println("Delete MRN002: " + repository.deleteById("MRN002"));

        System.out.println("Repository size after delete: " + repository.size());
    }

    private static void demonstratePecs() {

        System.out.println();
        System.out.println("--- PECS ---");

        List<Integer> integers =
                List.of(
                        10,
                        20,
                        30
                );

        double sum = Q1_MapRepository.sumNumbers(integers);

        System.out.println("Sum using extends: " + sum);

        List<Number> numbers = new ArrayList<>();

        Q1_MapRepository.addIntegers(
                numbers
        );

        System.out.println("List after super consumer: " + numbers);
    }

    private static void demonstrateCombine() {

        System.out.println();
        System.out.println("--- STRETCH: RESULT.COMBINE ---");

        List<Q1_Result<String>> allSuccessful =
                List.of(Q1_Result.ok("A"), Q1_Result.ok("B"), Q1_Result.ok("C"));

        Q1_Result<List<String>> combinedSuccess = Q1_Result.combine( allSuccessful );

        System.out.println( "All successful: " + combinedSuccess );

        List<Q1_Result<String>> mixedResults =
                List.of( Q1_Result.ok("A"),
                        Q1_Result.fail("TIMEOUT", "First operation timed out"),

                        Q1_Result.fail("NOT_FOUND", "Second record was not found"),

                        Q1_Result.fail("VALIDATION", "Third operation failed validation")
                );

        Q1_Result<List<String>> combinedFailure = Q1_Result.combine( mixedResults );

        System.out.println( "Combined failure: " + combinedFailure );

        switch (combinedFailure) {

            case Q1_Result.Ok<List<String>>(List<String> values) -> {
                System.out.println("Unexpected combined success: " + values);}

            case Q1_Result.Fail<List<String>>(String code, String message) -> {

                System.out.println("Aggregated error codes: " + code);

                System.out.println("Combined error message: " + message);
            }
        }
    }

    private static void demonstrateTypeErasure() {

        System.out.println();
        System.out.println("--- TYPE ERASURE ---");
        System.out.println("List<String> and List<Integer> " + "cannot overload the same method");
        System.out.println("because both erase to List at runtime.");
    }
}