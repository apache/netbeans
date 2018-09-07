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
package org.netbeans.modules.java.source.remoteapi.project;

import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registration;
import org.openide.util.Lookup;

/**
 *
 * @author lahvac
 */
@Registration(projectType="org-netbeans-modules-java-j2seproject", position=10000)
public class ProjectCustomizerImpl implements CompositeCategoryProvider {

    private static final String KEY_USE_REMOTE = "use.remote.java.patform";

    public static Preferences getPreferences(Project prj) {
        return ProjectUtils.getPreferences(prj, LookupProviderImpl.class, false);
    }

    public static boolean isRemotingEnabled(Preferences prefs) {
        return prefs.getBoolean(KEY_USE_REMOTE, false);
    }

    @Override
    public ProjectCustomizer.Category createCategory(Lookup lkp) {
        return ProjectCustomizer.Category.create("remoting", "Remoting", null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category cat, Lookup lkp) {
        Project prj = lkp.lookup(Project.class);
        Preferences prefs = getPreferences(prj);
        RemotePlatformSetting panel = new RemotePlatformSetting();
        panel.setEnableRemote(isRemotingEnabled(prefs));
        boolean[] value = new boolean[1];
        cat.setOkButtonListener(evt -> value[0] = panel.getEnableRemote());
        cat.setStoreListener(evt -> prefs.putBoolean(KEY_USE_REMOTE, value[0]));
        return panel;
    }

}
