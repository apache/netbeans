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
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.ExtractSuperclassRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Refactoring UI object for Extract Super Class refactoring.
 *
 * @author Martin Matula, Jan Pokorsky
 */
public class ExtractSuperclassRefactoringUI implements RefactoringUI {
    // reference to refactoring this UI object corresponds to
    private ExtractSuperclassRefactoring refactoring;
    // source type
    private TreePathHandle sourceType;
    // UI panel for collecting parameters
    private ExtractSuperclassPanel panel;
    private String name;
    private final TreePathHandle[] selected;

    private ExtractSuperclassRefactoringUI(TreePath path, TreePathHandle[] selected, CompilationInfo info) {
        this.name = ElementHeaders.getHeader(path, info, ElementHeaders.NAME);
        this.sourceType = TreePathHandle.create(path, info);
        this.refactoring = new ExtractSuperclassRefactoring(sourceType);
        this.selected = selected;
    }
    
    // --- IMPLEMENTATION OF RefactoringUI INTERFACE ---------------------------
    
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new ExtractSuperclassPanel(refactoring, selected, parent);
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
        return NbBundle.getMessage(ExtractSuperclassAction.class, "DSC_ExtractSC", name); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ExtractSuperclassAction.class, "LBL_ExtractSC"); // NOI18N
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.ExtractSuperclassRefactoringUI"); // NOI18N
    }
    
    // --- PRIVATE HELPER METHODS ----------------------------------------------
    
    /** Gets parameters from the refactoring panel and sets them
     * to the refactoring object.
     */
    private void captureParameters() {
        refactoring.setSuperClassName(panel.getSuperClassName());
        refactoring.setMembers(panel.getMembers());
    }
    
    public static JavaRefactoringUIFactory factory() {
        return new ExtractSuperclassRefactoringUIFactory();
    }
    
    public static class ExtractSuperclassRefactoringUIFactory implements JavaRefactoringUIFactory {

        @Override
        public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
            assert handles.length > 0;
            for (int i = 0; i < handles.length; i++) {
                handles[i] = resolveSelection(handles[i], info);
            }
            TreePath path = handles[0].resolve(info);

            path = JavaRefactoringUtils.findEnclosingClass(info, path, true, false, false, false, false);

            if (path != null) {
                return new ExtractSuperclassRefactoringUI(path, handles, info);
            }

            return null;
        }

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
}
