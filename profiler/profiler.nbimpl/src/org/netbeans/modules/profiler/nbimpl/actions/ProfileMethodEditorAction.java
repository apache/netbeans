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
    "ProfileMethodEditorAction_Name=Profile Method",
    "ProfileMethodEditorAction_NoMethodFoundAtPosition=No method found at current position.",
    "ProfileMethodEditorAction_CannotAddAbstractNativeProfilingRoot=Cannot profile abstract or native method.",
    "ProfileMethodEditorAction_ProblemProfilingMethod=Cannot profile selected method. This can happen for items placed in directory other than project sources directory (typically /src and /web). If so, please select appropriate item in project sources directory."
})
@ActionID(id = "org.netbeans.modules.profiler.nbimpl.actions.ProfileMethodEditorAction", category = "Profile")
@ActionRegistration(displayName = "#ProfileMethodEditorAction_Name", lazy = true, asynchronous = false)
@ActionReference(path = "Editors/text/x-java/Popup/Profile", position = 100)
public final class ProfileMethodEditorAction extends NodeAction {
    
    public String getName() { return Bundle.ProfileMethodEditorAction_Name(); }
    
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

                    // Resolve method at cursor
                    SourceMethodInfo resolvedMethod = src.resolveMethodAtPosition(currentOffsetInEditor);
                    if (resolvedMethod == null) {
                        ProfilerDialogs.displayWarning(Bundle.ProfileMethodEditorAction_NoMethodFoundAtPosition());
                        return;
                    }

                    // Filter out abstract & native methods
                    if (!resolvedMethod.isExecutable()) {
                        ProfilerDialogs.displayInfo(Bundle.ProfileMethodEditorAction_CannotAddAbstractNativeProfilingRoot());
                        return;
                    }
                    
                    // Resolve owner project
                    Lookup.Provider project = ProjectUtilities.getProject(dobj.getPrimaryFile());
                    
                    // Let the ProfilerSession handle the root method
                    ProfilerSession.findAndConfigure(Lookups.fixed(resolvedMethod), project, getName());
                } catch (Exception ex) {
                    ProfilerDialogs.displayWarning(Bundle.ProfileMethodEditorAction_ProblemProfilingMethod());
                }
            }
        });
    }


    public HelpCtx getHelpCtx() { return null; }
    
}
