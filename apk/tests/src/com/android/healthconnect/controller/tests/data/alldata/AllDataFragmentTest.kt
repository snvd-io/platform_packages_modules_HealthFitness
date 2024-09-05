/*
 * Copyright (C) 2023 The Android Open Source Project
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
package com.android.healthconnect.controller.tests.data.alldata

import android.content.Context
import android.content.pm.ActivityInfo
import android.health.connect.HealthConnectManager
import android.health.connect.RecordTypeInfoResponse
import android.health.connect.datatypes.Record
import android.os.OutcomeReceiver
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.preference.PreferenceCategory
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.android.healthconnect.controller.R
import com.android.healthconnect.controller.data.alldata.AllDataFragment
import com.android.healthconnect.controller.data.alldata.AllDataViewModel
import com.android.healthconnect.controller.data.appdata.AppDataUseCase
import com.android.healthconnect.controller.permissions.data.FitnessPermissionType
import com.android.healthconnect.controller.selectabledeletion.DeletionPermissionTypesPreference
import com.android.healthconnect.controller.selectabledeletion.SelectAllCheckboxPreference
import com.android.healthconnect.controller.shared.HealthDataCategoryExtensions.fromFitnessPermissionType
import com.android.healthconnect.controller.shared.HealthPermissionToDatatypeMapper
import com.android.healthconnect.controller.shared.children
import com.android.healthconnect.controller.tests.utils.TEST_APP_PACKAGE_NAME
import com.android.healthconnect.controller.tests.utils.getDataOrigin
import com.android.healthconnect.controller.tests.utils.launchFragment
import com.android.healthconnect.controller.tests.utils.toggleAnimation
import com.android.healthconnect.controller.utils.logging.AllDataElement
import com.android.healthconnect.controller.utils.logging.HealthConnectLogger
import com.android.healthconnect.controller.utils.logging.PageName
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import com.android.healthconnect.controller.data.alldata.AllDataViewModel.AllDataDeletionScreenState.VIEW
import com.android.healthconnect.controller.data.alldata.AllDataViewModel.AllDataDeletionScreenState.DELETE

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class AllDataFragmentTest {

    @get:Rule val hiltRule = HiltAndroidRule(this)
    var manager: HealthConnectManager = Mockito.mock(HealthConnectManager::class.java)

    private val appDataUseCase: AppDataUseCase = AppDataUseCase(manager, Dispatchers.Main)

    @BindValue val allDataViewModel: AllDataViewModel = AllDataViewModel(appDataUseCase)
    @BindValue val healthConnectLogger: HealthConnectLogger = mock()
    private lateinit var navHostController: TestNavHostController
    private lateinit var context: Context

    @Before
    fun setup() {
        hiltRule.inject()
        toggleAnimation(false)
        context = InstrumentationRegistry.getInstrumentation().context
        navHostController = TestNavHostController(context)
    }

    @After
    fun tearDown() {
        toggleAnimation(true)
        reset(healthConnectLogger)
    }

    @Test
    fun populatedDataTypesDisplayed_impressionsLogged() {
        mockData(
            listOf(
                FitnessPermissionType.STEPS,
                FitnessPermissionType.HEART_RATE,
                FitnessPermissionType.BASAL_BODY_TEMPERATURE))

        launchFragment<AllDataFragment>()

        onView(withText("Steps")).check(matches(isDisplayed()))
        onView(withText("Heart rate")).check(matches(isDisplayed()))
        onView(withText("Basal body temperature")).check(matches(isDisplayed()))
        onView(withText("No data")).check(doesNotExist())
        verify(healthConnectLogger, atLeast(1)).setPageId(PageName.ALL_DATA_PAGE)
        verify(healthConnectLogger).logPageImpression()
        verify(healthConnectLogger, times(3))
            .logImpression(AllDataElement.PERMISSION_TYPE_BUTTON_NO_CHECKBOX)
    }

    @Test
    fun whenNoData_noDataMessageDisplayed() {
        mockData(emptyList())

        launchFragment<AllDataFragment>()

        onView(withText("No data")).check(matches(isDisplayed()))
        onView(withText("Data from apps with access to Health Connect will show here"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigatesToAllEntries() {
        mockData(listOf(FitnessPermissionType.STEPS))

        launchFragment<AllDataFragment>() {
            navHostController.setGraph(R.navigation.data_nav_graph_new_ia)
            Navigation.setViewNavController(this.requireView(), navHostController)
        }

        onView(withText("Steps")).check(matches(isDisplayed()))
        onView(withText("Steps")).perform(click())
        verify(healthConnectLogger)
            .logInteraction(AllDataElement.PERMISSION_TYPE_BUTTON_NO_CHECKBOX)
        assertThat(navHostController.currentDestination?.id)
            .isEqualTo(R.id.entriesAndAccessFragment)
    }

    @Test
    fun triggerDeletionState_showsCheckboxes() {
        mockData(listOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.MENSTRUATION))

        val scenario = launchFragment<AllDataFragment>()

        assertCheckboxNotShown("Distance")
        assertCheckboxNotShown("Menstruation")

        // trigger deletion state
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("")
            (fragment as AllDataFragment).triggerDeletionState(DELETE)
        }

        assertCheckboxShown("Distance")
        assertCheckboxShown("Menstruation")
        verify(healthConnectLogger).logImpression(AllDataElement.SELECT_ALL_BUTTON)
        verify(healthConnectLogger, atLeast(2))
            .logImpression(AllDataElement.PERMISSION_TYPE_BUTTON_WITH_CHECKBOX)
    }

    @Test
    fun triggersDeletionState_checkedItemsAddedToDeleteSet() {
        mockData(listOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.HEART_RATE))

        val scenario = launchFragment<AllDataFragment>()
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("")
            (fragment as AllDataFragment).triggerDeletionState(DELETE)
        }

        onView(withText("Distance")).perform(click())
        onIdle()
        assertThat(allDataViewModel.setOfPermissionTypesToBeDeleted.value)
            .containsExactlyElementsIn(setOf(FitnessPermissionType.DISTANCE))
        verify(healthConnectLogger)
            .logInteraction(AllDataElement.PERMISSION_TYPE_BUTTON_WITH_CHECKBOX)
        onView(withText("Distance")).perform(click())
        assertThat(allDataViewModel.setOfPermissionTypesToBeDeleted.value).isEmpty()
    }

    @Test
    @Ignore("Flaky when rotating screen")
    fun triggerDeletionState_checkboxesRemainOnOrientationChange() = runTest {
        mockData(listOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.HEART_RATE))

        val scenario = launchFragment<AllDataFragment>()
        scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val fragment = activity.supportFragmentManager.findFragmentByTag("")
            (fragment as AllDataFragment).triggerDeletionState(DELETE)
        }

        advanceUntilIdle()

        assertCheckboxShown("Distance")
        assertCheckboxShown("Heart rate")
        onView(withText("Distance")).perform(click())

        scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            val fragment = activity.supportFragmentManager.findFragmentByTag("") as AllDataFragment
            val fitnessCategoryPreference =
                fragment.preferenceScreen.findPreference("key_permission_type")
                    as PreferenceCategory?
            fitnessCategoryPreference?.children?.forEach { preference ->
                if (preference is PreferenceCategory) {
                    preference.children.forEach { permissionTypePreference ->
                        if (permissionTypePreference is DeletionPermissionTypesPreference) {
                            if (permissionTypePreference.getHealthPermissionType() ==
                                FitnessPermissionType.DISTANCE) {
                                assertThat(permissionTypePreference.getIsChecked()).isTrue()
                            } else if (permissionTypePreference.getHealthPermissionType() ==
                                FitnessPermissionType.MENSTRUATION) {
                                assertThat(permissionTypePreference.getIsChecked()).isFalse()
                            }
                        }
                    }
                }
            }
        }

        assertCheckboxShown("Distance")
        assertCheckboxShown("Heart rate")

        scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    @Test
    fun triggerDeletionState_displaysSelectAllButton() {
        mockData(listOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.MENSTRUATION))

        val scenario = launchFragment<AllDataFragment>()

        // trigger deletion state
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("")
            (fragment as AllDataFragment).triggerDeletionState(DELETE)
        }

        assertCheckboxShown("Select all")
    }

    @Test
    fun triggerDeletionState_onSelectAllChecked_allPermissionTypesChecked() = runTest {
        mockData(listOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.MENSTRUATION))

        val scenario = launchFragment<AllDataFragment>()
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("")
            (fragment as AllDataFragment).triggerDeletionState(DELETE)
        }

        advanceUntilIdle()

        assertCheckboxShown("Select all")
        onView(withText("Select all")).perform(click())
        assertThat(allDataViewModel.setOfPermissionTypesToBeDeleted.value)
            .containsExactlyElementsIn(
                setOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.MENSTRUATION))
        verify(healthConnectLogger).logInteraction(AllDataElement.SELECT_ALL_BUTTON)
    }

    @Test
    fun triggerDeletionState_onSelectAllUnchecked_allPermissionTypesUnChecked() = runTest {
        mockData(listOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.MENSTRUATION))

        val scenario = launchFragment<AllDataFragment>()
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("")
            (fragment as AllDataFragment).triggerDeletionState(DELETE)
        }

        advanceUntilIdle()

        assertCheckboxShown("Select all")
        onView(withText("Select all")).perform(click())
        assertThat(allDataViewModel.setOfPermissionTypesToBeDeleted.value)
            .containsExactlyElementsIn(
                setOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.MENSTRUATION))
        onView(withText("Select all")).perform(click())
        assertThat(allDataViewModel.setOfPermissionTypesToBeDeleted.value).isEmpty()
    }

    @Test
    fun triggerDeletionState_allPermissionTypesChecked_selectAllShouldBeChecked() {
        mockData(listOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.MENSTRUATION))

        val scenario = launchFragment<AllDataFragment>()
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("")
            (fragment as AllDataFragment).triggerDeletionState(DELETE)
        }

        assertCheckboxShown("Distance")
        assertCheckboxShown("Menstruation")
        onView(withText("Distance")).perform(click())
        onView(withText("Menstruation")).perform(click())
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("") as AllDataFragment
            val selectAllCheckboxPreference =
                fragment.preferenceScreen.findPreference("key_select_all")
                    as SelectAllCheckboxPreference?
            assertThat(selectAllCheckboxPreference?.getIsChecked()).isTrue()
        }
    }

    @Test
    fun triggerDeletionState_selectAllChecked_stepsUnchecked_selectAllUnchecked() = runTest {
        mockData(listOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.MENSTRUATION))

        val scenario = launchFragment<AllDataFragment>()
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("")
            (fragment as AllDataFragment).triggerDeletionState(DELETE)
        }

        advanceUntilIdle()

        assertCheckboxShown("Select all")
        onView(withText("Select all")).perform(click())
        onView(withText("Distance")).perform(click())
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("") as AllDataFragment
            val selectAllCheckboxPreference =
                fragment.preferenceScreen.findPreference("key_select_all")
                    as SelectAllCheckboxPreference?
            assertThat(selectAllCheckboxPreference?.getIsChecked()).isFalse()
        }
    }

    @Test
    @Ignore("Flaky when rotating screen")
    fun triggerDeletionState_selectAllChecked_checkboxesRemainOnOrientationChange() = runTest {
        mockData(listOf(FitnessPermissionType.DISTANCE, FitnessPermissionType.HEART_RATE))

        val scenario = launchFragment<AllDataFragment>()
        scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val fragment = activity.supportFragmentManager.findFragmentByTag("")
            (fragment as AllDataFragment).triggerDeletionState(DELETE)
        }

        advanceUntilIdle()

        assertCheckboxShown("Select all")
        onView(withText("Select all")).perform(click())

        scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        onView(withText("Select all")).perform(scrollTo())
        onIdle()
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag("") as AllDataFragment
            val selectAllCheckboxPreference =
                fragment.preferenceScreen.findPreference("key_select_all")
                    as SelectAllCheckboxPreference?
            assertThat(selectAllCheckboxPreference?.getIsChecked()).isTrue()
            fragment.preferenceScreen.children.forEach { preference ->
                if (preference is PreferenceCategory) {
                    preference.children.forEach { permissionTypePreference ->
                        if (permissionTypePreference is DeletionPermissionTypesPreference) {
                            assertThat(permissionTypePreference.getIsChecked()).isTrue()
                        }
                    }
                }
            }
        }
        assertCheckboxShown("Distance")
        assertCheckboxShown("Heart rate")

        scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun assertCheckboxShown(title: String, tag: String = "checkbox") {
        onView(withId(androidx.preference.R.id.recycler_view))
            .check(
                matches(
                    allOf(hasDescendant(withText(title)), hasDescendant(withTagValue(`is`(tag))))))
    }

    private fun assertCheckboxNotShown(title: String, tag: String = "checkbox") {
        onView(withId(androidx.preference.R.id.recycler_view))
            .check(
                matches(
                    allOf(
                        hasDescendant(withText(title)),
                        not(hasDescendant(withTagValue(`is`(tag)))))))
    }

    private fun mockData(permissionTypesList: List<FitnessPermissionType>) {
        val recordTypeInfoMap =
            permissionTypesList.associate { fitnessPermissionType ->
                val permissionCategory = fitnessPermissionType.category
                val healthCategory = fromFitnessPermissionType(fitnessPermissionType)
                val dataType =
                    HealthPermissionToDatatypeMapper.getDataTypes(fitnessPermissionType)[0]

                dataType to
                    RecordTypeInfoResponse(
                        permissionCategory,
                        healthCategory,
                        listOf(getDataOrigin(TEST_APP_PACKAGE_NAME)))
            }

        doAnswer(prepareAnswer(recordTypeInfoMap))
            .`when`(manager)
            .queryAllRecordTypesInfo(any(), any())
    }

    private fun prepareAnswer(
        recordTypeInfoMap: Map<Class<out Record>, RecordTypeInfoResponse>
    ): (InvocationOnMock) -> Map<Class<out Record>, RecordTypeInfoResponse> {
        val answer = { args: InvocationOnMock ->
            val receiver = args.arguments[1] as OutcomeReceiver<Any?, *>
            receiver.onResult(recordTypeInfoMap)
            recordTypeInfoMap
        }
        return answer
    }
}
