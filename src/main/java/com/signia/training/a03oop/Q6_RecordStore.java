package com.signia.training.a03oop;

import java.util.LinkedHashMap;
import java.util.Map;

public class Q6_RecordStore {

    private final Map<String, PatientRecord> records =
            new LinkedHashMap<>();

    public void addRecord(PatientRecord record) {
        records.put(record.id(), record);
    }

    public PatientRecord getRecord(String id) {
        return records.get(id);
    }

    public PatientRecord removeRecord(String id) {
        return records.remove(id);
    }

    public boolean containsRecord(String id) {
        return records.containsKey(id);
    }

    public Map<String, PatientRecord> snapshot() {
        return Map.copyOf(records);
    }

    public void printState() {
        System.out.println("Store state: " + records);
    }

    public record PatientRecord(
            String id,
            Map<String, String> fields,
            java.util.List<String> diagnoses) {

        public PatientRecord {
            fields = new LinkedHashMap<>(fields);
            diagnoses = new java.util.ArrayList<>(diagnoses);
        }

        public PatientRecord withField(
                String field,
                String value) {

            Map<String, String> updatedFields =
                    new LinkedHashMap<>(fields);

            updatedFields.put(field, value);

            return new PatientRecord(
                    id,
                    updatedFields,
                    diagnoses
            );
        }

        public PatientRecord withDiagnosis(
                String diagnosis) {

            java.util.List<String> updatedDiagnoses =
                    new java.util.ArrayList<>(diagnoses);

            updatedDiagnoses.add(diagnosis);

            return new PatientRecord(
                    id,
                    fields,
                    updatedDiagnoses
            );
        }
    }
}