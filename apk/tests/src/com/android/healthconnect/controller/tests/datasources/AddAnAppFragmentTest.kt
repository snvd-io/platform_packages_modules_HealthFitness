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
package com.android.healthconnect.controller.tests.datasources

import android.health.connect.HealthDataCategory
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.android.healthconnect.controller.R
import com.android.healthconnect.controller.categories.HealthDataCategoriesFragment.Companion.CATEGORY_KEY
import com.android.healthconnect.controller.datasources.AddAnAppFragment
import com.android.healthconnect.controller.datasources.DataSourcesViewModel
import com.android.healthconnect.controller.datasources.DataSourcesViewModel.PotentialAppSourcesState
import com.android.healthconnect.controller.tests.utils.TEST_APP
import com.android.healthconnect.controller.tests.utils.TEST_APP_2
import com.android.healthconnect.controller.tests.utils.TEST_APP_3
import com.android.healthconnect.controller.tests.utils.TEST_APP_NAME
import com.android.healthconnect.controller.tests.utils.TEST_APP_NAME_2
import com.android.healthconnect.controller.tests.utils.TEST_APP_NAME_3
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
class AddAnAppFragmentTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    val dataSourcesViewModel: DataSourcesViewModel =
        Mockito.mock(DataSourcesViewModel::class.java)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun fragmentIsDisplayed() {
        whenever(dataSourcesViewModel.potentialAppSources).then {
            MutableLiveData<PotentialAppSourcesState>(
                PotentialAppSourcesState.WithData(listOf(
                    TEST_APP,
                    TEST_APP_2,
                    TEST_APP_3
                )))
        }

        launchFragment<AddAnAppFragment>(bundleOf(CATEGORY_KEY to HealthDataCategory.ACTIVITY))

        onView(withText(TEST_APP_NAME)).check(matches(isDisplayed()))
        onView(withText(TEST_APP_NAME_2)).check(matches(isDisplayed()))
        onView(withText(TEST_APP_NAME_3)).check(matches(isDisplayed()))
    }

    @Test
    fun showsLoading_whenAppSourcesLoading() {
        whenever(dataSourcesViewModel.potentialAppSources).then {
            MutableLiveData<PotentialAppSourcesState>(
                PotentialAppSourcesState.Loading)
        }
        launchFragment<AddAnAppFragment>(bundleOf(CATEGORY_KEY to HealthDataCategory.ACTIVITY))

        onView(ViewMatchers.withId(R.id.progress_indicator)).check(matches(isDisplayed()))
    }

    @Test
    fun showsError_whenAppSourcesError() {
        whenever(dataSourcesViewModel.potentialAppSources).then {
            MutableLiveData<PotentialAppSourcesState>(
                PotentialAppSourcesState.LoadingFailed
            )
        }
        launchFragment<AddAnAppFragment>(bundleOf(CATEGORY_KEY to HealthDataCategory.ACTIVITY))

        onView(ViewMatchers.withId(R.id.error_view)).check(matches(isDisplayed()))

    }
}