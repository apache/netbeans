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

import java.util.EventListener;
import org.netbeans.api.annotations.common.NonNull;

/**
 * listeners that get notified when project group is changed.
 * added and removed from <code>OpenProjects</code>
 * @author mkleint
 * @since 1.61
 */
public interface ProjectGroupChangeListener extends EventListener {
    
    /**
     * called when the process of changing from old to new project group has started. Will be called before
     * the actual projects from old group get closed and the ones from new group get opened.
     * @param event 
     */
    void projectGroupChanging(@NonNull ProjectGroupChangeEvent event);
        
    /**
     * called when the process of changing from old to new project group has been completed. Only projects 
     * related to current group should be open now, or projects explicitly opened by the user.
     * @param event 
     */
    void projectGroupChanged(@NonNull ProjectGroupChangeEvent event);
    
}
