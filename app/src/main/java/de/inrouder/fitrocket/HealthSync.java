package de.inrouder.fitrocket;

import android.app.Activity;
import android.os.Build;
import android.os.OutcomeReceiver;
import android.health.connect.AggregateRecordsRequest;
import android.health.connect.AggregateRecordsResponse;
import android.health.connect.HealthConnectException;
import android.health.connect.HealthConnectManager;
import android.health.connect.ReadRecordsRequestUsingFilters;
import android.health.connect.ReadRecordsResponse;
import android.health.connect.TimeInstantRangeFilter;
import android.health.connect.datatypes.ExerciseSessionRecord;
import android.health.connect.datatypes.StepsRecord;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Android 14+ Health Connect platform bridge (read-only).
 * All records are queried while the app is in the foreground after user consent.
 */
public final class HealthSync {
    private HealthSync() {}

    public static final class Workout {
        public final String id;
        public final String source;
        public final int type;
        public final Instant start;
        public final Instant end;
        public final String title;

        Workout(ExerciseSessionRecord r) {
            this.source = r.getMetadata().getDataOrigin().getPackageName();
            this.id = source + ":" + r.getMetadata().getId();
            this.type = r.getExerciseType();
            this.start = r.getStartTime();
            this.end = r.getEndTime();
            this.title = r.getTitle() == null ? "" : r.getTitle().toString();
        }

        public LocalDate date() { return start.atZone(ZoneId.systemDefault()).toLocalDate(); }
        public int minutes() {
            return (int) Math.min(600, Math.max(1, ChronoUnit.MINUTES.between(start, end)));
        }
    }

    public static final class Result {
        public final LinkedHashMap<String, Integer> dailySteps;
        public final List<Workout> workouts;

        Result(LinkedHashMap<String, Integer> dailySteps, List<Workout> workouts) {
            this.dailySteps = dailySteps;
            this.workouts = workouts;
        }
    }

    public interface Callback {
        void onSuccess(Result result);
        void onFailure(String reason);
    }

    public static boolean supported(Activity activity) {
        return Build.VERSION.SDK_INT >= 34 && activity.getSystemService(HealthConnectManager.class) != null;
    }

    public static void readWeek(Activity activity, LocalDate start, Callback callback) {
        if (!supported(activity)) {
            callback.onFailure("Health Connect ist auf diesem Android-Gerät nicht verfügbar.");
            return;
        }
        HealthConnectManager manager = activity.getSystemService(HealthConnectManager.class);
        LinkedHashMap<String, Integer> steps = new LinkedHashMap<>();
        readDay(activity, manager, start, 0, steps, callback);
    }

    private static Instant midnight(LocalDate d) {
        return d.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private static void readDay(Activity activity, HealthConnectManager manager, LocalDate monday, int index,
                                LinkedHashMap<String, Integer> output, Callback callback) {
        if (index == 7) {
            readWorkouts(activity, manager, monday, -1, output, new ArrayList<>(), callback);
            return;
        }
        LocalDate day = monday.plusDays(index);
        TimeInstantRangeFilter window = new TimeInstantRangeFilter.Builder()
                .setStartTime(midnight(day)).setEndTime(midnight(day.plusDays(1))).build();
        AggregateRecordsRequest<Long> request = new AggregateRecordsRequest.Builder<Long>(window)
                .addAggregationType(StepsRecord.STEPS_COUNT_TOTAL).build();
        try {
            manager.aggregate(request, activity.getMainExecutor(),
                    new OutcomeReceiver<AggregateRecordsResponse<Long>, HealthConnectException>() {
                        @Override public void onResult(AggregateRecordsResponse<Long> response) {
                            Long total = response.get(StepsRecord.STEPS_COUNT_TOTAL);
                            output.put(day.toString(), total == null ? 0 : (int) Math.min(100000L, Math.max(0L, total)));
                            readDay(activity, manager, monday, index + 1, output, callback);
                        }
                        @Override public void onError(HealthConnectException error) {
                            callback.onFailure("Schritte konnten nicht gelesen werden: " + error.getMessage());
                        }
                    });
        } catch (Exception e) {
            callback.onFailure("Health Connect: " + e.getMessage());
        }
    }

    private static void readWorkouts(Activity activity, HealthConnectManager manager, LocalDate monday, long token,
                                     LinkedHashMap<String, Integer> steps, List<Workout> workouts, Callback callback) {
        TimeInstantRangeFilter window = new TimeInstantRangeFilter.Builder()
                .setStartTime(midnight(monday)).setEndTime(midnight(monday.plusDays(7))).build();
        ReadRecordsRequestUsingFilters.Builder<ExerciseSessionRecord> builder =
                new ReadRecordsRequestUsingFilters.Builder<>(ExerciseSessionRecord.class)
                    .setTimeRangeFilter(window).setPageSize(200);
        if (token != -1) builder.setPageToken(token);
        try {
            manager.readRecords(builder.build(), activity.getMainExecutor(),
                    new OutcomeReceiver<ReadRecordsResponse<ExerciseSessionRecord>, HealthConnectException>() {
                        @Override public void onResult(ReadRecordsResponse<ExerciseSessionRecord> response) {
                            for (ExerciseSessionRecord record : response.getRecords()) {
                                if (record.getEndTime().isAfter(record.getStartTime())) workouts.add(new Workout(record));
                            }
                            long next = response.getNextPageToken();
                            if (next != -1 && workouts.size() < 4000) {
                                readWorkouts(activity, manager, monday, next, steps, workouts, callback);
                            } else if (next != -1) {
                                callback.onFailure("Zu viele Trainingsdatensätze. Bitte Datenquelle überprüfen.");
                            } else {
                                callback.onSuccess(new Result(steps, workouts));
                            }
                        }
                        @Override public void onError(HealthConnectException error) {
                            callback.onFailure("Training konnte nicht gelesen werden: " + error.getMessage());
                        }
                    });
        } catch (Exception e) {
            callback.onFailure("Health Connect: " + e.getMessage());
        }
    }
}
