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

package com.android.server.healthconnect.storage.datatypehelpers;

import static android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_IMMUNIZATION;
import static android.health.connect.datatypes.MedicalResource.MEDICAL_RESOURCE_TYPE_UNKNOWN;
import static android.healthconnect.cts.utils.PhrDataFactory.DATA_SOURCE_ID;
import static android.healthconnect.cts.utils.PhrDataFactory.DIFFERENT_DATA_SOURCE_ID;
import static android.healthconnect.cts.utils.PhrDataFactory.FHIR_DATA_ALLERGY;
import static android.healthconnect.cts.utils.PhrDataFactory.FHIR_DATA_IMMUNIZATION;
import static android.healthconnect.cts.utils.PhrDataFactory.FHIR_RESOURCE_TYPE_ALLERGY;
import static android.healthconnect.cts.utils.PhrDataFactory.FHIR_RESOURCE_TYPE_IMMUNIZATION;
import static android.healthconnect.cts.utils.PhrDataFactory.addCompletedStatus;
import static android.healthconnect.cts.utils.PhrDataFactory.getFhirResourceId;
import static android.healthconnect.cts.utils.PhrDataFactory.getFhirResourceType;

import static com.android.server.healthconnect.storage.datatypehelpers.MedicalResourceHelper.DATA_SOURCE_ID_COLUMN_NAME;
import static com.android.server.healthconnect.storage.datatypehelpers.MedicalResourceHelper.FHIR_DATA_COLUMN_NAME;
import static com.android.server.healthconnect.storage.datatypehelpers.MedicalResourceHelper.FHIR_RESOURCE_ID_COLUMN_NAME;
import static com.android.server.healthconnect.storage.datatypehelpers.MedicalResourceHelper.FHIR_RESOURCE_TYPE_COLUMN_NAME;
import static com.android.server.healthconnect.storage.datatypehelpers.MedicalResourceHelper.FHIR_VERSION_COLUMN_NAME;
import static com.android.server.healthconnect.storage.datatypehelpers.MedicalResourceHelper.MEDICAL_RESOURCE_TABLE_NAME;
import static com.android.server.healthconnect.storage.datatypehelpers.MedicalResourceHelper.getCreateTableRequest;
import static com.android.server.healthconnect.storage.datatypehelpers.MedicalResourceIndicesHelper.MEDICAL_RESOURCE_ID;
import static com.android.server.healthconnect.storage.datatypehelpers.MedicalResourceIndicesHelper.MEDICAL_RESOURCE_INDICES_TABLE_NAME;
import static com.android.server.healthconnect.storage.datatypehelpers.MedicalResourceIndicesHelper.MEDICAL_RESOURCE_TYPE;
import static com.android.server.healthconnect.storage.datatypehelpers.RecordHelper.LAST_MODIFIED_TIME_COLUMN_NAME;
import static com.android.server.healthconnect.storage.datatypehelpers.RecordHelper.PRIMARY_COLUMN_NAME;
import static com.android.server.healthconnect.storage.datatypehelpers.RecordHelper.UUID_COLUMN_NAME;
import static com.android.server.healthconnect.storage.utils.StorageUtils.BLOB_UNIQUE_NON_NULL;
import static com.android.server.healthconnect.storage.utils.StorageUtils.INTEGER;
import static com.android.server.healthconnect.storage.utils.StorageUtils.INTEGER_NOT_NULL;
import static com.android.server.healthconnect.storage.utils.StorageUtils.PRIMARY_AUTOINCREMENT;
import static com.android.server.healthconnect.storage.utils.StorageUtils.TEXT_NOT_NULL;
import static com.android.server.healthconnect.storage.utils.StorageUtils.TEXT_NULL;
import static com.android.server.healthconnect.storage.utils.StorageUtils.generateMedicalResourceUUID;
import static com.android.server.healthconnect.storage.utils.StorageUtils.getHexString;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.Assert.assertThrows;

import android.content.ContentValues;
import android.health.connect.MedicalResourceId;
import android.health.connect.datatypes.MedicalResource;
import android.health.connect.internal.datatypes.MedicalResourceInternal;
import android.platform.test.annotations.EnableFlags;
import android.platform.test.flag.junit.SetFlagsRule;
import android.util.Pair;

import com.android.healthfitness.flags.Flags;
import com.android.server.healthconnect.storage.request.CreateTableRequest;
import com.android.server.healthconnect.storage.request.DeleteTableRequest;
import com.android.server.healthconnect.storage.request.ReadTableRequest;
import com.android.server.healthconnect.storage.request.UpsertTableRequest;
import com.android.server.healthconnect.storage.utils.StorageUtils;

import com.google.common.collect.ImmutableList;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MedicalResourceHelperTest {
    private final HealthConnectDatabaseTestRule mHealthConnectDatabaseTestRule =
            new HealthConnectDatabaseTestRule();

    @Rule
    public TestRule chain =
            RuleChain.outerRule(new SetFlagsRule()).around(mHealthConnectDatabaseTestRule);

    private MedicalResourceHelper mMedicalResourceHelper;

    @Before
    public void setup() {
        mMedicalResourceHelper =
                new MedicalResourceHelper(mHealthConnectDatabaseTestRule.getTransactionManager());
    }

    @Test
    public void getCreateTableRequest_correctResult() {
        List<Pair<String, String>> columnInfoMedicalResource =
                List.of(
                        Pair.create(PRIMARY_COLUMN_NAME, PRIMARY_AUTOINCREMENT),
                        Pair.create(FHIR_RESOURCE_TYPE_COLUMN_NAME, INTEGER_NOT_NULL),
                        Pair.create(FHIR_RESOURCE_ID_COLUMN_NAME, TEXT_NOT_NULL),
                        Pair.create(FHIR_DATA_COLUMN_NAME, TEXT_NOT_NULL),
                        Pair.create(FHIR_VERSION_COLUMN_NAME, TEXT_NULL),
                        Pair.create(DATA_SOURCE_ID_COLUMN_NAME, INTEGER),
                        Pair.create(UUID_COLUMN_NAME, BLOB_UNIQUE_NON_NULL),
                        Pair.create(LAST_MODIFIED_TIME_COLUMN_NAME, INTEGER));
        List<Pair<String, String>> columnInfoMedicalResourceIndices =
                List.of(
                        Pair.create(MEDICAL_RESOURCE_ID, INTEGER_NOT_NULL),
                        Pair.create(MEDICAL_RESOURCE_TYPE, INTEGER_NOT_NULL));
        CreateTableRequest childTableRequest =
                new CreateTableRequest(
                                MEDICAL_RESOURCE_INDICES_TABLE_NAME,
                                columnInfoMedicalResourceIndices)
                        .addForeignKey(
                                MEDICAL_RESOURCE_TABLE_NAME,
                                Collections.singletonList(MEDICAL_RESOURCE_ID),
                                Collections.singletonList(PRIMARY_COLUMN_NAME));
        CreateTableRequest expected =
                new CreateTableRequest(MEDICAL_RESOURCE_TABLE_NAME, columnInfoMedicalResource)
                        .setChildTableRequests(List.of(childTableRequest));

        CreateTableRequest result = getCreateTableRequest();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void getUpsertTableRequest_correctResult() throws JSONException {
        String fhirResourceId = getFhirResourceId(FHIR_DATA_IMMUNIZATION);
        String fhirResourceType = getFhirResourceType(FHIR_DATA_IMMUNIZATION);
        MedicalResourceInternal medicalResourceInternal =
                new MedicalResourceInternal()
                        .setFhirResourceId(fhirResourceId)
                        .setFhirResourceType(fhirResourceType)
                        .setData(FHIR_DATA_IMMUNIZATION)
                        .setDataSourceId(DATA_SOURCE_ID);
        UUID uuid = generateMedicalResourceUUID(fhirResourceId, fhirResourceType, DATA_SOURCE_ID);

        UpsertTableRequest upsertRequest =
                MedicalResourceHelper.getUpsertTableRequest(uuid, medicalResourceInternal);
        ContentValues contentValues = upsertRequest.getContentValues();

        assertThat(upsertRequest.getTable()).isEqualTo(MEDICAL_RESOURCE_TABLE_NAME);
        assertThat(upsertRequest.getUniqueColumnsCount()).isEqualTo(1);
        assertThat(contentValues.size()).isEqualTo(5);
        assertThat(contentValues.get(FHIR_RESOURCE_TYPE_COLUMN_NAME))
                .isEqualTo(MedicalResourceHelper.getFhirResourceTypeInt(fhirResourceType));
        assertThat(contentValues.get(DATA_SOURCE_ID_COLUMN_NAME))
                .isEqualTo(Long.parseLong(DATA_SOURCE_ID));
        assertThat(contentValues.get(FHIR_DATA_COLUMN_NAME)).isEqualTo(FHIR_DATA_IMMUNIZATION);
        assertThat(contentValues.get(UUID_COLUMN_NAME))
                .isEqualTo(StorageUtils.convertUUIDToBytes(uuid));
    }

    @Test
    public void getReadTableRequest_usingMedicalResourceId_correctQuery() throws JSONException {
        MedicalResourceId medicalResourceId1 =
                new MedicalResourceId(
                        DATA_SOURCE_ID, FHIR_RESOURCE_TYPE_IMMUNIZATION, "resourceId1");
        MedicalResourceId medicalResourceId2 =
                new MedicalResourceId(
                        DIFFERENT_DATA_SOURCE_ID, FHIR_RESOURCE_TYPE_ALLERGY, "resourceId2");
        List<MedicalResourceId> medicalResourceIds =
                List.of(medicalResourceId1, medicalResourceId2);
        String hex1 = makeMedicalResourceHexString(medicalResourceId1);
        String hex2 = makeMedicalResourceHexString(medicalResourceId2);
        List<String> hexValues = List.of(hex1, hex2);

        ReadTableRequest readRequest =
                MedicalResourceHelper.getReadTableRequest(medicalResourceIds);

        assertThat(readRequest.getTableName()).isEqualTo(MEDICAL_RESOURCE_TABLE_NAME);
        assertThat(readRequest.getReadCommand())
                .isEqualTo(
                        "SELECT * FROM medical_resource_table WHERE uuid IN ("
                                + String.join(", ", hexValues)
                                + ")");
    }

    @Test
    @EnableFlags({Flags.FLAG_PERSONAL_HEALTH_RECORD_DATABASE})
    public void readMedicalResourcesByIds_returnsEmpty() throws JSONException {
        List<MedicalResourceId> medicalResourceIds =
                List.of(
                        new MedicalResourceId(
                                DATA_SOURCE_ID,
                                getFhirResourceType(FHIR_DATA_IMMUNIZATION),
                                getFhirResourceId(FHIR_DATA_IMMUNIZATION)));

        List<MedicalResource> resources =
                mMedicalResourceHelper.readMedicalResourcesByIds(medicalResourceIds);

        assertThat(resources).isEmpty();
    }

    @Test
    @EnableFlags({Flags.FLAG_PERSONAL_HEALTH_RECORD_DATABASE, Flags.FLAG_PERSONAL_HEALTH_RECORD})
    public void insertMedicalResources_returnsMedicalResources() throws JSONException {
        String fhirResourceId = getFhirResourceId(FHIR_DATA_IMMUNIZATION);
        String fhirResourceType = getFhirResourceType(FHIR_DATA_IMMUNIZATION);
        List<MedicalResource> expected =
                Collections.singletonList(
                        new MedicalResource.Builder(
                                        MEDICAL_RESOURCE_TYPE_IMMUNIZATION,
                                        DATA_SOURCE_ID,
                                        FHIR_DATA_IMMUNIZATION)
                                .build());
        MedicalResourceInternal medicalResourceInternal =
                new MedicalResourceInternal()
                        .setFhirResourceId(fhirResourceId)
                        .setFhirResourceType(fhirResourceType)
                        .setData(FHIR_DATA_IMMUNIZATION)
                        .setDataSourceId(DATA_SOURCE_ID);

        List<MedicalResource> result =
                mMedicalResourceHelper.upsertMedicalResources(List.of(medicalResourceInternal));

        assertThat(result.size()).isEqualTo(1);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @EnableFlags({Flags.FLAG_PERSONAL_HEALTH_RECORD_DATABASE, Flags.FLAG_PERSONAL_HEALTH_RECORD})
    public void insertSingleMedicalResource_readSingleResource() throws JSONException {
        String fhirResourceId = getFhirResourceId(FHIR_DATA_IMMUNIZATION);
        String fhirResourceType = getFhirResourceType(FHIR_DATA_IMMUNIZATION);
        MedicalResourceInternal medicalResourceInternal =
                new MedicalResourceInternal()
                        .setFhirResourceId(fhirResourceId)
                        .setFhirResourceType(fhirResourceType)
                        .setData(FHIR_DATA_IMMUNIZATION)
                        .setDataSourceId(DATA_SOURCE_ID);
        List<MedicalResourceId> medicalIdFilters =
                List.of(new MedicalResourceId(DATA_SOURCE_ID, fhirResourceType, fhirResourceId));
        List<MedicalResource> expected =
                Collections.singletonList(
                        new MedicalResource.Builder(
                                        MEDICAL_RESOURCE_TYPE_IMMUNIZATION,
                                        DATA_SOURCE_ID,
                                        FHIR_DATA_IMMUNIZATION)
                                .build());

        List<MedicalResource> upsertedMedicalResources =
                mMedicalResourceHelper.upsertMedicalResources(List.of(medicalResourceInternal));
        List<MedicalResource> result =
                mMedicalResourceHelper.readMedicalResourcesByIds(medicalIdFilters);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result).isEqualTo(upsertedMedicalResources);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @EnableFlags({Flags.FLAG_PERSONAL_HEALTH_RECORD_DATABASE, Flags.FLAG_PERSONAL_HEALTH_RECORD})
    public void insertMultipleMedicalResources_readMultipleResources() throws JSONException {
        String fhirResourceId1 = getFhirResourceId(FHIR_DATA_IMMUNIZATION);
        String fhirResourceType1 = getFhirResourceType(FHIR_DATA_IMMUNIZATION);
        MedicalResourceInternal medicalResourceInternal1 =
                new MedicalResourceInternal()
                        .setFhirResourceId(fhirResourceId1)
                        .setFhirResourceType(fhirResourceType1)
                        .setData(FHIR_DATA_IMMUNIZATION)
                        .setDataSourceId(DATA_SOURCE_ID);
        MedicalResource resource1 =
                new MedicalResource.Builder(
                                MEDICAL_RESOURCE_TYPE_IMMUNIZATION,
                                DATA_SOURCE_ID,
                                FHIR_DATA_IMMUNIZATION)
                        .build();
        MedicalResourceId medicalResourceId1 =
                new MedicalResourceId(DATA_SOURCE_ID, fhirResourceType1, fhirResourceId1);
        String fhirResourceId2 = getFhirResourceId(FHIR_DATA_ALLERGY);
        String fhirResourceType2 = getFhirResourceType(FHIR_DATA_ALLERGY);
        MedicalResourceInternal medicalResourceInternal2 =
                new MedicalResourceInternal()
                        .setFhirResourceId(fhirResourceId2)
                        .setFhirResourceType(fhirResourceType2)
                        .setData(FHIR_DATA_ALLERGY)
                        .setDataSourceId(DIFFERENT_DATA_SOURCE_ID);
        MedicalResource resource2 =
                new MedicalResource.Builder(
                                MEDICAL_RESOURCE_TYPE_UNKNOWN,
                                DIFFERENT_DATA_SOURCE_ID,
                                FHIR_DATA_ALLERGY)
                        .build();
        List<MedicalResource> expected = List.of(resource1, resource2);
        MedicalResourceId medicalResourceId2 =
                new MedicalResourceId(DIFFERENT_DATA_SOURCE_ID, fhirResourceType2, fhirResourceId2);
        List<MedicalResourceId> medicalIdFilters = List.of(medicalResourceId1, medicalResourceId2);

        List<MedicalResource> upsertedMedicalResources =
                mMedicalResourceHelper.upsertMedicalResources(
                        List.of(medicalResourceInternal1, medicalResourceInternal2));
        List<MedicalResource> result =
                mMedicalResourceHelper.readMedicalResourcesByIds(medicalIdFilters);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).isEqualTo(upsertedMedicalResources);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @EnableFlags({Flags.FLAG_PERSONAL_HEALTH_RECORD_DATABASE, Flags.FLAG_PERSONAL_HEALTH_RECORD})
    public void updateSingleMedicalResource_success() throws JSONException {
        String fhirResourceType = getFhirResourceType(FHIR_DATA_IMMUNIZATION);
        String fhirResourceId = getFhirResourceId(FHIR_DATA_IMMUNIZATION);
        MedicalResourceInternal medicalResourceInternal =
                new MedicalResourceInternal()
                        .setFhirResourceId(fhirResourceId)
                        .setFhirResourceType(fhirResourceType)
                        .setData(FHIR_DATA_IMMUNIZATION)
                        .setDataSourceId(DATA_SOURCE_ID);
        MedicalResourceInternal medicalResourceInternalUpdated =
                new MedicalResourceInternal()
                        .setFhirResourceId(fhirResourceId)
                        .setFhirResourceType(fhirResourceType)
                        .setData(addCompletedStatus(FHIR_DATA_IMMUNIZATION))
                        .setDataSourceId(DATA_SOURCE_ID);
        List<MedicalResourceId> medicalIdFilters =
                List.of(new MedicalResourceId(DATA_SOURCE_ID, fhirResourceType, fhirResourceId));
        List<MedicalResource> expected =
                Collections.singletonList(
                        new MedicalResource.Builder(
                                        MEDICAL_RESOURCE_TYPE_IMMUNIZATION,
                                        DATA_SOURCE_ID,
                                        addCompletedStatus(FHIR_DATA_IMMUNIZATION))
                                .build());

        mMedicalResourceHelper.upsertMedicalResources(List.of(medicalResourceInternal));
        List<MedicalResource> updatedMedicalResource =
                mMedicalResourceHelper.upsertMedicalResources(
                        List.of(medicalResourceInternalUpdated));
        List<MedicalResource> result =
                mMedicalResourceHelper.readMedicalResourcesByIds(medicalIdFilters);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result).isEqualTo(updatedMedicalResource);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @EnableFlags({Flags.FLAG_PERSONAL_HEALTH_RECORD_DATABASE, Flags.FLAG_PERSONAL_HEALTH_RECORD})
    public void deleteMedicalResourcesByIds_noId_fails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> mMedicalResourceHelper.deleteMedicalResourcesByIds(List.of()));
    }

    @Test
    @EnableFlags({Flags.FLAG_PERSONAL_HEALTH_RECORD_DATABASE, Flags.FLAG_PERSONAL_HEALTH_RECORD})
    public void deleteMedicalResourcesByIds_oneIdNotPresent_succeeds() throws Exception {
        String fhirResourceType = getFhirResourceType(FHIR_DATA_IMMUNIZATION);
        String fhirResourceId = getFhirResourceId(FHIR_DATA_IMMUNIZATION);

        mMedicalResourceHelper.deleteMedicalResourcesByIds(
                List.of(new MedicalResourceId(DATA_SOURCE_ID, fhirResourceType, fhirResourceId)));
    }

    @Test
    @EnableFlags({Flags.FLAG_PERSONAL_HEALTH_RECORD_DATABASE, Flags.FLAG_PERSONAL_HEALTH_RECORD})
    public void deleteMedicalResourcesByIds_oneIdPresent_succeedsDeleting() throws Exception {
        String fhirResourceId = getFhirResourceId(FHIR_DATA_IMMUNIZATION);
        String fhirResourceType = getFhirResourceType(FHIR_DATA_IMMUNIZATION);
        MedicalResourceInternal medicalResourceInternal =
                new MedicalResourceInternal()
                        .setFhirResourceId(fhirResourceId)
                        .setFhirResourceType(fhirResourceType)
                        .setData(FHIR_DATA_IMMUNIZATION)
                        .setDataSourceId(DATA_SOURCE_ID);
        mMedicalResourceHelper.upsertMedicalResources(List.of(medicalResourceInternal));

        MedicalResourceId id =
                new MedicalResourceId(DATA_SOURCE_ID, fhirResourceType, fhirResourceId);
        mMedicalResourceHelper.deleteMedicalResourcesByIds(List.of(id));

        List<MedicalResource> result =
                mMedicalResourceHelper.readMedicalResourcesByIds(List.of(id));
        assertThat(result).isEmpty();
    }

    @Test
    @EnableFlags({Flags.FLAG_PERSONAL_HEALTH_RECORD_DATABASE, Flags.FLAG_PERSONAL_HEALTH_RECORD})
    public void deleteMedicalResourcesByIds_oneOfTwoSpecified_onlySpecifiedDeleted()
            throws Exception {
        String fhirResourceId = getFhirResourceId(FHIR_DATA_IMMUNIZATION);
        String fhirResourceType = getFhirResourceType(FHIR_DATA_IMMUNIZATION);
        MedicalResourceInternal medicalResource1 =
                new MedicalResourceInternal()
                        .setFhirResourceId(fhirResourceId)
                        .setFhirResourceType(fhirResourceType)
                        .setData(FHIR_DATA_IMMUNIZATION)
                        .setDataSourceId(DATA_SOURCE_ID);

        String fhirResourceId2 = getFhirResourceId(FHIR_DATA_ALLERGY);
        String fhirResourceType2 = getFhirResourceType(FHIR_DATA_ALLERGY);
        UUID uuid2 =
                generateMedicalResourceUUID(fhirResourceId2, fhirResourceType2, DATA_SOURCE_ID);
        MedicalResourceInternal medicalResource2 =
                new MedicalResourceInternal()
                        .setFhirResourceId(fhirResourceId2)
                        .setFhirResourceType(fhirResourceType2)
                        .setData(FHIR_DATA_ALLERGY)
                        .setDataSourceId(DATA_SOURCE_ID);

        mMedicalResourceHelper.upsertMedicalResources(List.of(medicalResource1, medicalResource2));

        MedicalResourceId id1 =
                new MedicalResourceId(DATA_SOURCE_ID, fhirResourceType, fhirResourceId);
        MedicalResourceId id2 =
                new MedicalResourceId(DATA_SOURCE_ID, fhirResourceType2, fhirResourceId2);
        mMedicalResourceHelper.deleteMedicalResourcesByIds(List.of(id1));

        List<MedicalResource> result =
                mMedicalResourceHelper.readMedicalResourcesByIds(List.of(id1, id2));
        assertThat(result)
                .containsExactly(
                        new MedicalResource.Builder(
                                        uuid2.toString(),
                                        MEDICAL_RESOURCE_TYPE_UNKNOWN,
                                        DATA_SOURCE_ID,
                                        FHIR_DATA_ALLERGY)
                                .build());
    }

    @Test
    public void getDeleteRequest_noIds_exceptions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MedicalResourceHelper.getDeleteRequest(Collections.emptyList()));
    }

    @Test
    public void getDeleteRequest_oneId_success() throws JSONException {
        MedicalResourceId medicalResourceId =
                new MedicalResourceId(DATA_SOURCE_ID, FHIR_RESOURCE_TYPE_IMMUNIZATION, "anId");
        DeleteTableRequest request =
                MedicalResourceHelper.getDeleteRequest(ImmutableList.of(medicalResourceId));
        String hex = makeMedicalResourceHexString(medicalResourceId);

        assertThat(request.getDeleteCommand())
                .isEqualTo("DELETE FROM medical_resource_table WHERE uuid IN (" + hex + ")");
    }

    @Test
    public void getDeleteRequest_multipleId_success() {
        MedicalResourceId medicalResourceId1 =
                new MedicalResourceId(
                        DATA_SOURCE_ID, FHIR_RESOURCE_TYPE_IMMUNIZATION, "Immunization1");
        MedicalResourceId medicalResourceId2 =
                new MedicalResourceId(
                        DIFFERENT_DATA_SOURCE_ID, FHIR_RESOURCE_TYPE_ALLERGY, "Allergy1");
        MedicalResourceId medicalResourceId3 =
                new MedicalResourceId(
                        DIFFERENT_DATA_SOURCE_ID, FHIR_RESOURCE_TYPE_ALLERGY, "Allergy2");
        String uuidHex1 = makeMedicalResourceHexString(medicalResourceId1);
        String uuidHex2 = makeMedicalResourceHexString(medicalResourceId2);
        String uuidHex3 = makeMedicalResourceHexString(medicalResourceId3);

        DeleteTableRequest request =
                MedicalResourceHelper.getDeleteRequest(
                        ImmutableList.of(
                                medicalResourceId1, medicalResourceId2, medicalResourceId3));

        assertThat(request.getIds()).isEqualTo(ImmutableList.of(uuidHex1, uuidHex2, uuidHex3));
        assertThat(request.getIdColumnName()).isEqualTo("uuid");
        assertThat(request.getDeleteCommand())
                .isEqualTo(
                        "DELETE FROM medical_resource_table WHERE uuid IN ("
                                + uuidHex1
                                + ", "
                                + uuidHex2
                                + ", "
                                + uuidHex3
                                + ")");
    }

    /**
     * Returns a UUID for the given triple {@code resourceId}, {@code resourceType} and {@code
     * dataSourceId}.
     */
    private static String makeMedicalResourceHexString(MedicalResourceId medicalResourceId) {
        return getHexString(
                generateMedicalResourceUUID(
                        medicalResourceId.getFhirResourceId(),
                        medicalResourceId.getFhirResourceType(),
                        medicalResourceId.getDataSourceId()));
    }
}
