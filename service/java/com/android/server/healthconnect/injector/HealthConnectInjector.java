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

package com.android.server.healthconnect.injector;

import androidx.annotation.NonNull;

import com.android.server.healthconnect.permission.PackageInfoUtils;
import com.android.server.healthconnect.storage.TransactionManager;

/**
 * Interface for Health Connect Dependency Injector.
 *
 * @hide
 */
public interface HealthConnectInjector {

    /** Getter for PackageInfoUtils instance initialised by the Health Connect Injector. */
    @NonNull
    PackageInfoUtils getPackageInfoUtils();

    /** Getter for TransactionManager instance initialised by the Health Connect Injector. */
    @NonNull
    TransactionManager getTransactionManager();
}
