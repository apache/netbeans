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
package org.netbeans.modules.gradle.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import static org.netbeans.modules.gradle.actions.Bundle.*;
import org.netbeans.modules.gradle.api.GradleProjects;

/**
 *
 * @author lkishalmi
 */
@SuppressWarnings(value = "serial")
@ActionID(id = "org.netbeans.modules.maven.refresh", category = "Project")
@ActionRegistration(displayName = "#ACT_Reload_Project", lazy=false)
@ActionReference(position = 1700, path = "Projects/" + NbGradleProject.GRADLE_PROJECT_TYPE + "/Actions")
@NbBundle.Messages("ACT_Reload_Project=Reload Project")
public class ReloadAction  extends AbstractAction implements ContextAwareAction {

    private final Lookup context;
    public ReloadAction() {
        this(Lookup.EMPTY);
    }

    @NbBundle.Messages({"# {0} - count", "ACT_Reload_Projects=Reload {0} Projects"})
    private ReloadAction(Lookup lkp) {
        context = lkp;
        Collection<? extends NbGradleProjectImpl> col = context.lookupAll(NbGradleProjectImpl.class);
        if (col.size() > 1) {
            putValue(Action.NAME, ACT_Reload_Projects(col.size()));
        } else {
            putValue(Action.NAME, ACT_Reload_Project());
        }
    }

    @Override public void actionPerformed(ActionEvent event) {
        Set<Project> reload = new LinkedHashSet<>();
        for (NbGradleProjectImpl prj : context.lookupAll(NbGradleProjectImpl.class)) {
            reload.add(prj);
            reload.addAll(GradleProjects.openedProjectDependencies(prj).values());
        }
        for (Project project : reload) {
            NbGradleProject.fireGradleProjectReload(project);
        }
    }

    @Override public Action createContextAwareInstance(Lookup actionContext) {
        return new ReloadAction(actionContext);
    }


}
