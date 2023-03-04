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

/*
 * UseSuperTypeRefactoringUI.java
 *
 * Created on June 20, 2005
 *
 */

package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.UseSuperTypeRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * UseSuperTypeRefactoringUI.java
 *
 * Created on June 20, 2005, 7:23 PM
 *
 * @author Bharath Ravi Kumar
 */
public class UseSuperTypeRefactoringUI implements RefactoringUI, JavaRefactoringUIFactory {
    
    private TreePathHandle subType;
    private UseSuperTypeRefactoring refactoring;
    private UseSuperTypePanel panel;
    private ElementHandle superType;
    private String className;

    /**
     * Creates a new instance of UseSuperTypeRefactoringUI
     * @param selectedElement The sub type being used
     * @param info  
     */
    private UseSuperTypeRefactoringUI(TreePathHandle selectedElement, CompilationInfo info) {
        this.subType = selectedElement;
        refactoring = new UseSuperTypeRefactoring(subType);
        refactoring.getContext().add(RefactoringUtils.getClasspathInfoFor(subType));
        this.className = refactoring.getTypeElement().resolveElement(info).getSimpleName().toString();
    }

    private UseSuperTypeRefactoringUI() {
    }
    
    /**
     * Returns the name of the refactoring
     * @return 
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(UseSuperTypeRefactoringUI.class, "LBL_UseSuperType").substring(1); // NOI18N
    }
    
    /**
     * Returns the description of the refactoring
     * @return 
     */
    @Override
    public String getDescription() {
        return NbBundle.getMessage(UseSuperTypeRefactoringUI.class, "DSC_UseSuperType", refactoring.getTypeElement()); // NOI18N
    }
    
    /**
     * return false
     * @return 
     */
    @Override
    public boolean isQuery() {
        return false;
    }
    
    /**
     * Sets the target super type on the underlying refactoring
     * @return 
     */
    @Override
    public Problem setParameters() {
        superType = panel.getSuperType();
        refactoring.setTargetSuperType(superType);
        return refactoring.checkParameters();
    }
    
    /**
     * Calls fastCheckParameters on the underlying refactoring
     * @return 
     */
    @Override
    public Problem checkParameters() {
        superType = panel.getSuperType();
        refactoring.setTargetSuperType(superType);
        return refactoring.fastCheckParameters();
    }
    
    /**
     * Returns true
     * @return 
     */
    @Override
    public boolean hasParameters() {
        return true;
    }
    
    /**
     * Returns the use super type refactoring
     * @return 
     */
    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }
    
    /**
     * Returns the relevant Helpctx
     * @return 
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.UseSuperTypeRefactoringUI"); // NOI18N
    }
    
    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if(panel == null) {
            panel = new UseSuperTypePanel(refactoring, className);
        }
        return panel;
    }

    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        assert handles.length == 1;
        Element selected = handles[0].resolveElement(info);
        TreePathHandle s = handles[0];
        if (selected == null || !(selected.getKind().isClass() || selected.getKind().isInterface())) {
            TreePath classTreePath = JavaRefactoringUtils.findEnclosingClass(info, handles[0].resolve(info), true, true, true, true, true);

            if (classTreePath == null) {
                return null;
            }
            s = TreePathHandle.create(classTreePath, info);
        }
        return new UseSuperTypeRefactoringUI(s, info);
    }
    
    public static JavaRefactoringUIFactory factory() {
        return new UseSuperTypeRefactoringUI();
    }
    
}
