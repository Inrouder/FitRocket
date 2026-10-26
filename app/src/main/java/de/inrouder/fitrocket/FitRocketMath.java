package de.inrouder.fitrocket;

/** FitRocket's own transparent movement and nutrition point model. */
public final class FitRocketMath {
    private FitRocketMath() {}

    public static double restingCalories(int age, double heightCm, double weightKg, int metabolicProfile) {
        if (age < 18 || age > 100 || !Double.isFinite(heightCm) || heightCm < 120 || heightCm > 230
                || !Double.isFinite(weightKg) || weightKg < 30 || weightKg > 300
                || metabolicProfile < 0 || metabolicProfile > 2) throw new IllegalArgumentException("profile");
        // Mifflin-St Jeor reference profiles: female (-161), male (+5), or midpoint (-78).
        int constant = metabolicProfile == 0 ? -161 : metabolicProfile == 1 ? 5 : -78;
        return 10.0 * weightKg + 6.25 * heightCm - 5.0 * age + constant;
    }

    public static double activityMet(int activity, int intensity) {
        final double[][] mets = {
                {2.5, 3.8, 5.3}, // walking
                {4.0, 6.8, 10.0}, // cycling
                {4.5, 6.0, 9.0}, // swimming
                {6.0, 9.0, 12.0}, // running
                {3.0, 5.0, 6.0}, // weights
                {2.5, 3.0, 4.0}, // yoga
                {3.0, 5.0, 7.3}, // dancing
                {2.5, 4.0, 6.0}, // gardening
                {2.5, 4.5, 7.0} // other
        };
        if (activity < 0 || activity >= mets.length || intensity < 0 || intensity > 2)
            throw new IllegalArgumentException("intensity");
        return mets[activity][intensity];
    }

    public static double activeCalories(double restingKcalDay, double met, double minutes) {
        if (!Double.isFinite(restingKcalDay) || restingKcalDay <= 0 || !Double.isFinite(met)
                || met < 1 || !Double.isFinite(minutes) || minutes < 0 || minutes > 1440)
            throw new IllegalArgumentException("activity");
        // Above-rest energy only: resting daily calories / minutes per day times (MET - 1).
        return (restingKcalDay / 1440.0) * (met - 1.0) * minutes;
    }

    public static int extraSteps(int totalSteps, int baselineSteps, int workoutSteps) {
        if (totalSteps < 0 || totalSteps > 100000 || baselineSteps < 0 || baselineSteps > 20000
                || workoutSteps < 0 || workoutSteps > 100000) throw new IllegalArgumentException("steps");
        return Math.max(0, totalSteps - baselineSteps - workoutSteps);
    }

    public static double stepCalories(double restingKcalDay, int extraSteps, int cadence) {
        if (extraSteps < 0 || extraSteps > 100000 || cadence < 60 || cadence > 160)
            throw new IllegalArgumentException("cadence");
        return activeCalories(restingKcalDay, 3.5, extraSteps / (double) cadence);
    }

    public static int points(double activeKcal, int kcalPerPoint) {
        if (!Double.isFinite(activeKcal) || activeKcal < 0 || kcalPerPoint < 30 || kcalPerPoint > 150)
            throw new IllegalArgumentException("points");
        return (int) Math.min(2000, Math.round(activeKcal / kcalPerPoint));
    }

    public static int foodPoints(double kcal, double saturatedFat, double sugar, double protein) {
        if (!Double.isFinite(kcal) || !Double.isFinite(saturatedFat) || !Double.isFinite(sugar)
                || !Double.isFinite(protein) || kcal < 0 || saturatedFat < 0 || sugar < 0 || protein < 0)
            throw new IllegalArgumentException("food");
        // Independent FitRocket score. Base foods have separately defined zero-point status.
        return Math.max(0, (int) Math.round(0.026 * kcal + 0.25 * saturatedFat
                + 0.10 * sugar - 0.07 * protein));
    }

    public static int credit(int earnedPoints, int percent) {
        if (earnedPoints < 0 || !((percent == 0) || (percent == 50) || (percent == 100)))
            throw new IllegalArgumentException("share");
        return (int) Math.floor(earnedPoints * (percent / 100.0));
    }

    public static int remainingWeekly(int baseExtra, int earned, int share, int spent) {
        if (baseExtra < 0 || spent < 0) throw new IllegalArgumentException("weekly");
        return Math.max(0, baseExtra + credit(earned, share) - spent);
    }
}
