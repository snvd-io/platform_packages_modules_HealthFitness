/*
 * Copyright (C) 2022 The Android Open Source Project
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

package android.healthconnect.cts;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.health.connect.DeleteUsingFiltersRequest;
import android.health.connect.ReadRecordsRequestUsingFilters;
import android.health.connect.ReadRecordsRequestUsingIds;
import android.health.connect.RecordIdFilter;
import android.health.connect.TimeInstantRangeFilter;
import android.health.connect.datatypes.DataOrigin;
import android.health.connect.datatypes.Device;
import android.health.connect.datatypes.Metadata;
import android.health.connect.datatypes.Record;
import android.health.connect.datatypes.SpeedRecord;
import android.health.connect.datatypes.units.Velocity;
import android.platform.test.annotations.AppModeFull;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@AppModeFull(reason = "HealthConnectManager is not accessible to instant apps")
@RunWith(AndroidJUnit4.class)
public class SpeedRecordTest {

    private static final String TAG = "SpeedRecordTest";

    @After
    public void tearDown() throws InterruptedException {
        TestUtils.verifyDeleteRecords(
                SpeedRecord.class,
                new TimeInstantRangeFilter.Builder()
                        .setStartTime(Instant.EPOCH)
                        .setEndTime(Instant.now())
                        .build());
    }

    @Test
    public void testInsertSpeedRecord() throws InterruptedException {
        TestUtils.insertRecords(Arrays.asList(getBaseSpeedRecord(), getCompleteSpeedRecord()));
    }

    @Test
    public void testReadSpeedRecord_usingIds() throws InterruptedException {
        testReadSpeedRecordIds();
    }

    @Test
    public void testReadSpeedRecord_invalidIds() throws InterruptedException {
        ReadRecordsRequestUsingIds<SpeedRecord> request =
                new ReadRecordsRequestUsingIds.Builder<>(SpeedRecord.class).addId("abc").build();
        List<SpeedRecord> result = TestUtils.readRecords(request);
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void testReadSpeedRecord_usingClientRecordIds() throws InterruptedException {
        List<Record> recordList = Arrays.asList(getCompleteSpeedRecord(), getCompleteSpeedRecord());
        List<Record> insertedRecords = TestUtils.insertRecords(recordList);
        readSpeedRecordUsingClientId(insertedRecords);
    }

    @Test
    public void testReadSpeedRecord_invalidClientRecordIds() throws InterruptedException {
        ReadRecordsRequestUsingIds<SpeedRecord> request =
                new ReadRecordsRequestUsingIds.Builder<>(SpeedRecord.class)
                        .addClientRecordId("abc")
                        .build();
        List<SpeedRecord> result = TestUtils.readRecords(request);
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void testReadSpeedRecordUsingFilters_default() throws InterruptedException {
        List<SpeedRecord> oldSpeedRecords =
                TestUtils.readRecords(
                        new ReadRecordsRequestUsingFilters.Builder<>(SpeedRecord.class).build());
        SpeedRecord testRecord = getCompleteSpeedRecord();
        TestUtils.insertRecords(Collections.singletonList(testRecord));
        List<SpeedRecord> newSpeedRecords =
                TestUtils.readRecords(
                        new ReadRecordsRequestUsingFilters.Builder<>(SpeedRecord.class).build());
        assertThat(newSpeedRecords.size()).isEqualTo(oldSpeedRecords.size() + 1);
        assertThat(newSpeedRecords.get(newSpeedRecords.size() - 1).equals(testRecord)).isTrue();
    }

    @Test
    public void testReadSpeedRecordUsingFilters_timeFilter() throws InterruptedException {
        TimeInstantRangeFilter filter =
                new TimeInstantRangeFilter.Builder()
                        .setStartTime(Instant.now())
                        .setEndTime(Instant.now().plusMillis(3000))
                        .build();
        SpeedRecord testRecord = getCompleteSpeedRecord();
        TestUtils.insertRecords(Collections.singletonList(testRecord));
        List<SpeedRecord> newSpeedRecords =
                TestUtils.readRecords(
                        new ReadRecordsRequestUsingFilters.Builder<>(SpeedRecord.class)
                                .setTimeRangeFilter(filter)
                                .build());
        assertThat(newSpeedRecords.size()).isEqualTo(1);
        assertThat(newSpeedRecords.get(newSpeedRecords.size() - 1).equals(testRecord)).isTrue();
    }

    @Test
    public void testReadSpeedRecordUsingFilters_dataFilter_correct() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        List<SpeedRecord> oldSpeedRecords =
                TestUtils.readRecords(
                        new ReadRecordsRequestUsingFilters.Builder<>(SpeedRecord.class)
                                .addDataOrigins(
                                        new DataOrigin.Builder()
                                                .setPackageName(context.getPackageName())
                                                .build())
                                .build());
        SpeedRecord testRecord = getCompleteSpeedRecord();
        TestUtils.insertRecords(Collections.singletonList(testRecord));
        List<SpeedRecord> newSpeedRecords =
                TestUtils.readRecords(
                        new ReadRecordsRequestUsingFilters.Builder<>(SpeedRecord.class)
                                .addDataOrigins(
                                        new DataOrigin.Builder()
                                                .setPackageName(context.getPackageName())
                                                .build())
                                .build());
        assertThat(newSpeedRecords.size() - oldSpeedRecords.size()).isEqualTo(1);
        assertThat(newSpeedRecords.get(newSpeedRecords.size() - 1).equals(testRecord)).isTrue();
    }

    @Test
    public void testReadSpeedRecordUsingFilters_dataFilter_incorrect() throws InterruptedException {
        TestUtils.insertRecords(Collections.singletonList(getCompleteSpeedRecord()));
        List<SpeedRecord> newSpeedRecords =
                TestUtils.readRecords(
                        new ReadRecordsRequestUsingFilters.Builder<>(SpeedRecord.class)
                                .addDataOrigins(
                                        new DataOrigin.Builder().setPackageName("abc").build())
                                .build());
        assertThat(newSpeedRecords.size()).isEqualTo(0);
    }

    @Test
    public void testDeleteSpeedRecord_no_filters() throws InterruptedException {
        String id = TestUtils.insertRecordAndGetId(getCompleteSpeedRecord());
        TestUtils.verifyDeleteRecords(new DeleteUsingFiltersRequest.Builder().build());
        TestUtils.assertRecordNotFound(id, SpeedRecord.class);
    }

    @Test
    public void testDeleteSpeedRecord_time_filters() throws InterruptedException {
        TimeInstantRangeFilter timeRangeFilter =
                new TimeInstantRangeFilter.Builder()
                        .setStartTime(Instant.now())
                        .setEndTime(Instant.now().plusMillis(1000))
                        .build();
        String id = TestUtils.insertRecordAndGetId(getCompleteSpeedRecord());
        TestUtils.verifyDeleteRecords(
                new DeleteUsingFiltersRequest.Builder()
                        .addRecordType(SpeedRecord.class)
                        .setTimeRangeFilter(timeRangeFilter)
                        .build());
        TestUtils.assertRecordNotFound(id, SpeedRecord.class);
    }

    @Test
    public void testDeleteSpeedRecord_recordId_filters() throws InterruptedException {
        List<Record> records = List.of(getBaseSpeedRecord(), getCompleteSpeedRecord());
        TestUtils.insertRecords(records);

        for (Record record : records) {
            TestUtils.verifyDeleteRecords(
                    new DeleteUsingFiltersRequest.Builder()
                            .addRecordType(record.getClass())
                            .build());
            TestUtils.assertRecordNotFound(record.getMetadata().getId(), record.getClass());
        }
    }

    @Test
    public void testDeleteSpeedRecord_dataOrigin_filters() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        String id = TestUtils.insertRecordAndGetId(getCompleteSpeedRecord());
        TestUtils.verifyDeleteRecords(
                new DeleteUsingFiltersRequest.Builder()
                        .addDataOrigin(
                                new DataOrigin.Builder()
                                        .setPackageName(context.getPackageName())
                                        .build())
                        .build());
        TestUtils.assertRecordNotFound(id, SpeedRecord.class);
    }

    @Test
    public void testDeleteSpeedRecord_dataOrigin_filter_incorrect() throws InterruptedException {
        String id = TestUtils.insertRecordAndGetId(getCompleteSpeedRecord());
        TestUtils.verifyDeleteRecords(
                new DeleteUsingFiltersRequest.Builder()
                        .addDataOrigin(new DataOrigin.Builder().setPackageName("abc").build())
                        .build());
        TestUtils.assertRecordFound(id, SpeedRecord.class);
    }

    @Test
    public void testDeleteSpeedRecord_usingIds() throws InterruptedException {
        List<Record> records = List.of(getBaseSpeedRecord(), getCompleteSpeedRecord());
        List<Record> insertedRecord = TestUtils.insertRecords(records);
        List<RecordIdFilter> recordIds = new ArrayList<>(records.size());
        for (Record record : insertedRecord) {
            recordIds.add(RecordIdFilter.fromId(record.getClass(), record.getMetadata().getId()));
        }

        TestUtils.verifyDeleteRecords(recordIds);
        for (Record record : records) {
            TestUtils.assertRecordNotFound(record.getMetadata().getId(), record.getClass());
        }
    }

    @Test
    public void testDeleteSpeedRecord_time_range() throws InterruptedException {
        TimeInstantRangeFilter timeRangeFilter =
                new TimeInstantRangeFilter.Builder()
                        .setStartTime(Instant.now())
                        .setEndTime(Instant.now().plusMillis(1000))
                        .build();
        String id = TestUtils.insertRecordAndGetId(getCompleteSpeedRecord());
        TestUtils.verifyDeleteRecords(SpeedRecord.class, timeRangeFilter);
        TestUtils.assertRecordNotFound(id, SpeedRecord.class);
    }

    @Test
    public void testZoneOffsets() {
        final ZoneOffset defaultZoneOffset =
                ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
        final ZoneOffset startZoneOffset = ZoneOffset.UTC;
        final ZoneOffset endZoneOffset = ZoneOffset.MAX;
        SpeedRecord.Builder builder =
                new SpeedRecord.Builder(
                        new Metadata.Builder().build(),
                        Instant.now(),
                        Instant.now().plusMillis(1000),
                        Collections.emptyList());

        assertThat(builder.setStartZoneOffset(startZoneOffset).build().getStartZoneOffset())
                .isEqualTo(startZoneOffset);
        assertThat(builder.setEndZoneOffset(endZoneOffset).build().getEndZoneOffset())
                .isEqualTo(endZoneOffset);
        assertThat(builder.clearStartZoneOffset().build().getStartZoneOffset())
                .isEqualTo(defaultZoneOffset);
        assertThat(builder.clearEndZoneOffset().build().getEndZoneOffset())
                .isEqualTo(defaultZoneOffset);
    }

    private void testReadSpeedRecordIds() throws InterruptedException {
        List<Record> recordList = Arrays.asList(getCompleteSpeedRecord(), getCompleteSpeedRecord());
        readSpeedRecordUsingIds(recordList);
    }

    private void readSpeedRecordUsingClientId(List<Record> insertedRecord)
            throws InterruptedException {
        ReadRecordsRequestUsingIds.Builder<SpeedRecord> request =
                new ReadRecordsRequestUsingIds.Builder<>(SpeedRecord.class);
        for (Record record : insertedRecord) {
            request.addClientRecordId(record.getMetadata().getClientRecordId());
        }
        List<SpeedRecord> result = TestUtils.readRecords(request.build());
        result.sort(Comparator.comparing(item -> item.getMetadata().getClientRecordId()));
        insertedRecord.sort(Comparator.comparing(item -> item.getMetadata().getClientRecordId()));

        for (int i = 0; i < result.size(); i++) {
            SpeedRecord other = (SpeedRecord) insertedRecord.get(i);
            assertThat(result.get(i).equals(other)).isTrue();
        }
    }

    private void readSpeedRecordUsingIds(List<Record> recordList) throws InterruptedException {
        List<Record> insertedRecords = TestUtils.insertRecords(recordList);
        ReadRecordsRequestUsingIds.Builder<SpeedRecord> request =
                new ReadRecordsRequestUsingIds.Builder<>(SpeedRecord.class);
        for (Record record : insertedRecords) {
            request.addId(record.getMetadata().getId());
        }
        ReadRecordsRequestUsingIds requestUsingIds = request.build();
        assertThat(requestUsingIds.getRecordType()).isEqualTo(SpeedRecord.class);
        assertThat(requestUsingIds.getRecordIdFilters()).isNotNull();
        List<SpeedRecord> result = TestUtils.readRecords(requestUsingIds);
        assertThat(result).hasSize(insertedRecords.size());
        assertThat(result.containsAll(insertedRecords)).isTrue();
    }

    private static SpeedRecord getBaseSpeedRecord() {
        SpeedRecord.SpeedRecordSample speedRecord =
                new SpeedRecord.SpeedRecordSample(
                        Velocity.fromMetersPerSecond(10.0), Instant.now().plusMillis(100));
        ArrayList<SpeedRecord.SpeedRecordSample> speedRecords = new ArrayList<>();
        speedRecords.add(speedRecord);
        speedRecords.add(speedRecord);

        return new SpeedRecord.Builder(
                        new Metadata.Builder().build(),
                        Instant.now(),
                        Instant.now().plusMillis(1000),
                        speedRecords)
                .build();
    }

    private static SpeedRecord getCompleteSpeedRecord() {

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
        testMetadataBuilder.setClientRecordId("SPR" + Math.random());

        SpeedRecord.SpeedRecordSample speedRecord =
                new SpeedRecord.SpeedRecordSample(
                        Velocity.fromMetersPerSecond(10.0), Instant.now().plusMillis(100));

        ArrayList<SpeedRecord.SpeedRecordSample> speedRecords = new ArrayList<>();
        speedRecords.add(speedRecord);
        speedRecords.add(speedRecord);

        return new SpeedRecord.Builder(
                        testMetadataBuilder.build(),
                        Instant.now(),
                        Instant.now().plusMillis(1000),
                        speedRecords)
                .build();
    }
}
