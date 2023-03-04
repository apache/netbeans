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

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.java.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.plugins.EncapsulateFieldRefactoringPlugin.EncapsulateDesc;
import org.netbeans.modules.refactoring.java.plugins.EncapsulateFieldRefactoringPlugin.Encapsulator;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldPanel.Javadoc;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldPanel.SortBy;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring.EncapsulateFieldInfo;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** Encapsulate fields refactoring. This is a composed refactoring (uses instances of {@link org.netbeans.modules.refactoring.api.EncapsulateFieldRefactoring}
 * to encapsulate several fields at once.
 *
 * @author Pavel Flaska
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldsPlugin extends JavaRefactoringPlugin {
    
    private List<EncapsulateFieldRefactoringPlugin> refactorings;
    private final EncapsulateFieldsRefactoring refactoring;
    
    private ProgressListener listener = new ProgressListener() {
        @Override
        public void start(ProgressEvent event) {
            fireProgressListenerStart(event.getOperationType(),event.getCount());
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
        this.refactoring = refactoring;
    }

    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        return validation(2, javac);
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
                refactoring.isGeneratePropertyChangeSupport(),
                refactoring.isGenerateVetoableSupport());
        try {
            return validation(1, null);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        Problem preCheckProblem;
        preCheckProblem = isElementAvail(refactoring.getSelectedObject(), javac);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        TreePath selectedField = refactoring.getSelectedObject().resolve(javac);
        if (selectedField == null) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "DSC_ElNotAvail"));
        }

        Element elm = javac.getTrees().getElement(selectedField);
        if (elm != null && ElementKind.FIELD == elm.getKind()) {
            preCheckProblem = JavaPluginUtils.isSourceElement(elm, javac);
            if (preCheckProblem != null) {
                return preCheckProblem;
            }
            TreePath source = javac.getTrees().getPath(elm);
            if (source == null) {
                // missing sources with field declaration
                return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "DSC_ElNotAvail"));
            }
            
            TypeElement encloser = (TypeElement) elm.getEnclosingElement();
            if (ElementKind.INTERFACE == encloser.getKind() || NestingKind.ANONYMOUS == encloser.getNestingKind()) {
                // interface constants, local variables and annonymous declarations are unsupported
                return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateInIntf"));
            }
            return null;
        }

        TreePath clazz = JavaRefactoringUtils.findEnclosingClass(javac, selectedField, true, false, true, false, false);
        TypeElement clazzElm = (TypeElement) javac.getTrees().getElement(clazz);
        preCheckProblem = JavaPluginUtils.isSourceElement(clazzElm, javac);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        if (elm != clazzElm || clazzElm == null) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateWrongType"));
        }
        if (ElementKind.INTERFACE == clazzElm.getKind()
                || ElementKind.ANNOTATION_TYPE == clazzElm.getKind()
                || NestingKind.ANONYMOUS == clazzElm.getNestingKind()) {
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateInIntf"));
        }

        for (Element member : clazzElm.getEnclosedElements()) {
            if (ElementKind.FIELD == member.getKind()) { // no enum constant
                return null;
            }
        }
        return new Problem(true, NbBundle.getMessage(EncapsulateFieldsPlugin.class, "ERR_EncapsulateNoFields", clazzElm.getQualifiedName()));
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag elements) {
        Problem problem = null;
        Set<FileObject> references = new LinkedHashSet<FileObject>();
        List<EncapsulateDesc> descs = new ArrayList<EncapsulateDesc>(refactorings.size());
        fireProgressListenerStart(AbstractRefactoring.PREPARE, refactorings.size() + 1);
        for (EncapsulateFieldRefactoringPlugin ref : refactorings) {
            if (cancelRequested.get()) {
                return null;
            }
            
            EncapsulateDesc desc = ref.prepareEncapsulator(problem);
            problem = desc.p;
            desc.p = null;
            if (problem != null && problem.isFatal()) {
                return problem;
            }
            descs.add(desc);
            references.addAll(desc.refs);
            fireProgressListenerStep();
        }

        Encapsulator encapsulator = new Encapsulator(descs, problem,
                refactoring.getContext().lookup(Integer.class),
                refactoring.getContext().lookup(SortBy.class),
                refactoring.getContext().lookup(Javadoc.class)
                );
        Problem prob = createAndAddElements(references, new TransformTask(encapsulator, descs.get(0).fieldHandle), elements, refactoring);
        fireProgressListenerStop();
        problem = encapsulator.getProblem();
        return prob != null ? prob : problem;
    }
    
    private void initRefactorings(Collection<EncapsulateFieldInfo> refactorFields, Set<Modifier> methodModifier, Set<Modifier> fieldModifier, boolean alwaysUseAccessors, boolean pcs, boolean vcs) {
        refactorings = new ArrayList<EncapsulateFieldRefactoringPlugin>(refactorFields.size());
        for (EncapsulateFieldInfo info: refactorFields) {
            EncapsulateFieldRefactoring ref = new EncapsulateFieldRefactoring(info.getField());
            ref.setGetterName(info.getGetterName());
            ref.setSetterName(info.getSetterName());
            ref.setMethodModifiers(methodModifier);
            ref.setFieldModifiers(fieldModifier);
            ref.setAlwaysUseAccessors(alwaysUseAccessors);
            ref.setGeneratePropertyChangeSupport(pcs);
            ref.setGenerateVetoableSupport(vcs);
            refactorings.add(new EncapsulateFieldRefactoringPlugin(ref));
        }
    }
    
    private Problem validation(int phase, CompilationController javac) throws IOException {
        Problem result = null;
        for (EncapsulateFieldRefactoringPlugin ref : refactorings) {
            Problem lastresult = null;
            switch (phase) {
            case 1: lastresult = ref.fastCheckParameters(); break;
            case 2:
                lastresult = ref.preCheck(javac);
                result = JavaPluginUtils.chainProblems(result, lastresult);
                if (result != null && result.isFatal()) {
                    return result;
                }
                lastresult = ref.checkParameters(javac);
                ref.addProgressListener(listener);
                break;
            }
            
            result = JavaPluginUtils.chainProblems(result, lastresult);
            if (result != null && result.isFatal()) {
                return result;
            }
            
        }

        return result;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        TreePathHandle selectedField = refactoring.getSelectedObject();
        FileObject fo = selectedField.getFileObject();
        return JavaSource.forFileObject(fo);
    }

}    
