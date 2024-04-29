/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.healthconnect.cts.utils;

import static android.content.pm.PackageManager.GET_PERMISSIONS;
import static android.health.connect.HealthDataCategory.ACTIVITY;
import static android.health.connect.HealthDataCategory.BODY_MEASUREMENTS;
import static android.health.connect.HealthDataCategory.CYCLE_TRACKING;
import static android.health.connect.HealthDataCategory.NUTRITION;
import static android.health.connect.HealthDataCategory.SLEEP;
import static android.health.connect.HealthDataCategory.VITALS;
import static android.health.connect.HealthPermissionCategory.BASAL_METABOLIC_RATE;
import static android.health.connect.HealthPermissionCategory.EXERCISE;
import static android.health.connect.HealthPermissionCategory.HEART_RATE;
import static android.health.connect.HealthPermissionCategory.STEPS;
import static android.health.connect.datatypes.Metadata.RECORDING_METHOD_ACTIVELY_RECORDED;
import static android.health.connect.datatypes.RecordTypeIdentifier.RECORD_TYPE_BASAL_METABOLIC_RATE;
import static android.health.connect.datatypes.RecordTypeIdentifier.RECORD_TYPE_HEART_RATE;
import static android.health.connect.datatypes.RecordTypeIdentifier.RECORD_TYPE_STEPS;
import static android.healthconnect.test.app.TestAppReceiver.EXTRA_SENDER_PACKAGE_NAME;

import static com.android.compatibility.common.util.FeatureUtil.AUTOMOTIVE_FEATURE;
import static com.android.compatibility.common.util.FeatureUtil.hasSystemFeature;
import static com.android.compatibility.common.util.SystemUtil.runWithShellPermissionIdentity;

import static com.google.common.truth.Truth.assertThat;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.health.connect.AggregateRecordsGroupedByDurationResponse;
import android.health.connect.AggregateRecordsGroupedByPeriodResponse;
import android.health.connect.AggregateRecordsRequest;
import android.health.connect.AggregateRecordsResponse;
import android.health.connect.ApplicationInfoResponse;
import android.health.connect.DeleteUsingFiltersRequest;
import android.health.connect.FetchDataOriginsPriorityOrderResponse;
import android.health.connect.HealthConnectDataState;
import android.health.connect.HealthConnectException;
import android.health.connect.HealthConnectManager;
import android.health.connect.HealthPermissionCategory;
import android.health.connect.HealthPermissions;
import android.health.connect.InsertRecordsResponse;
import android.health.connect.ReadRecordsRequest;
import android.health.connect.ReadRecordsRequestUsingFilters;
import android.health.connect.ReadRecordsRequestUsingIds;
import android.health.connect.ReadRecordsResponse;
import android.health.connect.RecordIdFilter;
import android.health.connect.RecordTypeInfoResponse;
import android.health.connect.TimeInstantRangeFilter;
import android.health.connect.UpdateDataOriginPriorityOrderRequest;
import android.health.connect.accesslog.AccessLog;
import android.health.connect.changelog.ChangeLogTokenRequest;
import android.health.connect.changelog.ChangeLogTokenResponse;
import android.health.connect.changelog.ChangeLogsRequest;
import android.health.connect.changelog.ChangeLogsResponse;
import android.health.connect.datatypes.ActiveCaloriesBurnedRecord;
import android.health.connect.datatypes.AppInfo;
import android.health.connect.datatypes.BasalBodyTemperatureRecord;
import android.health.connect.datatypes.BasalMetabolicRateRecord;
import android.health.connect.datatypes.BloodGlucoseRecord;
import android.health.connect.datatypes.BloodPressureRecord;
import android.health.connect.datatypes.BodyFatRecord;
import android.health.connect.datatypes.BodyTemperatureRecord;
import android.health.connect.datatypes.BodyWaterMassRecord;
import android.health.connect.datatypes.BoneMassRecord;
import android.health.connect.datatypes.CervicalMucusRecord;
import android.health.connect.datatypes.CyclingPedalingCadenceRecord;
import android.health.connect.datatypes.DataOrigin;
import android.health.connect.datatypes.Device;
import android.health.connect.datatypes.DistanceRecord;
import android.health.connect.datatypes.ElevationGainedRecord;
import android.health.connect.datatypes.ExerciseLap;
import android.health.connect.datatypes.ExerciseRoute;
import android.health.connect.datatypes.ExerciseSegment;
import android.health.connect.datatypes.ExerciseSegmentType;
import android.health.connect.datatypes.ExerciseSessionRecord;
import android.health.connect.datatypes.ExerciseSessionType;
import android.health.connect.datatypes.FloorsClimbedRecord;
import android.health.connect.datatypes.HeartRateRecord;
import android.health.connect.datatypes.HeartRateVariabilityRmssdRecord;
import android.health.connect.datatypes.HeightRecord;
import android.health.connect.datatypes.HydrationRecord;
import android.health.connect.datatypes.IntermenstrualBleedingRecord;
import android.health.connect.datatypes.LeanBodyMassRecord;
import android.health.connect.datatypes.MenstruationFlowRecord;
import android.health.connect.datatypes.MenstruationPeriodRecord;
import android.health.connect.datatypes.Metadata;
import android.health.connect.datatypes.NutritionRecord;
import android.health.connect.datatypes.OvulationTestRecord;
import android.health.connect.datatypes.OxygenSaturationRecord;
import android.health.connect.datatypes.PowerRecord;
import android.health.connect.datatypes.Record;
import android.health.connect.datatypes.RespiratoryRateRecord;
import android.health.connect.datatypes.RestingHeartRateRecord;
import android.health.connect.datatypes.SexualActivityRecord;
import android.health.connect.datatypes.SleepSessionRecord;
import android.health.connect.datatypes.SpeedRecord;
import android.health.connect.datatypes.StepsCadenceRecord;
import android.health.connect.datatypes.StepsRecord;
import android.health.connect.datatypes.TotalCaloriesBurnedRecord;
import android.health.connect.datatypes.Vo2MaxRecord;
import android.health.connect.datatypes.WeightRecord;
import android.health.connect.datatypes.WheelchairPushesRecord;
import android.health.connect.datatypes.units.Length;
import android.health.connect.datatypes.units.Power;
import android.health.connect.migration.MigrationEntity;
import android.health.connect.migration.MigrationException;
import android.healthconnect.test.app.TestAppReceiver;
import android.os.Bundle;
import android.os.OutcomeReceiver;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import com.android.compatibility.common.util.SystemUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TestUtils {
    public static final String MANAGE_HEALTH_PERMISSIONS =
            HealthPermissions.MANAGE_HEALTH_PERMISSIONS;
    public static final String READ_EXERCISE_ROUTE_PERMISSION =
            "android.permission.health.READ_EXERCISE_ROUTE";
    private static final String HEALTH_PERMISSION_PREFIX = "android.permission.health.";
    public static final String MANAGE_HEALTH_DATA = HealthPermissions.MANAGE_HEALTH_DATA_PERMISSION;
    public static final Instant SESSION_START_TIME = Instant.now().minus(10, ChronoUnit.DAYS);
    public static final Instant SESSION_END_TIME =
            Instant.now().minus(10, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);
    private static final String TAG = "HCTestUtils";
    private static final int TIMEOUT_SECONDS = 5;

    private static final String PKG_TEST_APP = "android.healthconnect.test.app";
    private static final String TEST_APP_RECEIVER =
            PKG_TEST_APP + "." + TestAppReceiver.class.getSimpleName();

    public static boolean isHardwareAutomotive() {
        return hasSystemFeature(AUTOMOTIVE_FEATURE);
    }

    public static ChangeLogTokenResponse getChangeLogToken(ChangeLogTokenRequest request)
            throws InterruptedException {
        return getChangeLogToken(request, ApplicationProvider.getApplicationContext());
    }

    public static ChangeLogTokenResponse getChangeLogToken(
            ChangeLogTokenRequest request, Context context) throws InterruptedException {
        HealthConnectReceiver<ChangeLogTokenResponse> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager(context)
                .getChangeLogToken(request, Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse();
    }

    public static String insertRecordAndGetId(Record record) throws InterruptedException {
        return insertRecords(Collections.singletonList(record)).get(0).getMetadata().getId();
    }

    public static String insertRecordAndGetId(Record record, Context context)
            throws InterruptedException {
        return insertRecords(Collections.singletonList(record), context)
                .get(0)
                .getMetadata()
                .getId();
    }

    /**
     * Inserts records to the database.
     *
     * @param records records to insert
     * @return inserted records
     */
    public static List<Record> insertRecords(List<? extends Record> records)
            throws InterruptedException {
        return insertRecords(records, ApplicationProvider.getApplicationContext());
    }

    /**
     * Inserts records to the database.
     *
     * @param records records to insert.
     * @param context a {@link Context} to obtain {@link HealthConnectManager}.
     * @return inserted records.
     */
    public static List<Record> insertRecords(List<? extends Record> records, Context context)
            throws InterruptedException {
        HealthConnectReceiver<InsertRecordsResponse> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager(context)
                .insertRecords(
                        unmodifiableList(records), Executors.newSingleThreadExecutor(), receiver);
        List<Record> returnedRecords = receiver.getResponse().getRecords();
        assertThat(returnedRecords).hasSize(records.size());
        return returnedRecords;
    }

    public static List<RecordTypeAndRecordIds> insertRecordsAndGetIds(
            List<Record> records, Context context) throws InterruptedException {
        List<Record> insertedRecords = insertRecords(records, context);

        Map<String, List<String>> recordTypeToRecordIdsMap = new HashMap<>();
        for (Record record : insertedRecords) {
            recordTypeToRecordIdsMap.putIfAbsent(record.getClass().getName(), new ArrayList<>());
            recordTypeToRecordIdsMap
                    .get(record.getClass().getName())
                    .add(record.getMetadata().getId());
        }

        List<RecordTypeAndRecordIds> recordTypeAndRecordIdsList = new ArrayList<>();
        for (String recordType : recordTypeToRecordIdsMap.keySet()) {
            recordTypeAndRecordIdsList.add(
                    new RecordTypeAndRecordIds(
                            recordType, recordTypeToRecordIdsMap.get(recordType)));
        }

        return recordTypeAndRecordIdsList;
    }

    /**
     * Returns all records from the `records` list in their original order, but distinct by UUID.
     */
    public static <T extends Record> List<T> distinctByUuid(List<T> records) {
        return records.stream().filter(distinctByUuid()).toList();
    }

    private static Predicate<? super Record> distinctByUuid() {
        Set<String> seen = ConcurrentHashMap.newKeySet();
        return record -> seen.add(record.getMetadata().getId());
    }

    public static void updateRecords(List<Record> records) throws InterruptedException {
        updateRecords(records, ApplicationProvider.getApplicationContext());
    }

    /** Synchronously updates records in HC. */
    public static void updateRecords(List<? extends Record> records, Context context)
            throws InterruptedException {
        HealthConnectReceiver<Void> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager(context)
                .updateRecords(
                        unmodifiableList(records), Executors.newSingleThreadExecutor(), receiver);
        receiver.verifyNoExceptionOrThrow();
    }

    public static ChangeLogsResponse getChangeLogs(ChangeLogsRequest changeLogsRequest)
            throws InterruptedException {
        return getChangeLogs(changeLogsRequest, ApplicationProvider.getApplicationContext());
    }

    public static ChangeLogsResponse getChangeLogs(
            ChangeLogsRequest changeLogsRequest, Context context) throws InterruptedException {
        HealthConnectReceiver<ChangeLogsResponse> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager(context)
                .getChangeLogs(changeLogsRequest, Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse();
    }

    public static Device buildDevice() {
        return new Device.Builder()
                .setManufacturer("google")
                .setModel("Pixel4a")
                .setType(2)
                .build();
    }

    private static Metadata buildSessionMetadata(String packageName, double clientId) {
        Device device =
                new Device.Builder().setManufacturer("google").setModel("Pixel").setType(1).build();
        DataOrigin dataOrigin = new DataOrigin.Builder().setPackageName(packageName).build();
        return new Metadata.Builder()
                .setDevice(device)
                .setDataOrigin(dataOrigin)
                .setClientRecordId(String.valueOf(clientId))
                .build();
    }

    public static List<Record> getTestRecords() {
        return Arrays.asList(
                getStepsRecord(),
                getHeartRateRecord(),
                getBasalMetabolicRateRecord(),
                buildExerciseSession());
    }

    public static List<Record> getTestRecords(String packageName, Double clientId) {
        return Arrays.asList(
                getExerciseSessionRecord(packageName, clientId, /* withRoute= */ true),
                getStepsRecord(packageName, clientId),
                getHeartRateRecord(packageName, clientId),
                getBasalMetabolicRateRecord(packageName, clientId));
    }

    public static List<RecordAndIdentifier> getRecordsAndIdentifiers() {
        return Arrays.asList(
                new RecordAndIdentifier(RECORD_TYPE_STEPS, getStepsRecord()),
                new RecordAndIdentifier(RECORD_TYPE_HEART_RATE, getHeartRateRecord()),
                new RecordAndIdentifier(
                        RECORD_TYPE_BASAL_METABOLIC_RATE, getBasalMetabolicRateRecord()));
    }

    public static ExerciseRoute.Location buildLocationTimePoint(Instant startTime) {
        return new ExerciseRoute.Location.Builder(
                        Instant.ofEpochMilli(
                                (long) (startTime.toEpochMilli() + 10 + Math.random() * 50)),
                        Math.random() * 50,
                        Math.random() * 50)
                .build();
    }

    public static ExerciseRoute buildExerciseRoute() {
        return new ExerciseRoute(
                List.of(
                        buildLocationTimePoint(SESSION_START_TIME),
                        buildLocationTimePoint(SESSION_START_TIME),
                        buildLocationTimePoint(SESSION_START_TIME)));
    }

    public static StepsRecord getStepsRecord() {
        double clientId = Math.random();
        String packageName = ApplicationProvider.getApplicationContext().getPackageName();
        return getStepsRecord(packageName, clientId);
    }

    public static StepsRecord getStepsRecord(String packageName, double clientId) {
        Device device =
                new Device.Builder().setManufacturer("google").setModel("Pixel").setType(1).build();
        DataOrigin dataOrigin = new DataOrigin.Builder().setPackageName(packageName).build();
        return new StepsRecord.Builder(
                        new Metadata.Builder()
                                .setDevice(device)
                                .setDataOrigin(dataOrigin)
                                .setClientRecordId("SR" + clientId)
                                .build(),
                        Instant.now(),
                        Instant.now().plusMillis(1000),
                        10)
                .build();
    }

    public static StepsRecord getStepsRecord(String id) {
        Context context = ApplicationProvider.getApplicationContext();
        Device device =
                new Device.Builder().setManufacturer("google").setModel("Pixel").setType(1).build();
        DataOrigin dataOrigin =
                new DataOrigin.Builder().setPackageName(context.getPackageName()).build();
        return new StepsRecord.Builder(
                        new Metadata.Builder()
                                .setDevice(device)
                                .setId(id)
                                .setDataOrigin(dataOrigin)
                                .build(),
                        Instant.now(),
                        Instant.now().plusMillis(1000),
                        10)
                .build();
    }

    public static HeartRateRecord getHeartRateRecord() {
        String packageName = ApplicationProvider.getApplicationContext().getPackageName();
        double clientId = Math.random();
        return getHeartRateRecord(packageName, clientId);
    }

    public static HeartRateRecord getHeartRateRecord(String packageName, double clientId) {
        HeartRateRecord.HeartRateSample heartRateSample =
                new HeartRateRecord.HeartRateSample(72, Instant.now().plusMillis(100));
        ArrayList<HeartRateRecord.HeartRateSample> heartRateSamples = new ArrayList<>();
        heartRateSamples.add(heartRateSample);
        heartRateSamples.add(heartRateSample);
        Device device =
                new Device.Builder().setManufacturer("google").setModel("Pixel").setType(1).build();
        DataOrigin dataOrigin = new DataOrigin.Builder().setPackageName(packageName).build();

        return new HeartRateRecord.Builder(
                        new Metadata.Builder()
                                .setDevice(device)
                                .setDataOrigin(dataOrigin)
                                .setClientRecordId("HR" + clientId)
                                .build(),
                        Instant.now(),
                        Instant.now().plusMillis(500),
                        heartRateSamples)
                .build();
    }

    public static HeartRateRecord getHeartRateRecord(int heartRate) {
        HeartRateRecord.HeartRateSample heartRateSample =
                new HeartRateRecord.HeartRateSample(heartRate, Instant.now().plusMillis(100));
        ArrayList<HeartRateRecord.HeartRateSample> heartRateSamples = new ArrayList<>();
        heartRateSamples.add(heartRateSample);
        heartRateSamples.add(heartRateSample);

        return new HeartRateRecord.Builder(
                        new Metadata.Builder().build(),
                        Instant.now(),
                        Instant.now().plusMillis(500),
                        heartRateSamples)
                .build();
    }

    public static HeartRateRecord getHeartRateRecord(int heartRate, Instant instant) {
        HeartRateRecord.HeartRateSample heartRateSample =
                new HeartRateRecord.HeartRateSample(heartRate, instant);
        ArrayList<HeartRateRecord.HeartRateSample> heartRateSamples = new ArrayList<>();
        heartRateSamples.add(heartRateSample);
        heartRateSamples.add(heartRateSample);

        return new HeartRateRecord.Builder(
                        new Metadata.Builder().build(),
                        instant,
                        instant.plusMillis(1000),
                        heartRateSamples)
                .build();
    }

    public static BasalMetabolicRateRecord getBasalMetabolicRateRecord() {
        String packageName = ApplicationProvider.getApplicationContext().getPackageName();
        double clientId = Math.random();

        return getBasalMetabolicRateRecord(packageName, clientId);
    }

    public static BasalMetabolicRateRecord getBasalMetabolicRateRecord(
            String packageName, double clientId) {
        Device device =
                new Device.Builder()
                        .setManufacturer("google")
                        .setModel("Pixel4a")
                        .setType(2)
                        .build();
        DataOrigin dataOrigin = new DataOrigin.Builder().setPackageName(packageName).build();
        return new BasalMetabolicRateRecord.Builder(
                        new Metadata.Builder()
                                .setDevice(device)
                                .setDataOrigin(dataOrigin)
                                .setClientRecordId("BMR" + clientId)
                                .build(),
                        Instant.now(),
                        Power.fromWatts(100.0))
                .build();
    }

    public static ExerciseSessionRecord getExerciseSessionRecord(
            String packageName, double clientId, boolean withRoute) {
        Instant startTime = Instant.now().minusSeconds(3000).truncatedTo(ChronoUnit.MILLIS);
        Instant endTime = Instant.now();
        ExerciseSessionRecord.Builder builder =
                new ExerciseSessionRecord.Builder(
                                buildSessionMetadata(packageName, clientId),
                                startTime,
                                endTime,
                                ExerciseSessionType.EXERCISE_SESSION_TYPE_OTHER_WORKOUT)
                        .setEndZoneOffset(ZoneOffset.MAX)
                        .setStartZoneOffset(ZoneOffset.MIN)
                        .setNotes("notes")
                        .setTitle("title");

        if (withRoute) {
            builder.setRoute(
                    new ExerciseRoute(
                            List.of(
                                    new ExerciseRoute.Location.Builder(startTime, 50., 50.).build(),
                                    new ExerciseRoute.Location.Builder(
                                                    startTime.plusSeconds(2), 51., 51.)
                                            .build())));
        }
        return builder.build();
    }

    public static StepsRecord buildStepsRecord(
            String startTime, String endTime, int stepsCount, String packageName) {
        Device device =
                new Device.Builder().setManufacturer("google").setModel("Pixel").setType(1).build();
        DataOrigin dataOrigin = new DataOrigin.Builder().setPackageName(packageName).build();
        return new StepsRecord.Builder(
                        new Metadata.Builder().setDevice(device).setDataOrigin(dataOrigin).build(),
                        getInstantTime(startTime),
                        getInstantTime(endTime),
                        stepsCount)
                .build();
    }

    public static ExerciseSessionRecord buildExerciseSession(
            String sessionStartTime, String sessionEndTime, Context context) {
        return new ExerciseSessionRecord.Builder(
                        new Metadata.Builder()
                                .setDataOrigin(
                                        new DataOrigin.Builder()
                                                .setPackageName(context.getPackageName())
                                                .build())
                                .setId("ExerciseSession" + Math.random())
                                .setClientRecordId("ExerciseSessionClient" + Math.random())
                                .build(),
                        getInstantTime(sessionStartTime),
                        getInstantTime(sessionEndTime),
                        ExerciseSessionType.EXERCISE_SESSION_TYPE_FOOTBALL_AMERICAN)
                .build();
    }

    public static ExerciseSessionRecord buildExerciseSession(
            String sessionStartTime,
            String sessionEndTime,
            String pauseStart,
            String pauseEnd,
            Context context) {
        List<ExerciseSegment> segmentList =
                List.of(
                        new ExerciseSegment.Builder(
                                        getInstantTime(sessionStartTime),
                                        getInstantTime(pauseStart),
                                        ExerciseSegmentType.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT)
                                .setRepetitionsCount(10)
                                .build(),
                        new ExerciseSegment.Builder(
                                        getInstantTime(pauseStart),
                                        getInstantTime(pauseEnd),
                                        ExerciseSegmentType.EXERCISE_SEGMENT_TYPE_PAUSE)
                                .build());

        if (getInstantTime(sessionEndTime).compareTo(getInstantTime(pauseEnd)) > 0) {
            segmentList.add(
                    new ExerciseSegment.Builder(
                                    getInstantTime(pauseEnd),
                                    getInstantTime(sessionEndTime),
                                    ExerciseSegmentType.EXERCISE_SEGMENT_TYPE_OTHER_WORKOUT)
                            .setRepetitionsCount(10)
                            .build());
        }

        return new ExerciseSessionRecord.Builder(
                        new Metadata.Builder()
                                .setDataOrigin(
                                        new DataOrigin.Builder()
                                                .setPackageName(context.getPackageName())
                                                .build())
                                .setId("ExerciseSession" + Math.random())
                                .setClientRecordId("ExerciseSessionClient" + Math.random())
                                .build(),
                        getInstantTime(sessionStartTime),
                        getInstantTime(sessionEndTime),
                        ExerciseSessionType.EXERCISE_SESSION_TYPE_FOOTBALL_AMERICAN)
                .setSegments(segmentList)
                .build();
    }

    public static Instant getInstantTime(String time) {
        return LocalDateTime.parse(
                        time + " Mon 5/15/2023",
                        DateTimeFormatter.ofPattern("hh:mm a EEE M/d/uuuu", Locale.US))
                .atZone(ZoneId.of("America/Toronto"))
                .toInstant();
    }

    public static <T> AggregateRecordsResponse<T> getAggregateResponse(
            AggregateRecordsRequest<T> request) throws InterruptedException {
        HealthConnectReceiver<AggregateRecordsResponse<T>> receiver =
                new HealthConnectReceiver<AggregateRecordsResponse<T>>();
        getHealthConnectManager().aggregate(request, Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse();
    }

    public static <T> AggregateRecordsResponse<T> getAggregateResponse(
            AggregateRecordsRequest<T> request, List<Record> recordsToInsert)
            throws InterruptedException {
        if (recordsToInsert != null) {
            insertRecords(recordsToInsert);
        }

        HealthConnectReceiver<AggregateRecordsResponse<T>> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager().aggregate(request, Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse();
    }

    public static <T>
            List<AggregateRecordsGroupedByDurationResponse<T>> getAggregateResponseGroupByDuration(
                    AggregateRecordsRequest<T> request, Duration duration)
                    throws InterruptedException {
        HealthConnectReceiver<List<AggregateRecordsGroupedByDurationResponse<T>>> receiver =
                new HealthConnectReceiver<>();
        getHealthConnectManager()
                .aggregateGroupByDuration(
                        request, duration, Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse();
    }

    public static <T>
            List<AggregateRecordsGroupedByPeriodResponse<T>> getAggregateResponseGroupByPeriod(
                    AggregateRecordsRequest<T> request, Period period) throws InterruptedException {
        HealthConnectReceiver<List<AggregateRecordsGroupedByPeriodResponse<T>>> receiver =
                new HealthConnectReceiver<>();
        getHealthConnectManager()
                .aggregateGroupByPeriod(
                        request, period, Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse();
    }

    public static <T extends Record> List<T> readRecords(ReadRecordsRequest<T> request)
            throws InterruptedException {
        return readRecords(request, ApplicationProvider.getApplicationContext());
    }

    public static <T extends Record> List<T> readRecords(
            ReadRecordsRequest<T> request, Context context) throws InterruptedException {
        assertThat(request.getRecordType()).isNotNull();
        HealthConnectReceiver<ReadRecordsResponse<T>> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager(context)
                .readRecords(request, Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse().getRecords();
    }

    public static <T extends Record> void assertRecordNotFound(String uuid, Class<T> recordType)
            throws InterruptedException {
        assertThat(
                        readRecords(
                                new ReadRecordsRequestUsingIds.Builder<>(recordType)
                                        .addId(uuid)
                                        .build()))
                .isEmpty();
    }

    public static <T extends Record> void assertRecordFound(String uuid, Class<T> recordType)
            throws InterruptedException {
        assertThat(
                        readRecords(
                                new ReadRecordsRequestUsingIds.Builder<>(recordType)
                                        .addId(uuid)
                                        .build()))
                .isNotEmpty();
    }

    /** Reads all records in the DB for a given {@code recordClass}. */
    public static <T extends Record> List<T> readAllRecords(Class<T> recordClass)
            throws InterruptedException {
        List<T> records = new ArrayList<>();
        ReadRecordsResponse<T> readRecordsResponse =
                readRecordsWithPagination(
                        new ReadRecordsRequestUsingFilters.Builder<>(recordClass).build());
        while (true) {
            records.addAll(readRecordsResponse.getRecords());
            long pageToken = readRecordsResponse.getNextPageToken();
            if (pageToken == -1) {
                break;
            }
            readRecordsResponse =
                    readRecordsWithPagination(
                            new ReadRecordsRequestUsingFilters.Builder<>(recordClass)
                                    .setPageToken(pageToken)
                                    .build());
        }
        return records;
    }

    public static <T extends Record> ReadRecordsResponse<T> readRecordsWithPagination(
            ReadRecordsRequest<T> request) throws InterruptedException {
        HealthConnectReceiver<ReadRecordsResponse<T>> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager()
                .readRecords(request, Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse();
    }

    public static void setAutoDeletePeriod(int period) throws InterruptedException {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.adoptShellPermissionIdentity(MANAGE_HEALTH_DATA);
        try {
            HealthConnectReceiver<Void> receiver = new HealthConnectReceiver<>();
            getHealthConnectManager()
                    .setRecordRetentionPeriodInDays(
                            period, Executors.newSingleThreadExecutor(), receiver);
            receiver.verifyNoExceptionOrThrow();
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    public static void verifyDeleteRecords(DeleteUsingFiltersRequest request)
            throws InterruptedException {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.adoptShellPermissionIdentity(MANAGE_HEALTH_DATA);
        try {
            HealthConnectReceiver<Void> receiver = new HealthConnectReceiver<>();
            getHealthConnectManager()
                    .deleteRecords(request, Executors.newSingleThreadExecutor(), receiver);
            receiver.verifyNoExceptionOrThrow();
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    public static void verifyDeleteRecords(List<RecordIdFilter> request)
            throws InterruptedException {
        verifyDeleteRecords(request, ApplicationProvider.getApplicationContext());
    }

    public static void verifyDeleteRecords(List<RecordIdFilter> request, Context context)
            throws InterruptedException {
        HealthConnectReceiver<Void> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager(context)
                .deleteRecords(request, Executors.newSingleThreadExecutor(), receiver);
        receiver.verifyNoExceptionOrThrow();
    }

    public static void verifyDeleteRecords(
            Class<? extends Record> recordType, TimeInstantRangeFilter timeRangeFilter)
            throws InterruptedException {
        HealthConnectReceiver<Void> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager()
                .deleteRecords(
                        recordType, timeRangeFilter, Executors.newSingleThreadExecutor(), receiver);
        receiver.verifyNoExceptionOrThrow();
    }

    /** Helper function to delete records from the DB using HealthConnectManager. */
    public static void deleteRecords(List<? extends Record> records) throws InterruptedException {
        List<RecordIdFilter> recordIdFilters =
                records.stream()
                        .map(
                                (record ->
                                        RecordIdFilter.fromId(
                                                record.getClass(), record.getMetadata().getId())))
                        .collect(Collectors.toList());
        verifyDeleteRecords(recordIdFilters);
    }

    public static List<AccessLog> queryAccessLogs() throws InterruptedException {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.adoptShellPermissionIdentity(MANAGE_HEALTH_DATA);
        try {
            HealthConnectReceiver<List<AccessLog>> receiver = new HealthConnectReceiver<>();
            getHealthConnectManager()
                    .queryAccessLogs(Executors.newSingleThreadExecutor(), receiver);
            return receiver.getResponse();
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    public static Map<Class<? extends Record>, RecordTypeInfoResponse> queryAllRecordTypesInfo()
            throws InterruptedException, NullPointerException {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.adoptShellPermissionIdentity(MANAGE_HEALTH_DATA);
        try {
            HealthConnectReceiver<Map<Class<? extends Record>, RecordTypeInfoResponse>> receiver =
                    new HealthConnectReceiver<>();
            getHealthConnectManager()
                    .queryAllRecordTypesInfo(Executors.newSingleThreadExecutor(), receiver);
            return receiver.getResponse();
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    public static List<LocalDate> getActivityDates(List<Class<? extends Record>> recordTypes)
            throws InterruptedException {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        uiAutomation.adoptShellPermissionIdentity(MANAGE_HEALTH_DATA);
        try {
            HealthConnectReceiver<List<LocalDate>> receiver = new HealthConnectReceiver<>();
            getHealthConnectManager()
                    .queryActivityDates(recordTypes, Executors.newSingleThreadExecutor(), receiver);
            return receiver.getResponse();
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    public static ExerciseSessionRecord buildExerciseSession() {
        return buildExerciseSession(buildExerciseRoute(), "Morning training", "rain");
    }

    public static SleepSessionRecord buildSleepSession() {
        return new SleepSessionRecord.Builder(
                        generateMetadata(), SESSION_START_TIME, SESSION_END_TIME)
                .setNotes("warm")
                .setTitle("Afternoon nap")
                .setStages(
                        List.of(
                                new SleepSessionRecord.Stage(
                                        SESSION_START_TIME,
                                        SESSION_START_TIME.plusSeconds(300),
                                        SleepSessionRecord.StageType.STAGE_TYPE_SLEEPING_LIGHT),
                                new SleepSessionRecord.Stage(
                                        SESSION_START_TIME.plusSeconds(300),
                                        SESSION_START_TIME.plusSeconds(600),
                                        SleepSessionRecord.StageType.STAGE_TYPE_SLEEPING_REM),
                                new SleepSessionRecord.Stage(
                                        SESSION_START_TIME.plusSeconds(900),
                                        SESSION_START_TIME.plusSeconds(1200),
                                        SleepSessionRecord.StageType.STAGE_TYPE_SLEEPING_DEEP)))
                .build();
    }

    public static void startMigration() throws InterruptedException {
        MigrationReceiver receiver = new MigrationReceiver();
        getHealthConnectManager().startMigration(Executors.newSingleThreadExecutor(), receiver);
        receiver.verifyNoExceptionOrThrow();
    }

    public static void writeMigrationData(List<MigrationEntity> entities)
            throws InterruptedException {
        MigrationReceiver receiver = new MigrationReceiver();
        getHealthConnectManager()
                .writeMigrationData(entities, Executors.newSingleThreadExecutor(), receiver);
        receiver.verifyNoExceptionOrThrow();
    }

    public static void finishMigration() throws InterruptedException {
        MigrationReceiver receiver = new MigrationReceiver();
        getHealthConnectManager().finishMigration(Executors.newSingleThreadExecutor(), receiver);
        receiver.verifyNoExceptionOrThrow();
    }

    public static void insertMinDataMigrationSdkExtensionVersion(int version)
            throws InterruptedException {
        MigrationReceiver receiver = new MigrationReceiver();
        getHealthConnectManager()
                .insertMinDataMigrationSdkExtensionVersion(
                        version, Executors.newSingleThreadExecutor(), receiver);
        receiver.verifyNoExceptionOrThrow();
    }

    public static void deleteAllStagedRemoteData() {
        HealthConnectManager service = getHealthConnectManager();
        runWithShellPermissionIdentity(
                () ->
                        // TODO(b/241542162): Avoid reflection once TestApi can be called from CTS
                        service.getClass().getMethod("deleteAllStagedRemoteData").invoke(service),
                "android.permission.DELETE_STAGED_HEALTH_CONNECT_REMOTE_DATA");
    }

    public static int getHealthConnectDataMigrationState() throws InterruptedException {
        HealthConnectReceiver<HealthConnectDataState> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager()
                .getHealthConnectDataState(Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse().getDataMigrationState();
    }

    public static int getHealthConnectDataRestoreState() throws InterruptedException {
        HealthConnectReceiver<HealthConnectDataState> receiver = new HealthConnectReceiver<>();
        runWithShellPermissionIdentity(
                () ->
                        getHealthConnectManager()
                                .getHealthConnectDataState(
                                        Executors.newSingleThreadExecutor(), receiver),
                MANAGE_HEALTH_DATA);
        return receiver.getResponse().getDataRestoreState();
    }

    public static List<AppInfo> getApplicationInfo() throws InterruptedException {
        HealthConnectReceiver<ApplicationInfoResponse> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager()
                .getContributorApplicationsInfo(Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse().getApplicationInfoList();
    }

    public static <T extends Record> T getRecordById(List<T> list, String id) {
        for (T record : list) {
            if (record.getMetadata().getId().equals(id)) {
                return record;
            }
        }

        throw new AssertionError("Record not found with id: " + id);
    }

    public static Metadata generateMetadata() {
        Context context = ApplicationProvider.getApplicationContext();
        return new Metadata.Builder()
                .setDevice(buildDevice())
                .setId(UUID.randomUUID().toString())
                .setClientRecordId("clientRecordId" + Math.random())
                .setDataOrigin(
                        new DataOrigin.Builder().setPackageName(context.getPackageName()).build())
                .setDevice(buildDevice())
                .setRecordingMethod(Metadata.RECORDING_METHOD_UNKNOWN)
                .build();
    }

    public static HeartRateRecord getHugeHeartRateRecord() {
        Device device =
                new Device.Builder()
                        .setManufacturer("google")
                        .setModel("Pixel4a")
                        .setType(2)
                        .build();
        DataOrigin dataOrigin =
                new DataOrigin.Builder().setPackageName("android.healthconnect.cts").build();
        Metadata.Builder testMetadataBuilder = new Metadata.Builder();
        testMetadataBuilder.setDevice(device).setDataOrigin(dataOrigin);
        testMetadataBuilder.setClientRecordId("HRR" + Math.random());
        testMetadataBuilder.setRecordingMethod(Metadata.RECORDING_METHOD_ACTIVELY_RECORDED);

        HeartRateRecord.HeartRateSample heartRateRecord =
                new HeartRateRecord.HeartRateSample(10, Instant.now().plusMillis(100));
        ArrayList<HeartRateRecord.HeartRateSample> heartRateRecords =
                new ArrayList<>(Collections.nCopies(85000, heartRateRecord));

        return new HeartRateRecord.Builder(
                        testMetadataBuilder.build(),
                        Instant.now(),
                        Instant.now().plusMillis(500),
                        heartRateRecords)
                .build();
    }

    /** Creates and returns a {@link StepsRecord} with default arguments. */
    public static StepsRecord getCompleteStepsRecord() {
        return getCompleteStepsRecord(
                Instant.now(),
                Instant.now().plusMillis(1000),
                /* clientRecordId= */ "SR" + Math.random());
    }

    /** Creates and returns a {@link StepsRecord} with the specified arguments. */
    public static StepsRecord getCompleteStepsRecord(
            Instant startTime, Instant endTime, String clientRecordId) {
        Device device =
                new Device.Builder().setManufacturer("google").setModel("Pixel").setType(1).build();
        DataOrigin dataOrigin =
                new DataOrigin.Builder().setPackageName("android.healthconnect.cts").build();

        Metadata.Builder testMetadataBuilder = new Metadata.Builder();
        testMetadataBuilder.setDevice(device).setDataOrigin(dataOrigin);
        testMetadataBuilder.setClientRecordId(clientRecordId);
        testMetadataBuilder.setRecordingMethod(RECORDING_METHOD_ACTIVELY_RECORDED);
        Metadata testMetaData = testMetadataBuilder.build();
        assertThat(testMetaData.getRecordingMethod()).isEqualTo(RECORDING_METHOD_ACTIVELY_RECORDED);
        return new StepsRecord.Builder(testMetaData, startTime, endTime, 10).build();
    }

    public static StepsRecord getStepsRecord_update(
            Record record, String id, String clientRecordId) {
        Metadata metadata = record.getMetadata();
        Metadata metadataWithId =
                new Metadata.Builder()
                        .setId(id)
                        .setClientRecordId(clientRecordId)
                        .setClientRecordVersion(metadata.getClientRecordVersion())
                        .setDataOrigin(metadata.getDataOrigin())
                        .setDevice(metadata.getDevice())
                        .setLastModifiedTime(metadata.getLastModifiedTime())
                        .build();
        return new StepsRecord.Builder(
                        metadataWithId, Instant.now(), Instant.now().plusMillis(2000), 20)
                .setStartZoneOffset(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()))
                .setEndZoneOffset(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()))
                .build();
    }

    private static ExerciseSessionRecord buildExerciseSession(
            ExerciseRoute route, String title, String notes) {
        return new ExerciseSessionRecord.Builder(
                        generateMetadata(),
                        SESSION_START_TIME,
                        SESSION_END_TIME,
                        ExerciseSessionType.EXERCISE_SESSION_TYPE_OTHER_WORKOUT)
                .setRoute(route)
                .setLaps(
                        List.of(
                                new ExerciseLap.Builder(
                                                SESSION_START_TIME,
                                                SESSION_START_TIME.plusSeconds(20))
                                        .setLength(Length.fromMeters(10))
                                        .build(),
                                new ExerciseLap.Builder(
                                                SESSION_END_TIME.minusSeconds(20), SESSION_END_TIME)
                                        .build()))
                .setSegments(
                        List.of(
                                new ExerciseSegment.Builder(
                                                SESSION_START_TIME.plusSeconds(1),
                                                SESSION_START_TIME.plusSeconds(10),
                                                ExerciseSegmentType
                                                        .EXERCISE_SEGMENT_TYPE_BENCH_PRESS)
                                        .build(),
                                new ExerciseSegment.Builder(
                                                SESSION_START_TIME.plusSeconds(21),
                                                SESSION_START_TIME.plusSeconds(124),
                                                ExerciseSegmentType.EXERCISE_SEGMENT_TYPE_BURPEE)
                                        .setRepetitionsCount(15)
                                        .build()))
                .setEndZoneOffset(ZoneOffset.MAX)
                .setStartZoneOffset(ZoneOffset.MIN)
                .setNotes(notes)
                .setTitle(title)
                .build();
    }

    public static void populateAndResetExpectedResponseMap(
            HashMap<Class<? extends Record>, RecordTypeInfoTestResponse> expectedResponseMap) {
        expectedResponseMap.put(
                ElevationGainedRecord.class,
                new RecordTypeInfoTestResponse(
                        ACTIVITY, HealthPermissionCategory.ELEVATION_GAINED, new ArrayList<>()));
        expectedResponseMap.put(
                OvulationTestRecord.class,
                new RecordTypeInfoTestResponse(
                        CYCLE_TRACKING,
                        HealthPermissionCategory.OVULATION_TEST,
                        new ArrayList<>()));
        expectedResponseMap.put(
                DistanceRecord.class,
                new RecordTypeInfoTestResponse(
                        ACTIVITY, HealthPermissionCategory.DISTANCE, new ArrayList<>()));
        expectedResponseMap.put(
                SpeedRecord.class,
                new RecordTypeInfoTestResponse(
                        ACTIVITY, HealthPermissionCategory.SPEED, new ArrayList<>()));

        expectedResponseMap.put(
                Vo2MaxRecord.class,
                new RecordTypeInfoTestResponse(
                        ACTIVITY, HealthPermissionCategory.VO2_MAX, new ArrayList<>()));
        expectedResponseMap.put(
                OxygenSaturationRecord.class,
                new RecordTypeInfoTestResponse(
                        VITALS, HealthPermissionCategory.OXYGEN_SATURATION, new ArrayList<>()));
        expectedResponseMap.put(
                TotalCaloriesBurnedRecord.class,
                new RecordTypeInfoTestResponse(
                        ACTIVITY,
                        HealthPermissionCategory.TOTAL_CALORIES_BURNED,
                        new ArrayList<>()));
        expectedResponseMap.put(
                HydrationRecord.class,
                new RecordTypeInfoTestResponse(
                        NUTRITION, HealthPermissionCategory.HYDRATION, new ArrayList<>()));
        expectedResponseMap.put(
                StepsRecord.class,
                new RecordTypeInfoTestResponse(ACTIVITY, STEPS, new ArrayList<>()));
        expectedResponseMap.put(
                CervicalMucusRecord.class,
                new RecordTypeInfoTestResponse(
                        CYCLE_TRACKING,
                        HealthPermissionCategory.CERVICAL_MUCUS,
                        new ArrayList<>()));
        expectedResponseMap.put(
                ExerciseSessionRecord.class,
                new RecordTypeInfoTestResponse(ACTIVITY, EXERCISE, new ArrayList<>()));
        expectedResponseMap.put(
                HeartRateRecord.class,
                new RecordTypeInfoTestResponse(VITALS, HEART_RATE, new ArrayList<>()));
        expectedResponseMap.put(
                RespiratoryRateRecord.class,
                new RecordTypeInfoTestResponse(
                        VITALS, HealthPermissionCategory.RESPIRATORY_RATE, new ArrayList<>()));
        expectedResponseMap.put(
                BasalBodyTemperatureRecord.class,
                new RecordTypeInfoTestResponse(
                        VITALS,
                        HealthPermissionCategory.BASAL_BODY_TEMPERATURE,
                        new ArrayList<>()));
        expectedResponseMap.put(
                WheelchairPushesRecord.class,
                new RecordTypeInfoTestResponse(
                        ACTIVITY, HealthPermissionCategory.WHEELCHAIR_PUSHES, new ArrayList<>()));
        expectedResponseMap.put(
                PowerRecord.class,
                new RecordTypeInfoTestResponse(
                        ACTIVITY, HealthPermissionCategory.POWER, new ArrayList<>()));
        expectedResponseMap.put(
                BodyWaterMassRecord.class,
                new RecordTypeInfoTestResponse(
                        BODY_MEASUREMENTS,
                        HealthPermissionCategory.BODY_WATER_MASS,
                        new ArrayList<>()));
        expectedResponseMap.put(
                WeightRecord.class,
                new RecordTypeInfoTestResponse(
                        BODY_MEASUREMENTS, HealthPermissionCategory.WEIGHT, new ArrayList<>()));
        expectedResponseMap.put(
                BoneMassRecord.class,
                new RecordTypeInfoTestResponse(
                        BODY_MEASUREMENTS, HealthPermissionCategory.BONE_MASS, new ArrayList<>()));
        expectedResponseMap.put(
                RestingHeartRateRecord.class,
                new RecordTypeInfoTestResponse(
                        VITALS, HealthPermissionCategory.RESTING_HEART_RATE, new ArrayList<>()));
        expectedResponseMap.put(
                ActiveCaloriesBurnedRecord.class,
                new RecordTypeInfoTestResponse(
                        ACTIVITY,
                        HealthPermissionCategory.ACTIVE_CALORIES_BURNED,
                        new ArrayList<>()));
        expectedResponseMap.put(
                BodyFatRecord.class,
                new RecordTypeInfoTestResponse(
                        BODY_MEASUREMENTS, HealthPermissionCategory.BODY_FAT, new ArrayList<>()));
        expectedResponseMap.put(
                BodyTemperatureRecord.class,
                new RecordTypeInfoTestResponse(
                        VITALS, HealthPermissionCategory.BODY_TEMPERATURE, new ArrayList<>()));
        expectedResponseMap.put(
                NutritionRecord.class,
                new RecordTypeInfoTestResponse(
                        NUTRITION, HealthPermissionCategory.NUTRITION, new ArrayList<>()));
        expectedResponseMap.put(
                LeanBodyMassRecord.class,
                new RecordTypeInfoTestResponse(
                        BODY_MEASUREMENTS,
                        HealthPermissionCategory.LEAN_BODY_MASS,
                        new ArrayList<>()));
        expectedResponseMap.put(
                HeartRateVariabilityRmssdRecord.class,
                new RecordTypeInfoTestResponse(
                        VITALS,
                        HealthPermissionCategory.HEART_RATE_VARIABILITY,
                        new ArrayList<>()));
        expectedResponseMap.put(
                MenstruationFlowRecord.class,
                new RecordTypeInfoTestResponse(
                        CYCLE_TRACKING, HealthPermissionCategory.MENSTRUATION, new ArrayList<>()));
        expectedResponseMap.put(
                BloodGlucoseRecord.class,
                new RecordTypeInfoTestResponse(
                        VITALS, HealthPermissionCategory.BLOOD_GLUCOSE, new ArrayList<>()));
        expectedResponseMap.put(
                BloodPressureRecord.class,
                new RecordTypeInfoTestResponse(
                        VITALS, HealthPermissionCategory.BLOOD_PRESSURE, new ArrayList<>()));
        expectedResponseMap.put(
                CyclingPedalingCadenceRecord.class,
                new RecordTypeInfoTestResponse(ACTIVITY, EXERCISE, new ArrayList<>()));
        expectedResponseMap.put(
                IntermenstrualBleedingRecord.class,
                new RecordTypeInfoTestResponse(
                        CYCLE_TRACKING,
                        HealthPermissionCategory.INTERMENSTRUAL_BLEEDING,
                        new ArrayList<>()));
        expectedResponseMap.put(
                FloorsClimbedRecord.class,
                new RecordTypeInfoTestResponse(
                        ACTIVITY, HealthPermissionCategory.FLOORS_CLIMBED, new ArrayList<>()));
        expectedResponseMap.put(
                StepsCadenceRecord.class,
                new RecordTypeInfoTestResponse(ACTIVITY, STEPS, new ArrayList<>()));
        expectedResponseMap.put(
                HeightRecord.class,
                new RecordTypeInfoTestResponse(
                        BODY_MEASUREMENTS, HealthPermissionCategory.HEIGHT, new ArrayList<>()));
        expectedResponseMap.put(
                SexualActivityRecord.class,
                new RecordTypeInfoTestResponse(
                        CYCLE_TRACKING,
                        HealthPermissionCategory.SEXUAL_ACTIVITY,
                        new ArrayList<>()));
        expectedResponseMap.put(
                MenstruationPeriodRecord.class,
                new RecordTypeInfoTestResponse(
                        CYCLE_TRACKING, HealthPermissionCategory.MENSTRUATION, new ArrayList<>()));
        expectedResponseMap.put(
                SleepSessionRecord.class,
                new RecordTypeInfoTestResponse(
                        SLEEP, HealthPermissionCategory.SLEEP, new ArrayList<>()));
        expectedResponseMap.put(
                BasalMetabolicRateRecord.class,
                new RecordTypeInfoTestResponse(
                        BODY_MEASUREMENTS, BASAL_METABOLIC_RATE, new ArrayList<>()));
    }

    public static FetchDataOriginsPriorityOrderResponse fetchDataOriginsPriorityOrder(
            int dataCategory) throws InterruptedException {
        HealthConnectReceiver<FetchDataOriginsPriorityOrderResponse> receiver =
                new HealthConnectReceiver<>();
        getHealthConnectManager()
                .fetchDataOriginsPriorityOrder(
                        dataCategory, Executors.newSingleThreadExecutor(), receiver);
        return receiver.getResponse();
    }

    public static void updateDataOriginPriorityOrder(UpdateDataOriginPriorityOrderRequest request)
            throws InterruptedException {
        HealthConnectReceiver<Void> receiver = new HealthConnectReceiver<>();
        getHealthConnectManager()
                .updateDataOriginPriorityOrder(
                        request, Executors.newSingleThreadExecutor(), receiver);
        receiver.verifyNoExceptionOrThrow();
    }

    public static void grantPermission(String pkgName, String permission) {
        HealthConnectManager service = getHealthConnectManager();
        runWithShellPermissionIdentity(
                () ->
                        service.getClass()
                                .getMethod("grantHealthPermission", String.class, String.class)
                                .invoke(service, pkgName, permission),
                MANAGE_HEALTH_PERMISSIONS);
    }

    public static void revokePermission(String pkgName, String permission) {
        HealthConnectManager service = getHealthConnectManager();
        runWithShellPermissionIdentity(
                () ->
                        service.getClass()
                                .getMethod(
                                        "revokeHealthPermission",
                                        String.class,
                                        String.class,
                                        String.class)
                                .invoke(service, pkgName, permission, null),
                MANAGE_HEALTH_PERMISSIONS);
    }

    /**
     * Utility method to call {@link HealthConnectManager#revokeAllHealthPermissions(String,
     * String)}.
     */
    public static void revokeAllPermissions(String packageName, @Nullable String reason) {
        HealthConnectManager service = getHealthConnectManager();
        runWithShellPermissionIdentity(
                () ->
                        service.getClass()
                                .getMethod("revokeAllHealthPermissions", String.class, String.class)
                                .invoke(service, packageName, reason),
                MANAGE_HEALTH_PERMISSIONS);
    }

    /**
     * Same as {@link #revokeAllPermissions(String, String)} but with a delay to wait for grant time
     * to be updated.
     */
    public static void revokeAllPermissionsWithDelay(String packageName, @Nullable String reason)
            throws InterruptedException {
        revokeAllPermissions(packageName, reason);
        Thread.sleep(500);
    }

    /**
     * Utility method to call {@link
     * HealthConnectManager#getHealthDataHistoricalAccessStartDate(String)}.
     */
    public static Instant getHealthDataHistoricalAccessStartDate(String packageName) {
        HealthConnectManager service = getHealthConnectManager();
        return (Instant)
                runWithShellPermissionIdentity(
                        () ->
                                service.getClass()
                                        .getMethod(
                                                "getHealthDataHistoricalAccessStartDate",
                                                String.class)
                                        .invoke(service, packageName),
                        MANAGE_HEALTH_PERMISSIONS);
    }

    public static void revokeHealthPermissions(String packageName) {
        runWithShellPermissionIdentity(() -> revokeHealthPermissionsPrivileged(packageName));
    }

    private static void revokeHealthPermissionsPrivileged(String packageName)
            throws PackageManager.NameNotFoundException {
        final Context targetContext = androidx.test.InstrumentationRegistry.getTargetContext();
        final PackageManager packageManager = targetContext.getPackageManager();
        final UserHandle user = targetContext.getUser();

        final PackageInfo packageInfo =
                packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS));

        final String[] permissions = packageInfo.requestedPermissions;
        if (permissions == null) {
            return;
        }

        for (String permission : permissions) {
            if (permission.startsWith(HEALTH_PERMISSION_PREFIX)) {
                packageManager.revokeRuntimePermission(packageName, permission, user);
            }
        }
    }

    public static List<String> getGrantedHealthPermissions(String pkgName) {
        final PackageInfo pi = getAppPackageInfo(pkgName);
        final String[] requestedPermissions = pi.requestedPermissions;
        final int[] requestedPermissionsFlags = pi.requestedPermissionsFlags;

        if (requestedPermissions == null) {
            return List.of();
        }

        final List<String> permissions = new ArrayList<>();

        for (int i = 0; i < requestedPermissions.length; i++) {
            if ((requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                if (requestedPermissions[i].startsWith(HEALTH_PERMISSION_PREFIX)) {
                    permissions.add(requestedPermissions[i]);
                }
            }
        }

        return permissions;
    }

    private static PackageInfo getAppPackageInfo(String pkgName) {
        final Context targetContext = androidx.test.InstrumentationRegistry.getTargetContext();
        return runWithShellPermissionIdentity(
                () ->
                        targetContext
                                .getPackageManager()
                                .getPackageInfo(
                                        pkgName,
                                        PackageManager.PackageInfoFlags.of(GET_PERMISSIONS)));
    }

    public static void deleteTestData() throws InterruptedException {
        verifyDeleteRecords(
                new DeleteUsingFiltersRequest.Builder()
                        .setTimeRangeFilter(
                                new TimeInstantRangeFilter.Builder()
                                        .setStartTime(Instant.EPOCH)
                                        .setEndTime(Instant.now().plus(10, ChronoUnit.DAYS))
                                        .build())
                        .addRecordType(ExerciseSessionRecord.class)
                        .addRecordType(StepsRecord.class)
                        .addRecordType(HeartRateRecord.class)
                        .addRecordType(BasalMetabolicRateRecord.class)
                        .build());
    }

    /** Revokes all granted Health permissions and re-grants them back. */
    public static void revokeAndThenGrantHealthPermissions(String packageName) {
        List<String> healthPerms = getGrantedHealthPermissions(packageName);

        revokeHealthPermissions(packageName);

        for (String perm : healthPerms) {
            grantPermission(packageName, perm);
        }
    }

    public static String runShellCommand(String command) throws IOException {
        UiAutomation uiAutomation =
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                        .getUiAutomation();
        uiAutomation.adoptShellPermissionIdentity();
        final ParcelFileDescriptor stdout = uiAutomation.executeShellCommand(command);
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(new FileInputStream(stdout.getFileDescriptor())))) {
            char[] buffer = new char[4096];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                output.append(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        return output.toString();
    }

    @NonNull
    private static HealthConnectManager getHealthConnectManager() {
        return getHealthConnectManager(ApplicationProvider.getApplicationContext());
    }

    @NonNull
    private static HealthConnectManager getHealthConnectManager(Context context) {
        return requireNonNull(context.getSystemService(HealthConnectManager.class));
    }

    public static String getDeviceConfigValue(String key) {
        return SystemUtil.runShellCommand("device_config get health_fitness " + key);
    }

    public static void setDeviceConfigValue(String key, String value) {
        SystemUtil.runShellCommand("device_config put health_fitness " + key + " " + value);
    }

    public static void sendCommandToTestAppReceiver(Context context, String action) {
        sendCommandToTestAppReceiver(context, action, /* extras= */ null);
    }

    public static void sendCommandToTestAppReceiver(Context context, String action, Bundle extras) {
        final Intent intent = new Intent(action).setClassName(PKG_TEST_APP, TEST_APP_RECEIVER);
        intent.putExtra(EXTRA_SENDER_PACKAGE_NAME, context.getPackageName());
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.sendBroadcast(intent);
    }

    /** Sets up the priority list for aggregation tests. */
    public static void setupAggregation(String packageName, int dataCategory)
            throws InterruptedException {
        insertRecordsForPriority(packageName);

        // Add the packageName inserting the records to the priority list manually
        // Since CTS tests get their permissions granted at install time and skip
        // the Health Connect APIs that would otherwise add the packageName to the priority list

        updatePriorityWithManageHealthDataPermission(dataCategory, Arrays.asList(packageName));
        FetchDataOriginsPriorityOrderResponse newPriority =
                getPriorityWithManageHealthDataPermission(dataCategory);
        List<String> newPriorityString =
                newPriority.getDataOriginsPriorityOrder().stream()
                        .map(DataOrigin::getPackageName)
                        .toList();
        assertThat(newPriorityString.size()).isEqualTo(1);
        assertThat(newPriorityString.get(0)).isEqualTo(packageName);
    }

    /** Inserts a record that does not support aggregation to enable the priority list. */
    public static void insertRecordsForPriority(String packageName) throws InterruptedException {
        // Insert records that do not support aggregation so that the AppInfoTable is initialised
        MenstruationPeriodRecord recordToInsert =
                new MenstruationPeriodRecord.Builder(
                                new Metadata.Builder()
                                        .setDataOrigin(
                                                new DataOrigin.Builder()
                                                        .setPackageName(packageName)
                                                        .build())
                                        .build(),
                                Instant.now(),
                                Instant.now().plusMillis(1000))
                        .build();
        insertRecords(Arrays.asList(recordToInsert));
    }

    /** Updates the priority list after getting the MANAGE_HEALTH_DATA permission. */
    public static void updatePriorityWithManageHealthDataPermission(
            int permissionCategory, List<String> packageNames) throws InterruptedException {
        UiAutomation uiAutomation =
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                        .getUiAutomation();

        uiAutomation.adoptShellPermissionIdentity(MANAGE_HEALTH_DATA);
        try {
            updatePriority(permissionCategory, packageNames);
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    /** Updates the priority list without getting the MANAGE_HEALTH_DATA permission. */
    public static void updatePriority(int permissionCategory, List<String> packageNames)
            throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        HealthConnectManager service = context.getSystemService(HealthConnectManager.class);
        assertThat(service).isNotNull();

        List<DataOrigin> dataOrigins =
                packageNames.stream()
                        .map(
                                (packageName) ->
                                        new DataOrigin.Builder()
                                                .setPackageName(packageName)
                                                .build())
                        .collect(Collectors.toList());

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<HealthConnectException> healthConnectExceptionAtomicReference =
                new AtomicReference<>();
        UpdateDataOriginPriorityOrderRequest updateDataOriginPriorityOrderRequest =
                new UpdateDataOriginPriorityOrderRequest(dataOrigins, permissionCategory);
        service.updateDataOriginPriorityOrder(
                updateDataOriginPriorityOrderRequest,
                Executors.newSingleThreadExecutor(),
                new OutcomeReceiver<>() {
                    @Override
                    public void onResult(Void result) {
                        latch.countDown();
                    }

                    @Override
                    public void onError(HealthConnectException exception) {
                        healthConnectExceptionAtomicReference.set(exception);
                        latch.countDown();
                    }
                });
        assertThat(updateDataOriginPriorityOrderRequest.getDataCategory())
                .isEqualTo(permissionCategory);
        assertThat(updateDataOriginPriorityOrderRequest.getDataOriginInOrder()).isNotNull();
        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        if (healthConnectExceptionAtomicReference.get() != null) {
            throw healthConnectExceptionAtomicReference.get();
        }
    }

    public static boolean isHardwareSupported() {
        return isHardwareSupported(ApplicationProvider.getApplicationContext());
    }

    /** returns true if the hardware is supported by HealthConnect. */
    public static boolean isHardwareSupported(Context context) {
        PackageManager pm = context.getPackageManager();
        return (!pm.hasSystemFeature(PackageManager.FEATURE_EMBEDDED)
                && !pm.hasSystemFeature(PackageManager.FEATURE_WATCH)
                && !pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                && !pm.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE));
    }

    /** Gets the priority list after getting the MANAGE_HEALTH_DATA permission. */
    public static FetchDataOriginsPriorityOrderResponse getPriorityWithManageHealthDataPermission(
            int permissionCategory) throws InterruptedException {
        UiAutomation uiAutomation =
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                        .getUiAutomation();

        uiAutomation.adoptShellPermissionIdentity(MANAGE_HEALTH_DATA);
        FetchDataOriginsPriorityOrderResponse response;

        try {
            response = getPriority(permissionCategory);
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }

        return response;
    }

    /** Gets the priority list without requesting the MANAGE_HEALTH_DATA permission. */
    public static FetchDataOriginsPriorityOrderResponse getPriority(int permissionCategory)
            throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        HealthConnectManager service = context.getSystemService(HealthConnectManager.class);
        assertThat(service).isNotNull();

        AtomicReference<FetchDataOriginsPriorityOrderResponse> response = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<HealthConnectException> healthConnectExceptionAtomicReference =
                new AtomicReference<>();
        service.fetchDataOriginsPriorityOrder(
                permissionCategory,
                Executors.newSingleThreadExecutor(),
                new OutcomeReceiver<>() {
                    @Override
                    public void onResult(FetchDataOriginsPriorityOrderResponse result) {
                        response.set(result);
                        latch.countDown();
                    }

                    @Override
                    public void onError(HealthConnectException exception) {
                        healthConnectExceptionAtomicReference.set(exception);
                        latch.countDown();
                    }
                });
        assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
        if (healthConnectExceptionAtomicReference.get() != null) {
            throw healthConnectExceptionAtomicReference.get();
        }

        return response.get();
    }

    /** Zips given id and records lists to create a list of {@link RecordIdFilter}. */
    public static List<RecordIdFilter> getRecordIdFilters(
            List<String> recordIds, List<Record> records) {
        return IntStream.range(0, recordIds.size())
                .mapToObj(
                        i -> {
                            Class<? extends Record> recordClass = records.get(i).getClass();
                            String id = recordIds.get(i);
                            return RecordIdFilter.fromId(recordClass, id);
                        })
                .toList();
    }

    public static Metadata getEmptyMetadata() {
        return new Metadata.Builder().build();
    }

    /** Creates a {@link Metadata} with the given record id. */
    public static Metadata getMetadataForId(String id) {
        return new Metadata.Builder().setId(id).build();
    }

    /** Creates a {@link Metadata} with the given record id and data origin. */
    public static Metadata getMetadataForId(String id, DataOrigin dataOrigin) {
        return new Metadata.Builder().setId(id).setDataOrigin(dataOrigin).build();
    }

    /** Creates a {@link Metadata} with the given client record id. */
    public static Metadata getMetadataForClientId(String clientId) {
        return new Metadata.Builder().setClientRecordId(clientId).build();
    }

    /** Creates a {@link Metadata} with the given client record id and data origin. */
    public static Metadata getMetadataForClientId(String clientId, DataOrigin dataOrigin) {
        return new Metadata.Builder().setClientRecordId(clientId).setDataOrigin(dataOrigin).build();
    }

    /** Creates a {@link Metadata} with the given data origin. */
    public static Metadata getMetadata(DataOrigin dataOrigin) {
        return new Metadata.Builder().setDataOrigin(dataOrigin).build();
    }

    /** Creates a {@link DataOrigin} with the given package name. */
    public static DataOrigin getDataOrigin(String packageName) {
        return new DataOrigin.Builder().setPackageName(packageName).build();
    }

    /** Creates a list of {@link DataOrigin} from a list of package names. */
    public static List<DataOrigin> getDataOrigins(String... packageNames) {
        return Arrays.stream(packageNames).map(TestUtils::getDataOrigin).toList();
    }

    /** Creates a {@link ExerciseRoute.Location}. */
    public static ExerciseRoute.Location getLocation(
            Instant time, double latitude, double longitude) {
        return new ExerciseRoute.Location.Builder(time, latitude, longitude).build();
    }

    /** Creates a {@link ExerciseRoute} with given locations. */
    public static ExerciseRoute getExerciseRoute(ExerciseRoute.Location... locations) {
        return new ExerciseRoute(Arrays.asList(locations));
    }

    /** Creates an {@link Instant} representing the given local time yesterday at UTC. */
    public static Instant yesterdayAt(String localTime) {
        return LocalTime.parse(localTime)
                .atDate(LocalDate.now().minusDays(1))
                .toInstant(ZoneOffset.UTC);
    }

    public static final class RecordAndIdentifier {
        private final int mId;
        private final Record mRecordClass;

        public RecordAndIdentifier(int id, Record recordClass) {
            this.mId = id;
            this.mRecordClass = recordClass;
        }

        public int getId() {
            return mId;
        }

        public Record getRecordClass() {
            return mRecordClass;
        }
    }

    public static class RecordTypeInfoTestResponse {
        private final int mRecordTypePermission;
        private final ArrayList<String> mContributingPackages;
        private final int mRecordTypeCategory;

        RecordTypeInfoTestResponse(
                int recordTypeCategory,
                int recordTypePermission,
                ArrayList<String> contributingPackages) {
            mRecordTypeCategory = recordTypeCategory;
            mRecordTypePermission = recordTypePermission;
            mContributingPackages = contributingPackages;
        }

        public int getRecordTypeCategory() {
            return mRecordTypeCategory;
        }

        public int getRecordTypePermission() {
            return mRecordTypePermission;
        }

        public ArrayList<String> getContributingPackages() {
            return mContributingPackages;
        }
    }

    public static class RecordTypeAndRecordIds implements Serializable {
        private String mRecordType;
        private List<String> mRecordIds;

        public RecordTypeAndRecordIds(String recordType, List<String> ids) {
            mRecordType = recordType;
            mRecordIds = ids;
        }

        public String getRecordType() {
            return mRecordType;
        }

        public List<String> getRecordIds() {
            return mRecordIds;
        }
    }

    private static class TestReceiver<T, E extends RuntimeException>
            implements OutcomeReceiver<T, E> {
        private final CountDownLatch mLatch = new CountDownLatch(1);
        private final AtomicReference<T> mResponse = new AtomicReference<>();
        private final AtomicReference<E> mException = new AtomicReference<>();

        public T getResponse() throws InterruptedException {
            verifyNoExceptionOrThrow();
            return mResponse.get();
        }

        public void verifyNoExceptionOrThrow() throws InterruptedException {
            assertThat(mLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            if (mException.get() != null) {
                throw mException.get();
            }
        }

        @Override
        public void onResult(T result) {
            mResponse.set(result);
            mLatch.countDown();
        }

        @Override
        public void onError(@NonNull E error) {
            mException.set(error);
            Log.e(TAG, error.getMessage());
            mLatch.countDown();
        }
    }

    private static final class HealthConnectReceiver<T>
            extends TestReceiver<T, HealthConnectException> {}

    private static final class MigrationReceiver extends TestReceiver<Void, MigrationException> {}
}
