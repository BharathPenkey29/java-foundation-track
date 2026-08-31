package com.signia.training.a03oop;

public class Q6_SetFieldCommand implements Q6_Command {

    private final Q6_RecordStore store;
    private final String recordId;
    private final String field;
    private final String newValue;

    private String oldValue;
    private boolean applied;

    public Q6_SetFieldCommand(
            Q6_RecordStore store,
            String recordId,
            String field,
            String newValue) {

        this.store = store;
        this.recordId = recordId;
        this.field = field;
        this.newValue = newValue;
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

        oldValue =
                record.fields().get(field);

        store.addRecord(
                record.withField(
                        field,
                        newValue
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

        if (oldValue == null) {

            java.util.Map<String, String> fields =
                    new java.util.LinkedHashMap<>(
                            record.fields()
                    );

            fields.remove(field);

            store.addRecord(
                    new Q6_RecordStore.PatientRecord(
                            record.id(),
                            fields,
                            record.diagnoses()
                    )
            );

        } else {

            store.addRecord(
                    record.withField(
                            field,
                            oldValue
                    )
            );
        }
    }
}