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

import java.net.URI;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Knowledge of which project some files belong to.
 * <p>An implementation must attempt to return a result quickly and avoid
 * blocking on foreign locks. In particular, it should not call {@code OpenProjects}.
 * @see org.netbeans.api.project.FileOwnerQuery
 * @author Jesse Glick
 */
public interface FileOwnerQueryImplementation {

    /**
     * Decide which project, if any, "owns" a given file.
     * @param file an absolute URI to some file (typically on disk; need not currently exist)
     * @return a project which owns it, or null for no response
     */
    Project getOwner(URI file);

    /**
     * Decide which project, if any, "owns" a given file.
     * @param file FileObject of an existing file
     * @return a project which owns it, or null for no response
     */
    Project getOwner(FileObject file);

}
