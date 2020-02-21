/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldsRefactoring;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldsRefactoring.EncapsulateFieldInfo;
import org.netbeans.modules.cnd.refactoring.hints.infrastructure.Utilities;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.refactoring.ui.EncapsulateFieldPanel.Documentation;
import org.netbeans.modules.cnd.refactoring.ui.InsertPoint;
import org.netbeans.modules.cnd.refactoring.ui.EncapsulateFieldPanel.SortBy;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.openide.util.NbBundle;

/** Encapsulate fields refactoring. This is a composed refactoring (uses instances of {@link org.netbeans.modules.refactoring.api.EncapsulateFieldRefactoring}
 * to encapsulate several fields at once.
 *
 */
public final class EncapsulateFieldsPlugin extends CsmModificationRefactoringPlugin {

    private static final int FAST_CHECK_PARAMETERS = 1;
    private static final int CHECK_PARAMETERS = 2;
    private List<EncapsulateFieldRefactoringPlugin> refactorings;
    private final EncapsulateFieldsRefactoring refactoring;
    // objects affected by refactoring
    private Collection<CsmField> referencedFields = new ArrayList<>();
    private CsmClass enclosingClass;
    private ProgressListener listener = new ProgressListener() {

        @Override
        public void start(ProgressEvent event) {
            fireProgressListenerStart(event.getOperationType(), event.getCount());
        }

        @Override
        public void step(ProgressEvent event) {
            fireProgressListenerStep();
        }

        @Override
        public void stop(ProgressEvent event) {
            fireProgressListenerStop();
        }
    };

    /** Creates a new instance of EcapsulateFields.
     * @param selectedObjects Array of objects (fields) that should be encapsulated.
     */
    public EncapsulateFieldsPlugin(EncapsulateFieldsRefactoring refactoring) {
        super(refactoring);
        this.refactoring = refactoring;
    }

    @Override
    public Problem checkParameters() {
        return validation(CHECK_PARAMETERS);
    }

    @Override
    public Problem fastCheckParameters() {
        Collection<EncapsulateFieldInfo> fields = refactoring.getRefactorFields();
        if (fields.isEmpty()) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateNothingSelected"));
        }
        initRefactorings(fields,
                refactoring.getMethodModifiers(),
                refactoring.getFieldModifiers(),
                refactoring.isAlwaysUseAccessors(),
                refactoring.isMethodInline());
        return validation(FAST_CHECK_PARAMETERS);
    }

    private CsmObject getRefactoredCsmElement() {
        CsmObject out = getStartReferenceObject();
        if (out == null) {
            CsmContext editorContext = getEditorContext();
            if (editorContext != null) {
                out = editorContext.getObjectUnderOffset();
                if (!CsmKindUtilities.isField(out)) {
                    out = Utilities.extractEnclosingClass(getEditorContext());
                }
            }
        }
        return out;
    }

    @Override
    public Problem preCheck() {
        Problem preCheckProblem = null;
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 5);
        CsmRefactoringUtils.waitParsedAllProjects();
        fireProgressListenerStep();
        // check if resolved element
        Collection<EncapsulateFieldInfo> fieldsInfo = refactoring.getRefactorFields();
        CsmObject refactoredElement = getRefactoredCsmElement();
        preCheckProblem = fieldsInfo.isEmpty() ? isResovledElement(refactoredElement) : null;
        fireProgressListenerStep();
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        // check if valid element
        CsmObject directReferencedObject = CsmRefactoringUtils.getReferencedElement(refactoredElement);
        initReferencedObjects(directReferencedObject, fieldsInfo);
        fireProgressListenerStep();
        // support only fields and enclosing classes
        if (this.enclosingClass == null) {
            preCheckProblem = createProblem(preCheckProblem, true, getString("ERR_EncapsulateWrongType")); // NOI18N
            return preCheckProblem;
        }
        // check read-only elements
        preCheckProblem = checkIfModificationPossible(preCheckProblem, this.enclosingClass);
        fireProgressListenerStop();
        if (fieldsInfo.isEmpty()) {
            // check that class has at least one field
            for (CsmMember csmMember : this.enclosingClass.getMembers()) {
                if (CsmKindUtilities.isField(csmMember)) {
                    return null;
                }
            }
            return new Problem(true, getString("ERR_EncapsulateNoFields", enclosingClass.getQualifiedName().toString())); // NOI18N
        } else {
            return preCheckProblem;
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(EncapsulateFieldsPlugin.class, key);
    }

    private static String getString(String key, String param) {
        return NbBundle.getMessage(EncapsulateFieldsPlugin.class, key, param);
    }
//    public Problem prepare(RefactoringElementsBag elements) {
//        Problem problem = null;
//        Set<FileObject> references = new HashSet<FileObject>();
//        List<EncapsulateDesc> descs = new ArrayList<EncapsulateDesc>(refactorings.size());
//        fireProgressListenerStart(ProgressEvent.START, refactorings.size() + 1);
//        for (EncapsulateFieldRefactoringPlugin ref : refactorings) {
//            if (cancelRequest) {
//                return null;
//            }
//
//            EncapsulateDesc desc = ref.prepareEncapsulator(problem);
//            problem = desc.p;
//            desc.p = null;
//            if (problem != null && problem.isFatal()) {
//                return problem;
//            }
//            descs.add(desc);
//            references.addAll(desc.refs);
//            fireProgressListenerStep();
//        }
//
//        Encapsulator encapsulator = new Encapsulator(descs, problem,
//                refactoring.getContext().lookup(InsertPoint.class),
//                refactoring.getContext().lookup(SortBy.class),
//                refactoring.getContext().lookup(Javadoc.class)
//                );
//        Problem prob = createAndAddElements(references, new TransformTask(encapsulator, descs.get(0).fieldHandle), elements, refactoring);
//        fireProgressListenerStop();
//        problem = encapsulator.getProblem();
//        return prob != null ? prob : problem;
//    }

    private void initRefactorings(Collection<EncapsulateFieldInfo> refactorFields, Set<CsmVisibility> methodModifier, Set<CsmVisibility> fieldModifier, 
            boolean alwaysUseAccessors, boolean methodInline) {
        CsmCacheManager.enter();
        try {
            refactorings = new ArrayList<>(refactorFields.size());
            CsmFile[] declDefFiles = null;
            for (EncapsulateFieldInfo info : refactorFields) {
                if (declDefFiles == null) {
                    declDefFiles = GeneratorUtils.getDeclarationDefinitionFiles(info.getField().getContainingClass());
                }
                EncapsulateFieldRefactoring ref = new EncapsulateFieldRefactoring(info.getField(), declDefFiles[0], declDefFiles[1]);
                ref.setGetterName(info.getGetterName());
                ref.setSetterName(info.getSetterName());
                ref.setMethodModifiers(methodModifier);
                ref.setFieldModifiers(fieldModifier);
                ref.setAlwaysUseAccessors(alwaysUseAccessors);
                ref.setMethodInline(methodInline);
                ref.setDefaultGetter(info.getDefaultGetter());
                ref.setDefaultSetter(info.getDefaultSetter());
                ref.getContext().add(refactoring.getContext().lookup(InsertPoint.class));
                ref.getContext().add(refactoring.getContext().lookup(Documentation.class));
                ref.getContext().add(refactoring.getContext().lookup(SortBy.class));
                refactorings.add(new EncapsulateFieldRefactoringPlugin(ref));
            }
        } finally {
            CsmCacheManager.leave();
        }
    }

    private Problem validation(int phase) {
        Problem result = null;
        for (EncapsulateFieldRefactoringPlugin ref : refactorings) {
            Problem lastresult = null;
            switch (phase) {
                case FAST_CHECK_PARAMETERS:
                    lastresult = ref.fastCheckParameters();
                    break;
                case CHECK_PARAMETERS:
                    lastresult = ref.preCheck();
                    result = chainProblems(result, lastresult);
                    if (result != null && result.isFatal()) {
                        return result;
                    }
                    lastresult = ref.checkParameters();
                    ref.addProgressListener(listener);
                    break;
            }

            result = chainProblems(result, lastresult);
            if (result != null && result.isFatal()) {
                return result;
            }

        }

        return result;
    }

    private static Problem chainProblems(Problem oldp, Problem newp) {
        if (oldp == null) {
            return newp;
        } else if (newp == null) {
            return oldp;
        } else if (newp.isFatal()) {
            newp.setNext(oldp);
            return newp;
        } else {
            // [TODO] performance
            Problem p = oldp;
            while (p.getNext() != null) {
                p = p.getNext();
            }
            p.setNext(newp);
            return oldp;
        }
    }

    @Override
    protected Collection<CsmFile> getRefactoredFiles() {
        if (enclosingClass == null) {
            return Collections.emptySet();
        }
        Collection<CsmFile> files = new HashSet<>();
        CsmFile startFile = CsmRefactoringUtils.getCsmFile(enclosingClass);
        if (refactoring.isAlwaysUseAccessors()) {
            Collection<CsmProject> prjs = CsmRefactoringUtils.getRelatedCsmProjects(enclosingClass, null);
            CsmProject[] ar = prjs.toArray(new CsmProject[prjs.size()]);
            refactoring.getContext().add(ar);
            files.addAll(getRelevantFiles(startFile, enclosingClass, refactoring));
        } else {
            // declarations are added to file with class declaration
            files.add(startFile);
            if (!refactoring.isMethodInline()) {
                // add files with definitions
                Collection<CsmFunction> extDefs = GeneratorUtils.getAllOutOfClassMethodDefinitions(enclosingClass);
                for (CsmFunction extDef : extDefs) {
                    CsmFile defFile = CsmRefactoringUtils.getCsmFile(extDef);
                    if (defFile != null) {
                        files.add(defFile);
                    }
                }
            }
        }
        return files;
    }


    private void initReferencedObjects(CsmObject referencedObject, Collection<EncapsulateFieldInfo> fieldsInfo) {
        referencedFields = new ArrayList<>(fieldsInfo.size());
        if (!fieldsInfo.isEmpty()) {
            for (EncapsulateFieldInfo info : fieldsInfo) {
                final CsmField field = info.getField();
                referencedFields.add(field);
                if (enclosingClass == null) {
                    enclosingClass = field.getContainingClass();
                }
            }
        } else if (referencedObject != null) {
            if (CsmKindUtilities.isClass(referencedObject)) {
                this.enclosingClass = (CsmClass) referencedObject;
            } else if (CsmKindUtilities.isField(referencedObject)) {
                this.enclosingClass = ((CsmField) referencedObject).getContainingClass();
            }
        }
    }

    @Override
    protected void processFile(CsmFile csmFile, ModificationResult mr, AtomicReference<Problem> outProblem) {
        for (EncapsulateFieldRefactoringPlugin ref : refactorings) {
            ref.processFile(csmFile, mr, outProblem);
        }
//        if (refactoring.isAlwaysUseAccessors()) {
//            for (EncapsulateFieldRefactoringPlugin ref : refactorings) {
//                ref.processFile(csmFile, mr, outProblem);
//            }
//        } else {
//            // only generate definitions/declarations
//            Collection<EncapsulateFieldInfo> fieldsInfo = refactoring.getRefactorFields();
//            for (EncapsulateFieldInfo fieldInfo : fieldsInfo) {
//
//            }
//        }
    }
}    
