/*
 * Copyright (C) 2024 The Android Open Source Project
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

package android.healthconnect.cts.readdata;

import static android.healthconnect.cts.utils.DataFactory.getDataOrigin;
import static android.healthconnect.cts.utils.DataFactory.getDistanceRecord;
import static android.healthconnect.cts.utils.DataFactory.getHeartRateRecord;
import static android.healthconnect.cts.utils.DataFactory.getStepsRecord;
import static android.healthconnect.cts.utils.DataFactory.getTotalCaloriesBurnedRecord;
import static android.healthconnect.cts.utils.TestUtils.PKG_TEST_APP;
import static android.healthconnect.cts.utils.TestUtils.insertRecords;
import static android.healthconnect.cts.utils.TestUtils.insertStepsRecordViaTestApp;
import static android.healthconnect.cts.utils.TestUtils.readRecords;

import static com.google.common.truth.Truth.assertThat;

import static java.time.temporal.ChronoUnit.DAYS;

import android.content.Context;
import android.health.connect.ReadRecordsRequest;
import android.health.connect.ReadRecordsRequestUsingFilters;
import android.health.connect.datatypes.DistanceRecord;
import android.health.connect.datatypes.HeartRateRecord;
import android.health.connect.datatypes.Record;
import android.health.connect.datatypes.StepsRecord;
import android.health.connect.datatypes.TotalCaloriesBurnedRecord;
import android.healthconnect.cts.utils.AssumptionCheckerRule;
import android.healthconnect.cts.utils.TestUtils;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.Instant;
import java.util.List;

public class ReadByFilterTests {
    private Context mContext;

    @Rule
    public AssumptionCheckerRule mSupportedHardwareRule =
            new AssumptionCheckerRule(
                    TestUtils::isHardwareSupported, "Tests should run on supported hardware only.");

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        TestUtils.deleteAllStagedRemoteData();
    }

    @After
    public void tearDown() {
        TestUtils.deleteAllStagedRemoteData();
    }

    @Test
    public void filterByDataType_dataOfOtherTypesExcluded() throws Exception {
        String distanceId = "distanceRecord";
        String stepsId = "stepsRecord";
        String heartRateId = "heartRateRecord";
        String caloriesId = "totalCaloriesRecord";
        Instant startTime = Instant.now().minus(1, DAYS);
        insertRecords(
                List.of(
                        getDistanceRecord(123.0, startTime, startTime.plusMillis(1000), distanceId),
                        getStepsRecord(100, stepsId),
                        getHeartRateRecord(72, heartRateId),
                        getTotalCaloriesBurnedRecord(caloriesId)));

        ReadRecordsRequest<DistanceRecord> distanceRequest =
                new ReadRecordsRequestUsingFilters.Builder<>(DistanceRecord.class).build();
        List<DistanceRecord> distanceRecords = readRecords(distanceRequest);
        assertThat(distanceRecords).hasSize(1);
        assertClientId(distanceRecords.get(0), distanceId);

        ReadRecordsRequest<StepsRecord> stepsRequest =
                new ReadRecordsRequestUsingFilters.Builder<>(StepsRecord.class).build();
        List<StepsRecord> stepsRecords = readRecords(stepsRequest);
        assertThat(stepsRecords).hasSize(1);
        assertClientId(stepsRecords.get(0), stepsId);

        ReadRecordsRequest<HeartRateRecord> heartRateRequest =
                new ReadRecordsRequestUsingFilters.Builder<>(HeartRateRecord.class).build();
        List<HeartRateRecord> heartRateRecords = readRecords(heartRateRequest);
        assertThat(heartRateRecords).hasSize(1);
        assertClientId(heartRateRecords.get(0), heartRateId);

        ReadRecordsRequest<TotalCaloriesBurnedRecord> caloriesRequest =
                new ReadRecordsRequestUsingFilters.Builder<>(TotalCaloriesBurnedRecord.class)
                        .build();
        List<TotalCaloriesBurnedRecord> caloriesRecords = readRecords(caloriesRequest);
        assertThat(caloriesRecords).hasSize(1);
        assertClientId(caloriesRecords.get(0), caloriesId);
    }

    @Test
    public void filterByDataOrigin_dataOfOtherOriginsExcluded() throws Exception {
        Instant startTime = Instant.now().minus(1, DAYS);
        String id =
                insertStepsRecordViaTestApp(mContext, startTime, startTime.plusMillis(1000), 50)
                        .get(0);
        insertRecords(
                List.of(
                        getStepsRecord(
                                100,
                                startTime.plusMillis(1000),
                                startTime.plusMillis(2000),
                                "own_steps")));

        ReadRecordsRequest<StepsRecord> ownDataRequest =
                new ReadRecordsRequestUsingFilters.Builder<>(StepsRecord.class)
                        .addDataOrigins(getDataOrigin(mContext.getPackageName()))
                        .build();
        List<StepsRecord> stepsRecords = readRecords(ownDataRequest);
        assertThat(stepsRecords).hasSize(1);
        assertClientId(stepsRecords.get(0), "own_steps");

        ReadRecordsRequest<StepsRecord> testAppDataRequest =
                new ReadRecordsRequestUsingFilters.Builder<>(StepsRecord.class)
                        .addDataOrigins(getDataOrigin(PKG_TEST_APP))
                        .build();
        stepsRecords = readRecords(testAppDataRequest);
        assertThat(stepsRecords).hasSize(1);
        assertThat(stepsRecords.get(0).getMetadata().getId()).isEqualTo(id);
    }

    @Test
    public void filterByDataOrigin_noFilterSet_allDataReturned() throws Exception {
        Instant startTime = Instant.now().minus(1, DAYS);
        insertStepsRecordViaTestApp(mContext, startTime, startTime.plusMillis(1000), 50);
        insertRecords(
                List.of(
                        getStepsRecord(
                                100,
                                startTime.plusMillis(1000),
                                startTime.plusMillis(2000),
                                "own_steps")));

        ReadRecordsRequest<StepsRecord> request =
                new ReadRecordsRequestUsingFilters.Builder<>(StepsRecord.class).build();
        assertThat(readRecords(request)).hasSize(2);
    }

    @Test
    public void filterByDataOrigin_invalidOrigin_emptyResult() throws Exception {
        insertRecords(List.of(getStepsRecord()));

        ReadRecordsRequest<StepsRecord> allDataRequest =
                new ReadRecordsRequestUsingFilters.Builder<>(StepsRecord.class).build();
        assertThat(readRecords(allDataRequest)).hasSize(1);

        ReadRecordsRequest<StepsRecord> invalidOriginRequest =
                new ReadRecordsRequestUsingFilters.Builder<>(StepsRecord.class)
                        .addDataOrigins(getDataOrigin("invalid.app"))
                        .build();
        List<StepsRecord> stepsRecords = readRecords(invalidOriginRequest);
        assertThat(stepsRecords).isEmpty();
    }

    private static void assertClientId(Record record, String expected) {
        assertThat(record.getMetadata().getClientRecordId()).isEqualTo(expected);
    }
}
