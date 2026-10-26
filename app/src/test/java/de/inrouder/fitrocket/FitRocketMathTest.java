package de.inrouder.fitrocket;

import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketMathTest {
    @Test public void restingKcalUsesFullProfile() {
        assertEquals(1755.0, FitRocketMath.restingCalories(35, 180, 80, 1), 0.001);
        assertEquals(1589.0, FitRocketMath.restingCalories(35, 180, 80, 0), 0.001);
        assertEquals(1672.0, FitRocketMath.restingCalories(35, 180, 80, 2), 0.001);
    }
    @Test public void activitySubtractsRestingExpenditure() {
        assertEquals(127.96875, FitRocketMath.activeCalories(1755, 4.5, 30), 0.0001);
        assertEquals(2, FitRocketMath.points(127.96875, 65));
        assertEquals(0.0, FitRocketMath.activeCalories(1755, 1.0, 30), 0.0);
    }
    @Test public void stepsSubtractBaselineAndWorkoutOverlap() {
        assertEquals(4000, FitRocketMath.extraSteps(8500, 3000, 1500));
        assertEquals(0, FitRocketMath.extraSteps(3000, 3000, 0));
        assertEquals(0, FitRocketMath.extraSteps(4000, 3000, 2000));
        assertTrue(FitRocketMath.stepCalories(1755, 4000, 100) > 0);
    }
    @Test public void creditDoesNotChangeBaseExtra() {
        assertEquals(5, FitRocketMath.credit(11, 50));
        assertEquals(30, FitRocketMath.remainingWeekly(28, 11, 50, 3));
        assertEquals(0, FitRocketMath.remainingWeekly(0, 0, 0, 25));
    }
    @Test public void foodScoreAcceptsMacroInputs() {
        assertEquals(0, FitRocketMath.foodPoints(0, 0, 0, 0));
        assertTrue(FitRocketMath.foodPoints(500, 10, 20, 8) > FitRocketMath.foodPoints(100, 0, 0, 20));
    }
    @Test(expected = IllegalArgumentException.class)
    public void rejectsIncompleteBodyProfile() {
        FitRocketMath.restingCalories(12, 180, 80, 1);
    }
    @Test(expected = IllegalArgumentException.class)
    public void rejectsImpossibleStepInput() {
        FitRocketMath.extraSteps(-12, 3000, 0);
    }
}
