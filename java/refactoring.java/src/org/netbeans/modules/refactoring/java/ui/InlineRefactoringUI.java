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
import javax.lang.model.element.VariableElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.InlineRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Refactoring UI object for the inline refactoring.
 * @author Ralph Ruijs
 */
public class InlineRefactoringUI implements RefactoringUI {

    private InlineRefactoring refactoring;
    private String type;
    private String elementName;

    /** Creates a new instance of InlineRefactoringUI
     * @param selectedElements Elements the refactoring action was invoked on.
     */
    public InlineRefactoringUI(TreePathHandle selectedElement, InlineRefactoring.Type refactoringType, String elementName, String type) {
        refactoring = new InlineRefactoring(selectedElement, refactoringType);
        this.elementName = elementName;
        this.type = type;
    }

    private InlineRefactoringUI() {
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(InlineAction.class, "LBL_Inline", type); // NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(InnerToOuterAction.class, "DSC_Inline", elementName); // NOI18N
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        return null;
    }

    @Override
    public Problem setParameters() {
        return refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
        return refactoring.fastCheckParameters();
    }

    @Override
    public boolean hasParameters() {
        return false;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.InlineRefactoringUI"); // NOI18N
    }
    
    public static JavaRefactoringUIFactory factory() {
        return new JavaRefactoringUIFactory() {

            @Override
            public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
                assert handles.length == 1;
                TreePathHandle selectedElement = handles[0];
                switch (selectedElement.getKind()) {
                    case BOOLEAN_LITERAL:
                    case CHAR_LITERAL:
                    case DOUBLE_LITERAL:
                    case FLOAT_LITERAL:
                    case INT_LITERAL:
                    case LONG_LITERAL:
                    case NULL_LITERAL:
                    case STRING_LITERAL:
                        TreePath tp = selectedElement.resolve(info);
                        if (tp == null) {
                            return null;
                        }
                        TreePath parent = tp.getParentPath();
                        Element parentElement = info.getTrees().getElement(parent);
                        if (parentElement != null && parentElement.getKind() == ElementKind.LOCAL_VARIABLE) {
                            selectedElement = TreePathHandle.create(parent, info);
                        }
                        break;
                }
                TreePath path = selectedElement.resolve(info);
                if(path == null || info.getTreeUtilities().isSynthetic(path)) {
                    return null;
                }
                Element element = info.getTrees().getElement(path);
                if(element == null) {
                    return null;
                }
                InlineRefactoring.Type refactoringType;
                String type;
                switch (element.getKind()) {
                    case FIELD:
                        type = "Constant";
                        refactoringType = InlineRefactoring.Type.CONSTANT;
                        break;
                    case LOCAL_VARIABLE:
                        type = "Temp";
                        refactoringType = InlineRefactoring.Type.TEMP;
                        break;
                    case METHOD:
                        type = "Method";
                        refactoringType = InlineRefactoring.Type.METHOD;
                        break;
                    default:
                        return null;
                }
                return new InlineRefactoringUI(selectedElement, refactoringType, element.getSimpleName().toString(), type);
            }
        };
    }
}
