/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

    final private String command;

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
