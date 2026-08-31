package com.signia.training.a04advanced;

/**
 * Demonstrates Java generic type erasure.
 */
public final class Q1_TypeErasureDemo {

    private Q1_TypeErasureDemo() {
    }

    /*
     * TYPE ERASURE DEMONSTRATION
     *
     * The following two methods cannot coexist:
     *
     * static void process(List<String> values) {
     * }
     *
     * static void process(List<Integer> values) {
     * }
     *
     * Compiler error:
     *
     * name clash:
     * process(java.util.List<java.lang.Integer>)
     * and
     * process(java.util.List<java.lang.String>)
     * have the same erasure
     *
     * Explanation:
     *
     * Java generics are implemented using type erasure.
     *
     * At runtime, both:
     *
     *     List<String>
     *
     * and:
     *
     *     List<Integer>
     *
     * become:
     *
     *     List
     *
     * Therefore the JVM cannot distinguish the two methods
     * using their generic element type.
     *
     * We keep the invalid overloads commented out so the
     * project itself remains compilable.
     */
}