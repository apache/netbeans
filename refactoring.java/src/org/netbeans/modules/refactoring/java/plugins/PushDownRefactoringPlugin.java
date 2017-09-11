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
package org.netbeans.modules.refactoring.java.plugins;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PushDownRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * Plugin that implements the core functionality of Push Down refactoring.
 *
 * @author Pavel Flaska
 * @author Jan Becicka
 */
public final class PushDownRefactoringPlugin extends JavaRefactoringPlugin {
    
    /** Reference to the parent refactoring instance */
    private final PushDownRefactoring refactoring;
    private final TreePathHandle treePathHandle;
    
    /** Creates a new instance of PushDownRefactoringPlugin */
    public PushDownRefactoringPlugin(PushDownRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getSourceType();
    }
    
    @Override
    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
        default: 
            ClasspathInfo cpInfo = getClasspathInfo(refactoring);
            return JavaSource.create(cpInfo, treePathHandle.getFileObject());
        }
    }
    
    @Override
    protected Problem preCheck(CompilationController cc) throws IOException {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 4);
        try {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            Problem precheckProblem = isElementAvail(treePathHandle, cc);
            if (precheckProblem != null) {
                // fatal error -> don't continue with further checks
                return precheckProblem;
            }

            // increase progress (step 1)
            fireProgressListenerStep();
            final Element el = treePathHandle.resolveElement(cc);
            precheckProblem = JavaPluginUtils.isSourceElement(el, cc);
            if (precheckProblem != null) {
                return precheckProblem;
            }
            if (!(el instanceof TypeElement)) {
                return new Problem(true, NbBundle.getMessage(PushDownRefactoringPlugin.class, "ERR_PushDown_InvalidSource", treePathHandle, el)); // NOI18N
            }
            ElementHandle<TypeElement> eh = ElementHandle.create((TypeElement) el);
            Set<FileObject> resources = cc.getClasspathInfo().getClassIndex().getResources(eh, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
            if (resources.isEmpty()) {
                return new Problem(true, NbBundle.getMessage(PushDownRefactoringPlugin.class, "ERR_PushDOwn_NoSubtype")); // NOI18N
            }
            // increase progress (step 2)
            fireProgressListenerStep();
            // #2 - check if there are any members to pull up
            boolean hasMembers = false;
            for (Element element : el.getEnclosedElements()) {
                if (element.getKind() != ElementKind.CONSTRUCTOR) {
                    hasMembers = true;
                    break;
                }
            }
            if(!hasMembers) {
                precheckProblem = new Problem(true, NbBundle.getMessage(PushDownRefactoringPlugin.class, "ERR_PushDown_NoMembers")); // NOI18N
            }
            // increase progress (step 3)
            fireProgressListenerStep();
            return precheckProblem;
        } finally {
            fireProgressListenerStop();
        }
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        // #1 - check whether there are any members to pull up
        if (refactoring.getMembers().length == 0) {
            return new Problem(true, NbBundle.getMessage(PushDownRefactoringPlugin.class, "ERR_PushDown_NoMembersSelected")); // NOI18N
        }
        return null;
    }
   
    private Set<FileObject> getRelevantFiles(TreePathHandle handle) {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        ClassIndex idx = cpInfo.getClassIndex();
        Set<FileObject> set = new HashSet<>();
        set.add(RefactoringUtils.getFileObject(handle));
        set.addAll(idx.getResources(handle.getElementHandle(), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS, ClassIndex.SearchKind.TYPE_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        Set<ElementHandle<TypeElement>> elements = idx.getElements(handle.getElementHandle(), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
        for (ElementHandle<TypeElement> type : elements) {
            set.addAll(idx.getResources(type, EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.METHOD_REFERENCES, ClassIndex.SearchKind.FIELD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        }
        return set;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Set<FileObject> a = getRelevantFiles(treePathHandle);
        fireProgressListenerStart(AbstractRefactoring.PREPARE, a.size());
        PushDownTransformer pdt = new PushDownTransformer(refactoring.getMembers()); 
        Problem prob = createAndAddElements(a, new TransformTask(pdt, treePathHandle), refactoringElements, refactoring);
        fireProgressListenerStop();
        return prob != null ? prob : pdt.getProblem();
    }
}
