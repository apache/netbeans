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

package org.netbeans.modules.generic.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.DirectoryProjectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Promotion SPI for the "New Group" (directory) flow and other usecase which
 * require a project directory.
 * 
 * The stock group machinery (projectui) asks the default lookup for this
 * service when a directory group is created, and turns the
 * directory into a local generic project.
 */
@ServiceProvider(service=DirectoryProjectProvider.class)
public final class GpDirectoryProjectProvider implements DirectoryProjectProvider {

    @Override
    public Project createProject(FileObject dir) throws IOException {
        return GenericProject.promoteToProject(dir);
    }
}
