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
package org.netbeans.modules.javascript.grunt.ui.navigator;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.grunt.GruntBuildTool;
import org.netbeans.modules.javascript.grunt.GruntBuildToolSupport;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

@NbBundle.Messages({
    "NavigatorPanelSupportImpl.name=Grunt Tasks",
    "NavigatorPanelSupportImpl.hint=Displays tasks in the current Gruntfile.js script.",
})
public class NavigatorPanelSupportImpl implements BuildTools.NavigatorPanelSupport, ChangeListener {

    private static final RequestProcessor RP = new RequestProcessor(NavigatorPanelSupportImpl.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final RequestProcessor.Task task;


    public NavigatorPanelSupportImpl() {
        task = RP.create(new Runnable() {
            @Override
            public void run() {
                fireChange();
            }
        });
    }

    @Override
    public String getDisplayName() {
        return Bundle.NavigatorPanelSupportImpl_name();
    }

    @Override
    public String getDisplayHint() {
        return Bundle.NavigatorPanelSupportImpl_hint();
    }

    @Override
    public BuildTools.BuildToolSupport getBuildToolSupport(FileObject buildFile) {
        Project project = FileOwnerQuery.getOwner(buildFile);
        if (project == null) {
            return null;
        }
        if (GruntBuildTool.inProject(project) == null) {
            return null;
        }
        GruntBuildToolSupport support = new GruntBuildToolSupport(project, buildFile);
        support.addChangeListener(WeakListeners.change(this, support));
        return support;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        task.schedule(2000);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    //~ Factories

    @NavigatorPanel.Registration(mimeType = "text/grunt+javascript", displayName = "#NavigatorPanelSupportImpl.name", position = 100)
    public static NavigatorPanel createNavigatorPanel() {
        return BuildTools.getDefault().createNavigatorPanel(new NavigatorPanelSupportImpl());
    }

}
