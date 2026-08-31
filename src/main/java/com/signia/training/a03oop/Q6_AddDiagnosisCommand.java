package com.signia.training.a03oop;

public class Q6_AddDiagnosisCommand
        implements Q6_Command {

    private final Q6_RecordStore store;
    private final String recordId;
    private final String diagnosis;

    private boolean applied;

    public Q6_AddDiagnosisCommand(
            Q6_RecordStore store,
            String recordId,
            String diagnosis) {

        this.store = store;
        this.recordId = recordId;
        this.diagnosis = diagnosis;
    }

    @Override
    public void apply() {

        Q6_RecordStore.PatientRecord record =
                store.getRecord(recordId);

        if (record == null) {
            throw new IllegalArgumentException(
                    "Record not found: " + recordId
            );
        }

        store.addRecord(
                record.withDiagnosis(
                        diagnosis
                )
        );

        applied = true;
    }

    @Override
    public void revert() {

        if (!applied) {
            return;
        }

        Q6_RecordStore.PatientRecord record =
                store.getRecord(recordId);

        if (record == null) {
            return;
        }

        java.util.List<String> diagnoses =
                new java.util.ArrayList<>(
                        record.diagnoses()
                );

        /*
         * Remove the diagnosis added by this command.
         */
        for (int i = diagnoses.size() - 1; i >= 0; i--) {

            if (diagnoses.get(i).equals(diagnosis)) {
                diagnoses.remove(i);
                break;
            }
        }

        store.addRecord(
                new Q6_RecordStore.PatientRecord(
                        record.id(),
                        record.fields(),
                        diagnoses
                )
        );
    }
}