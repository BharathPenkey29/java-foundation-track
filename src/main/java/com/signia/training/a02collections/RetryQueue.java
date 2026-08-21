package com.signia.training.a02collections;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RetryQueue {

    private static final int MAX_ATTEMPTS = 3;

    public static void main(String[] args) {

        /*
         * ArrayDeque is preferred over the legacy Stack class.
         *
         * Stack extends Vector and exposes old indexed/vector-style operations
         * that are not appropriate for a queue/deque abstraction.
         *
         * ArrayDeque directly models a double-ended queue.
         * Since Java 21, ArrayDeque implements SequencedCollection, so methods
         * such as addFirst(), addLast(), removeFirst(), and removeLast()
         * explicitly describe head and tail operations.
         */
        ArrayDeque<Job> retryQueue = new ArrayDeque<>();
        List<Job> deadLetterQueue = new ArrayList<>();

        /*
         * Seeded Random makes the simulation reproducible.
         */
        Random random = new Random(14);

        LocalDateTime now = LocalDateTime.now();

        /*
         * Create sample jobs.
         */
        retryQueue.addLast(
                new Job("JOB-001", now.minusDays(10))
        );

        retryQueue.addLast(
                new Job("JOB-002", now.minusDays(5))
        );

        retryQueue.addLast(
                new Job("JOB-003", now.minusDays(12))
        );

        retryQueue.addLast(
                new Job("JOB-004", now.minusDays(2))
        );

        retryQueue.addLast(
                new Job("JOB-005", now.minusDays(15))
        );

        int succeeded = 0;
        int deadLettered = 0;
        int totalAttempts = 0;

        System.out.println("===== RETRY QUEUE RUN =====");

        /*
         * FIFO processing.
         */
        while (!retryQueue.isEmpty()) {

            Job job = retryQueue.removeFirst();

            job.incrementAttempt();
            totalAttempts++;

            /*
             * Simulate approximately 30% failure.
             */
            boolean failed = random.nextDouble() < 0.30;

            if (!failed) {

                succeeded++;

                System.out.println(
                        job.getJobId()
                                + " | attempt "
                                + job.getAttempt()
                                + " | SUCCESS"
                );

            } else {

                job.setLastError("Simulated failure");

                System.out.println(
                        job.getJobId()
                                + " | attempt "
                                + job.getAttempt()
                                + " | FAILED"
                );

                if (job.getAttempt() < MAX_ATTEMPTS) {

                    retryQueue.addLast(job);

                    System.out.println(
                            "  -> requeued for retry"
                    );

                } else {

                    deadLetterQueue.add(job);

                    deadLettered++;

                    System.out.println(
                            "  -> moved to dead letter queue"
                    );
                }
            }
        }

        /*
         * Final tally.
         */
        System.out.println();
        System.out.println("===== FINAL TALLY =====");

        System.out.println(
                "Succeeded: " + succeeded
        );

        System.out.println(
                "Dead-lettered: " + deadLettered
        );

        System.out.println(
                "Total attempts consumed: " + totalAttempts
        );

        /*
         * =====================================================
         * DEAD LETTERED - MOST RECENT FIRST
         * =====================================================
         *
         * List.reversed() returns a view.
         * It does not create a new independent list.
         */
        System.out.println();
        System.out.println(
                "===== DEAD LETTERED - MOST RECENT FIRST ====="
        );

        /*
         * List.reversed() returns a live reversed view of the original list.
         *
         * Reading or iterating this view does not modify the original list.
         * However, structural modifications made through the reversed view
         * would also affect the original list.
         *
         * This loop only reads from the view, so the original list is unchanged.
         */
        List<Job> reversedDeadLetters =
                deadLetterQueue.reversed();

        for (Job job : reversedDeadLetters) {
            System.out.println(job);
        }

        /*
         * =====================================================
         * PURGE JOBS OLDER THAN 7 DAYS
         * =====================================================
         */

        LocalDateTime cutoff =
                now.minusDays(7);

        /*
         * =====================================================
         * STRETCH: MUTATION BUG DEMONSTRATION
         * =====================================================
         *
         * The following approach is WRONG:
         *
         * for (Job job : deadLetterQueue) {
         *
         *     if (job.getSubmittedAt().isBefore(cutoff)) {
         *         deadLetterQueue.remove(job);
         *     }
         * }
         *
         * It causes ConcurrentModificationException because
         * the ArrayList is structurally modified while its
         * iterator is being used by the for-each loop.
         *
         * We intentionally DO NOT execute the broken code.
         */

        System.out.println();
        System.out.println(
                "===== PURGE DEMONSTRATION ====="
        );
        /*
         * INTENTIONALLY BROKEN CODE
         *
         * Removing directly from an ArrayList while using a for-each loop
         * modifies the list while its iterator is active.
         *
         * This is intentionally executed once to demonstrate the
         * ConcurrentModificationException required by the assignment.
         */
        /*
         * =====================================================
         * BROKEN APPROACH: for-each + list.remove()
         * =====================================================
         *
         * The following code is intentionally broken:
         *
         * for (Job job : deadLetterQueue) {
         *
         *     if (job.getSubmittedAt().isBefore(cutoff)) {
         *         deadLetterQueue.remove(job);
         *     }
         * }
         *
         * ArrayList's enhanced for-loop uses an Iterator internally.
         * Calling deadLetterQueue.remove(job) structurally modifies the
         * ArrayList while that Iterator is still active.
         *
         * The Iterator detects the modification and throws
         * ConcurrentModificationException.
         *
         * REAL STACK TRACE FROM THIS ASSIGNMENT:
         *
         * java.util.ConcurrentModificationException
         *     at java.util.ArrayList$Itr.checkForComodification (ArrayList.java:1104)
         *     at java.util.ArrayList$Itr.next (ArrayList.java:1058)
         *     at com.signia.training.a02collections.RetryQueue.main (RetryQueue.java:214)
         *     at org.codehaus.mojo.exec.ExecJavaMojo.doMain (ExecJavaMojo.java:385)
         *     at org.codehaus.mojo.exec.ExecJavaMojo.doExec (ExecJavaMojo.java:374)
         *     at org.codehaus.mojo.exec.ExecJavaMojo.lambda$execute$0 (ExecJavaMojo.java:296)
         *     at java.lang.Thread.run (Thread.java:1516)
         *
         * The Maven/exec-maven-plugin wrapper stack trace is intentionally
         * not included because the important application exception is the
         * ConcurrentModificationException and its ArrayList iterator frames.
         */
        /*
         * =====================================================
         * SAFE APPROACH 1: Iterator.remove()
         * =====================================================
         *
         * Use a copy so Iterator.remove() can be demonstrated
         * independently from the final purge.
         */
        List<Job> iteratorVersion =
                new ArrayList<>(deadLetterQueue);

        Iterator<Job> iterator =
                iteratorVersion.iterator();

        while (iterator.hasNext()) {

            Job job = iterator.next();

            if (job.getSubmittedAt().isBefore(cutoff)) {
                iterator.remove();
            }
        }

        System.out.println(
                "After Iterator.remove(): "
                        + iteratorVersion.size()
                        + " jobs remain"
        );

        /*
         * =====================================================
         * SAFE APPROACH 2: removeIf()
         * =====================================================
         *
         * Again use a copy so the two techniques are
         * demonstrated independently.
         */
        List<Job> removeIfVersion =
                new ArrayList<>(deadLetterQueue);

        removeIfVersion.removeIf(
                job -> job.getSubmittedAt().isBefore(cutoff)
        );

        System.out.println(
                "After removeIf(): "
                        + removeIfVersion.size()
                        + " jobs remain"
        );

        /*
         * =====================================================
         * ACTUAL PURGE
         * =====================================================
         *
         * removeIf() is concise and safe for ArrayList.
         */
        deadLetterQueue.removeIf(
                job -> job.getSubmittedAt().isBefore(cutoff)
        );

        System.out.println();
        System.out.println(
                "===== FINAL DEAD-LETTER LIST ====="
        );

        System.out.println(
                "Remaining dead-letter jobs: "
                        + deadLetterQueue.size()
        );

        for (Job job : deadLetterQueue) {
            System.out.println(job);
        }
    }
}