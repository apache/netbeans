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

import org.netbeans.modules.refactoring.java.api.InvertBooleanRefactoring;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.ui.JavaRefactoringUIFactory;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 * @author Jan Becicka
 */
public class InvertBooleanRefactoringUI implements RefactoringUI, JavaRefactoringUIFactory {

    private String initialName;
    private InvertBooleanRefactoringPanel panel;
    private InvertBooleanRefactoring refactoring;

    private InvertBooleanRefactoringUI() {
    }
    
    private InvertBooleanRefactoringUI(TreePathHandle path, String name) {
        this.refactoring = new InvertBooleanRefactoring(path);
        this.initialName = name;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(InvertBooleanRefactoringUI.class, "InvertBooleanName");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(InvertBooleanRefactoringUI.class, "InvertBooleanDescription", initialName);
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(final ChangeListener parent) {
        if(panel == null)
            panel = new InvertBooleanRefactoringPanel(parent, initialName);
        return panel;
    }

    @Override
    public Problem setParameters() {
        refactoring.setNewName(panel.getNewName());
        return refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
        refactoring.setNewName(panel.getNewName());
        return refactoring.fastCheckParameters();
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(InvertBooleanRefactoringUI.class);
    }

    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        assert handles.length == 1;
        TreePath p = handles[0].resolve(info);

        Set<Tree.Kind> treeKinds = EnumSet.of(
                Tree.Kind.METHOD,
                Tree.Kind.METHOD_INVOCATION,
                Tree.Kind.VARIABLE,
                Tree.Kind.IDENTIFIER);

        while (p != null && !treeKinds.contains(p.getLeaf().getKind())) {
            p = p.getParentPath();
        }
        if (p != null && treeKinds.contains(p.getLeaf().getKind())) {
            Element selected = info.getTrees().getElement(p);
            if (selected == null) {
                return null;
            }
            TreePath selectedTree = info.getTrees().getPath(selected);
            if (selected.getKind().isField() && ((VariableElement) selected).asType().getKind() == TypeKind.BOOLEAN) {
                return new InvertBooleanRefactoringUI(TreePathHandle.create(selectedTree, info), ((VariableElement) selected).getSimpleName().toString());
            }
            if (selected.getKind() == ElementKind.METHOD && ((ExecutableElement) selected).getReturnType().getKind() == TypeKind.BOOLEAN) {
                return new InvertBooleanRefactoringUI(TreePathHandle.create(selectedTree, info), ((ExecutableElement) selected).getSimpleName().toString());
            }
        }
        return null;
    }
    
    public static JavaRefactoringUIFactory factory() {
        return new InvertBooleanRefactoringUI();
    }

}
