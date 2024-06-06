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

package com.android.server.healthconnect.storage.datatypehelpers;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

public class DatabaseHelperTest {

    private final Set<Class<?>> mNonSingletonClasses =
            Set.of(ActivityDateHelper.class, AccessLogsHelper.class);

    @Test
    public void nonSingletons_doNotHaveCentralState() {
        for (Class<?> nonSingletonClass : mNonSingletonClasses) {
            Field[] declaredFields = nonSingletonClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {

                if (!declaredField.getType().equals(String.class)) {
                    assertThat(declaredField.getType().isPrimitive()).isTrue();
                }
                assertThat(Modifier.isStatic(declaredField.getModifiers())).isTrue();
                assertThat(Modifier.isFinal(declaredField.getModifiers())).isTrue();
            }
        }
    }
}
