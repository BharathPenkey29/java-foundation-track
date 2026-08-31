package com.signia.training.a04advanced;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Required tests for A4 Q6 interval merging.
 */
class IntervalMergerTest {

    @Test
    void emptyListProducesNoMergedIntervals() {

        List<Q6_MergedInterval> result =
                Q6_IntervalMerger.merge(
                        List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void singleIntervalRemainsUnchanged() {

        Q6_VisitInterval interval =
                new Q6_VisitInterval(
                        "K. Rao",
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 45));

        List<Q6_MergedInterval> result =
                Q6_IntervalMerger.merge(
                        List.of(interval));

        assertEquals(1, result.size());

        assertEquals(
                LocalTime.of(9, 0),
                result.get(0).start());

        assertEquals(
                LocalTime.of(9, 45),
                result.get(0).end());
    }

    @Test
    void allIntervalsOverlappingBecomeOneBlock() {

        List<Q6_VisitInterval> intervals =
                List.of(
                        interval(
                                "K. Rao",
                                9, 0,
                                10, 0),

                        interval(
                                "K. Rao",
                                9, 30,
                                11, 0),

                        interval(
                                "K. Rao",
                                10, 15,
                                12, 0),

                        interval(
                                "K. Rao",
                                11, 30,
                                13, 0)
                );

        List<Q6_MergedInterval> result =
                Q6_IntervalMerger.merge(intervals);

        assertEquals(1, result.size());

        assertEquals(
                LocalTime.of(9, 0),
                result.get(0).start());

        assertEquals(
                LocalTime.of(13, 0),
                result.get(0).end());
    }

    @Test
    void fullyContainedIntervalDoesNotCreateExtraBlock() {

        List<Q6_VisitInterval> intervals =
                List.of(
                        interval(
                                "P. Adeyemi",
                                10, 0,
                                11, 30),

                        interval(
                                "P. Adeyemi",
                                10, 45,
                                11, 0)
                );

        List<Q6_MergedInterval> result =
                Q6_IntervalMerger.merge(intervals);

        assertEquals(1, result.size());

        assertEquals(
                LocalTime.of(10, 0),
                result.get(0).start());

        assertEquals(
                LocalTime.of(11, 30),
                result.get(0).end());
    }

    @Test
    void touchingIntervalsCollapseIntoOneBlock() {

        List<Q6_VisitInterval> intervals =
                List.of(
                        interval(
                                "K. Rao",
                                10, 0,
                                10, 15),

                        interval(
                                "K. Rao",
                                10, 15,
                                11, 0)
                );

        List<Q6_MergedInterval> result =
                Q6_IntervalMerger.merge(intervals);

        /*
         * Touching intervals are merged into one busy block.
         */
        assertEquals(1, result.size());

        assertEquals(
                LocalTime.of(10, 0),
                result.get(0).start());

        assertEquals(
                LocalTime.of(11, 0),
                result.get(0).end());

        /*
         * But touching intervals are NOT a double-booking.
         */
        List<Q6_VisitInterval[]> conflicts =
                Q6_IntervalMerger
                        .findDoubleBookings(intervals);

        assertTrue(conflicts.isEmpty());
    }

    @Test
    void actualOverlapIsReportedAsDoubleBooking() {

        List<Q6_VisitInterval> intervals =
                List.of(
                        interval(
                                "K. Rao",
                                9, 0,
                                9, 45),

                        interval(
                                "K. Rao",
                                9, 30,
                                10, 15)
                );

        List<Q6_VisitInterval[]> conflicts =
                Q6_IntervalMerger
                        .findDoubleBookings(intervals);

        assertEquals(1, conflicts.size());

        assertEquals(
                LocalTime.of(9, 0),
                conflicts.get(0)[0].start());

        assertEquals(
                LocalTime.of(9, 30),
                conflicts.get(0)[1].start());
    }

    @Test
    void binarySearchFindsBusyTime() {

        List<Q6_MergedInterval> merged =
                List.of(
                        merged(9, 0, 11, 0),
                        merged(13, 0, 13, 30),
                        merged(14, 20, 16, 0)
                );

        assertTrue(
                Q6_IntervalMerger.isBusyAt(
                        merged,
                        LocalTime.of(14, 20)));

        assertTrue(
                Q6_IntervalMerger.isBusyAt(
                        merged,
                        LocalTime.of(14, 45)));

        assertFalse(
                Q6_IntervalMerger.isBusyAt(
                        merged,
                        LocalTime.of(14, 0)));

        /*
         * Half-open boundary:
         * 16:00 is not busy.
         */
        assertFalse(
                Q6_IntervalMerger.isBusyAt(
                        merged,
                        LocalTime.of(16, 0)));
    }

    private static Q6_VisitInterval interval(
            String provider,
            int startHour,
            int startMinute,
            int endHour,
            int endMinute) {

        return new Q6_VisitInterval(
                provider,
                LocalTime.of(
                        startHour,
                        startMinute),
                LocalTime.of(
                        endHour,
                        endMinute));
    }

    private static Q6_MergedInterval merged(
            int startHour,
            int startMinute,
            int endHour,
            int endMinute) {

        return new Q6_MergedInterval(
                LocalTime.of(
                        startHour,
                        startMinute),
                LocalTime.of(
                        endHour,
                        endMinute));
    }
}