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

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.api.project.ProjectProfilingSupport;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileActionPerformer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Bachorik
 * @author Jiri Sedlacek
 */
public class FileSensitivePerformer implements FileActionPerformer {

    private final String command;

    public FileSensitivePerformer(String command) {
        this.command = command;
    }
    
    static boolean supportsProfileFile(String command, FileObject file) {
        Project p = file == null ? null : FileOwnerQuery.getOwner(file);
        if (p == null) return false;
        
        ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        try {
            if (ap != null && contains(ap.getSupportedActions(), command)) {
                ProjectProfilingSupport ppp = ProjectProfilingSupport.get(p);
                return ppp.isProfilingSupported() && ppp.isFileObjectSupported(file) &&
                       ap.isActionEnabled(command, getContext(file, p, command));
            }
        } catch (IllegalArgumentException e) {
            // command not supported
        }
        return false;
    }

    @Override
    public boolean enable(FileObject file) {
        return supportsProfileFile(command, file);
    }

    @Override
    public void perform(final FileObject file) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Project p = FileOwnerQuery.getOwner(file);
                
                ProjectProfilingSupport ppp = ProjectProfilingSupport.get(p);
                if (!ppp.checkProjectCanBeProfiled(file)) return; // Project not configured, user notified by ProjectProfilingSupportProvider 
                JavaProfilerSource src = JavaProfilerSource.createFrom(file);
                if (ppp.startProfilingSession(file, src == null ? false : src.isTest())) return; // Profiling session started by ProjectProfilingSupportProvider
                
//                ActionProvider ap = p.getLookup().lookup(ActionProvider.class); // Let's assume this is handled by enable(FileObject)
//                if (ap != null) {
                    Lookup context = getContext(file, p, command);
                    ProfilerSession session = ProfilerSession.forContext(context);
                    if (session != null) session.open();
//                }
            }
        });
    }

    private static Lookup getContext(FileObject file, Project p, String command) {
        ProfilerLauncher.Command _command = new ProfilerLauncher.Command(command);
        try {
            return Lookups.fixed(file, p, DataObject.find(file), _command);
        } catch (DataObjectNotFoundException e) {}
        return Lookups.fixed(file, p, _command);
    }
    
    private static boolean contains(String[] actions, String action) {
        for(String a : actions) {
            if (a.equals(action)) return true;
        }
        return false;
    }
}
