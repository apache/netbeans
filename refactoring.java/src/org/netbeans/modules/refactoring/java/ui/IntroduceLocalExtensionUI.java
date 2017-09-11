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

import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.IntroduceLocalExtensionRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Ralph Ruijs
 */
@NbBundle.Messages({"# {0} - Element Html Display Name","DSC_IntroduceLocalExtension=Introduce Local Extension for {0}",
    "LBL_IntroduceLocalExtension=Introduce Local Extension"})
public final class IntroduceLocalExtensionUI implements RefactoringUI {

    private TreePathHandle type;
    private IntroduceLocalExtensionRefactoring refactoring;
    private IntroduceLocalExtensionPanel panel;
    private final String htmlHeader;
    private final Icon icon;
    private final String newName;
    private final String startPackage;

    private IntroduceLocalExtensionUI(TreePathHandle type, String htmlHeader, Icon icon, String newName, String startPackage) {
        this.refactoring = new IntroduceLocalExtensionRefactoring(type);
        this.type = type;
        this.htmlHeader = htmlHeader;
        this.icon = icon;
        this.newName = newName;
        this.startPackage = startPackage;
    }

    public static IntroduceLocalExtensionUI create(TreePathHandle type, CompilationInfo javac) {
        TypeElement typeElement = (TypeElement) type.resolveElement(javac);
        Icon icon = ElementIcons.getElementIcon(typeElement.getKind(), typeElement.getModifiers());
        String startPackage = getPackageName(type.getFileObject().getParent());
        return new IntroduceLocalExtensionUI(type, typeElement.getSimpleName().toString(), icon, typeElement.getSimpleName().toString(), startPackage);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(IntroduceParameterUI.class, "LBL_IntroduceLocalExtension");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(IntroduceLocalExtensionUI.class, "DSC_IntroduceLocalExtension", htmlHeader); // NOI18N
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new IntroduceLocalExtensionPanel(htmlHeader, icon, newName, startPackage, type, parent);
        }
        return panel;
    }

    private Problem setParameters(boolean checkOnly) {
        if (panel==null) {
            return null;
        }
        refactoring.setNewName(panel.getNewName());
        refactoring.setSourceRoot(panel.getRootFolder());
        refactoring.setPackageName(panel.getPackageName()); // NOI18N
        refactoring.setWrap(panel.getWrap());
        refactoring.setEquality(panel.getEquality());
        refactoring.setReplace(panel.getReplace());
        if (checkOnly) {
            return refactoring.fastCheckParameters();
        } else {
            return refactoring.checkParameters();
        }
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
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.IntroduceLocalExtensionUI"); //NOI18N
    }
    
    private static String getPackageName(FileObject file) {
        ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
        return cp.getResourceName(file, '.', false);
    }
    
    public static JavaRefactoringUIFactory factory() {
        return new IntroduceLocalExtensionUIFactory();
    }
    
    public static class IntroduceLocalExtensionUIFactory implements JavaRefactoringUIFactory {
        @Override
        public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
            assert handles.length == 1;
            Element selected = handles[0].resolveElement(info);
            TreePathHandle s = handles[0];
            if (selected == null || !(selected.getKind().isClass() || (selected.getKind().isInterface()))) {
                TreePath classTreePath = JavaRefactoringUtils.findEnclosingClass(info, handles[0].resolve(info), true, true, true, true, true);

                if (classTreePath == null) {
                    return null;
                }
                s = TreePathHandle.create(classTreePath, info);
            }
            return IntroduceLocalExtensionUI.create(s, info);
        }
    }
}
