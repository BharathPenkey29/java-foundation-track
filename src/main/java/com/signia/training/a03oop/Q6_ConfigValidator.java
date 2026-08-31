package com.signia.training.a03oop;

import java.util.ArrayDeque;
import java.util.Deque;

public class Q6_ConfigValidator {

    public static ValidationResult validate(String config) {

        Deque<Character> stack = new ArrayDeque<>();

        char quote = '\0';

        for (int i = 0; i < config.length(); i++) {

            char current = config.charAt(i);

            /*
             * If we are inside a quoted string, brackets are
             * ordinary characters.
             */
            if (quote != '\0') {

                if (current == quote) {
                    quote = '\0';
                }

                continue;
            }

            /*
             * Start a quoted string.
             */
            if (current == '"' || current == '\'') {
                quote = current;
                continue;
            }

            /*
             * Opening brackets go onto the stack.
             */
            if (isOpeningBracket(current)) {
                stack.push(current);
                continue;
            }

            /*
             * Closing bracket must match the latest opening bracket.
             */
            if (isClosingBracket(current)) {

                if (stack.isEmpty()) {

                    return new ValidationResult(
                            false,
                            i,
                            current,
                            "opening bracket"
                    );
                }

                char opening = stack.pop();

                char expected =
                        matchingClosingBracket(opening);

                if (current != expected) {

                    return new ValidationResult(
                            false,
                            i,
                            current,
                            "'" + expected + "'"
                    );
                }
            }
        }

        /*
         * Unterminated quote.
         */
        if (quote != '\0') {

            return new ValidationResult(
                    false,
                    config.length(),
                    quote,
                    "closing quote '" + quote + "'"
            );
        }

        /*
         * Remaining opening bracket means it never closed.
         */
        if (!stack.isEmpty()) {

            char opening = stack.peek();

            return new ValidationResult(
                    false,
                    config.length(),
                    '\0',
                    "'" + matchingClosingBracket(opening) + "'"
            );
        }

        return new ValidationResult(
                true,
                -1,
                '\0',
                ""
        );
    }

    private static boolean isOpeningBracket(char value) {

        return value == '('
                || value == '['
                || value == '{';
    }

    private static boolean isClosingBracket(char value) {

        return value == ')'
                || value == ']'
                || value == '}';
    }

    private static char matchingClosingBracket(
            char opening) {

        return switch (opening) {
            case '(' -> ')';
            case '[' -> ']';
            case '{' -> '}';
            default -> throw new IllegalArgumentException(
                    "Not an opening bracket: " + opening
            );
        };
    }

    public record ValidationResult(
            boolean valid,
            int index,
            char found,
            String expected) {

        public String message() {

            if (valid) {
                return "Configuration is valid.";
            }

            return "Invalid configuration at index "
                    + index
                    + ": found '"
                    + found
                    + "', expected "
                    + expected
                    + ".";
        }
    }
}