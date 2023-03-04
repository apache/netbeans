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
package org.netbeans.modules.java.api.common.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;

/**
 * A provider of project's active {@link JavaPlatform}.
 * Allows client to obtain and set the active project platform
 * @author Tomas Zezula
 * @since 1.111
 */
public interface ProjectPlatformProvider {
    /**
     * Name of the "projectPlatform" property.
     */
    String PROP_PROJECT_PLATFORM = "projectPlatform";   //NOI18N

    /**
     * Return the active project platform.
     * @return the active {@link JavaPlatform} or null if the
     * active platform cannot be resolved (it's broken)
     */
    @CheckForNull
    JavaPlatform getProjectPlatform();

    /**
     * Sets active project platform.
     * @param platform the platform to become active project active platform
     * @throws IOException in case of IO error.
     * @throws IllegalArgumentException if the platform is not a valid platform supported by the project type.
     */
    void setProjectPlatform(@NonNull JavaPlatform platform) throws IOException;

    /**
     * Adds {@link PropertyChangeListener} for listening on project platform changes.
     * @param listener the listener to be added
     */
    void addPropertyChangeListener(@NonNull PropertyChangeListener listener);

    /**
     * Removes {@link PropertyChangeListener} for listening on project platform changes.
     * @param listener the listener to be removed
     */
    void removePropertyChangeListener(@NonNull PropertyChangeListener listener);
}
