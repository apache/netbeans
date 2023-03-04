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
package org.netbeans.modules.web.clientproject.spi.build;

import org.netbeans.api.annotations.common.NonNull;

/**
 * Interface for build tool.
 * <p>
 * Implementations are expected to be found in project's lookup.
 * @since 1.81
 */
public interface BuildToolImplementation {

    /**
     * Returns the <b>non-localized (usually english)</b> identifier of this build tool.
     * @return the <b>non-localized (usually english)</b> identifier; never {@code null}.
     */
    @NonNull
    String getIdentifier();

    /**
     * Returns the display name of this build tool. The display name is used
     * in the UI.
     * @return the display name; never {@code null}
     */
    @NonNull
    String getDisplayName();

    /**
     * Checks whether this build tool supports the current project.
     * @return {@code true} if this build tool supports the current project, {@code false} otherwise
     * @since 1.82
     */
    boolean isEnabled();

    /**
     * Run "build" for the given command identifier.
     * <p>
     * This method is called only if this build tool is {@link #isEnabled() enabled} in the current project.
     * @param commandId command identifier
     * @param waitFinished wait till the command finishes?
     * @param warnUser warn user (show dialog, customizer) if any problem occurs (e.g. command is not known/set to this build tool)
     * @return {@code true} if command was run, {@code false} otherwise
     */
    boolean run(@NonNull String commandId, boolean waitFinished, boolean warnUser);

}
