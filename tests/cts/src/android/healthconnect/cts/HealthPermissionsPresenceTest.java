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

import static android.healthconnect.HealthPermissions.HEALTH_PERMISSION_GROUP;
import static android.healthconnect.HealthPermissions.READ_ACTIVE_CALORIES_BURNED;
import static android.healthconnect.HealthPermissions.READ_BASAL_BODY_TEMPERATURE;
import static android.healthconnect.HealthPermissions.READ_BASAL_METABOLIC_RATE;
import static android.healthconnect.HealthPermissions.READ_BLOOD_GLUCOSE;
import static android.healthconnect.HealthPermissions.READ_BLOOD_PRESSURE;
import static android.healthconnect.HealthPermissions.READ_BODY_FAT;
import static android.healthconnect.HealthPermissions.READ_BODY_TEMPERATURE;
import static android.healthconnect.HealthPermissions.READ_BODY_WATER_MASS;
import static android.healthconnect.HealthPermissions.READ_BONE_MASS;
import static android.healthconnect.HealthPermissions.READ_CERVICAL_MUCUS;
import static android.healthconnect.HealthPermissions.READ_DISTANCE;
import static android.healthconnect.HealthPermissions.READ_ELEVATION_GAINED;
import static android.healthconnect.HealthPermissions.READ_EXERCISE;
import static android.healthconnect.HealthPermissions.READ_FLOORS_CLIMBED;
import static android.healthconnect.HealthPermissions.READ_HEART_RATE;
import static android.healthconnect.HealthPermissions.READ_HEART_RATE_VARIABILITY;
import static android.healthconnect.HealthPermissions.READ_HEIGHT;
import static android.healthconnect.HealthPermissions.READ_HIP_CIRCUMFERENCE;
import static android.healthconnect.HealthPermissions.READ_HYDRATION;
import static android.healthconnect.HealthPermissions.READ_INTERMENSTRUAL_BLEEDING;
import static android.healthconnect.HealthPermissions.READ_LEAN_BODY_MASS;
import static android.healthconnect.HealthPermissions.READ_MENSTRUATION;
import static android.healthconnect.HealthPermissions.READ_NUTRITION;
import static android.healthconnect.HealthPermissions.READ_OVULATION_TEST;
import static android.healthconnect.HealthPermissions.READ_OXYGEN_SATURATION;
import static android.healthconnect.HealthPermissions.READ_POWER;
import static android.healthconnect.HealthPermissions.READ_RESPIRATORY_RATE;
import static android.healthconnect.HealthPermissions.READ_RESTING_HEART_RATE;
import static android.healthconnect.HealthPermissions.READ_SEXUAL_ACTIVITY;
import static android.healthconnect.HealthPermissions.READ_SLEEP;
import static android.healthconnect.HealthPermissions.READ_SPEED;
import static android.healthconnect.HealthPermissions.READ_STEPS;
import static android.healthconnect.HealthPermissions.READ_TOTAL_CALORIES_BURNED;
import static android.healthconnect.HealthPermissions.READ_VO2_MAX;
import static android.healthconnect.HealthPermissions.READ_WAIST_CIRCUMFERENCE;
import static android.healthconnect.HealthPermissions.READ_WEIGHT;
import static android.healthconnect.HealthPermissions.READ_WHEELCHAIR_PUSHES;
import static android.healthconnect.HealthPermissions.WRITE_ACTIVE_CALORIES_BURNED;
import static android.healthconnect.HealthPermissions.WRITE_BASAL_BODY_TEMPERATURE;
import static android.healthconnect.HealthPermissions.WRITE_BASAL_METABOLIC_RATE;
import static android.healthconnect.HealthPermissions.WRITE_BLOOD_GLUCOSE;
import static android.healthconnect.HealthPermissions.WRITE_BLOOD_PRESSURE;
import static android.healthconnect.HealthPermissions.WRITE_BODY_FAT;
import static android.healthconnect.HealthPermissions.WRITE_BODY_TEMPERATURE;
import static android.healthconnect.HealthPermissions.WRITE_BODY_WATER_MASS;
import static android.healthconnect.HealthPermissions.WRITE_BONE_MASS;
import static android.healthconnect.HealthPermissions.WRITE_CERVICAL_MUCUS;
import static android.healthconnect.HealthPermissions.WRITE_DISTANCE;
import static android.healthconnect.HealthPermissions.WRITE_ELEVATION_GAINED;
import static android.healthconnect.HealthPermissions.WRITE_EXERCISE;
import static android.healthconnect.HealthPermissions.WRITE_EXERCISE_ROUTE;
import static android.healthconnect.HealthPermissions.WRITE_FLOORS_CLIMBED;
import static android.healthconnect.HealthPermissions.WRITE_HEART_RATE;
import static android.healthconnect.HealthPermissions.WRITE_HEART_RATE_VARIABILITY;
import static android.healthconnect.HealthPermissions.WRITE_HEIGHT;
import static android.healthconnect.HealthPermissions.WRITE_HIP_CIRCUMFERENCE;
import static android.healthconnect.HealthPermissions.WRITE_HYDRATION;
import static android.healthconnect.HealthPermissions.WRITE_INTERMENSTRUAL_BLEEDING;
import static android.healthconnect.HealthPermissions.WRITE_LEAN_BODY_MASS;
import static android.healthconnect.HealthPermissions.WRITE_MENSTRUATION;
import static android.healthconnect.HealthPermissions.WRITE_NUTRITION;
import static android.healthconnect.HealthPermissions.WRITE_OVULATION_TEST;
import static android.healthconnect.HealthPermissions.WRITE_OXYGEN_SATURATION;
import static android.healthconnect.HealthPermissions.WRITE_POWER;
import static android.healthconnect.HealthPermissions.WRITE_RESPIRATORY_RATE;
import static android.healthconnect.HealthPermissions.WRITE_RESTING_HEART_RATE;
import static android.healthconnect.HealthPermissions.WRITE_SEXUAL_ACTIVITY;
import static android.healthconnect.HealthPermissions.WRITE_SLEEP;
import static android.healthconnect.HealthPermissions.WRITE_SPEED;
import static android.healthconnect.HealthPermissions.WRITE_STEPS;
import static android.healthconnect.HealthPermissions.WRITE_TOTAL_CALORIES_BURNED;
import static android.healthconnect.HealthPermissions.WRITE_VO2_MAX;
import static android.healthconnect.HealthPermissions.WRITE_WAIST_CIRCUMFERENCE;
import static android.healthconnect.HealthPermissions.WRITE_WEIGHT;
import static android.healthconnect.HealthPermissions.WRITE_WHEELCHAIR_PUSHES;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.healthconnect.HealthConnectManager;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

/*
 * Configuration test to check that all health permissions are defined.
 */
@RunWith(AndroidJUnit4.class)
public class HealthPermissionsPresenceTest {
    private static final Set<String> HEALTH_PERMISSIONS =
            Set.of(
                    READ_ACTIVE_CALORIES_BURNED,
                    READ_DISTANCE,
                    READ_ELEVATION_GAINED,
                    READ_EXERCISE,
                    READ_FLOORS_CLIMBED,
                    READ_STEPS,
                    READ_TOTAL_CALORIES_BURNED,
                    READ_VO2_MAX,
                    READ_WHEELCHAIR_PUSHES,
                    READ_POWER,
                    READ_SPEED,
                    READ_BASAL_METABOLIC_RATE,
                    READ_BODY_FAT,
                    READ_BODY_WATER_MASS,
                    READ_BONE_MASS,
                    READ_HEIGHT,
                    READ_HIP_CIRCUMFERENCE,
                    READ_LEAN_BODY_MASS,
                    READ_WAIST_CIRCUMFERENCE,
                    READ_WEIGHT,
                    READ_CERVICAL_MUCUS,
                    READ_MENSTRUATION,
                    READ_OVULATION_TEST,
                    READ_SEXUAL_ACTIVITY,
                    READ_HYDRATION,
                    READ_NUTRITION,
                    READ_SLEEP,
                    READ_BASAL_BODY_TEMPERATURE,
                    READ_BLOOD_GLUCOSE,
                    READ_BLOOD_PRESSURE,
                    READ_BODY_TEMPERATURE,
                    READ_HEART_RATE,
                    READ_HEART_RATE_VARIABILITY,
                    READ_OXYGEN_SATURATION,
                    READ_RESPIRATORY_RATE,
                    READ_RESTING_HEART_RATE,
                    READ_INTERMENSTRUAL_BLEEDING,
                    WRITE_ACTIVE_CALORIES_BURNED,
                    WRITE_DISTANCE,
                    WRITE_ELEVATION_GAINED,
                    WRITE_EXERCISE,
                    WRITE_EXERCISE_ROUTE,
                    WRITE_FLOORS_CLIMBED,
                    WRITE_STEPS,
                    WRITE_TOTAL_CALORIES_BURNED,
                    WRITE_VO2_MAX,
                    WRITE_WHEELCHAIR_PUSHES,
                    WRITE_POWER,
                    WRITE_SPEED,
                    WRITE_BASAL_METABOLIC_RATE,
                    WRITE_BODY_FAT,
                    WRITE_BODY_WATER_MASS,
                    WRITE_BONE_MASS,
                    WRITE_HEIGHT,
                    WRITE_HIP_CIRCUMFERENCE,
                    WRITE_LEAN_BODY_MASS,
                    WRITE_WAIST_CIRCUMFERENCE,
                    WRITE_WEIGHT,
                    WRITE_CERVICAL_MUCUS,
                    WRITE_MENSTRUATION,
                    WRITE_OVULATION_TEST,
                    WRITE_SEXUAL_ACTIVITY,
                    WRITE_HYDRATION,
                    WRITE_NUTRITION,
                    WRITE_SLEEP,
                    WRITE_BASAL_BODY_TEMPERATURE,
                    WRITE_BLOOD_GLUCOSE,
                    WRITE_BLOOD_PRESSURE,
                    WRITE_BODY_TEMPERATURE,
                    WRITE_HEART_RATE,
                    WRITE_HEART_RATE_VARIABILITY,
                    WRITE_OXYGEN_SATURATION,
                    WRITE_RESPIRATORY_RATE,
                    WRITE_RESTING_HEART_RATE,
                    WRITE_INTERMENSTRUAL_BLEEDING);

    private PackageManager mPackageManager;

    @Before
    public void setUp() {
        mPackageManager = InstrumentationRegistry.getTargetContext().getPackageManager();
    }

    @Test
    public void testHealthPermissionGroup_isDefined() throws Exception {
        PermissionGroupInfo info =
                mPackageManager.getPermissionGroupInfo(HEALTH_PERMISSION_GROUP, /* flags= */ 0);
        assertThat(info).isNotNull();
    }

    @Test
    public void testHealthPermissions_isDefined() throws Exception {
        for (String permissionName : HEALTH_PERMISSIONS) {
            assertHealthPermissionIsDefined(permissionName);
        }
    }

    @Test
    public void testGetHealthPermissions_returns_allHealthPermissions() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        Set<String> healthPermissions = HealthConnectManager.getHealthPermissions(context);
        for (String permission : HEALTH_PERMISSIONS) {
            assertThat(healthPermissions.contains(permission)).isTrue();
        }
    }

    private void assertHealthPermissionIsDefined(String permissionName) throws Exception {
        PermissionInfo info =
                mPackageManager.getPermissionInfo(permissionName, PackageManager.GET_META_DATA);
        assertThat(info.getProtection()).isEqualTo(PermissionInfo.PROTECTION_DANGEROUS);
    }
}
