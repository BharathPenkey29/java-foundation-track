package com.signia.training.a04advanced;

import java.time.LocalTime;

/**
 * Represents a provider who is free for the requested period.
 */
public record Q6_FreeProvider(
        String provider,
        LocalTime start,
        LocalTime end,
        long freeMinutes) {

    @Override
    public String toString() {
        return provider + " free from " + start + " to " + end + " (" + freeMinutes + " minutes)";
    }
}