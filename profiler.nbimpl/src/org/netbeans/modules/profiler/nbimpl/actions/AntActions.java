/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
