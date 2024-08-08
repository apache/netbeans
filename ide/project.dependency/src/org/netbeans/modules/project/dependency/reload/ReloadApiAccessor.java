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
package org.netbeans.modules.project.dependency.reload;

import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.ProjectState;
import org.netbeans.modules.project.dependency.ProjectReload.Quality;
import org.netbeans.modules.project.dependency.reload.ProjectReloadInternal.StateParts;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
public abstract class ReloadApiAccessor {
    private static volatile ReloadApiAccessor INSTANCE;
    
    public static void set(ReloadApiAccessor a) {
        assert INSTANCE == null;
        INSTANCE = a;
    }
    
    public static ReloadApiAccessor get() {
        return INSTANCE;
    }
    
    public abstract StateParts getParts(ProjectReload.ProjectState state);
    public abstract ProjectReload.ProjectState createState(Project project, long timestamp,  StateParts parts,
            Quality status, Quality target, boolean consistent, boolean valid, Collection<FileObject> loaded, Collection<FileObject> modified, Collection<FileObject> edited, Object track);
    public abstract void updateProjectState(ProjectReload.ProjectState ps, boolean inconsistent, boolean invalid, Collection<FileObject> modified, Collection<FileObject> edited, ProjectState targetQualityFrom);
    public abstract void fireInvalid(ProjectReload.ProjectState ps);
    public abstract void chainPrevious(ProjectReload.ProjectState ps, ProjectReload.ProjectState previous, Collection<ProjectState> collector);
}
