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

import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.api.project.ProjectProfilingSupport;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@oracle.com>
 * @author Jiri Sedlacek
 */
public class ProjectSensitivePerformer implements ProjectActionPerformer {
    
    final private String command;
    final private boolean attach;
    
    public static ProjectSensitivePerformer profileProject(String command) {
        return new ProjectSensitivePerformer(command, false);
    }
    
    public static ProjectSensitivePerformer attachProject() {
        return new ProjectSensitivePerformer(null, true);
    }
    
    private ProjectSensitivePerformer(String command, boolean attach) {
        this.command = command;
        this.attach = attach;
    }
    
    
    static boolean supportsProfileProject(String command, Project project) {
        if (project == null) return false;
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        try {
            if (ap != null && contains(ap.getSupportedActions(), command)) {
                ProjectProfilingSupport ppp = ProjectProfilingSupport.get(project);
                return ppp.isProfilingSupported() && ap.isActionEnabled(command, project.getLookup());
            }
        } catch (IllegalArgumentException e) {
            // command not supported
        }
        return false;
    }
    
    static boolean supportsAttachProject(Project project) {
        if (project == null) return false;
        
        ProjectProfilingSupport ppp = ProjectProfilingSupport.get(project);
        return ppp.isAttachSupported();
    }
    
    @Override
    public boolean enable(Project project) {
        if (attach) return supportsAttachProject(project);
        else return supportsProfileProject(command, project);
    }

    @Override
    public void perform(final Project project) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectProfilingSupport ppp = ProjectProfilingSupport.get(project);
                if (!ppp.checkProjectCanBeProfiled(null)) return; // Project not configured, user notified by ProjectProfilingSupportProvider 
                if (ppp.startProfilingSession(null, false)) return; // Profiling session started by ProjectProfilingSupportProvider
                
                ProfilerSession session;
        
                Lookup projectLookup = project.getLookup();
                if (attach) {
                    Lookup context = new ProxyLookup(projectLookup, Lookups.fixed(project));
                    session = ProfilerSession.forContext(context);
                } else {
//                    ActionProvider ap = projectLookup.lookup(ActionProvider.class); // Let's assume this is handled by enable(Project)
//                    if (ap != null) {
                        ProfilerLauncher.Command _command = new ProfilerLauncher.Command(command);
                        Lookup context = new ProxyLookup(projectLookup, Lookups.fixed(project, _command));
                        session = ProfilerSession.forContext(context);
//                    }
                }

                if (session != null) {
                    session.setAttach(attach);
                    session.open();
                }
            }
        });
    }
    
    private static boolean contains(String[] actions, String action) {
        for(String a : actions) if (action.equals(a)) return true;
        return false;
    }
}
