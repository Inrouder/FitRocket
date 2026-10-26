package de.inrouder.fitrocket;

import java.time.LocalDate;
import java.util.function.ToIntFunction;

/**
 * Pure, calendar-based FitRocket weekly allowance.
 * The unused-day transfer is awarded once a day has FINISHED.
 * No ledger mutation is necessary, so editing older diary entries recalculates the week.
 */
public final class FitRocketWeeklyWallet {
    public static final int DEFAULT_WEEKLY_POINTS = 28;
    public static final int MAX_DAILY_TRANSFER = 4;

    private FitRocketWeeklyWallet() {}

    public static LocalDate weekMonday(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("date");
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    public static int dailyTransfer(int dailyBudget, int pointsUsed) {
        if (dailyBudget <= 0 || pointsUsed < 0) throw new IllegalArgumentException("day");
        return Math.min(MAX_DAILY_TRANSFER, Math.max(0, dailyBudget - pointsUsed));
    }

    /**
     * Credits the elapsed days of the CURRENT week only (Monday to yesterday).
     * A day without diary entries has used zero points and may earn up to four.
     * Today is intentionally never credited until it has ended.
     */
    public static int rolloversWeek(LocalDate today, int dailyBudget, ToIntFunction<LocalDate> pointsUsed) {
        if (pointsUsed == null || dailyBudget <= 0) throw new IllegalArgumentException("input");
        int total = 0;
        for (LocalDate day = weekMonday(today); day.isBefore(today); day = day.plusDays(1)) {
            total += dailyTransfer(dailyBudget, pointsUsed.applyAsInt(day));
        }
        return total;
    }

    /** Daily overruns are immediately charged against the week's available points. */
    public static int spentWeek(LocalDate today, int dailyBudget, ToIntFunction<LocalDate> pointsUsed) {
        if (pointsUsed == null || dailyBudget <= 0) throw new IllegalArgumentException("input");
        int total = 0;
        for (LocalDate day = weekMonday(today); !day.isAfter(today); day = day.plusDays(1)) {
            total += Math.max(0, pointsUsed.applyAsInt(day) - dailyBudget);
        }
        return total;
    }

    public static int remaining(int weeklyStart, int dailyTransfers, int earnedActivityCredit, int spentOverBudget) {
        if (weeklyStart < 0 || dailyTransfers < 0 || earnedActivityCredit < 0 || spentOverBudget < 0)
            throw new IllegalArgumentException("weekly");
        long total = (long) weeklyStart + dailyTransfers + earnedActivityCredit - spentOverBudget;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, total));
    }
}
