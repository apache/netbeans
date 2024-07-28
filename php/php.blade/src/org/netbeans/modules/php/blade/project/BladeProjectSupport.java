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
package org.netbeans.modules.php.blade.project;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
/**
 *
 * @author bhaidu
 */
public class BladeProjectSupport extends ProjectOpenedHook implements PreferenceChangeListener{
    private final Project project;
        
    public BladeProjectSupport(Project project) {
        assert project != null;
        this.project = project;
    }

    private static BladeProjectSupport create(Project project) {
        BladeProjectSupport support = new BladeProjectSupport(project);
        return support;
    }
    
    @ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = "org-netbeans-modules-php-blade-project") // NOI18N
    public static BladeProjectSupport forBladeProject(Project project) {
        return create(project);
    }
    
    @ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = "org-netbeans-modules-php-project") // NOI18N
    public static BladeProjectSupport forPhpProject(Project project) {
        return create(project);
    }
    
    @ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = "org-netbeans-modules-web-project") // NOI18N
    public static BladeProjectSupport forWebProject(Project project) {
        return create(project);
    }
    
    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        //
    }

    @Override
    protected void projectOpened() {
        BladeProjectProperties.getInstance(project);
        ComponentsSupport.getInstance(project);
    }

    @Override
    protected void projectClosed() {
        BladeProjectProperties.closeProject(project);
    }
    
}
