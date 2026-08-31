package com.signia.training.a03oop;

import java.util.ArrayDeque;
import java.util.Deque;

public class Q6_EditSession {

    private final Deque<Q6_Command> undoStack =
            new ArrayDeque<>();

    private final Deque<Q6_Command> redoStack =
            new ArrayDeque<>();

    private final Q6_RecordStore store;

    public Q6_EditSession(
            Q6_RecordStore store) {

        this.store = store;
    }

    public void execute(Q6_Command command) {

        command.apply();

        undoStack.push(command);

        /*
         * A new edit creates a new branch of history.
         */
        redoStack.clear();

        printState(
                "APPLY"
        );
    }

    public void undo() {

        if (undoStack.isEmpty()) {

            System.out.println(
                    "UNDO: nothing to undo."
            );

            printState(
                    "UNDO"
            );

            return;
        }

        Q6_Command command =
                undoStack.pop();

        command.revert();

        redoStack.push(command);

        printState(
                "UNDO"
        );
    }

    public void redo() {

        if (redoStack.isEmpty()) {

            System.out.println(
                    "REDO: nothing to redo."
            );

            printState(
                    "REDO"
            );

            return;
        }

        Q6_Command command =
                redoStack.pop();

        command.apply();

        undoStack.push(command);

        printState(
                "REDO"
        );
    }

    private void printState(String operation) {

        System.out.println(
                operation
                        + " -> "
                        + store.snapshot()
        );
    }
}