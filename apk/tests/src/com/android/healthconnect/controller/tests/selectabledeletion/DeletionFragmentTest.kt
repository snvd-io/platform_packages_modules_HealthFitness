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
package com.android.healthconnect.controller.tests.selectabledeletion

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.android.healthconnect.controller.permissions.data.FitnessPermissionType
import com.android.healthconnect.controller.selectabledeletion.DeletionConstants.START_DELETION_KEY
import com.android.healthconnect.controller.selectabledeletion.DeletionFragment
import com.android.healthconnect.controller.selectabledeletion.DeletionType
import com.android.healthconnect.controller.selectabledeletion.DeletionViewModel
import com.android.healthconnect.controller.tests.utils.TEST_APP_NAME
import com.android.healthconnect.controller.tests.utils.TEST_APP_PACKAGE_NAME
import com.android.healthconnect.controller.tests.utils.launchFragment
import com.android.healthconnect.controller.tests.utils.whenever
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

@HiltAndroidTest
class DeletionFragmentTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @BindValue val viewModel: DeletionViewModel = Mockito.mock(DeletionViewModel::class.java)

    @Before
    fun setup() {
        hiltRule.inject()
        whenever(viewModel.getDeletionType()).then {
            DeletionType.DeleteHealthPermissionTypes(
                setOf(FitnessPermissionType.STEPS, FitnessPermissionType.SPEED),
                totalPermissionTypes = 10,
            )
        }
    }

    // region DeletePermissionTypes
    @Test
    fun deletePermissionTypes_oneSelected_confirmationDialog_showsCorrectText() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.NOT_STARTED)
        }
        whenever(viewModel.getDeletionType()).then {
            DeletionType.DeleteHealthPermissionTypes(
                setOf(FitnessPermissionType.STEPS),
                totalPermissionTypes = 10,
            )
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete selected data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Cancel")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Delete")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun deletePermissionTypes_someSelected_confirmationDialog_showsCorrectText() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.NOT_STARTED)
        }
        whenever(viewModel.getDeletionType()).then {
            DeletionType.DeleteHealthPermissionTypes(
                setOf(FitnessPermissionType.STEPS, FitnessPermissionType.SPEED),
                totalPermissionTypes = 10,
            )
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete selected data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Cancel")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Delete")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun deletePermissionTypes_allSelected_confirmationDialog_showsCorrectText() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.NOT_STARTED)
        }
        whenever(viewModel.getDeletionType()).then {
            DeletionType.DeleteHealthPermissionTypes(
                setOf(FitnessPermissionType.STEPS, FitnessPermissionType.SPEED),
                totalPermissionTypes = 2,
            )
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete all data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Cancel")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Delete")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    // endregion

    // TODO app data
    // region DeletePermissionTypesFromApp
    @Test
    fun deletePermissionTypesFromApp_oneSelected_confirmationDialog_showsCorrectText() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.NOT_STARTED)
        }
        whenever(viewModel.getDeletionType()).then {
            DeletionType.DeleteHealthPermissionTypesFromApp(
                setOf(FitnessPermissionType.STEPS),
                totalPermissionTypes = 10,
                appName = TEST_APP_NAME,
                packageName = TEST_APP_PACKAGE_NAME,
            )
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete selected $TEST_APP_NAME data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Cancel")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Delete")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun deletePermissionTypesFromApp_someSelected_confirmationDialog_showsCorrectText() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.NOT_STARTED)
        }
        whenever(viewModel.getDeletionType()).then {
            DeletionType.DeleteHealthPermissionTypesFromApp(
                setOf(FitnessPermissionType.STEPS, FitnessPermissionType.SPEED),
                totalPermissionTypes = 10,
                appName = TEST_APP_NAME,
                packageName = TEST_APP_PACKAGE_NAME,
            )
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete selected $TEST_APP_NAME data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Cancel")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Delete")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun deletePermissionTypesFromApp_allSelected_confirmationDialog_showsCorrectText() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.NOT_STARTED)
        }
        whenever(viewModel.getDeletionType()).then {
            DeletionType.DeleteHealthPermissionTypesFromApp(
                setOf(FitnessPermissionType.STEPS, FitnessPermissionType.SPEED),
                totalPermissionTypes = 2,
                appName = TEST_APP_NAME,
                packageName = TEST_APP_PACKAGE_NAME,
            )
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete all $TEST_APP_NAME data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Cancel")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Delete")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    // endregion
    // TODO all entries
    // TODO app entries
    // TODO inactive app data

    // region confirmation dialog buttons
    @Test
    fun confirmationDialog_cancelButton_exitsDialog() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.NOT_STARTED)
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete selected data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Cancel")).inRoot(isDialog()).perform(click())

        onView(withText("Permanently delete selected data?")).check(doesNotExist())
    }

    // endregion

    // region deletion states
    @Test
    fun whenProgressIndicatorCanStartState_progressDialogShown() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.PROGRESS_INDICATOR_CAN_START)
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete selected data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(withText("Delete")).inRoot(isDialog()).perform(click())

        onView(withText("Deleting your data")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun whenCompletedState_progressDialogDisappears() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.COMPLETED)
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete selected data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Delete")).inRoot(isDialog()).perform(click())
        onView(withText("Deleting your data")).inRoot(isDialog()).check(doesNotExist())
    }

    @Test
    fun whenCompletedState_successDialogShown() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.COMPLETED)
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete selected data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Delete")).inRoot(isDialog()).perform(click())
        onView(withText("Data deleted from Health Connect"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "If you want to completely delete the data from your connected apps, check each app where your data may be saved."
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Done")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("See connected apps")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun whenFailedState_failureDialogShown() {
        whenever(viewModel.deletionProgress).then {
            MutableLiveData(DeletionViewModel.DeletionProgress.FAILED)
        }

        launchFragment<DeletionFragment>(Bundle()) {
            (this as DeletionFragment)
                .parentFragmentManager
                .setFragmentResult(START_DELETION_KEY, bundleOf())
        }

        onView(withText("Permanently delete selected data?"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(
                withText(
                    "Connected apps will no longer be able to read this data from Health\u00A0Connect"
                )
            )
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withText("Delete")).inRoot(isDialog()).perform(click())
        onView(withText("Deleting your data")).inRoot(isDialog()).check(doesNotExist())
        onView(withText("Couldn't delete data")).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText("Something went wrong and Health\u00A0Connect couldn't delete your data"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    // endregion
}
