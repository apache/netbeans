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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.nbimpl.javac.ElementUtilitiesEx;
import org.netbeans.modules.profiler.nbimpl.javac.JavacClassInfo;
import org.netbeans.modules.profiler.nbimpl.javac.JavacMethodInfo;
import org.netbeans.modules.profiler.nbimpl.javac.ParsingUtils;
import org.netbeans.modules.profiler.nbimpl.javac.ScanSensitiveTask;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
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
    "ProfileElementNavigatorAction_Name=Profile",
    "ProfileElementNavigatorAction_ProblemProfilingElement=Cannot profile selected element. This can happen for items placed in directory other than project sources directory (typically /src and /web). If so, please select appropriate item in project sources directory."
})
@ActionID(id = "org.netbeans.modules.profiler.nbimpl.actions.ProfileElementNavigatorAction", category = "Profile")
@ActionRegistration(displayName = "#ProfileElementNavigatorAction_Name", lazy = false, asynchronous = false)
@ActionReferences({
    @ActionReference(path = "Navigator/Actions/Members/text/x-java", position = 100),
    @ActionReference(path = "Navigator/Actions/Hierarchy/text/x-java", position = 100)
})
public final class ProfileElementNavigatorAction extends NodeAction {
    
    private String name = Bundle.ProfileElementNavigatorAction_Name();
    
    public String getName() { return name; }
    
    protected boolean enable(Node[] activatedNodes) {
        ElementHandle eh = getHandle(activatedNodes);
        ElementKind kind = eh == null ? null : eh.getKind();
        
        if (ElementKind.METHOD.equals(kind) || ElementKind.CONSTRUCTOR.equals(kind)) {
            name = Bundle.ProfileMethodEditorAction_Name();
            return true;
        }
        
        if (ElementKind.CLASS.equals(kind)) {
            name = Bundle.ProfileClassEditorAction_Name();
            return true;
        }
        
        name = Bundle.ProfileElementNavigatorAction_Name();
        return false;
    }

    
    protected void performAction(final Node[] nodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    final ElementHandle eh = getHandle(nodes);
                    if (eh == null) return;

                    FileObject fo = nodes[0].getLookup().lookup(FileObject.class);
                    if (fo == null) return;

                    Lookup.Provider project = ProjectUtilities.getProject(fo);
                    if (project == null) return;

                    JavaSource src = ElementUtilitiesEx.getSources((Project)project);
                    if (src == null) return;

                    Object sourceInfo = null;
                    ElementKind kind = eh.getKind();
                    if (ElementKind.METHOD.equals(kind) || ElementKind.CONSTRUCTOR.equals(kind)) {
                        sourceInfo = getMethod(eh, src.getClasspathInfo());
                    } else if (ElementKind.CLASS.equals(kind)) {
                        sourceInfo = new JavacClassInfo(eh, src.getClasspathInfo());
                    }

                    if (sourceInfo != null)
                        ProfilerSession.findAndConfigure(Lookups.fixed(sourceInfo), project, getName());
                } catch (Exception ex) {
                    ProfilerDialogs.displayWarning(Bundle.ProfileElementNavigatorAction_ProblemProfilingElement());
                }
            }
        });
    }
    
    private static ElementHandle getHandle(Node[] nodes) {
        if (nodes.length != 1) return null;
        TreePathHandle tph = nodes[0].getLookup().lookup(TreePathHandle.class);
        return tph == null ? null : tph.getElementHandle();
    }
    
    private static JavacMethodInfo getMethod(final ElementHandle eh, ClasspathInfo ci) {
        final JavacMethodInfo[] info = new JavacMethodInfo[1];
        
        ParsingUtils.invokeScanSensitiveTask(ci, new ScanSensitiveTask<CompilationController>() {
            public void run(CompilationController cc) throws Exception {
                Element el = eh.resolve(cc);
                if (el instanceof ExecutableElement) info[0] = new JavacMethodInfo((ExecutableElement)el, cc);
            }
            public boolean shouldRetry() {
                return info[0] == null;
            }
        });
        
        return info[0];
    }


    public HelpCtx getHelpCtx() { return null; }
    
}
