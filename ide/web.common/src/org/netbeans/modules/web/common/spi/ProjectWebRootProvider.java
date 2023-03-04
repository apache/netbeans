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

package org.netbeans.modules.web.common.spi;

import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 * Provides an ability to get the web root folder for a file
 * within web-like project.
 *
 * Instance of this interface must be registered into project's lookup
 *
 * @author marekfukala
 */
public interface ProjectWebRootProvider {

    /**
     * Finds a web root for a file.
     *
     * @param file The file you wish to find a web root for.
     * @return A web root containing the searched file. The returned web root
     * must contain the searched file. Null is returned if no web root find for
     * the file.
     */
    public FileObject getWebRoot(FileObject file);

    /**
     * Finds all web roots for a project.
     * @return collection of web roots of the given project, can be empty but never {@code null}.
     * @since 1.57
     */
    @NonNull
    Collection<FileObject> getWebRoots();

}
