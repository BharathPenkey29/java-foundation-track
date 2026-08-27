package com.signia.training;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Q6_EditSessionDemo {

    public static void main(String[] args) {

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Q6 - CONFIG VALIDATOR"
        );

        System.out.println(
                "=========================================="
        );

        runConfigValidation();

        System.out.println();

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "Q6 - EDIT SESSION"
        );

        System.out.println(
                "=========================================="
        );

        runEditSession();
    }

    private static void runConfigValidation() {

        Path validPath = Path.of(
                "src/main/resources/a03/sample-config.txt"
        );

        Path brokenPath = Path.of(
                "src/main/resources/a03/sample-config-broken.txt"
        );

        try {

            String validConfig =
                    Files.readString(
                            validPath,
                            StandardCharsets.UTF_8
                    );

            String brokenConfig =
                    Files.readString(
                            brokenPath,
                            StandardCharsets.UTF_8
                    );

            Q6_ConfigValidator.ValidationResult valid =
                    Q6_ConfigValidator.validate(
                            validConfig
                    );

            System.out.println(
                    "Valid fixture: " + validPath
            );

            System.out.println(
                    valid.message()
            );

            Q6_ConfigValidator.ValidationResult broken =
                    Q6_ConfigValidator.validate(
                            brokenConfig
                    );

            System.out.println();

            System.out.println(
                    "Broken fixture: " + brokenPath
            );

            System.out.println(
                    broken.message()
            );

        } catch (IOException exception) {

            System.out.println(
                    "Unable to read configuration fixture: "
                            + exception.getMessage()
            );
        }
    }
    private static void runEditSession() {

        Q6_RecordStore store =
                new Q6_RecordStore();

        Q6_RecordStore.PatientRecord patient =
                new Q6_RecordStore.PatientRecord(
                        "P1001",
                        Map.of(
                                "name",
                                "John Smith",
                                "status",
                                "ACTIVE"
                        ),
                        List.of()
                );

        store.addRecord(patient);

        System.out.println(
                "Initial state:"
        );

        store.printState();

        Q6_EditSession session =
                new Q6_EditSession(store);

        System.out.println();

        System.out.println(
                "--- SetField ---"
        );

        session.execute(
                new Q6_SetFieldCommand(
                        store,
                        "P1001",
                        "status",
                        "INACTIVE"
                )
        );

        System.out.println();

        System.out.println(
                "--- AddDiagnosis ---"
        );

        session.execute(
                new Q6_AddDiagnosisCommand(
                        store,
                        "P1001",
                        "Hypertension"
                )
        );

        System.out.println();

        System.out.println(
                "--- DeleteRecord ---"
        );

        session.execute(
                new Q6_DeleteRecordCommand(
                        store,
                        "P1001"
                )
        );

        System.out.println();

        System.out.println(
                "--- Undo Delete ---"
        );

        session.undo();

        System.out.println();

        System.out.println(
                "--- Undo Diagnosis ---"
        );

        session.undo();

        System.out.println();

        System.out.println(
                "--- Redo Diagnosis ---"
        );

        session.redo();

        System.out.println();

        System.out.println(
                "--- New Edit After Undo ---"
        );

        /*
         * This new command must clear the redo stack.
         */
        session.execute(
                new Q6_SetFieldCommand(
                        store,
                        "P1001",
                        "status",
                        "ACTIVE"
                )
        );

        System.out.println();

        System.out.println(
                "--- Attempt Redo After New Edit ---"
        );

        session.redo();

        System.out.println();

        System.out.println(
                "--- Undo Until Empty ---"
        );

        session.undo();
        session.undo();
        session.undo();
        session.undo();

        /*
         * Empty undo must be a no-op.
         */
        session.undo();
    }
}