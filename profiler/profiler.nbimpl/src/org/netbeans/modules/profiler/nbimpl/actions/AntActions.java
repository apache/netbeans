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
package org.netbeans.modules.profiler.nbimpl.actions;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.Action;

import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@oracle.com>
 */
public class AntActions {
    @Messages({
        "# {0} - # of selected projects (0 if disabled), or -1 if main project", 
        "# {1} - project name, if exactly one project", 
//        "LBL_ProfileMainProjectAction=&Profile {0,choice,-1#Main Project|0#Project|1#Project ({1})|1<{0} Projects}" // #231371
        "LBL_ProfileMainProjectAction=&Profile {0,choice,-1#Main Project|0#Project|1#Project ({1})|1<Project}"
    })
    @ActionID(category="Profile", id="org.netbeans.modules.profiler.actions.ProfileMainProject")
    @ActionRegistration(displayName="#LBL_ProfileMainProjectAction", lazy=false)
    @ActionReferences({
        @ActionReference(path="Menu/Profile", position=100),
        @ActionReference(path="Shortcuts", name="C-F2")
    })
    public static Action profileMainProjectAction() {
        Action ref = mainProjectA == null ? null : mainProjectA.get();
        if (ref != null) return ref;

        Action delegate = MainProjectSensitiveActions.mainProjectSensitiveAction(
                ProjectSensitivePerformer.profileProject(ActionProvider.COMMAND_PROFILE), 
                NbBundle.getMessage(AntActions.class, "LBL_ProfileMainProjectAction"), // NOI18N
                Icons.getIcon(ProfilerIcons.PROFILE)
        );
        delegate.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(AntActions.class, "HINT_ProfileMainProjectAction")); // NOI18N
        delegate.putValue("iconBase", Icons.getResource(ProfilerIcons.PROFILE)); // NOI18N
        
        mainProjectA = new WeakReference(delegate);
        return delegate;
    }
    private static Reference<Action> mainProjectA;
    
    @Messages({
        "LBL_ProfileProject=Profile"
    })
    @ActionID(category="Profile", id="org.netbeans.modules.profiler.actions.ProfileProjectPopup")
    @ActionRegistration(displayName="#LBL_ProfileProject", lazy=false, asynchronous=true)
    @ActionReferences({
        @ActionReference(path="Projects/org-netbeans-modules-java-j2seproject/Actions", position=1000),
        @ActionReference(path="Projects/org-netbeans-modules-java-j2semodule/Actions", position=1000),
        @ActionReference(path="Projects/org-netbeans-modules-apisupport-project/Actions", position=900),
        @ActionReference(path="Projects/org-netbeans-modules-apisupport-project-suite/Actions", position=1000),
        @ActionReference(path="Projects/org-netbeans-modules-web-project/Actions", position=1000)
    })
    public static Action profileProjectPopup() {
        Action delegate = ProjectSensitiveActions.projectSensitiveAction(
                ProjectSensitivePerformer.profileProject(ActionProvider.COMMAND_PROFILE), 
                NbBundle.getMessage(AntActions.class, "LBL_ProfileProject"), // NOI18N
                null
        );
        
        return delegate;
    }
    
    @ActionID(category="Project", id="org.netbeans.modules.apisupport.project.suite.ProfileOsgi")
    @ActionRegistration(displayName="#SUITE_ACTION_profile_osgi", asynchronous=true)
    @ActionReference(path="Projects/org-netbeans-modules-apisupport-project-suite-osgi/Actions", position=500)
    @NbBundle.Messages("SUITE_ACTION_profile_osgi=Profile in Felix")
    public static Action profileOsgi() {
        Action delegate = ProjectSensitiveActions.projectSensitiveAction(
                ProjectSensitivePerformer.profileProject("profile-osgi"), 
                Bundle.SUITE_ACTION_profile_osgi(), 
                null
        );
        
        return delegate;
    }
    
    @Messages("LBL_ProfileFile=Profile &File")
    @ActionID(category="Profile", id="org.netbeans.modules.profiler.actions.ProfileSingle")
    @ActionRegistration(displayName="#LBL_ProfileFile", lazy=false)
    @ActionReferences({
        @ActionReference(path="Loaders/text/x-java/Actions", position=1200),
        @ActionReference(path="Loaders/text/x-jsp/Actions", position=800),
        @ActionReference(path="Menu/Profile", position=110)
    })
    public static Action profileSingle() {
        Action delegate = FileSensitiveActions.fileSensitiveAction(
                new FileSensitivePerformer(ActionProvider.COMMAND_PROFILE_SINGLE),  
                Bundle.LBL_ProfileFile(),
                null);
        
        return delegate;
    }
        
    @Messages("LBL_ProfileTest=Prof&ile Test File")
    @ActionID(category = "Profile", id = "org.netbeans.modules.profiler.actions.ProfileTest")
    @ActionRegistration(displayName = "#LBL_ProfileTest", lazy=false)
    @ActionReferences(value = {
        @ActionReference(path = "Loaders/text/x-java/Actions", position = 1280),
        @ActionReference(path = "Menu/Profile", position = 120)})
    public static Action profileTest() {
        return FileSensitiveActions.fileSensitiveAction(
                new FileSensitivePerformer(ActionProvider.COMMAND_PROFILE_TEST_SINGLE), 
                Bundle.LBL_ProfileTest(),
                null);
    }
    
    @Messages({
        "# {0} - # of selected projects (0 if disabled), or -1 if main project", 
        "# {1} - project name, if exactly one project", 
//        "LBL_ProfileMainProjectAction=&Profile {0,choice,-1#Main Project|0#Project|1#Project ({1})|1<{0} Projects}" // #231371
        "LBL_AttachMainProjectAction=&Attach to {0,choice,-1#Main Project|0#Project|1#Project ({1})|1<Project}"
    })
    @ActionID(category="Profile", id="org.netbeans.modules.profiler.actions.AttachMainProject")
    @ActionRegistration(displayName="#LBL_AttachMainProjectAction", lazy=false)
    @ActionReferences({
        @ActionReference(path="Menu/Profile", position=125)
    })
    public static Action attachMainProjectAction() {
        Action delegate = ProjectSensitiveActions.projectSensitiveAction(
                ProjectSensitivePerformer.attachProject(), 
                NbBundle.getMessage(AntActions.class, "LBL_AttachMainProjectAction"), // NOI18N
                Icons.getIcon(ProfilerIcons.ATTACH)
        );
        delegate.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(AntActions.class, "LBL_AttachMainProjectAction")); // NOI18N
        delegate.putValue("iconBase", Icons.getResource(ProfilerIcons.ATTACH)); // NOI18N
        
        return delegate;
    }
}
