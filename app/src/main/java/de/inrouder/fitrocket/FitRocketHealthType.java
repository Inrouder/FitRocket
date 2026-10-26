package de.inrouder.fitrocket;

import android.health.connect.datatypes.ExerciseSessionType;

/** Maps Android exercise categories to FitRocket's nine movement profiles. */
public final class FitRocketHealthType {
    private FitRocketHealthType() {}

    public static int typeIndex(int exerciseType) {
        switch (exerciseType) {
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_WALKING:
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_HIKING:
                return 0;
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_BIKING:
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_BIKING_STATIONARY:
                return 1;
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_SWIMMING_POOL:
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_SWIMMING_OPEN_WATER:
                return 2;
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_RUNNING:
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_RUNNING_TREADMILL:
                return 3;
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_WEIGHTLIFTING:
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_STRENGTH_TRAINING:
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_CALISTHENICS:
                return 4;
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_YOGA:
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_PILATES:
                return 5;
            case ExerciseSessionType.EXERCISE_SESSION_TYPE_DANCING:
                return 6;
            default:
                return 8;
        }
    }

    public static String name(int index) {
        switch (index) {
            case 0: return "Gehen";
            case 1: return "Radfahren";
            case 2: return "Schwimmen";
            case 3: return "Laufen";
            case 4: return "Krafttraining";
            case 5: return "Yoga";
            case 6: return "Tanzen";
            case 7: return "Gartenarbeit";
            default: return "Sport (Uhr)";
        }
    }
}
