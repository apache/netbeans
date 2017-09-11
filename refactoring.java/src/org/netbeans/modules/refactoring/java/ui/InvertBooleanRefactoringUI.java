/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
