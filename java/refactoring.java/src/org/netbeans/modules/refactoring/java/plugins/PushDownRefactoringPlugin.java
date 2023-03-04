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
        PushDownTransformer pdt = new PushDownTransformer(treePathHandle.getFileObject(), refactoring.getMembers()); 
        Problem prob = createAndAddElements(a, new TransformTask(pdt, treePathHandle), refactoringElements, refactoring);
        fireProgressListenerStop();
        return prob != null ? prob : pdt.getProblem();
    }
}
