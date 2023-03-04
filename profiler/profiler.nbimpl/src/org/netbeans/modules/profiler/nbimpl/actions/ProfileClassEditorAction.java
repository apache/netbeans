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

import org.netbeans.modules.profiler.api.EditorSupport;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.api.java.ProfilerTypeUtils;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProfileClassEditorAction_Name=Profile Class",
    "ProfileClassEditorAction_NoClassFoundAtPosition=No class found at current position.",
    "ProfileClassEditorAction_ProblemProfilingClass=Cannot profile selected class. This can happen for items placed in directory other than project sources directory (typically /src and /web). If so, please select appropriate item in project sources directory."
})
@ActionID(id = "org.netbeans.modules.profiler.nbimpl.actions.ProfileClassEditorAction", category = "Profile")
@ActionRegistration(displayName = "#ProfileClassEditorAction_Name", lazy = true, asynchronous = false)
@ActionReference(path = "Editors/text/x-java/Popup/Profile", position = 101)
public final class ProfileClassEditorAction extends NodeAction {
    
    public String getName() { return Bundle.ProfileClassEditorAction_Name(); }
    
    protected boolean enable(Node[] activatedNodes) { return true; }

    
    protected void performAction(final Node[] nodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    // Resolve DataObject
                    DataObject dobj = (DataObject) nodes[0].getLookup().lookup(DataObject.class);
                    if (dobj == null) return;

                    // Resolve Java source
                    JavaProfilerSource src = JavaProfilerSource.createFrom(dobj.getPrimaryFile());
                    if (src == null) return;

                    // Resolve current offset in editor
                    int currentOffsetInEditor = EditorSupport.getCurrentOffset();
                    if (currentOffsetInEditor == -1) return;
                    
                    Lookup.Provider project = null;

                    // Resolve class at cursor
                    SourceClassInfo resolvedClass = src.resolveClassAtPosition(currentOffsetInEditor, true);
                    if (resolvedClass == null) {
                        // Resolve method at cursor
                        SourceMethodInfo resolvedMethod = src.resolveMethodAtPosition(currentOffsetInEditor);
                        if (resolvedMethod != null) {
                            project = ProjectUtilities.getProject(dobj.getPrimaryFile());
                            resolvedClass = ProfilerTypeUtils.resolveClass(resolvedMethod.getClassName(), project);
                        }
                        
                        if (resolvedClass == null) {
                            ProfilerDialogs.displayWarning(Bundle.ProfileClassEditorAction_NoClassFoundAtPosition());
                            return;
                        }
                    }
                    
                    // Resolve owner project
                    if (project == null) project = ProjectUtilities.getProject(dobj.getPrimaryFile());
                    
                    // Let the ProfilerSession handle the root class
                    ProfilerSession.findAndConfigure(Lookups.fixed(resolvedClass), project, getName());
                } catch (Exception ex) {
                    ProfilerDialogs.displayWarning(Bundle.ProfileClassEditorAction_ProblemProfilingClass());
                }
            }
        });
    }


    public HelpCtx getHelpCtx() { return null; }
    
}
