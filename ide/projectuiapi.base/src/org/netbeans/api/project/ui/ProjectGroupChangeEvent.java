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
package org.netbeans.api.project.ui;

import java.util.EventObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;


/**
 * event describing the change of active project group by the user.
 * @author mkleint
 * @since 1.61
 */
public final class ProjectGroupChangeEvent extends EventObject {
    private final ProjectGroup newGroup;
    private final ProjectGroup oldGroup;
    
    public ProjectGroupChangeEvent(@NullAllowed ProjectGroup o, @NullAllowed ProjectGroup n) {
        super(OpenProjects.getDefault());
        this.oldGroup = o;
        this.newGroup = n;
    }

    /**
     * the newly current project group, can be null
     * @return 
     */
    public @CheckForNull ProjectGroup getNewGroup() {
        return newGroup;
    }

    /**
     * the previous active project group, can be null
     * @return 
     */
    public @CheckForNull ProjectGroup getOldGroup() {
        return oldGroup;
    }
}

