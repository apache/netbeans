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
    
    private final String command;
    private final boolean attach;
    
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
