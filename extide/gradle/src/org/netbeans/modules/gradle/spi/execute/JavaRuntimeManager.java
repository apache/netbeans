/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.gradle.spi.execute;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;

/**
 * JavaRuntimeManager is to provide Java Runtime for Gradle execution. Gradle
 * is not just a build system for JVM based languages, but can build C, C++,
 * NodeJS projects as well.
 * <p>
 * Gradle just requires a suitable JRE to run on.
 * </p>
 * <p>Most cases though Gradle build Java libraries/applications, and by default
 * Gradle uses the JVM, that it is running on.</p>
 *
 * <p>This manager separates the JavaRuntime on which Gradle can run from
 * the JavaPlatform on which Gradle Java project can be built. Every JavaPlatform
 * is a JavaRuntime, but not every JavaRuntime is a JavaPlatform way.
 * </p>
 *
 * @author Laszlo Kishalmi
 * @since 2.32
 */
public interface JavaRuntimeManager {

    /**
     * The ID that the Runtime shall use for the Java Runtime the IDE is runnoing on.
     */
    public static String DEFAULT_RUNTIME_ID = "default_platform"; //NOI18N

    /**
     * Return the available runtimes stored in the IDE with their respective Id-s.
     *
     * @return the available runtimes stored in the IDE.
     */
    Map<String, JavaRuntime> getAvailableRuntimes();

    /**
     * This method is called from the Gradle Execution Customizer when
     * the "Manage Runtimes..." button is called.
     *
     * @return can provide a runnable to configure the available runtimes.
     */
    default Optional<Runnable> manageRuntimesAction() {
        return Optional.empty();
    }

    /**
     * Register a change listener to this manager.
     * Implementations shall fire a change event when the available runtimes
     * changed.
     * <p>
     * The default implementation does nothing.
     * </p>
     *
     * @param l the listener to be registered.
     */
    default void addChangeListener(ChangeListener l) {}

    /**
     * Removes a change listener from this manager.
     * <p>
     * The default implementation does nothing.
     * </p>
     * @param l the listener to be removed.
     */
    default void removeChangeListener(ChangeListener l) {}

    public static JavaRuntime createJavaRuntime(@NonNull String id, File javaHome) {
        return new JavaRuntime(id, null, javaHome);
    }

    public static JavaRuntime createJavaRuntime(@NonNull String id, String displayName, File javaHome) {
        return new JavaRuntime(id, displayName, javaHome);
    }

    /**
     * This class represents a java home directory with and id,
     * and an optional display name.
     *
     * @since 2.32
     */
    public final class JavaRuntime implements Comparable<JavaRuntime> {
        final String id;
        final String displayName;
        final File javaHome;

        private JavaRuntime(@NonNull String id, String displayName, File javaHome) {
            this.id = id;
            this.displayName = displayName != null ? displayName : id;
            this.javaHome = javaHome;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * The Java home directory associated with the id. This can be null or
         * non existent directory, that would represent a broken JavaRuntime.
         *
         * @return the Java home directory of this runtime.
         */
        public File getJavaHome() {
            return javaHome;
        }

        /**
         * Returns {@code true} if the Java home of this runtime is {@code null}
         * or points to a non-existing directory.
         *
         * @return {@code true} if this runtime should not be used.
         */
        public boolean isBroken() {
            return javaHome == null || !javaHome.isDirectory();
        }

        @Override
        public int compareTo(JavaRuntime o) {
            return getDisplayName().compareToIgnoreCase(o.getDisplayName());
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JavaRuntime other = (JavaRuntime) obj;
            return Objects.equals(this.id, other.id);
        }


    }
}
