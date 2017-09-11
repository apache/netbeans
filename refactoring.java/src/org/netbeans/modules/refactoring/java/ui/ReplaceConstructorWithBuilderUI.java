/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.ArrayTypeTree;
import org.netbeans.modules.refactoring.java.api.ReplaceConstructorWithBuilderRefactoring;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class ReplaceConstructorWithBuilderUI implements RefactoringUI, JavaRefactoringUIFactory {

    private ReplaceConstructorWithBuilderRefactoring refactoring;
    private String builderFQN;
    private ReplaceConstructorWithBuilderPanel panel;
    private String name;
    private List <String> paramaterNames;
    private List <String> parameterTypes;
    private List <Boolean> parameterTypeVars;

    private ReplaceConstructorWithBuilderUI(TreePathHandle constructor, CompilationInfo info) {
        this.refactoring = new ReplaceConstructorWithBuilderRefactoring(constructor);
        ExecutableElement contructorElement = (ExecutableElement) constructor.resolveElement(info);
        this.name = contructorElement.getSimpleName().toString();
        MethodTree constTree = (MethodTree) constructor.resolve(info).getLeaf();
        paramaterNames = new ArrayList<String>();
        parameterTypes = new ArrayList<String>();
        parameterTypeVars = new ArrayList<Boolean>();
        boolean varargs = contructorElement.isVarArgs();
        List<? extends VariableElement> parameterElements = contructorElement.getParameters();
        List<? extends VariableTree> parameters = constTree.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            VariableTree var = parameters.get(i);
            paramaterNames.add(var.getName().toString());
            String type = contructorElement.getParameters().get(i).asType().toString();
            if(varargs && i+1 == parameters.size()) {
                if(var.getType().getKind() == Tree.Kind.ARRAY_TYPE) {
                    ArrayTypeTree att = (ArrayTypeTree) var.getType();
                    type = att.getType().toString();
                    type += "..."; //NOI18N
                }
            }
            parameterTypes.add(type);
            parameterTypeVars.add(parameterElements.get(i).asType().getKind() == TypeKind.TYPEVAR);
        }
        TypeElement typeEl = (TypeElement) contructorElement.getEnclosingElement();
        if(typeEl.getNestingKind() != NestingKind.TOP_LEVEL) {
            PackageElement packageOf = info.getElements().getPackageOf(typeEl);
            builderFQN = packageOf.toString() + "." + typeEl.getSimpleName().toString();
        } else {
            builderFQN = typeEl.getQualifiedName().toString();
        }
    }

    private ReplaceConstructorWithBuilderUI() {
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ReplaceConstructorWithBuilderUI.class, "ReplaceConstructorWithBuilderName");    
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ReplaceConstructorWithBuilderUI.class, "ReplaceConstructorWithBuilderDescription", name ,builderFQN);    
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(final ChangeListener parent) {
        if (panel == null) {
            panel = new ReplaceConstructorWithBuilderPanel(parent, builderFQN + "Builder", paramaterNames, parameterTypes, parameterTypeVars);
        }
        return panel;
    }

    @Override
    public Problem setParameters() {
        refactoring.setSetters(panel.getSetters());
        refactoring.setBuilderName(panel.getBuilderName());
        return refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
        refactoring.setSetters(panel.getSetters());
        refactoring.setBuilderName(panel.getBuilderName());
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
        return new HelpCtx(ReplaceConstructorWithBuilderUI.class);
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
            if (selected != null && selected.getKind() == ElementKind.CONSTRUCTOR &&
                    selected.getEnclosingElement().getKind() != ElementKind.ENUM) {
                return new ReplaceConstructorWithBuilderUI(TreePathHandle.create(selected, info), info);
            }
        }
        return null;
    }
    
    public static JavaRefactoringUIFactory factory() {
        return new ReplaceConstructorWithBuilderUI();
    }

}
