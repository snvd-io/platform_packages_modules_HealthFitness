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

package android.healthconnect.cts.changelogs;

import static android.healthconnect.cts.utils.DataFactory.getDataOrigin;
import static android.healthconnect.cts.utils.DataFactory.getDistanceRecord;
import static android.healthconnect.cts.utils.DataFactory.getHeartRateRecord;
import static android.healthconnect.cts.utils.DataFactory.getMetadataForId;
import static android.healthconnect.cts.utils.DataFactory.getStepsRecord;
import static android.healthconnect.cts.utils.DataFactory.getTestRecords;
import static android.healthconnect.cts.utils.TestUtils.deleteRecordsByIdFilter;
import static android.healthconnect.cts.utils.TestUtils.getChangeLogToken;
import static android.healthconnect.cts.utils.TestUtils.getChangeLogs;
import static android.healthconnect.cts.utils.TestUtils.insertRecordAndGetId;
import static android.healthconnect.cts.utils.TestUtils.insertRecords;
import static android.healthconnect.cts.utils.TestUtils.updateRecords;

import static com.google.common.truth.Truth.assertThat;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;

import android.content.Context;
import android.health.connect.DeleteUsingFiltersRequest;
import android.health.connect.RecordIdFilter;
import android.health.connect.changelog.ChangeLogTokenRequest;
import android.health.connect.changelog.ChangeLogTokenResponse;
import android.health.connect.changelog.ChangeLogsRequest;
import android.health.connect.changelog.ChangeLogsResponse;
import android.health.connect.datatypes.DataOrigin;
import android.health.connect.datatypes.DistanceRecord;
import android.health.connect.datatypes.HeartRateRecord;
import android.health.connect.datatypes.Metadata;
import android.health.connect.datatypes.Record;
import android.health.connect.datatypes.StepsRecord;
import android.healthconnect.cts.utils.AssumptionCheckerRule;
import android.healthconnect.cts.utils.TestUtils;
import android.platform.test.annotations.AppModeFull;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** CTS test for API provided by HealthConnectManager. */
@AppModeFull(reason = "HealthConnectManager is not accessible to instant apps")
@RunWith(AndroidJUnit4.class)
public class HealthConnectChangeLogsTests {
    private final Context mContext = ApplicationProvider.getApplicationContext();
    private final String mPackageName = mContext.getPackageName();

    @Rule
    public AssumptionCheckerRule mSupportedHardwareRule =
            new AssumptionCheckerRule(
                    TestUtils::isHardwareSupported, "Tests should run on supported hardware only.");

    @After
    public void tearDown() throws InterruptedException {
        TestUtils.verifyDeleteRecords(
                new DeleteUsingFiltersRequest.Builder()
                        .addDataOrigin(
                                new DataOrigin.Builder().setPackageName(mPackageName).build())
                        .build());
        TestUtils.deleteAllStagedRemoteData();
    }

    @Test
    public void testGetChangeLogToken() throws InterruptedException {
        ChangeLogTokenRequest changeLogTokenRequest = new ChangeLogTokenRequest.Builder().build();
        assertThat(TestUtils.getChangeLogToken(changeLogTokenRequest)).isNotNull();
        assertThat(changeLogTokenRequest.getRecordTypes()).isNotNull();
        assertThat(changeLogTokenRequest.getDataOriginFilters()).isNotNull();
    }

    @Test
    public void testChangeLogs_insert_default() throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        assertThat(changeLogsRequest.getToken()).isNotNull();
        assertThat(changeLogsRequest.getPageSize()).isNotNull();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);

        List<Record> testRecord = getTestRecords();
        TestUtils.insertRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(testRecord.size());
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);
    }

    @Test
    public void testChangeLogs_insert_dataOrigin_filter_incorrect() throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(
                        new ChangeLogTokenRequest.Builder()
                                .addDataOriginFilter(
                                        new DataOrigin.Builder().setPackageName("random").build())
                                .build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        assertThat(changeLogsRequest.getPageSize()).isNotNull();
        assertThat(changeLogsRequest.getToken()).isNotNull();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);

        List<Record> testRecord = getTestRecords();
        TestUtils.insertRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);
    }

    @Test
    public void testChangeLogs_insert_dataOrigin_filter_correct() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(
                        new ChangeLogTokenRequest.Builder()
                                .addDataOriginFilter(
                                        new DataOrigin.Builder()
                                                .setPackageName(context.getPackageName())
                                                .build())
                                .build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        assertThat(changeLogsRequest.getPageSize()).isNotNull();
        assertThat(changeLogsRequest.getToken()).isNotNull();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);

        List<Record> testRecord = getTestRecords();
        TestUtils.insertRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(testRecord.size());
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);
    }

    @Test
    public void testChangeLogs_insert_record_filter() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(
                        new ChangeLogTokenRequest.Builder()
                                .addDataOriginFilter(
                                        new DataOrigin.Builder()
                                                .setPackageName(context.getPackageName())
                                                .build())
                                .addRecordType(StepsRecord.class)
                                .build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);

        List<Record> testRecord = Collections.singletonList(getStepsRecord());
        TestUtils.insertRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(1);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);
        testRecord = Collections.singletonList(getHeartRateRecord());
        TestUtils.insertRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(1);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);
    }

    @Test
    public void testChangeLogs_insertAndDeleteDataById_returnsDeleteChangeLogOnly()
            throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords()).isEmpty();

        List<Record> testRecords = TestUtils.insertRecords(getTestRecords());
        TestUtils.deleteRecords(testRecords);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords()).isEmpty();
        assertThat(response.getDeletedLogs()).hasSize(testRecords.size());
        List<ChangeLogsResponse.DeletedLog> deletedLogs = response.getDeletedLogs();

        List<String> deletedLogIds =
                deletedLogs.stream()
                        .map(ChangeLogsResponse.DeletedLog::getDeletedRecordId)
                        .toList();
        List<String> testRecordIds =
                testRecords.stream().map(record -> record.getMetadata().getId()).toList();

        assertThat(deletedLogIds).containsExactlyElementsIn(testRecordIds);
    }

    @Test
    public void testChangeLogs_insertAndDeleteByClientId_returnsDeleteChangeLogOnly()
            throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();

        String insertedRecordId =
                TestUtils.insertRecordAndGetId(getStepsRecord(/* steps = */ 10, "stepsId"));
        TestUtils.deleteRecordsByIdFilter(
                Collections.singletonList(
                        RecordIdFilter.fromClientRecordId(StepsRecord.class, "stepsId")));
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);

        assertThat(response.getUpsertedRecords()).isEmpty();
        assertThat(response.getDeletedLogs()).hasSize(1);
        String deletedRecordId = response.getDeletedLogs().get(0).getDeletedRecordId();
        assertThat(deletedRecordId).isEqualTo(insertedRecordId);
    }

    @Test
    public void testChangeLogs_insertAndDelete_beforePermission() throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);

        List<Record> testRecord =
                Arrays.asList(
                        getStepsRecord_minusDays(45),
                        getStepsRecord_minusDays(20),
                        getStepsRecord_minusDays(5));
        TestUtils.insertRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(2);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);
        TestUtils.deleteRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(3);
    }

    @Test
    public void testChangeLogs_insertAndDelete_dataOrigin_filter_incorrect()
            throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(
                        new ChangeLogTokenRequest.Builder()
                                .addDataOriginFilter(
                                        new DataOrigin.Builder().setPackageName("random").build())
                                .build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        assertThat(changeLogsRequest.getPageSize()).isNotNull();
        assertThat(changeLogsRequest.getToken()).isNotNull();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);

        List<Record> testRecord = getTestRecords();
        TestUtils.insertRecords(testRecord);
        TestUtils.deleteRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);
    }

    @Test
    public void testChangeLogs_insertAndDelete_dataOrigin_filter_correct()
            throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(
                        new ChangeLogTokenRequest.Builder()
                                .addDataOriginFilter(
                                        new DataOrigin.Builder()
                                                .setPackageName(context.getPackageName())
                                                .build())
                                .build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        assertThat(changeLogsRequest.getPageSize()).isNotNull();
        assertThat(changeLogsRequest.getToken()).isNotNull();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);

        List<Record> testRecord = getTestRecords();
        TestUtils.insertRecords(testRecord);
        TestUtils.deleteRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(testRecord.size());
    }

    @Test
    public void testChangeLogs_insertAndDelete_record_filter() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(
                        new ChangeLogTokenRequest.Builder()
                                .addDataOriginFilter(
                                        new DataOrigin.Builder()
                                                .setPackageName(context.getPackageName())
                                                .build())
                                .addRecordType(StepsRecord.class)
                                .build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        assertThat(changeLogsRequest.getPageSize()).isNotNull();
        assertThat(changeLogsRequest.getToken()).isNotNull();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);

        List<Record> testRecord = Collections.singletonList(getStepsRecord());
        TestUtils.insertRecords(testRecord);
        TestUtils.deleteRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(1);
        testRecord = Collections.singletonList(getHeartRateRecord());
        TestUtils.insertRecords(testRecord);
        TestUtils.deleteRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.getDeletedLogs().size()).isEqualTo(1);
    }

    @Test
    public void testChangeLogs_insertAndUpdateById_returnsUpdateChangeLogOnly()
            throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();

        Metadata insertedRecordMetadata =
                TestUtils.insertRecords(
                                Collections.singletonList(
                                        getStepsRecord(
                                                /* steps = */ 10, new Metadata.Builder().build())))
                        .get(0)
                        .getMetadata();
        assertThat(insertedRecordMetadata.getClientRecordId()).isNull();
        TestUtils.updateRecords(
                Collections.singletonList(
                        getStepsRecord(/* steps = */ 123, insertedRecordMetadata)));
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);

        assertThat(response.getUpsertedRecords().size()).isEqualTo(1);
        StepsRecord upsertedStepsRecord = (StepsRecord) response.getUpsertedRecords().get(0);
        assertThat(upsertedStepsRecord.getMetadata().getId())
                .isEqualTo(insertedRecordMetadata.getId());
        assertThat(upsertedStepsRecord.getCount()).isEqualTo(123);
        assertThat(response.getDeletedLogs().size()).isEqualTo(0);
    }

    @Test
    public void testChangeLogs_insertAndUpdateByClientId_returnsUpdateChangeLogOnly()
            throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();

        TestUtils.insertRecords(
                Collections.singletonList(getStepsRecord(/* steps = */ 10, "stepsId")));
        TestUtils.updateRecords(
                Collections.singletonList(getStepsRecord(/* steps = */ 123, "stepsId")));
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);

        assertThat(response.getUpsertedRecords()).hasSize(1);
        StepsRecord upsertedStepsRecord = (StepsRecord) response.getUpsertedRecords().get(0);
        assertThat(upsertedStepsRecord.getMetadata().getClientRecordId()).isEqualTo("stepsId");
        assertThat(upsertedStepsRecord.getCount()).isEqualTo(123);
        assertThat(response.getDeletedLogs()).isEmpty();
    }

    @Test
    public void testChangeLogs_insertUpdateAndDeleteById_returnsDeleteChangeLogOnly()
            throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();

        Metadata insertedRecordMetadata =
                TestUtils.insertRecords(
                                Collections.singletonList(
                                        getStepsRecord(
                                                /* steps = */ 10, new Metadata.Builder().build())))
                        .get(0)
                        .getMetadata();
        assertThat(insertedRecordMetadata.getClientRecordId()).isNull();
        TestUtils.updateRecords(
                Collections.singletonList(
                        getStepsRecord(/* steps = */ 123, insertedRecordMetadata)));
        TestUtils.deleteRecords(
                Collections.singletonList(
                        getStepsRecord(/* steps = */ 123, insertedRecordMetadata)));
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);

        assertThat(response.getUpsertedRecords()).isEmpty();
        assertThat(response.getDeletedLogs()).hasSize(1);
        String deletedRecordId = response.getDeletedLogs().get(0).getDeletedRecordId();
        assertThat(deletedRecordId).isEqualTo(insertedRecordMetadata.getId());
    }

    @Test
    public void testChangeLogs_insertUpdateAndDeleteByClientId_returnsDeleteChangeLogOnly()
            throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();

        String insertedRecordId =
                TestUtils.insertRecordAndGetId(getStepsRecord(/* steps = */ 10, "stepsId"));
        TestUtils.updateRecords(
                Collections.singletonList(getStepsRecord(/* steps = */ 123, "stepsId")));
        TestUtils.deleteRecordsByIdFilter(
                Collections.singletonList(
                        RecordIdFilter.fromClientRecordId(StepsRecord.class, "stepsId")));
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);

        assertThat(response.getUpsertedRecords()).isEmpty();
        assertThat(response.getDeletedLogs()).hasSize(1);
        String deletedRecordId = response.getDeletedLogs().get(0).getDeletedRecordId();
        assertThat(deletedRecordId).isEqualTo(insertedRecordId);
    }

    @Test
    public void testChangeLogs_insert_default_withPageSize() throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).setPageSize(1).build();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);

        List<Record> testRecord = getTestRecords();
        TestUtils.insertRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(1);
    }

    @Test
    public void testChangeLogs_insert_default_withNextPageToken() throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).setPageSize(1).build();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.hasMorePages()).isFalse();

        List<Record> testRecord = getTestRecords();
        TestUtils.insertRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.hasMorePages()).isTrue();
        assertThat(response.getUpsertedRecords().size()).isEqualTo(1);
        ChangeLogsRequest nextChangeLogsRequest =
                new ChangeLogsRequest.Builder(response.getNextChangesToken())
                        .setPageSize(1)
                        .build();
        ChangeLogsResponse nextResponse = TestUtils.getChangeLogs(nextChangeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(1);
        assertThat(nextResponse.hasMorePages()).isTrue();
        assertThat(nextResponse.getNextChangesToken())
                .isGreaterThan(response.getNextChangesToken());
        nextChangeLogsRequest =
                new ChangeLogsRequest.Builder(nextResponse.getNextChangesToken()).build();
        nextResponse = TestUtils.getChangeLogs(nextChangeLogsRequest);
        assertThat(nextResponse.hasMorePages()).isFalse();
    }

    @Test
    public void testChangeLogs_insert_default_withSamePageToken() throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.hasMorePages()).isFalse();
        List<Record> testRecord = getTestRecords();
        TestUtils.insertRecords(testRecord);
        ChangeLogsResponse newResponse = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(newResponse.getUpsertedRecords().size()).isEqualTo(testRecord.size());
    }

    // Test added for b/271607816 to make sure that getChangeLogs() method returns the requested
    // changelog token as nextPageToken in the response when it is the end of page.
    // ( i.e. hasMoreRecords is false)
    @Test
    public void testChangeLogs_checkToken_hasMorePages_False() throws InterruptedException {
        ChangeLogTokenResponse tokenResponse =
                TestUtils.getChangeLogToken(new ChangeLogTokenRequest.Builder().build());
        ChangeLogsRequest changeLogsRequest =
                new ChangeLogsRequest.Builder(tokenResponse.getToken()).build();
        ChangeLogsResponse response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(response.hasMorePages()).isFalse();
        List<Record> testRecord = getTestRecords();
        TestUtils.insertRecords(testRecord);
        response = TestUtils.getChangeLogs(changeLogsRequest);
        assertThat(response.getUpsertedRecords().size()).isEqualTo(testRecord.size());
        assertThat(response.hasMorePages()).isFalse();
        ChangeLogsRequest changeLogsRequestNew =
                new ChangeLogsRequest.Builder(response.getNextChangesToken())
                        .setPageSize(2)
                        .build();
        ChangeLogsResponse newResponse = TestUtils.getChangeLogs(changeLogsRequestNew);
        assertThat(newResponse.getUpsertedRecords().size()).isEqualTo(0);
        assertThat(newResponse.hasMorePages()).isFalse();
        assertThat(newResponse.getNextChangesToken()).isEqualTo(changeLogsRequestNew.getToken());
    }

    @Test
    public void testChangeLogs_operationCombinations_expectCorrectChangeLogs() throws Exception {
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(MILLIS);

        // insert some unrelated records
        insertRecords(getTestRecords());
        StepsRecord preExistingStepsRecord =
                getStepsRecord(
                        /* steps= */ 3456,
                        yesterday.plus(4, HOURS),
                        yesterday.plus(5, HOURS),
                        /* clientId= */ "preExistingStepsRecord");
        String preExistingStepsId = insertRecordAndGetId(preExistingStepsRecord);
        StepsRecord updatedPreExistingStepsRecord =
                new StepsRecord.Builder(
                                getMetadataForId(preExistingStepsId, getDataOrigin(mPackageName)),
                                preExistingStepsRecord.getStartTime(),
                                preExistingStepsRecord.getEndTime(),
                                6543)
                        .build();
        String preExistingRecordId = insertRecordAndGetId(getDistanceRecord());
        // then get changes token. The above records shouldn't be included in the change logs.
        String changeToken =
                getChangeLogToken(new ChangeLogTokenRequest.Builder().build()).getToken();

        ImmutableList.Builder<Record> recordBuilder = ImmutableList.builder();
        for (int i = 0; i < 50; i++) {
            recordBuilder.add(
                    getStepsRecord(
                            i + 100,
                            yesterday.plus(i, MINUTES),
                            yesterday.plus(i + 1, MINUTES),
                            "stepsId_" + i));
        }
        for (int i = 0; i < 50; i++) {
            int heartRate = (int) (Math.random() * 30) + 80;
            recordBuilder.add(
                    getHeartRateRecord(heartRate, yesterday.plus(i, MINUTES), "heartRateId_" + i));
        }
        List<Record> records = insertRecords(recordBuilder.build());

        StepsRecord stepsRecordToUpdate = (StepsRecord) records.get(2);
        StepsRecord updatedStepsRecord =
                new StepsRecord.Builder(
                                getMetadataForId(
                                        stepsRecordToUpdate.getMetadata().getId(),
                                        getDataOrigin(mPackageName)), // workaround for b/325029194
                                stepsRecordToUpdate.getStartTime(),
                                stepsRecordToUpdate.getEndTime(),
                                /* count= */ 9876)
                        .build();
        HeartRateRecord heartRateRecordToUpdate = (HeartRateRecord) records.get(68);
        HeartRateRecord updatedHeartRateRecord =
                new HeartRateRecord.Builder(
                                getMetadataForId(
                                        heartRateRecordToUpdate.getMetadata().getId(),
                                        getDataOrigin(mPackageName)),
                                heartRateRecordToUpdate.getStartTime(),
                                heartRateRecordToUpdate.getEndTime(),
                                List.of(
                                        new HeartRateRecord.HeartRateSample(
                                                150, heartRateRecordToUpdate.getStartTime())))
                        .build();
        // Update steps record, only the updated value should appear in upsert change log
        updateRecords(
                List.of(updatedPreExistingStepsRecord, updatedStepsRecord, updatedHeartRateRecord));

        deleteRecordsByIdFilter(
                List.of(
                        // Delete updated HR record, it should not appear in upsert change log
                        RecordIdFilter.fromId(
                                HeartRateRecord.class,
                                updatedHeartRateRecord.getMetadata().getId()),
                        RecordIdFilter.fromClientRecordId(
                                StepsRecord.class,
                                records.get(6).getMetadata().getClientRecordId()),
                        RecordIdFilter.fromId(DistanceRecord.class, preExistingRecordId)));
        List<String> expectedDeletedIds =
                List.of(
                        updatedHeartRateRecord.getMetadata().getId(),
                        records.get(6).getMetadata().getId(),
                        preExistingRecordId);

        ChangeLogsResponse response =
                getChangeLogs(new ChangeLogsRequest.Builder(changeToken).build());
        List<String> updatedIdsFromLog =
                response.getUpsertedRecords().stream()
                        .map(log -> log.getMetadata().getId())
                        .toList();
        List<String> deletedIdsFromLog =
                response.getDeletedLogs().stream()
                        .map(ChangeLogsResponse.DeletedLog::getDeletedRecordId)
                        .collect(Collectors.toList());

        // inserted 50*2 - deleted 2 + updated pre-existing 1
        assertThat(response.getUpsertedRecords()).hasSize(99);
        assertThat(updatedIdsFromLog).doesNotContain(updatedHeartRateRecord.getMetadata().getId());
        assertThat(response.getUpsertedRecords()).contains(updatedPreExistingStepsRecord);
        assertThat(response.getUpsertedRecords()).doesNotContain(stepsRecordToUpdate);
        assertThat(response.getUpsertedRecords()).contains(updatedStepsRecord);
        assertThat(deletedIdsFromLog).containsExactlyElementsIn(expectedDeletedIds);
    }

    private static StepsRecord getStepsRecord_minusDays(int days) {
        return new StepsRecord.Builder(
                        new Metadata.Builder().build(),
                        Instant.now().minus(days, ChronoUnit.DAYS),
                        Instant.now().minus(days, ChronoUnit.DAYS).plusMillis(1000),
                        10)
                .build();
    }
}
