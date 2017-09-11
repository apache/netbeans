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
package org.netbeans.modules.refactoring.java.plugins;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.IntroduceLocalExtensionRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Ralph Ruijs
 */
public final class IntroduceLocalExtensionPlugin extends JavaRefactoringPlugin {
    private IntroduceLocalExtensionRefactoring refactoring;
    private TreePathHandle treePathHandle;

    public IntroduceLocalExtensionPlugin(IntroduceLocalExtensionRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
            case CHECKPARAMETERS:
            case FASTCHECKPARAMETERS:
            case PRECHECK:
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                return JavaSource.create(cpInfo, treePathHandle.getFileObject());
        }
        return null;
    }

    @Override
    protected Problem preCheck(CompilationController info) throws IOException {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 4);
        Problem preCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);
        preCheckProblem = isElementAvail(treePathHandle, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        Element el = treePathHandle.resolveElement(info);
        if (!(el.getKind() == ElementKind.CLASS || el.getKind() == ElementKind.INTERFACE)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(IntroduceLocalExtensionPlugin.class, "ERR_IntroduceLEWrongType")); // NOI18N
            return preCheckProblem;
        }
        for (Element element : el.getEnclosedElements()) {
            if((element.getKind().isClass() || element.getKind().isInterface()) && !element.getModifiers().contains(Modifier.PRIVATE)) {
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(IntroduceLocalExtensionPlugin.class, "ERR_IntroduceLEInnerType")); // NOI18N
                return preCheckProblem;
            }
        }
        return preCheckProblem;
    }

    @Override
    public Problem fastCheckParameters() {
        Problem fastCheckProblem = null;
        String newName = refactoring.getNewName();
        if (!Utilities.isJavaIdentifier(newName)) {
            String msg = NbBundle.getMessage(IntroduceLocalExtensionPlugin.class, "ERR_InvalidIdentifier", newName);
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }
        String targetPackageName = refactoring.getPackageName();
        if (!RefactoringUtils.isValidPackageName(targetPackageName)) {
            String msg = NbBundle.getMessage(IntroduceLocalExtensionPlugin.class, "ERR_InvalidPackage", targetPackageName);
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }
        
        FileObject targetRoot = refactoring.getSourceRoot();
        if(targetRoot == null) {
            String msg = NbBundle.getMessage(IntroduceLocalExtensionPlugin.class, "ERR_TargetFolderNotSet", targetPackageName);
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }
        
        FileObject targetF = targetRoot.getFileObject(targetPackageName.replace('.', '/'));
        if ((targetF!=null && !targetF.canWrite())) {
            String msg = NbBundle.getMessage(IntroduceLocalExtensionPlugin.class, "ERR_PackageIsReadOnly", targetPackageName);
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }
        return super.fastCheckParameters();
    }

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = RefactoringUtils.getClasspathInfoFor(true, treePathHandle.getFileObject());
        Set<FileObject> set = new LinkedHashSet<FileObject>();
        ClassIndex idx = cpInfo.getClassIndex();
        Set<FileObject> resources = idx.getResources(treePathHandle.getElementHandle(), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE));
//        set.add(treePathHandle.getFileObject());
        set.addAll(resources);
        return set;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(AbstractRefactoring.PREPARE, a.size());
        final IntroduceLocalExtensionTransformer transformer = new IntroduceLocalExtensionTransformer(refactoring);
        TransformTask transform = new TransformTask(transformer, treePathHandle);
        Problem problem = createAndAddElements(a, transform, refactoringElements, refactoring);
        fireProgressListenerStop();
        return problem != null ? problem : transformer.getProblem();
    }
}
