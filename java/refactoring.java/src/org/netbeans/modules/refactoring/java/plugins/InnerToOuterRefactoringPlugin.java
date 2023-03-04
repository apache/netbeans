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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import static org.netbeans.modules.refactoring.java.plugins.Bundle.*;


/** Plugin that implements the core functionality of "inner to outer" refactoring.
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public class InnerToOuterRefactoringPlugin extends JavaRefactoringPlugin {
    /** Reference to the parent refactoring instance */
    private final InnerToOuterRefactoring refactoring;
    private TreePathHandle treePathHandle;
    
    
    /** Creates a new instance of InnerToOuterRefactoringPlugin
     * @param refactoring Parent refactoring instance.
     */
    InnerToOuterRefactoringPlugin(InnerToOuterRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
        case PRECHECK:
            ClasspathInfo cpInfo = getClasspathInfo(refactoring);
            return JavaSource.create(cpInfo, treePathHandle.getFileObject());
        default:
            return JavaSource.forFileObject(treePathHandle.getFileObject());
        }
    }
    
    @Override
    protected Problem preCheck(CompilationController info) throws IOException {
        // fire operation start on the registered progress listeners (4 steps)
        fireProgressListenerStart(refactoring.PRE_CHECK, 4);
        Problem preCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);
        Element el = treePathHandle.resolveElement(info);
        TreePathHandle sourceType = refactoring.getSourceType();
        
        // check whether the element is valid
        Problem result = isElementAvail(sourceType, info);
        if (result != null) {
            // fatal error -> don't continue with further checks
            return result;
        }
        result = JavaPluginUtils.isSourceElement(el, info);
        if (result != null) {
            return result;
        }
        
        
        refactoring.setClassName(sourceType.resolveElement(info).getSimpleName().toString());
        
        // increase progress (step 1)
        fireProgressListenerStep();
        
        // #1 - check if the class is an inner class
        //            RefObject declCls = (RefObject) sourceType.refImmediateComposite();
        if (el instanceof TypeElement) {
            if (((TypeElement)el).getNestingKind() == NestingKind.ANONYMOUS) {
                // fatal error -> return
                preCheckProblem = new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_InnerToOuter_Anonymous")); // NOI18N
                return preCheckProblem;
            }
            if (!((TypeElement)el).getNestingKind().isNested()) {
                // fatal error -> return
                preCheckProblem = new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_InnerToOuter_MustBeInnerClass")); // NOI18N
                return preCheckProblem;
            }
        } else {
            preCheckProblem = new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_InnerToOuter_MustBeInnerClass")); // NOI18N
            return preCheckProblem;
        }
        
        // increase progress (step 2)
        fireProgressListenerStep();
        
        fireProgressListenerStop();
        return preCheckProblem;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    @NbBundle.Messages(
            "ERR_InnerToOuter_ClassNameClash=Inner class named <b>{0}</b> already exists in the target class.")
    protected Problem fastCheckParameters(final CompilationController javac) throws IOException {
        Problem problem = null;
        String name = refactoring.getReferenceName();
        javac.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
        Element resolved = refactoring.getSourceType().resolveElement(javac);
        if(resolved != null && resolved.getKind().isClass()) {
            if(name != null) {
                List<VariableElement> fieldsIn = ElementFilter.fieldsIn(((TypeElement)resolved).getEnclosedElements());
                for (VariableElement variableElement : fieldsIn) {
                    if(variableElement.getSimpleName().toString().equals(name)) {
                        problem = new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_OuterNameAlreadyUsed", name, variableElement.getEnclosingElement().getSimpleName())); // NOI18N
                        return problem;
                    }
                }
                
                fieldsIn = ElementFilter.fieldsIn(javac.getElements().getAllMembers((TypeElement)resolved));
                for (VariableElement variableElement : fieldsIn) {
                    if(variableElement.getSimpleName().toString().equals(name)) {
                        problem = new Problem(false, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "WRN_OuterNameAlreadyUsed", name, variableElement.getEnclosingElement().getSimpleName())); // NOI18N
                        break;
                    }
                }
                
                List<ExecutableElement> constructors = ElementFilter.constructorsIn(((TypeElement)resolved).getEnclosedElements());
                for( ExecutableElement execElement: constructors ) {
                    List<? extends VariableElement> parameters = execElement.getParameters();
                    
                    for( VariableElement variableElement: parameters ) {
                        if(variableElement.getSimpleName().toString().equals(name)) {
                            return new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_InnerToOuter_OuterNameClash", name, resolved.getSimpleName())); // NOI18N
                        }
                    }
                }
            }
            String className = refactoring.getClassName();
            if(className != null) {
                TypeElement outer = javac.getElementUtilities().enclosingTypeElement(resolved);
                Element outerouter = outer.getEnclosingElement();
                
                if (outerouter.getKind() != ElementKind.PACKAGE) {
                    if(!outerouter.getKind().isClass()) {
                        outerouter = javac.getElementUtilities().enclosingTypeElement(outerouter);
                    }
                    List<TypeElement> types = ElementFilter.typesIn(javac.getElements().getAllMembers((TypeElement)outerouter));
                    for (TypeElement type : types) {
                        if (className.contentEquals(type.getSimpleName()) && type != resolved) {
                            return new Problem(true, ERR_InnerToOuter_ClassNameClash(className)); // NOI18N
                        }
                    }
                }
            }
        }
        return problem;
    }

    @Override
    public Problem fastCheckParameters() {
        Problem result = null;
        
        String newName = refactoring.getClassName();
        
        if (!Utilities.isJavaIdentifier(newName)) {
            result = createProblem(result, true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_InvalidIdentifier", newName)); // NOI18N
            return result;
        }
        String referenceName = refactoring.getReferenceName();
        if(referenceName != null) {
            if (referenceName.length() < 1) {
                result = new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_EmptyReferenceName")); // NOI18N
                return result;
            } else {
                if (!Utilities.isJavaIdentifier(referenceName)) {
                    result = new Problem(true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_InvalidIdentifier", referenceName)); // NOI18N
                    return result;
                }
            }
        }
        
        FileObject primFile = refactoring.getSourceType().getFileObject();
        FileObject folder = primFile.getParent();
        FileObject[] children = folder.getChildren();
        for (FileObject child: children) {
            if (!child.isVirtual() && child.getName().equals(newName) && "java".equals(child.getExt())) { // NOI18N
                result = createProblem(result, true, NbBundle.getMessage(InnerToOuterRefactoringPlugin.class, "ERR_ClassClash", newName, folder.getName())); // NOI18N
                return result;
            }
        }

        return super.fastCheckParameters();
    }

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        HashSet<FileObject> set = new LinkedHashSet<FileObject>();
        set.add(refactoring.getSourceType().getFileObject());
        ClassIndex idx = cpInfo.getClassIndex();
        set.addAll(idx.getResources(refactoring.getSourceType().getElementHandle(), EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        return set;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(AbstractRefactoring.PREPARE, a.size());
        final InnerToOuterTransformer innerToOuter = new InnerToOuterTransformer(refactoring);
        TransformTask transform = new TransformTask(innerToOuter, refactoring.getSourceType());
        Problem problem = createAndAddElements(a, transform, refactoringElements, refactoring);
        fireProgressListenerStop();
        return problem != null ? problem : innerToOuter.getProblem();
    }
}
