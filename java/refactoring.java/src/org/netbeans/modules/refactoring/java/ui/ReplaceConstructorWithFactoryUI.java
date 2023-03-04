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

import org.netbeans.modules.refactoring.java.api.ReplaceConstructorWithFactoryRefactoring;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
 */
public class ReplaceConstructorWithFactoryUI implements RefactoringUI, JavaRefactoringUIFactory {

    private ReplaceConstructorWithFactoryPanel panel;
    private ReplaceConstructorWithFactoryRefactoring refactoring;
    private String initialName;

    private ReplaceConstructorWithFactoryUI(TreePathHandle constructor, String name) {
        refactoring = new ReplaceConstructorWithFactoryRefactoring(constructor);
        initialName = name;
        
    }

    private ReplaceConstructorWithFactoryUI() {
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ReplaceConstructorWithFactoryUI.class, "ReplaceConstructorName");    
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ReplaceConstructorWithFactoryUI.class, "ReplaceConstructorDescription", initialName ,panel.getFactoryName());    
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(final ChangeListener parent) {
        if (panel == null) {
            panel = new ReplaceConstructorWithFactoryPanel(parent, "create");
        }
        return panel;
    }

    @Override
    public Problem setParameters() {
        refactoring.setFactoryName(panel.getFactoryName());
        return refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
        refactoring.setFactoryName(panel.getFactoryName());
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
        return new HelpCtx(ReplaceConstructorWithFactoryUI.class);
    }

    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        assert handles.length == 1;
        TreePath path = handles[0].resolve(info);

        Set<Tree.Kind> treeKinds = EnumSet.of(
                Tree.Kind.NEW_CLASS,
                Tree.Kind.METHOD);

        while (path != null && !treeKinds.contains(path.getLeaf().getKind())) {
            path = path.getParentPath();
        }
        if (path != null && treeKinds.contains(path.getLeaf().getKind())) {
            Element selected = info.getTrees().getElement(path);
            if (selected != null && selected.getKind() == ElementKind.CONSTRUCTOR && selected.getEnclosingElement().getKind() != ElementKind.ENUM) {
                return new ReplaceConstructorWithFactoryUI(TreePathHandle.create(selected, info), selected.getEnclosingElement().getSimpleName().toString());
            }
        }
        return null;
    }
    
    public static JavaRefactoringUIFactory factory() {
        return new ReplaceConstructorWithFactoryUI();
    }

}
