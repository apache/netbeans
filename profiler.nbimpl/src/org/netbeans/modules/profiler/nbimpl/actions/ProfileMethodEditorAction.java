/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
