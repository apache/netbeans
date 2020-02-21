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

package org.netbeans.modules.cnd.remote.projectui.wizard.ide;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

/**
 * List of projects open in the GUI.
 */
public final class OpenProjectList {
    private static OpenProjectList INSTANCE = new OpenProjectList();
    
    private OpenProjectList() {
    }
    
           
    // Implementation of the class ---------------------------------------------
    
    public static OpenProjectList getDefault() {
        return INSTANCE;
    }
    
    public void open(final Project[] projects, final boolean openSubprojects, final boolean asynchronously, final Project/*|null*/ mainProject) {
        if (projects.length == 0) {
            //nothing to do:
            return ;
        }
        if (mainProject != null) {
            OpenProjects.getDefault().open(new Project[]{mainProject}, openSubprojects);
        }
        for(Project p : projects) {
            if (!p.equals(mainProject)) {
                OpenProjects.getDefault().open(new Project[]{p}, false);
            }
        }
        if (mainProject != null) {
            OpenProjects.getDefault().setMainProject(mainProject);
        }
    }
}
