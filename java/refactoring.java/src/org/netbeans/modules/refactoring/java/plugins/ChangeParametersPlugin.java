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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.ChangeParametersPanel.Javadoc;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Refactoring used for changing method signature. It changes method declaration
 * and also all its references (callers).
 *
 * @author  Pavel Flaska
 * @author  Tomas Hurka
 * @author  Jan Becicka
 * @author  Ralph Ruijs
 */
public class ChangeParametersPlugin extends JavaRefactoringPlugin {
    
    private ChangeParametersRefactoring refactoring;
    private TreePathHandle treePathHandle;
    private boolean inited;
    
    /**
     * Creates a new instance of change parameters refactoring.
     *
     * @param method  refactored object, i.e. method or constructor
     */
    public ChangeParametersPlugin(ChangeParametersRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }
    
    @Override
    public Problem checkParameters() {
        Problem p = null;
        // TODO: Rename checks
        return p;
    }

    @Override
    public Problem fastCheckParameters(final CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        ParameterInfo paramTable[] = refactoring.getParameterInfo();
        
        final ExecutableElement method = (ExecutableElement) treePathHandle.resolveElement(javac);
        Problem p=null;
        boolean isConstructor = method.getKind() == ElementKind.CONSTRUCTOR;
        final Checks check = new Checks(javac);
        
        p = check.methodName(p, refactoring.getMethodName());
        
        TypeElement enclosingTypeElement = javac.getElementUtilities().enclosingTypeElement(method);
        List<? extends Element> allMembers = javac.getElements().getAllMembers(enclosingTypeElement);

        if(!isConstructor) {
            p = check.returnType(p, refactoring.getReturnType(), method, enclosingTypeElement);
            p = check.duplicateSignature(p, paramTable, method, enclosingTypeElement, allMembers);
            p = check.accessModifiers(p, refactoring.getModifiers() != null ? refactoring.getModifiers() : method.getModifiers(), method, enclosingTypeElement, paramTable, cancelRequested);
        } else {
            p = check.duplicateConstructor(p, paramTable, method, enclosingTypeElement, allMembers);
        }
        for (int i = 0; i< paramTable.length; i++) {
            ParameterInfo parameterInfo = paramTable[i];

            p = check.checkParameterName(p, parameterInfo.getName());
            if (parameterInfo.getOriginalIndex() == -1) {
                p = check.defaultValue(p, parameterInfo.getDefaultValue());
            }
            p = check.duplicateParamName(p, paramTable, i);
            p = check.duplicateLocalName(p, paramTable, i, method);
            p = check.parameterType(p, paramTable, i, method, enclosingTypeElement);
        }
        return p;
    }
    
    private static String newParMessage(String par) {
        return new MessageFormat(
                getString("ERR_newpar")).format(new Object[] { getString(par) } // NOI18N
            );
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(ChangeParametersPlugin.class, key);
    }

    private Set<ElementHandle<ExecutableElement>> allMethods;
    
    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new LinkedHashSet<FileObject>();
        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        set.add(tph.getFileObject());
        JavaSource source = JavaSource.create(cpInfo, tph.getFileObject());
        
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                @Override
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                
                @Override
                public void run(CompilationController info) throws Exception {
                    final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    final ElementUtilities elmUtils = info.getElementUtilities();

                    //add all references of overriding methods
                    ExecutableElement el = (ExecutableElement)treePathHandle.resolveElement(info);
                    ElementHandle<ExecutableElement> methodHandle = ElementHandle.create(el);
                    ElementHandle<TypeElement>  enclosingType = ElementHandle.create(elmUtils.enclosingTypeElement(el));
                    allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                    allMethods.add(methodHandle);
                    for (ExecutableElement e:JavaRefactoringUtils.getOverridingMethods(el, info,cancelRequested)) {
                        ElementHandle<ExecutableElement> handle = ElementHandle.create(e);
                        set.add(SourceUtils.getFile(handle, info.getClasspathInfo()));
                        ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                        set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        allMethods.add(ElementHandle.create(e));
                    }
                    //add all references of overriden methods
                    for (ExecutableElement e:JavaRefactoringUtils.getOverriddenMethods(el, info)) {
                        ElementHandle<ExecutableElement> handle = ElementHandle.create(e);
                        set.add(SourceUtils.getFile(handle, info.getClasspathInfo()));
                        ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                        set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        allMethods.add(ElementHandle.create(e));
                    }
                    set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                    set.add(SourceUtils.getFile(methodHandle, info.getClasspathInfo()));
                }
            }, true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return set;
    }
    
    
    @Override
    public Problem prepare(RefactoringElementsBag elements) {
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(AbstractRefactoring.PREPARE, (a.size() * 2) + 1);
        Problem problem = null;
        if (!a.isEmpty()) {
            fireProgressListenerStep();

            ChangeParamsTransformer changeParamsTransformer = new ChangeParamsTransformer(refactoring.getParameterInfo(),
                    refactoring.getModifiers(),
                    refactoring.getReturnType(),
                    refactoring.getMethodName(),
                    refactoring.isOverloadMethod(),
                    refactoring.getContext().lookup(Javadoc.class),
                    allMethods,
                    treePathHandle);
            
//            ChangeParamsJavaDocTransformer changeJavaDocParamsTransformer = new ChangeParamsJavaDocTransformer(refactoring.getParameterInfo(),
//                    refactoring.getReturnType(),
//                    refactoring.isOverloadMethod(),
//                    refactoring.getContext().lookup(Javadoc.class),
//                    allMethods,
//                    treePathHandle);
//
//            TransformTask transformJavadoc = new TransformTask(changeJavaDocParamsTransformer, treePathHandle);
//            problem = JavaPluginUtils.chainProblems(problem, createAndAddElements(a, transformJavadoc, elements, refactoring));
            TransformTask transform = new TransformTask(changeParamsTransformer, treePathHandle);
            problem = JavaPluginUtils.chainProblems(problem, createAndAddElements(a, transform, elements, refactoring, getClasspathInfo(refactoring)));
            problem = JavaPluginUtils.chainProblems(problem, changeParamsTransformer.getProblem());
        }
        fireProgressListenerStop();
        return problem;
    }
    
    @Override
    protected JavaSource getJavaSource(JavaRefactoringPlugin.Phase p) {
        switch(p) {
            case CHECKPARAMETERS:
            case FASTCHECKPARAMETERS:    
            case PRECHECK:
            case PREPARE:
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                return JavaSource.create(cpInfo, treePathHandle.getFileObject());
        }
        return null;
    }
    /**
     * Returns list of problems. For the change method signature, there are two
     * possible warnings - if the method is overriden or if it overrides
     * another method.
     *
     * @return  overrides or overriden problem or both
     */
    @Override
    public Problem preCheck(CompilationController info) throws IOException {
        fireProgressListenerStart(refactoring.PRE_CHECK, 4);
        Problem preCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);
        preCheckProblem = isElementAvail(treePathHandle, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        Element el = treePathHandle.resolveElement(info);
        if (!RefactoringUtils.isExecutableElement(el)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ChangeParamsWrongType")); // NOI18N
            return preCheckProblem;
        }

        preCheckProblem = JavaPluginUtils.isSourceElement(el, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }

        if (info.getElementUtilities().enclosingTypeElement(el).getKind() == ElementKind.ANNOTATION_TYPE) {
            preCheckProblem =new Problem(true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_MethodsInAnnotationsNotSupported")); // NOI18N
            return preCheckProblem;
        }
        
        for (ExecutableElement e : JavaRefactoringUtils.getOverriddenMethods((ExecutableElement) el, info)) {
            ElementHandle<ExecutableElement> handle = ElementHandle.create(e);
            if (RefactoringUtils.isFromLibrary(handle, info.getClasspathInfo())) { //NOI18N
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_CannnotRefactorLibrary", el)); // NOI18N
            }
        }
                    
        fireProgressListenerStop();
        return preCheckProblem;
    }
    
//    private void initDelegates() {
//        if (inited) {
//            return;
//        }
//        final LinkedList<RenameRefactoring> renameRefactoringsList = new LinkedList<RenameRefactoring>();
//
//        try {
//            getJavaSource(Phase.PREPARE).runUserActionTask(new Task<CompilationController>() {
//
//                @Override
//                public void run(CompilationController javac) throws Exception {
//                    javac.toPhase(JavaSource.Phase.RESOLVED);
//                    ExecutableElement method = (ExecutableElement) treePathHandle.resolveElement(javac);
//                    List<? extends VariableElement> parameters = method.getParameters();
//
//                    ParameterInfo paramTable[] = refactoring.getParameterInfo();
//                    for (int i = 0; i < paramTable.length; i++) {
//                        ParameterInfo param = paramTable[i];
//                        int origIndex = param.getOriginalIndex();
//                        if (origIndex != -1) {
//                            VariableElement variable = parameters.get(origIndex);
//                            if(!variable.getSimpleName().contentEquals(param.getName())) {
//                                TreePath path = javac.getTrees().getPath(variable);
//                                RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(path, javac)));
//                                renameRefactoring.setNewName(param.getName());
//                                renameRefactoring.setSearchInComments(true);
//                                renameRefactoringsList.add(renameRefactoring);
//                            }
//                        }
//                    }
//                    if(refactoring.getMethodName() != null && !method.getSimpleName().toString().equals(refactoring.getMethodName())) {
//                        RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(method, javac)));
//                        renameRefactoring.setNewName(refactoring.getMethodName());
//                        renameRefactoring.setSearchInComments(true);
//                        renameRefactoringsList.add(renameRefactoring);
//                    }
//                }
//            }, true);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        inited = true;
//    }
    
    static class Checks {
        protected CompilationController javac;

        public Checks(CompilationController javac) {
            this.javac = javac;
        }
        
        Problem methodName(Problem p, String methodName) {
            if(methodName != null && !Utilities.isJavaIdentifier(methodName)) {
                p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_InvalidIdentifier", methodName)); // NOI18N
            }
            return p;
        }

        Problem defaultValue(Problem p, String defaultValue) {
            if ((defaultValue == null || defaultValue.length() < 1)) {
                p = createProblem(p, true, newParMessage("ERR_pardefv")); // NOI18N
            }
            return p;
        }
        
        Problem checkParameterName(Problem p, String s) {
            if ((s == null || s.length() < 1)) {
                p = createProblem(p, true, newParMessage("ERR_parname"));
            } // NOI18N
            else {
                if (!Utilities.isJavaIdentifier(s)) {
                    p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_InvalidIdentifier",s)); // NOI18N
                }
            }

            return p;
        }
        
        Problem duplicateParamName(Problem p, ParameterInfo[] paramTable, int index) {
            String name = paramTable[index].getName();
            for (int j = 0; j < paramTable.length; j++) {
                ParameterInfo pInfo = paramTable[j];
                if(pInfo.getName().equals(name) && index != j) {
                    p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ParamAlreadyUsed", name)); // NOI18N
                }
            }
            return p;
        }


        Problem returnType(Problem p, String returnType, ExecutableElement method, TypeElement enclosingTypeElement) {
            TypeMirror parseType;
            if (returnType != null && returnType.length() < 1) {
                p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_NoReturn", returnType)); // NOI18N
            } else if (returnType != null) {
                TypeElement typeElement = javac.getElements().getTypeElement(returnType);
                parseType = typeElement == null ? null : typeElement.asType();
                if(parseType == null) {
                    boolean isGenericType = false;
                    List<? extends TypeParameterElement> typeParameters = method.getTypeParameters();
                    for (TypeParameterElement typeParameterElement : typeParameters) {
                        TypeParameterTree tpTree = (TypeParameterTree) javac.getTrees().getTree(typeParameterElement);
                        if(returnType.equals(tpTree.getName().toString())) {
                            isGenericType = true;
                        }
                    }
                    if(!isGenericType) {
                        parseType = javac.getTreeUtilities().parseType(returnType, enclosingTypeElement);
                        if(parseType == null || parseType.getKind() == TypeKind.ERROR) {
                            p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "WRN_canNotResolveReturn", returnType)); // NOI18N
                        }
                    }
                }
            }
            return p;
        }

        Problem duplicateSignature(Problem p, ParameterInfo[] paramTable, final ExecutableElement method, TypeElement enclosingTypeElement, List<? extends Element> allMembers) {
            List<ExecutableElement> methods = ElementFilter.methodsIn(allMembers);
            for (ExecutableElement exMethod : methods) {
                if(!exMethod.equals(method)) {
                    if(exMethod.getSimpleName().equals(method.getSimpleName())
                            && exMethod.getParameters().size() == paramTable.length) {
                        boolean sameParameters = true;
                        boolean wideningConversion = true;
                        for (int j = 0; j < exMethod.getParameters().size(); j++) {
                            TypeMirror exType = ((VariableElement)exMethod.getParameters().get(j)).asType();
                            String type = paramTable[j].getType();
                            TypeMirror paramType = javac.getTreeUtilities().parseType(type, enclosingTypeElement);
                            if(!javac.getTypes().isSameType(exType, paramType)) {
                                sameParameters = false;
                                if(exType.getKind().isPrimitive() && paramType.getKind().isPrimitive()) {
                                    /*
                                     * byte to short, int, long, float, or double
                                     * short to int, long, float, or double
                                     * char to int, long, float, or double
                                     * int to long, float, or double
                                     * long to float or double
                                     * float to double
                                     */
                                    switch (exType.getKind()) {
                                        case DOUBLE:
                                            if(paramType.getKind() == TypeKind.FLOAT) {
                                                break;
                                            }
                                        case FLOAT:
                                            if(paramType.getKind() == TypeKind.LONG) {
                                                break;
                                            }
                                        case LONG:
                                            if(paramType.getKind() == TypeKind.INT) {
                                                break;
                                            }
                                        case INT:
                                            if(paramType.getKind() == TypeKind.SHORT) {
                                                break;
                                            }
                                        case SHORT:
                                            if(paramType.getKind() == TypeKind.BYTE) {
                                                break;
                                            }
                                        case BYTE:
                                            wideningConversion = false;
                                            break;
                                    }
                                } else {
                                    wideningConversion = false;
                                }
                            }
                        }
                        if(sameParameters) {
                            p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_existingMethod", exMethod.toString(), enclosingTypeElement.getQualifiedName())); // NOI18N
                        } else if(wideningConversion) {
                            p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "WRN_wideningConversion", exMethod.toString(), enclosingTypeElement.getQualifiedName())); // NOI18N
                        }
                    }
                }
            }
            return p;
        }

        Problem parameterType(Problem p, ParameterInfo[] paramTable, int index, ExecutableElement method, TypeElement enclosingTypeElement) {
            String type = paramTable[index].getType();
            String name = paramTable[index].getName();
            type = type.split("<", 2)[0];
            String[] split = type.split(" "); //NOI18N
            type = split[split.length-1];
            TypeMirror parseType = null;
            if (type == null || type.length() < 1) {
                p = createProblem(p, true, newParMessage("ERR_partype")); // NOI18N
            } else {
                TypeElement typeElement = javac.getElements().getTypeElement(type);
                parseType = typeElement == null ? null : typeElement.asType();
                if(parseType == null) {
                    boolean isGenericType = false;
                    List<? extends TypeParameterElement> typeParameters = method.getTypeParameters();
                    for (TypeParameterElement typeParameterElement : typeParameters) {
                        TypeParameterTree tpTree = (TypeParameterTree) javac.getTrees().getTree(typeParameterElement);
                        if(type.equals(tpTree.getName().toString())) {
                            isGenericType = true;
                        }
                    }
                    if(!isGenericType) {
                        parseType = javac.getTreeUtilities().parseType(type, enclosingTypeElement);
                        if(parseType == null || parseType.getKind() == TypeKind.ERROR) {
                            p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "WRN_canNotResolve", type, name)); // NOI18N
                        }
                    }
                }
            }
            // check if the changed type is assigneble
            int originalIndex = paramTable[index].getOriginalIndex();
            if(parseType != null && originalIndex > -1) {
                final VariableElement parameterElement = method.getParameters().get(originalIndex);
                if(!javac.getTypes().isAssignable(parseType, parameterElement.asType())) {
                    if(type != null && type.endsWith("...") && parameterElement.asType().getKind() == TypeKind.ARRAY) { //NOI18N
                        ArrayType arrayType = (ArrayType) parameterElement.asType();
                        if(!javac.getTypes().isAssignable(parseType, arrayType.getComponentType())) {
                            p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "WRN_isNotAssignable", parameterElement.asType().toString(), type)); // NOI18N
                        }
                    } else {
                        p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "WRN_isNotAssignable", parameterElement.asType().toString(), type)); // NOI18N
                    }
                }
            }
            // check ...
            if (type != null && type.endsWith("...") && index != paramTable.length - 1) {//NOI18N
                p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_VarargsFinalPosition", new Object[]{})); // NOI18N
            }
            return p;
        }

        Problem duplicateLocalName(Problem p, ParameterInfo[] paramTable, int index, ExecutableElement method) {
            String name = paramTable[index].getName();
            int originalIndex = paramTable[index].getOriginalIndex();
            final VariableElement parameterElement = originalIndex == -1 ? null : method.getParameters().get(originalIndex);
            ErrorAwareTreeScanner<Boolean, String> scanner = new ErrorAwareTreeScanner<Boolean, String>() {

                @Override
                public Boolean visitVariable(VariableTree vt, String p) {
                    super.visitVariable(vt, p);
                    TreePath path = javac.getTrees().getPath(javac.getCompilationUnit(), vt);
                    Element element = javac.getTrees().getElement(path);

                    return vt.getName().contentEquals(p) && (element == null || !element.equals(parameterElement));
                }

                @Override
                public Boolean reduce(Boolean left, Boolean right) {
                    return (left == null ? false : left) || (right == null ? false : right);
                }
            };

            if (scanner.scan(javac.getTrees().getTree(method), name)) {
                if (!isParameterBeingRemoved(method, name, paramTable)) {
                    p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_NameAlreadyUsed", name)); // NOI18N
                }
            }
            return p;
        }

        Problem duplicateConstructor(Problem p, ParameterInfo[] paramTable, final ExecutableElement method, TypeElement enclosingTypeElement, List<? extends Element> allMembers) {
            List<ExecutableElement> constructors = ElementFilter.constructorsIn(allMembers);
            for (ExecutableElement constructor : constructors) {
                if(!constructor.equals(method)) {
                    if(constructor.getParameters().size() == paramTable.length) {
                        boolean sameParameters = true;
                        for (int j = 0; j < constructor.getParameters().size(); j++) {
                            TypeMirror exType = ((VariableElement)constructor.getParameters().get(j)).asType();
                            String type = paramTable[j].getType();
                            TypeMirror paramType = javac.getTreeUtilities().parseType(type, enclosingTypeElement);
                            if(!javac.getTypes().isSameType(exType, paramType)) {
                                sameParameters = false;
                            }
                        }
                        if(sameParameters) {
                            p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_existingConstructor", constructor.toString(), enclosingTypeElement.getQualifiedName())); // NOI18N
                        }
                    }
                }
            }
            return p;
        }
        
        
        private Problem accessModifiers(Problem p, Set<Modifier> modifiers, ExecutableElement method, TypeElement enclosingTypeElement, ParameterInfo[] paramTable, AtomicBoolean cancel) {
            List<ExecutableElement> allMethods = findDuplicateSubMethods(enclosingTypeElement, method, paramTable, cancel);
            if(!allMethods.isEmpty()) {
                Collection<ExecutableElement> overridingMethods = JavaRefactoringUtils.getOverridingMethods(method, javac,cancel);
                boolean willBeOverriden = false;
                for (ExecutableElement executableElement : allMethods) {
                    if(!overridingMethods.contains(executableElement)) {
                        willBeOverriden = true;
                        break;
                    }
                }
                if(willBeOverriden) {
                    p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "WRN_MethodIsOverridden", enclosingTypeElement.toString())); // NOI18N
                }
            }
            for (ExecutableElement exMethod : allMethods) {
                if(!javac.getTypes().isSameType(exMethod.getReturnType(),method.getReturnType())) {
                    p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_existingReturnType", exMethod.getSimpleName(), exMethod.getEnclosingElement().getSimpleName(), method.getReturnType().toString(), exMethod.getReturnType().toString())); // NOI18N
                }
            }
            for (ExecutableElement exMethod : allMethods) {
                if(!modifiers.contains(Modifier.PRIVATE)) {
                    if(RefactoringUtils.isWeakerAccess(exMethod.getModifiers(), modifiers)) {
                        p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_WeakerAccess", exMethod.getSimpleName(), exMethod.getEnclosingElement().getSimpleName())); // NOI18N
                    }
                }
            }
            return p;
        }

        private List<ExecutableElement> findDuplicateSubMethods(TypeElement enclosingTypeElement, ExecutableElement method, ParameterInfo[] paramTable, AtomicBoolean cancel) {
            List<ExecutableElement> returnmethods = new LinkedList<ExecutableElement>();
            Set<ElementHandle<TypeElement>> subTypes = RefactoringUtils.getImplementorsAsHandles(javac.getClasspathInfo().getClassIndex(), javac.getClasspathInfo(), enclosingTypeElement, cancel);
            for (ElementHandle<TypeElement> elementHandle : subTypes) {
                TypeElement subtype = elementHandle.resolve(javac);
                if(subtype != null) {
                    List<ExecutableElement> methods = ElementFilter.methodsIn(javac.getElements().getAllMembers(subtype));
                    for (ExecutableElement exMethod : methods) {
                        if (!exMethod.equals(method)) {
                            if (exMethod.getSimpleName().equals(method.getSimpleName())
                                    && exMethod.getParameters().size() == paramTable.length) {
                                boolean sameParameters = true;
                                for (int j = 0; j < exMethod.getParameters().size(); j++) {
                                    TypeMirror exType = ((VariableElement) exMethod.getParameters().get(j)).asType();
                                    String type = paramTable[j].getType();
                                    if (type == null || type.length() == 0) {
                                        sameParameters = false;
                                    } else {
                                        TypeMirror paramType = javac.getTreeUtilities().parseType(type, enclosingTypeElement);
                                        if (!javac.getTypes().isSameType(exType, paramType)) {
                                            sameParameters = false;
                                        }
                                    }
                                }
                                if (sameParameters) {
                                    returnmethods.add(exMethod);
                                }
                            }
                        }
                    }
                }
            }
            return returnmethods;
        }

        boolean isParameterBeingRemoved(ExecutableElement method, String s, ParameterInfo[] paramTable) {
            boolean beingRemoved = false;
            for (int j = 0; j < method.getParameters().size(); j++) {
                VariableElement variable = method.getParameters().get(j);
                if (variable.getSimpleName().contentEquals(s)) {

                    boolean isInNewList = false;
                    for (ParameterInfo parameterInfo : paramTable) {
                        if (parameterInfo.getOriginalIndex() == j) {
                            isInNewList = true;
                        }
                    }
                    beingRemoved = !isInNewList;
                    break;
                }
            }
            return beingRemoved;
        }
    }
}
