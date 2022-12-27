/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

/**
 *
 * @author Laszlo Kishalmi
 */
public interface JavaRuntimeManager {

    public static String DEFAULT_RUNTIME_ID = "default_platform"; //NOI18N
    
    Map<String, JavaRuntime> getAvailableRuntimes();

    default Optional<Runnable> manageRuntimesAction() {
        return Optional.empty();
    }

    default void addChangeListener(ChangeListener l) {}
    default void removeChangeListener(ChangeListener l) {}

    public static JavaRuntime createJavaRuntime(String id, File javaHome) {
        return new JavaRuntime(id, null, javaHome);
    }

    public static JavaRuntime createJavaRuntime(String id, String displayName, File javaHome) {
        return new JavaRuntime(id, displayName, javaHome);
    }

    public final class JavaRuntime implements Comparable<JavaRuntime> {
        final String id;
        final String displayName;
        final File javaHome;

        private JavaRuntime(String id, String displayName, File javaHome) {
            this.id = id;
            this.displayName = displayName;
            this.javaHome = javaHome;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName != null ? displayName : id;
        }

        public File getJavaHome() {
            return javaHome;
        }

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
