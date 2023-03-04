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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.util.TreePath;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PushDownRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** Refactoring UI object for Push Down refactoring.
 *
 * @author Pavel Flaska, Jan Becicka
 */
public class PushDownRefactoringUI implements RefactoringUI, JavaRefactoringUIFactory {
    // reference to pull up refactoring this UI object corresponds to
    private PushDownRefactoring refactoring;
    // initially selected members
    private Set initialMembers;
    // UI panel for collecting parameters
    private PushDownPanel panel;
    
    private String description;
    
    /** Creates a new instance of PushDownRefactoringUI
     * @param selectedElements Elements the refactoring action was invoked on.
     */
    private PushDownRefactoringUI(TreePathHandle selectedElements, CompilationInfo info) {
        initialMembers = new HashSet();
        TreePathHandle selectedPath = resolveSelection(selectedElements, info);

        if (selectedPath != null) {
            Element selected = selectedPath.resolveElement(info);
            initialMembers.add(MemberInfo.create(selected, info));
            // compute source type and members that should be pre-selected from the
            // set of elements the action was invoked on

           // create an instance of push down refactoring object
            if (!(selected instanceof TypeElement)) {
                selected = info.getElementUtilities().enclosingTypeElement(selected);
            }
            TreePath tp = info.getTrees().getPath(selected);
            if(tp != null) {
                TreePathHandle sourceType = TreePathHandle.create(tp, info);
                description = ElementHeaders.getHeader(tp, info, ElementHeaders.NAME);
                refactoring = new PushDownRefactoring(sourceType);
                refactoring.getContext().add(RefactoringUtils.getClasspathInfoFor(sourceType));
            } else {
                // put the unresolvable selection to refactoring,
                // user notification is provided by PushDownRefactoringPlugin.preCheck
                refactoring = new PushDownRefactoring(selectedElements);
            }
        } else {
            // put the unresolvable selection to refactoring,
            // user notification is provided by PushDownRefactoringPlugin.preCheck
            refactoring = new PushDownRefactoring(selectedElements);
        }
    }

    private PushDownRefactoringUI() {
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new PushDownPanel(refactoring, initialMembers, parent);
        }
        return panel;
    }

    @Override
    public Problem setParameters() {
        captureParameters();
        return refactoring.checkParameters();
    }
    
    @Override
    public Problem checkParameters() {
        captureParameters();
        return refactoring.fastCheckParameters();
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(PushDownRefactoringUI.class, "DSC_PushDown", description); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(PushDownRefactoringUI.class, "LBL_PushDown"); // NOI18N
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.PushDownRefactoringUI"); // NOI18N
    }
    
    // --- PRIVATE HELPER METHODS ----------------------------------------------
    
    /** Gets parameters from the refactoring panel and sets them
     * to the refactoring object.
     */
    private void captureParameters() {
        refactoring.setMembers(panel.getMembers());
    }

    static TreePathHandle resolveSelection(TreePathHandle source, CompilationInfo javac) {
        TreePath resolvedPath = source.resolve(javac);
        TreePath path = resolvedPath;
        Element resolvedElement = source.resolveElement(javac);
        while (path != null && resolvedElement == null) {
            path = path.getParentPath();
            if (path == null) {
                return null;
            }
            resolvedElement = javac.getTrees().getElement(path);
        }

        return path == resolvedPath ? source : TreePathHandle.create(path, javac);
    }

    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        assert handles.length == 1;
        TreePathHandle selectedElement = PullUpRefactoringUI.findSelectedClassMemberDeclaration(handles[0], info);
                    return selectedElement != null
                            ? new PushDownRefactoringUI(selectedElement, info)
                            : null;
    }
    
    public static JavaRefactoringUIFactory factory() {
        return new PushDownRefactoringUI();
    }

}
