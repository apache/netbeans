/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.refactoring.ui;

import java.text.MessageFormat;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldsRefactoring;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * (based on Java version)
 *
 */
public final class EncapsulateFieldUI implements RefactoringUI {

    private EncapsulateFieldPanel panel;
    private final transient EncapsulateFieldsRefactoring refactoring;
    private final CsmObject selectedObj;
    private final CsmContext editorContext;

    /** Creates new form EncapsulateField */
    private EncapsulateFieldUI(CsmObject selectedObject, CsmContext editorContext) {
        this.refactoring = new EncapsulateFieldsRefactoring(selectedObject, editorContext);
        this.selectedObj = selectedObject;
        this.editorContext = editorContext;
    }

    public static EncapsulateFieldUI create(CsmObject refactoredObj, CsmContext editorContext) {
        return new EncapsulateFieldUI(refactoredObj, editorContext);
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new EncapsulateFieldPanel(selectedObj, editorContext, parent);
        }
        return panel;
    }

    private Problem setParameters(boolean checkOnly) {
        refactoring.setRefactorFields(panel.getAllFields());
        refactoring.setMethodModifiers(panel.getMethodModifiers());
        refactoring.setFieldModifiers(panel.getFieldModifiers());
        refactoring.setAlwaysUseAccessors(panel.isCheckAccess());
        refactoring.setMethodInline(panel.isMethodInline());
        refactoring.getContext().add(panel.getInsertPoint());
        refactoring.getContext().add(panel.getSortBy());
        refactoring.getContext().add(panel.getDocumentation());
        if (checkOnly) {
            return refactoring.fastCheckParameters();
        } else {
            return refactoring.checkParameters();
        }
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public String getDescription() {
        String name = panel.getClassname();
//        name = "<anonymous>"; // NOI18N
        return new MessageFormat(NbBundle.getMessage(EncapsulateFieldUI.class, "DSC_EncapsulateFields")).format (
                    new Object[] {name}
                );
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EncapsulateFieldUI.class, "LBL_EncapsulateFields");
    }
    
    @Override
    public Problem checkParameters() {
        return setParameters(true);
    }
    
    @Override
    public Problem setParameters() {
        return setParameters(false);
    }

    @Override
    public boolean hasParameters() {
        return true;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("EncapsulateFields"); //NOI18N
    }
    
    /**
     * returns field in case the selectedObject is field or enclosing class
     * in other cases.
     */
//    private static TreePathHandle resolveSourceType(TreePathHandle selectedObject, CompilationInfo javac) {
//        TreePath selectedField = selectedObject.resolve(javac);
//        Element elm = javac.getTrees().getElement(selectedField);
//        TypeElement encloser = null;
//        if (elm != null && ElementKind.FIELD == elm.getKind()
//                && !"this".contentEquals(elm.getSimpleName())) { // NOI18N
//            encloser = (TypeElement) elm.getEnclosingElement();
//            if (ElementKind.INTERFACE != encloser.getKind() && NestingKind.ANONYMOUS != encloser.getNestingKind()) {
//                // interface constants, local variables and annonymous declarations are unsupported
//                TreePath tp = javac.getTrees().getPath(elm);
//                return TreePathHandle.create(tp, javac);
//            }
//        }
//
//        // neither interface, annotation type nor annonymous declaration
//        TreePath tpencloser = RetoucheUtils.findEnclosingClass(javac, selectedField, true, false, true, false, false);
//        return TreePathHandle.create(tpencloser, javac);
//    }
}
