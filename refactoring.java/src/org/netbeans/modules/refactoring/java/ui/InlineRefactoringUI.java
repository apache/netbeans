/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
