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

package org.netbeans.modules.cnd.modelui.trace;

import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelui.actions.ProjectActionBase;

/**
 * A common abstract parent for  test actions on projects
 */
public abstract class TestProjectActionBase extends ProjectActionBase {

    protected final static boolean TEST_XREF = Boolean.getBoolean("test.xref.action"); // NOI18N

    public TestProjectActionBase() {
        super(TEST_XREF);
    }
    
    protected final Preferences getProjectPrefs(CsmProject p) {
        Preferences out = null;
        Object project = p.getPlatformProject();
        if (project instanceof NativeProject) {
            Object prj = ((NativeProject) project).getProject();
            if (prj instanceof Project) {
                out = ProjectUtils.getPreferences((Project) prj, TestProjectActionBase.class, false);
            }
        }
        return out;
    }
}
