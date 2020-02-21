/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
