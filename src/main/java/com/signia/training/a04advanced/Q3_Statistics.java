package com.signia.training.a04advanced;

/**
 * Statistics accumulator used by the Q3 custom Collector stretch.
 *
 * One pass computes:
 * count
 * sum
 * minimum
 * maximum
 * average
 */
public final class Q3_Statistics {
    private long count;
    private long sum;
    private int min;
    private int max;
    private boolean empty;
    public Q3_Statistics() {
        count = 0;
        sum = 0;
        min = 0;
        max = 0;
        empty = true;
    }

    /**
     * Adds one visit to this accumulator.
     */
    public void accept(Q3_Visit visit) {

        int duration = visit.durationMins();

        if (empty) {
            min = duration;
            max = duration;
            empty = false;

        } else {
            min = Math.min(min, duration);
            max = Math.max(max, duration);
        }
        count++;
        sum += duration;
    }

    /**
     * Combines another accumulator.
     *
     * This is required by Collector even though Q3 does not
     * use parallelStream().
     */
    public Q3_Statistics combine(
            Q3_Statistics other) {

        if (other.empty) {
            return this;
        }

        if (this.empty) {
            this.count = other.count;
            this.sum = other.sum;
            this.min = other.min;
            this.max = other.max;
            this.empty = false;

            return this;
        }
        this.count += other.count;
        this.sum += other.sum;
        this.min = Math.min(this.min, other.min);
        this.max = Math.max(this.max, other.max);

        return this;
    }

    /**
     * Collector finisher.
     *
     * Returns this immutable-style result snapshot.
     */
    public Q3_Statistics finish() {

        return this;
    }

    public long count() {
        return count;
    }
    public long sum() {
        return sum;
    }
    public int min() {
        return min;
    }
    public int max() {
        return max;
    }
    public double average() {
        if (count == 0) {
            return 0.0;
        }
        return (double) sum / count;
    }

    @Override
    public String toString() {

        if (count == 0) {
            return "Q3_Statistics[count=0]";
        }

        return "Q3_Statistics[" + "count=" + count + ", sum=" + sum + ", min=" + min + ", max=" + max + ", average=" + average() + "]";
    }
}