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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Refactoring UI object for Move Inner To Outer Level refactoring.
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public class InnerToOuterRefactoringUI implements RefactoringUI, JavaRefactoringUIFactory {
    // reference to pull up refactoring this UI object corresponds to
    private InnerToOuterRefactoring refactoring;
    // UI panel for collecting parameters
    private InnerToOuterPanel panel;
    
    private boolean disableDeclareFields;

    private String className;
    
    /** Creates a new instance of InnerToOuterRefactoringUI
     * @param selectedElements Elements the refactoring action was invoked on.
     */
    private InnerToOuterRefactoringUI(TreePathHandle sourceType, CompilationInfo info) {
        refactoring = new InnerToOuterRefactoring(sourceType);
        refactoring.setReferenceName("outer"); //NOI18N
        Element temp = sourceType.resolveElement(info);
        className = temp.getSimpleName().toString();
        disableDeclareFields = temp.getModifiers().contains(Modifier.STATIC) || temp.getKind() !=ElementKind.CLASS;
    }

    private InnerToOuterRefactoringUI() {
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new InnerToOuterPanel(refactoring, parent, disableDeclareFields);
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
        return NbBundle.getMessage(InnerToOuterAction.class, "DSC_InnerToOuter",className); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(InnerToOuterAction.class, "LBL_InnerToOuter"); // NOI18N
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.InnerToOuterRefactoringUI"); // NOI18N
    }
    
    // --- PRIVATE HELPER METHODS ----------------------------------------------
    
    /** Gets parameters from the refactoring panel and sets them
     * to the refactoring object.
     */
    private void captureParameters() {
        refactoring.setClassName(panel.getClassName());
        refactoring.setReferenceName(panel.getReferenceName());
    }

    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        assert handles.length == 1;
        TreePath resolved = handles[0].resolve(info);
        TreePath enclosing = resolved == null
                ? null
                : JavaRefactoringUtils.findEnclosingClass(info, resolved, true, true, true, true, false);
        if (enclosing != null && enclosing != resolved) {
                handles[0] = TreePathHandle.create(enclosing, info);
        }
        if(handles[0] != null && resolved != null) {
            Element inner = handles[0].resolveElement(info);
            if(inner != null && inner.getKind() != ElementKind.PACKAGE) {
                TypeElement outer = info.getElementUtilities().enclosingTypeElement(inner);
                if (outer != null && outer.getEnclosedElements().contains(inner)) {
                    return new InnerToOuterRefactoringUI(handles[0], info);
                }
            }
        }
        return null;
    }
    
    public static JavaRefactoringUIFactory factory() {
        return new InnerToOuterRefactoringUI();
    }
}
