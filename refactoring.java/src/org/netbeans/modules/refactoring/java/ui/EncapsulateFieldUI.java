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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 *
 * @author  Tomas Hurka
 * @author  Pavel Flaska
 * @author  Jan Pokorsky
 */
public final class EncapsulateFieldUI implements RefactoringUI {

    private EncapsulateFieldPanel panel;
    private transient EncapsulateFieldsRefactoring refactoring;
    private int offset;
    
    private EncapsulateFieldUI(TreePathHandle sourceType, int offset) {
        refactoring = new EncapsulateFieldsRefactoring(sourceType);
        this.offset = offset;
    }
    
    private EncapsulateFieldUI(TreePathHandle[] handles) {
        refactoring = new EncapsulateFieldsRefactoring(Arrays.asList(handles));
    }
    
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            Collection selectedObjects = refactoring.getRefactoringSource().lookup(Collection.class);
            panel = new EncapsulateFieldPanel(refactoring.getSelectedObject(), (Collection<TreePathHandle>) selectedObjects, offset, parent);
        }
        return panel;
    }

    private Problem setParameters(boolean checkOnly) {
        refactoring.setRefactorFields(panel.getAllFields());
        refactoring.setMethodModifiers(panel.getMethodModifiers());
        refactoring.setFieldModifiers(panel.getFieldModifiers());
        refactoring.setAlwaysUseAccessors(panel.isCheckAccess());
        refactoring.setGeneratePropertyChangeSupport(panel.isBound());
        refactoring.setGenerateVetoableSupport(panel.isVetoable());
        refactoring.getContext().add(panel.getInsertPoint());
        refactoring.getContext().add(panel.getSortBy());
        refactoring.getContext().add(panel.getJavadoc());
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
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.EncapsulateFieldUI"); // NOI18N
    }
    
    /**
     * returns field in case the selectedObject is field or enclosing class
     * in other cases.
     */
    private static TreePathHandle resolveSourceType(TreePathHandle selectedObject, CompilationInfo javac) {
        TreePath selectedField = selectedObject.resolve(javac);
        if (selectedField == null) {
            return null;
        }
        Element elm = javac.getTrees().getElement(selectedField);
        TypeElement encloser = null;
        if (elm != null && ElementKind.FIELD == elm.getKind()
                && !"this".contentEquals(elm.getSimpleName())) { // NOI18N
            encloser = (TypeElement) elm.getEnclosingElement();
            if (ElementKind.INTERFACE != encloser.getKind() && NestingKind.ANONYMOUS != encloser.getNestingKind()) {
                // interface constants, local variables and annonymous declarations are unsupported
                TreePath tp = javac.getTrees().getPath(elm);
                return TreePathHandle.create(tp, javac);
            }
        }
        
        // neither interface, annotation type nor annonymous declaration
        TreePath tpencloser = JavaRefactoringUtils.findEnclosingClass(javac, selectedField, true, false, true, false, false);

        if (tpencloser == null) {
            return null;
        }
        return TreePathHandle.create(tpencloser, javac);
    }

    public static JavaRefactoringUIFactory factory(Lookup lookup) {
        return new EncapsulateFieldUIFactory(lookup);
    }

    private static final class EncapsulateFieldUIFactory implements JavaRefactoringUIFactory {

        private Lookup lookup;

        private EncapsulateFieldUIFactory(Lookup lookup) {
            this.lookup = lookup;
        }

        private static EncapsulateFieldUI create(CompilationInfo info, int offset, TreePathHandle... selectedObject) {
            if (selectedObject.length == 1) {
                TreePathHandle sourceType = resolveSourceType(selectedObject[0], info);

                if (sourceType == null) {
                    return null;
                }

                return new EncapsulateFieldUI(sourceType, offset);
            } else {
                return new EncapsulateFieldUI(selectedObject);
            }
        }

        @Override
        public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if (ec == null) {
                return create(info, -1, handles);
            }
            JEditorPane textC = NbDocument.findRecentEditorPane(ec);
            if (textC == null) {
                return create(info, -1, handles);
            }
            int startOffset = textC.getSelectionStart();
            int endOffset = textC.getSelectionEnd();

            if (startOffset == endOffset) {
                //cursor position
                return create(info, startOffset, handles[0]);
            }

            //editor selection
            TreePath path = info.getTreeUtilities().pathFor(startOffset);
            if (path == null) {
                return null;
            }
            TreePath enclosingClass = JavaRefactoringUtils.findEnclosingClass(info, path, true, true, true, true, true);
            if (enclosingClass == null) {
                return null;
            }
            Element el = info.getTrees().getElement(enclosingClass);
            if (el == null) {
                return null;
            }
            if (!(el.getKind().isClass() || el.getKind().isInterface())) {
                el = info.getElementUtilities().enclosingTypeElement(el);
            }
            Collection<TreePathHandle> h = new ArrayList<TreePathHandle>();
            for (Element e : ElementFilter.fieldsIn(el.getEnclosedElements())) {
                //            SourcePositions sourcePositions = info.getTrees().getSourcePositions();
                Tree leaf = info.getTrees().getPath(e).getLeaf();
                int[] namespan = info.getTreeUtilities().findNameSpan((VariableTree) leaf);
                if (namespan != null) {
                    long start = namespan[0]; //sourcePositions.getStartPosition(info.getCompilationUnit(), leaf);
                    long end = namespan[1]; //sourcePositions.getEndPosition(info.getCompilationUnit(), leaf);
                    if ((start <= endOffset && start >= startOffset)
                            || (end <= endOffset && end >= startOffset)) {
                        h.add(TreePathHandle.create(e, info));
                    }
                }
            }
            if (h.isEmpty()) {
                return create(info, startOffset, handles[0]);
            }
            return create(info, -1, h.toArray(new TreePathHandle[h.size()]));
        }
    }
}
