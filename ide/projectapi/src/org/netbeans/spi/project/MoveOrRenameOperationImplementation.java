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

package org.netbeans.spi.project;

import java.io.IOException;

/**
 * A preferred substitute for {@code MoveOperationImplementation} to be used when
 * the project can behave more simply, efficiently, and robustly when it is simply
 * being renamed (code and/or display name) without actually being moved to a new location.
 * In this case, {@link #notifyMoving} and {@link #notifyMoved} will not be called.
 * @since org.netbeans.modules.projectapi/1 1.31
 */
public interface MoveOrRenameOperationImplementation extends MoveOperationImplementation {

    /**
     * Pre-rename notification.
     * The exact meaning is left to the project's implementation;
     * it might for example undeploy an application and remove all artifacts
     * created by the build, in case they used the old name.
     * @throws IOException if an I/O operation fails
     */
    void notifyRenaming() throws IOException;

    /**
     * Notification that the rename operation has finished.
     * The project might for example change its display name in metadata.
     * @param nueName new name for the project
     * @throws IOException if an I/O operation fails
     */
    void notifyRenamed(String nueName) throws IOException;

}
