package de.inrouder.fitrocket;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketWeeklyWalletTest {
    private static LocalDate d(String value){return LocalDate.parse(value);}

    @Test public void mondayStartsWith28WithoutPriorWeekCredits() {
        Map<LocalDate,Integer> consumed=new HashMap<>();
        consumed.put(d("2026-10-25"),0); // Prior Sunday must not be credited
        assertEquals(28,FitRocketWeeklyWallet.DEFAULT_WEEKLY_POINTS);
        assertEquals(0,FitRocketWeeklyWallet.rolloversWeek(d("2026-10-26"),23,
                 day->consumed.getOrDefault(day,0)));
        assertEquals(28,FitRocketWeeklyWallet.remaining(28,0,0,0));
        assertEquals(0,FitRocketWeeklyWallet.rolloversWeek(d("2026-11-02"),23,day->0));
    }

    @Test public void creditsOnlyCompletedDaysWithFourPointCap() {
        Map<LocalDate,Integer> consumed=new HashMap<>();
        consumed.put(d("2026-10-26"),20); // Three unused
        consumed.put(d("2026-10-27"),12); // Eleven unused, capped at four
        consumed.put(d("2026-10-28"),1);  // Wednesday is still in progress
        assertEquals(7,FitRocketWeeklyWallet.rolloversWeek(d("2026-10-28"),23,
                  day->consumed.getOrDefault(day,0)));
        assertEquals(3,FitRocketWeeklyWallet.dailyTransfer(23,20));
        assertEquals(4,FitRocketWeeklyWallet.dailyTransfer(23,0));
        assertEquals(0,FitRocketWeeklyWallet.dailyTransfer(23,23));
        assertEquals(0,FitRocketWeeklyWallet.dailyTransfer(23,26));
    }

    @Test public void weeklyBudgetSubtractsOnlyTheDailyOverages() {
        Map<LocalDate,Integer> consumed=new HashMap<>();
        consumed.put(d("2026-10-26"),28); // Five taken from weekly budget
        consumed.put(d("2026-10-27"),18); // Max four rollover
        consumed.put(d("2026-10-28"),27); // Four taken today
        int rollover=FitRocketWeeklyWallet.rolloversWeek(d("2026-10-28"),23,
                 day->consumed.getOrDefault(day,0));
        int spent=FitRocketWeeklyWallet.spentWeek(d("2026-10-28"),23,
                 day->consumed.getOrDefault(day,0));
        assertEquals(4,rollover);
        assertEquals(9,spent);
        assertEquals(23,FitRocketWeeklyWallet.remaining(28,rollover,0,spent));
        assertEquals(28,FitRocketWeeklyWallet.remaining(28,rollover,5,spent));
    }

    @Test public void weeklyResetDropsPriorWeekTransfersAndOverages() {
        Map<LocalDate,Integer> consumed=new HashMap<>();
        consumed.put(d("2026-10-26"),100);
        consumed.put(d("2026-11-01"),4);
        LocalDate monday=d("2026-11-02");
        assertEquals(0,FitRocketWeeklyWallet.spentWeek(monday,23,
            day->consumed.getOrDefault(day,0)));
        assertEquals(0,FitRocketWeeklyWallet.rolloversWeek(monday,23,
            day->consumed.getOrDefault(day,0)));
    }

    @Test public void sundayCountsOnlySixCompletedDays() {
        assertEquals(24,FitRocketWeeklyWallet.rolloversWeek(d("2026-11-01"),23,day->0));
        assertEquals(0,FitRocketWeeklyWallet.remaining(28,24,0,999));
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsInvalidDayBudget() {
        FitRocketWeeklyWallet.dailyTransfer(0,5);
    }
}
