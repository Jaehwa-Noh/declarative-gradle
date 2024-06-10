/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.experimental.android.application;

import com.android.build.api.dsl.ApplicationBaseFlavor;
import org.gradle.api.Action;
import org.gradle.api.experimental.android.AndroidSoftware;
import org.gradle.api.experimental.android.extensions.DependencyGuard;
import org.gradle.api.experimental.android.extensions.Firebase;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.declarative.dsl.model.annotations.Configuring;
import org.gradle.declarative.dsl.model.annotations.Restricted;

@Restricted
public interface AndroidApplication extends AndroidSoftware {
    /**
     * @see ApplicationBaseFlavor#setVersionName(String)
     */
    @Restricted
    Property<String> getVersionName();

    /**
     * @see ApplicationBaseFlavor#setVersionCode(Integer)
     */
    @Restricted
    Property<Integer> getVersionCode();

    /**
     * @see ApplicationBaseFlavor#setApplicationId(String)
     */
    @Restricted
    Property<String> getApplicationId();

    @Override
    @Nested
    AndroidApplicationDependencies getDependencies();

    @Configuring
    default void dependencies(Action<? super AndroidApplicationDependencies> action) {
        action.execute(getDependencies());
    }

    @Override
    @Nested
    AndroidApplicationBuildTypes getBuildTypes();

    @Configuring
    default void buildTypes(Action<? super AndroidApplicationBuildTypes> action) {
        action.execute(getBuildTypes());
    }

    @Nested
    DependencyGuard getDependencyGuard();

    @Configuring
    default void dependencyGuard(Action<? super DependencyGuard> action) {
        DependencyGuard dependencyGuard = getDependencyGuard();
        dependencyGuard.getEnabled().set(true);
        action.execute(dependencyGuard);
    }

    @Nested
    Firebase getFirebase();

    @Configuring
    default void firebase(Action<? super Firebase> action) {
        Firebase firebase = getFirebase();
        firebase.getEnabled().set(true);
        action.execute(firebase);
    }
}
