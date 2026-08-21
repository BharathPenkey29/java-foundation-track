package com.signia.training.a02collections;

import java.time.LocalDate;

public class VisitTest {

    public static void main(String[] args) {

        Visit visit = new Visit(
                "V1001",
                "MRN00042",
                "K. Rao",
                LocalDate.of(2026, 8, 10),
                45,
                "COMPLETED"
        );

        System.out.println(visit);
    }
}