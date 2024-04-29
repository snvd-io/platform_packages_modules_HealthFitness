/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.android.healthconnect.controller.tests.utils.di

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.android.healthconnect.controller.utils.DeviceInfoUtils

class FakeDeviceInfoUtils : DeviceInfoUtils {
    private var sendFeedbackAvailable = false

    private var playStoreAvailable = false

    private var isHealthConnectAvailable = true

    fun setSendFeedbackAvailability(available: Boolean) {
        sendFeedbackAvailable = available
    }

    fun setPlayStoreAvailability(available: Boolean) {
        playStoreAvailable = available
    }

    fun setHealthConnectAvailable(isAvailable: Boolean) {
        isHealthConnectAvailable = isAvailable
    }

    override fun isHealthConnectAvailable(context: Context): Boolean {
        return isHealthConnectAvailable
    }

    override fun isSendFeedbackAvailable(context: Context): Boolean {
        return sendFeedbackAvailable
    }

    override fun isPlayStoreAvailable(context: Context): Boolean {
        return playStoreAvailable
    }

    override fun openHCGetStartedLink(activity: FragmentActivity) {}

    override fun openSendFeedbackActivity(activity: FragmentActivity) {}

    override fun isIntentHandlerAvailable(context: Context, intent: Intent): Boolean {
        return false
    }
}
