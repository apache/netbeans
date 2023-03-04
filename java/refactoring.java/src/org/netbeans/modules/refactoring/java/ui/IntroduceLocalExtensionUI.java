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
