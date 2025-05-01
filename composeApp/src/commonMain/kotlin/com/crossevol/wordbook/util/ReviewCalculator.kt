package com.crossevol.wordbook.util

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

/**
 * Utility class for Ebbinghaus forgetting curve calculations and spaced repetition logic.
 * Implements a spaced repetition system (SRS) based on memory retention research.
 */
object ReviewCalculator {

    /**
     * Calculate next review time based on review progress (Ebbinghaus forgetting curve).
     *
     * @param reviewProgress The review progress level (0-7).
     * @param immediateReview If true, sets review time to now (for testing or immediate review).
     * @return The time in milliseconds when the next review should occur.
     */
    fun calculateNextReviewTime(
        reviewProgress: Long,
        immediateReview: Boolean = false
    ): Long {
        val currentTime = System.currentTimeMillis()

        if (immediateReview) {
            return currentTime
        }

        // Apply Ebbinghaus forgetting curve intervals based on review progress
        val nextReviewOffset = when (reviewProgress) {
            0L   -> 10.minutes  // 10 minutes
            1L   -> 1.hours     // 1 hour
            2L   -> 1.days      // 1 day
            3L   -> 7.days      // 1 week
            4L   -> 14.days     // 2 weeks
            5L   -> 30.days     // 1 month
            6L   -> 60.days     // 2 months
            else -> 180.days  // 6 months (half year)
        }

        // Convert Duration to milliseconds and add to current time
        return currentTime + nextReviewOffset.toLong(DurationUnit.MILLISECONDS)
    }

    /**
     * Calculate new progress level and next review time based on current progress and whether
     * the word was remembered or forgotten.
     *
     * @param currentProgress The current progress level (0-7)
     * @param remembered Whether the user remembered the word
     * @param immediateReview If true, sets next review time to now (for testing)
     * @return Pair of (newProgressLevel, nextReviewTime)
     */
    fun calculateReviewUpdate(
        currentProgress: Long,
        remembered: Boolean,
        immediateReview: Boolean = false
    ): Pair<Long, Long> {
        val newProgress = if (remembered) {
            // Increase progress level, but cap at 7
            minOf(
                currentProgress + 1,
                7
            )
        } else {
            // Reset to 0 if forgotten
            maxOf(
                currentProgress - 1,
                0
            )
        }

        // Calculate the next review time based on new progress
        val nextReviewTime = calculateNextReviewTime(
            newProgress,
            immediateReview
        )

        return Pair(
            newProgress,
            nextReviewTime
        )
    }

    /**
     * Gets a human-readable description of when the next review will be based on progress level.
     *
     * @param reviewProgress The review progress level (0-7)
     * @return A human-readable string describing the next review interval
     */
    fun getReviewIntervalDescription(reviewProgress: Long): String {
        return when (reviewProgress) {
            0L   -> "10 minutes"
            1L   -> "1 hour"
            2L   -> "1 day"
            3L   -> "1 week"
            4L   -> "2 weeks"
            5L   -> "1 month"
            6L   -> "2 months"
            else -> "6 months"
        }
    }
} 