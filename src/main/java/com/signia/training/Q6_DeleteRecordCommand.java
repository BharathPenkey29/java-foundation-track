package com.signia.training;

public class Q6_DeleteRecordCommand
        implements Q6_Command {

    private final Q6_RecordStore store;
    private final String recordId;

    private Q6_RecordStore.PatientRecord deletedRecord;

    public Q6_DeleteRecordCommand(
            Q6_RecordStore store,
            String recordId) {

        this.store = store;
        this.recordId = recordId;
    }

    @Override
    public void apply() {

        deletedRecord =
                store.removeRecord(recordId);

        if (deletedRecord == null) {

            throw new IllegalArgumentException(
                    "Record not found: " + recordId
            );
        }
    }

    @Override
    public void revert() {

        if (deletedRecord != null) {

            store.addRecord(
                    deletedRecord
            );
        }
    }
}