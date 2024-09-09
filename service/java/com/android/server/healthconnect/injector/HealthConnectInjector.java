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

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.server.healthconnect.HealthConnectDeviceConfigManager;
import com.android.server.healthconnect.exportimport.ExportManager;
import com.android.server.healthconnect.migration.MigrationStateManager;
import com.android.server.healthconnect.migration.PriorityMigrationHelper;
import com.android.server.healthconnect.permission.PackageInfoUtils;
import com.android.server.healthconnect.storage.ExportImportSettingsStorage;
import com.android.server.healthconnect.storage.TransactionManager;
import com.android.server.healthconnect.storage.datatypehelpers.DeviceInfoHelper;
import com.android.server.healthconnect.storage.datatypehelpers.HealthDataCategoryPriorityHelper;
import com.android.server.healthconnect.storage.datatypehelpers.PreferenceHelper;

/**
 * Interface for Health Connect Dependency Injector.
 *
 * @hide
 */
public abstract class HealthConnectInjector {

    @Nullable private static HealthConnectInjector sHealthConnectInjector;

    /** Getter for PackageInfoUtils instance initialised by the Health Connect Injector. */
    public abstract PackageInfoUtils getPackageInfoUtils();

    /** Getter for TransactionManager instance initialised by the Health Connect Injector. */
    public abstract TransactionManager getTransactionManager();

    /**
     * Getter for HealthDataCategoryPriorityHelper instance initialised by the Health Connect
     * Injector.
     */
    public abstract HealthDataCategoryPriorityHelper getHealthDataCategoryPriorityHelper();

    /** Getter for PriorityMigrationHelper instance initialised by the Health Connect Injector. */
    public abstract PriorityMigrationHelper getPriorityMigrationHelper();

    /** Getter for PreferenceHelper instance initialised by the Health Connect Injector. */
    public abstract PreferenceHelper getPreferenceHelper();

    /**
     * Getter for ExportImportSettingsStorage instance initialised by the Health Connect Injector.
     */
    public abstract ExportImportSettingsStorage getExportImportSettingsStorage();

    /** Getter for ExportManager instance initialised by the Health Connect Injector. */
    public abstract ExportManager getExportManager();

    /** Getter for MigrationStateManager instance initialised by the Health Connect Injector. */
    public abstract MigrationStateManager getMigrationStateManager();

    /**
     * Getter for HealthConnectDeviceConfigManager instance initialised by the Health Connect
     * Injector.
     */
    public abstract HealthConnectDeviceConfigManager getHealthConnectDeviceConfigManager();

    /** Getter for DeviceInfoHelper instance initialised by the Health Connect Injector. */
    public abstract DeviceInfoHelper getDeviceInfoHelper();

    /** Used to initialize the Injector. */
    public static void setInstance(HealthConnectInjector healthConnectInjector) {
        if (sHealthConnectInjector != null) {
            throw new IllegalStateException(
                    "An instance of injector has already been initialized.");
        }
        sHealthConnectInjector = healthConnectInjector;
    }

    /**
     * Used to getInstance of the Injector so that it can be used statically by other base services.
     */
    public static HealthConnectInjector getInstance() {
        if (sHealthConnectInjector == null) {
            throw new IllegalStateException(
                    "Please initialize an instance of injector and call setInstance.");
        }
        return sHealthConnectInjector;
    }

    /** Used to reset instance of the Injector for testing. */
    @VisibleForTesting
    public static void resetInstanceForTest() {
        sHealthConnectInjector = null;
    }
}
