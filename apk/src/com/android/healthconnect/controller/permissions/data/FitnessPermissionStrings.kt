/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * ```
 *      http://www.apache.org/licenses/LICENSE-2.0
 * ```
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.healthconnect.controller.permissions.data

import androidx.annotation.StringRes
import com.android.healthconnect.controller.R
import com.google.common.collect.ImmutableMap

data class FitnessPermissionStrings(
    @StringRes val uppercaseLabel: Int,
    @StringRes val lowercaseLabel: Int,
    @StringRes val readContentDescription: Int,
    @StringRes val writeContentDescription: Int
) {
    companion object {
        fun fromPermissionType(
            fitnessPermissionType: FitnessPermissionType
        ): FitnessPermissionStrings {
            return PERMISSION_TYPE_STRINGS[fitnessPermissionType]
                ?: throw IllegalArgumentException(
                    "No strings for permission group " + fitnessPermissionType.name)
        }
    }
}

private val PERMISSION_TYPE_STRINGS: ImmutableMap<FitnessPermissionType, FitnessPermissionStrings> =
    ImmutableMap.Builder<FitnessPermissionType, FitnessPermissionStrings>()
        .put(
            FitnessPermissionType.ACTIVE_CALORIES_BURNED,
            FitnessPermissionStrings(
                R.string.active_calories_burned_uppercase_label,
                R.string.active_calories_burned_lowercase_label,
                R.string.active_calories_burned_read_content_description,
                R.string.active_calories_burned_write_content_description))
        .put(
            FitnessPermissionType.DISTANCE,
            FitnessPermissionStrings(
                R.string.distance_uppercase_label,
                R.string.distance_lowercase_label,
                R.string.distance_read_content_description,
                R.string.distance_write_content_description))
        .put(
            FitnessPermissionType.ELEVATION_GAINED,
            FitnessPermissionStrings(
                R.string.elevation_gained_uppercase_label,
                R.string.elevation_gained_lowercase_label,
                R.string.elevation_gained_read_content_description,
                R.string.elevation_gained_write_content_description))
        .put(
            FitnessPermissionType.EXERCISE,
            FitnessPermissionStrings(
                R.string.exercise_uppercase_label,
                R.string.exercise_lowercase_label,
                R.string.exercise_read_content_description,
                R.string.exercise_write_content_description))
        .put(
            FitnessPermissionType.SPEED,
            FitnessPermissionStrings(
                R.string.speed_uppercase_label,
                R.string.speed_lowercase_label,
                R.string.speed_read_content_description,
                R.string.speed_write_content_description))
        .put(
            FitnessPermissionType.POWER,
            FitnessPermissionStrings(
                R.string.power_uppercase_label,
                R.string.power_lowercase_label,
                R.string.power_read_content_description,
                R.string.power_write_content_description))
        .put(
            FitnessPermissionType.FLOORS_CLIMBED,
            FitnessPermissionStrings(
                R.string.floors_climbed_uppercase_label,
                R.string.floors_climbed_lowercase_label,
                R.string.floors_climbed_read_content_description,
                R.string.floors_climbed_write_content_description))
        .put(
            FitnessPermissionType.INTERMENSTRUAL_BLEEDING,
            FitnessPermissionStrings(
                R.string.spotting_uppercase_label,
                R.string.spotting_lowercase_label,
                R.string.spotting_read_content_description,
                R.string.spotting_write_content_description))
        .put(
            FitnessPermissionType.STEPS,
            FitnessPermissionStrings(
                R.string.steps_uppercase_label,
                R.string.steps_lowercase_label,
                R.string.steps_read_content_description,
                R.string.steps_write_content_description))
        .put(
            FitnessPermissionType.TOTAL_CALORIES_BURNED,
            FitnessPermissionStrings(
                R.string.total_calories_burned_uppercase_label,
                R.string.total_calories_burned_lowercase_label,
                R.string.total_calories_burned_read_content_description,
                R.string.total_calories_burned_write_content_description))
        .put(
            FitnessPermissionType.VO2_MAX,
            FitnessPermissionStrings(
                R.string.vo2_max_uppercase_label,
                R.string.vo2_max_lowercase_label,
                R.string.vo2_max_read_content_description,
                R.string.vo2_max_write_content_description))
        .put(
            FitnessPermissionType.WHEELCHAIR_PUSHES,
            FitnessPermissionStrings(
                R.string.wheelchair_pushes_uppercase_label,
                R.string.wheelchair_pushes_lowercase_label,
                R.string.wheelchair_pushes_read_content_description,
                R.string.wheelchair_pushes_write_content_description))
        .put(
            FitnessPermissionType.BASAL_METABOLIC_RATE,
            FitnessPermissionStrings(
                R.string.basal_metabolic_rate_uppercase_label,
                R.string.basal_metabolic_rate_lowercase_label,
                R.string.basal_metabolic_rate_read_content_description,
                R.string.basal_metabolic_rate_write_content_description))
        .put(
            FitnessPermissionType.BODY_FAT,
            FitnessPermissionStrings(
                R.string.body_fat_uppercase_label,
                R.string.body_fat_lowercase_label,
                R.string.body_fat_read_content_description,
                R.string.body_fat_write_content_description))
        .put(
            FitnessPermissionType.BODY_WATER_MASS,
            FitnessPermissionStrings(
                R.string.body_water_mass_uppercase_label,
                R.string.body_water_mass_lowercase_label,
                R.string.body_water_mass_read_content_description,
                R.string.body_water_mass_write_content_description))
        .put(
            FitnessPermissionType.BONE_MASS,
            FitnessPermissionStrings(
                R.string.bone_mass_uppercase_label,
                R.string.bone_mass_lowercase_label,
                R.string.bone_mass_read_content_description,
                R.string.bone_mass_write_content_description))
        .put(
            FitnessPermissionType.HEIGHT,
            FitnessPermissionStrings(
                R.string.height_uppercase_label,
                R.string.height_lowercase_label,
                R.string.height_read_content_description,
                R.string.height_write_content_description))
        .put(
            FitnessPermissionType.LEAN_BODY_MASS,
            FitnessPermissionStrings(
                R.string.lean_body_mass_uppercase_label,
                R.string.lean_body_mass_lowercase_label,
                R.string.lean_body_mass_read_content_description,
                R.string.lean_body_mass_write_content_description))
        .put(
            FitnessPermissionType.WEIGHT,
            FitnessPermissionStrings(
                R.string.weight_uppercase_label,
                R.string.weight_lowercase_label,
                R.string.weight_read_content_description,
                R.string.weight_write_content_description))
        .put(
            FitnessPermissionType.CERVICAL_MUCUS,
            FitnessPermissionStrings(
                R.string.cervical_mucus_uppercase_label,
                R.string.cervical_mucus_lowercase_label,
                R.string.cervical_mucus_read_content_description,
                R.string.cervical_mucus_write_content_description))
        .put(
            FitnessPermissionType.MENSTRUATION,
            FitnessPermissionStrings(
                R.string.menstruation_uppercase_label,
                R.string.menstruation_lowercase_label,
                R.string.menstruation_read_content_description,
                R.string.menstruation_write_content_description))
        .put(
            FitnessPermissionType.OVULATION_TEST,
            FitnessPermissionStrings(
                R.string.ovulation_test_uppercase_label,
                R.string.ovulation_test_lowercase_label,
                R.string.ovulation_test_read_content_description,
                R.string.ovulation_test_write_content_description))
        .put(
            FitnessPermissionType.SEXUAL_ACTIVITY,
            FitnessPermissionStrings(
                R.string.sexual_activity_uppercase_label,
                R.string.sexual_activity_lowercase_label,
                R.string.sexual_activity_read_content_description,
                R.string.sexual_activity_write_content_description))
        .put(
            FitnessPermissionType.HYDRATION,
            FitnessPermissionStrings(
                R.string.hydration_uppercase_label,
                R.string.hydration_lowercase_label,
                R.string.hydration_read_content_description,
                R.string.hydration_write_content_description))
        .put(
            FitnessPermissionType.NUTRITION,
            FitnessPermissionStrings(
                R.string.nutrition_uppercase_label,
                R.string.nutrition_lowercase_label,
                R.string.nutrition_read_content_description,
                R.string.nutrition_write_content_description))
        .put(
            FitnessPermissionType.SLEEP,
            FitnessPermissionStrings(
                R.string.sleep_uppercase_label,
                R.string.sleep_lowercase_label,
                R.string.sleep_read_content_description,
                R.string.sleep_write_content_description))
        .put(
            FitnessPermissionType.BASAL_BODY_TEMPERATURE,
            FitnessPermissionStrings(
                R.string.basal_body_temperature_uppercase_label,
                R.string.basal_body_temperature_lowercase_label,
                R.string.basal_body_temperature_read_content_description,
                R.string.basal_body_temperature_write_content_description))
        .put(
            FitnessPermissionType.BLOOD_GLUCOSE,
            FitnessPermissionStrings(
                R.string.blood_glucose_uppercase_label,
                R.string.blood_glucose_lowercase_label,
                R.string.blood_glucose_read_content_description,
                R.string.blood_glucose_write_content_description))
        .put(
            FitnessPermissionType.BLOOD_PRESSURE,
            FitnessPermissionStrings(
                R.string.blood_pressure_uppercase_label,
                R.string.blood_pressure_lowercase_label,
                R.string.blood_pressure_read_content_description,
                R.string.blood_pressure_write_content_description))
        .put(
            FitnessPermissionType.BODY_TEMPERATURE,
            FitnessPermissionStrings(
                R.string.body_temperature_uppercase_label,
                R.string.body_temperature_lowercase_label,
                R.string.body_temperature_read_content_description,
                R.string.body_temperature_write_content_description))
        .put(
            FitnessPermissionType.HEART_RATE,
            FitnessPermissionStrings(
                R.string.heart_rate_uppercase_label,
                R.string.heart_rate_lowercase_label,
                R.string.heart_rate_read_content_description,
                R.string.heart_rate_write_content_description))
        .put(
            FitnessPermissionType.HEART_RATE_VARIABILITY,
            FitnessPermissionStrings(
                R.string.heart_rate_variability_uppercase_label,
                R.string.heart_rate_variability_lowercase_label,
                R.string.heart_rate_variability_read_content_description,
                R.string.heart_rate_variability_write_content_description))
        .put(
            FitnessPermissionType.OXYGEN_SATURATION,
            FitnessPermissionStrings(
                R.string.oxygen_saturation_uppercase_label,
                R.string.oxygen_saturation_lowercase_label,
                R.string.oxygen_saturation_read_content_description,
                R.string.oxygen_saturation_write_content_description))
        .put(
            FitnessPermissionType.RESPIRATORY_RATE,
            FitnessPermissionStrings(
                R.string.respiratory_rate_uppercase_label,
                R.string.respiratory_rate_lowercase_label,
                R.string.respiratory_rate_read_content_description,
                R.string.respiratory_rate_write_content_description))
        .put(
            FitnessPermissionType.RESTING_HEART_RATE,
            FitnessPermissionStrings(
                R.string.resting_heart_rate_uppercase_label,
                R.string.resting_heart_rate_lowercase_label,
                R.string.resting_heart_rate_read_content_description,
                R.string.resting_heart_rate_write_content_description))
        .put(
            FitnessPermissionType.SKIN_TEMPERATURE,
            FitnessPermissionStrings(
                R.string.skin_temperature_uppercase_label,
                R.string.skin_temperature_lowercase_label,
                R.string.skin_temperature_read_content_description,
                R.string.skin_temperature_write_content_description))
        .put(
            FitnessPermissionType.EXERCISE_ROUTE,
            FitnessPermissionStrings(
                R.string.exercise_route_uppercase_label,
                R.string.exercise_route_lowercase_label,
                R.string.exercise_route_read_content_description,
                R.string.exercise_route_write_content_description,
            ))
        .put(
            FitnessPermissionType.PLANNED_EXERCISE,
            FitnessPermissionStrings(
                R.string.planned_exercise_uppercase_label,
                R.string.planned_exercise_lowercase_label,
                R.string.planned_exercise_read_content_description,
                R.string.planned_exercise_write_content_description,
            ))
        .put(
            FitnessPermissionType.MINDFULNESS,
            FitnessPermissionStrings(
                R.string.mindfulness_uppercase_label,
                R.string.mindfulness_lowercase_label,
                R.string.mindfulness_read_content_description,
                R.string.mindfulness_write_content_description,
            ))
        .buildOrThrow()
