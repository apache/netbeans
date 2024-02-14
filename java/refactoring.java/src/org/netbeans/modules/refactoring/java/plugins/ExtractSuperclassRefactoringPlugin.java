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

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.ExtractSuperclassRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Plugin that implements the core functionality of Extract Super Class refactoring.
 *
 * @author Martin Matula, Jan Pokorsky
 */
public final class ExtractSuperclassRefactoringPlugin extends JavaRefactoringPlugin {
    /** Reference to the parent refactoring instance */
    private final ExtractSuperclassRefactoring refactoring;
    
    /** source class */
    private ElementHandle<TypeElement> classHandle;
        
    private String pkgName;

    /** Creates a new instance of ExtractSuperClassRefactoringPlugin
     * @param refactoring Parent refactoring instance.
     */
    ExtractSuperclassRefactoringPlugin(ExtractSuperclassRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        return JavaSource.forFileObject(refactoring.getSourceType().getFileObject());
    }

    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        // fire operation start on the registered progress listeners (2 step)
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 2);
        javac.toPhase(JavaSource.Phase.RESOLVED);
        try {
            TreePathHandle sourceType = refactoring.getSourceType();
            
            // check whether the element is valid
            Problem result = isElementAvail(sourceType, javac);
            if (result != null) {
                // fatal error -> don't continue with further checks
                return result;
            }
            
            // check whether the element is an unresolved class
            Element sourceElm = sourceType.resolveElement(javac);
            result = JavaPluginUtils.isSourceElement(sourceElm, javac);
            if (result != null) {
                return result;
            }
            if (sourceElm == null) {
                // fatal error -> return
                return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
            }

            if (sourceElm.getKind() != ElementKind.CLASS) {
                return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ExtractSC_MustBeClass"));
            }
            
            classHandle = ElementHandle.<TypeElement>create((TypeElement) sourceElm);
            
            PackageElement pkgElm = (PackageElement) javac.getElementUtilities().outermostTypeElement(sourceElm).getEnclosingElement();
            pkgName = pkgElm.getQualifiedName().toString();
            
            // increase progress (step 1)
            fireProgressListenerStep();
            
            // all checks passed -> return null
            return null;
        } finally {
            // fire operation end on the registered progress listeners
            fireProgressListenerStop();
        }
    }
    
    @Override
    public Problem fastCheckParameters() {
        Problem result = null;
        
        String newName = refactoring.getSuperClassName();
        
        if (!Utilities.isJavaIdentifier(newName)) {
            result = createProblem(result, true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_InvalidIdentifier", newName)); // NOI18N
            return result;
        }
        
        FileObject primFile = refactoring.getSourceType().getFileObject();
        FileObject folder = primFile.getParent();
        FileObject[] children = folder.getChildren();
        for (FileObject child: children) {
            if (!child.isVirtual() && child.getName().equals(newName) && "java".equals(child.getExt())) { // NOI18N
                result = createProblem(result, true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ClassClash", newName, pkgName)); // NOI18N
                return result;
            }
        }

        return super.fastCheckParameters();
    }

    @Override
    protected Problem fastCheckParameters(CompilationController javac) throws IOException {
        Problem result = null;
        String newName = refactoring.getSuperClassName();
        TypeMirror parsedType = javac.getTreeUtilities().parseType(newName, classHandle.resolve(javac));
        if(parsedType != null && parsedType.getKind() != TypeKind.ERROR) {
            result = createProblem(result, true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ClassClash", newName, pkgName)); // NOI18N
            return result;
        }
        return super.fastCheckParameters(javac);
    }

    @Override
    public Problem checkParameters() {
        MemberInfo[] members = refactoring.getMembers();
        if (members.length == 0) {
            return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ExtractSuperClass_MembersNotAvailable")); // NOI18N);
        }
        return super.checkParameters();

    }
    
    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        
        TypeElement sourceType = (TypeElement) refactoring.getSourceType().resolveElement(javac);
        assert sourceType != null;
        
        Set<? extends Element> members = new HashSet<Element>(sourceType.getEnclosedElements());
        
        fireProgressListenerStart(AbstractRefactoring.PARAMETERS_CHECK, refactoring.getMembers().length);
        try {
            for (MemberInfo info : refactoring.getMembers()) {
                Problem p = null;
                switch(info.getGroup()) {
                case FIELD:
                    @SuppressWarnings("unchecked")
                    ElementHandle<VariableElement> vehandle = (ElementHandle<VariableElement>) info.getElementHandle();
                    VariableElement field = vehandle.resolve(javac);
                    p = checkFieldParameter(javac, field, members);
                    break;
                case METHOD:
                    @SuppressWarnings("unchecked")
                    ElementHandle<ExecutableElement> eehandle = (ElementHandle<ExecutableElement>) info.getElementHandle();
                    ExecutableElement method = eehandle.resolve(javac);
                    p = checkMethodParameter(javac, method, members);
                    break;
                }

                if (p != null) {
                    return p;
                }
                
                fireProgressListenerStep();
            }
        } finally {
            fireProgressListenerStop();
        }

        // XXX check refactoring.getImplements()

        return null;
    }
    
    private Problem checkFieldParameter(CompilationController javac, VariableElement elm, Set<? extends Element> members) throws IOException {
        if (elm == null) {
            return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
        }
        if (javac.getElementUtilities().isSynthetic(elm) || elm.getKind() != ElementKind.FIELD) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
        if (!members.contains(elm)) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
//        Set<Modifier> mods = elm.getModifiers();
//        if (mods.contains(Modifier.PUBLIC) && mods.contains(Modifier.STATIC) && mods.contains(Modifier.FINAL)) {
//            VariableTree tree = (VariableTree) javac.getTrees().getTree(elm);
//            if (tree.getInitializer() != null) {
//                continue;
//            }
//        }
//        return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_WrongModifiers", elm.getSimpleName().toString())); // NOI18N
        return null;
    }
    
    private Problem checkMethodParameter(CompilationController javac, ExecutableElement elm, Set<? extends Element> members) throws IOException {
        if (elm == null) {
            return new Problem(true, NbBundle.getMessage(ExtractSuperclassRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
        }
        if (javac.getElementUtilities().isSynthetic(elm) || elm.getKind() != ElementKind.METHOD) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
        if (!members.contains(elm)) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractSuperClass_UnknownMember", // NOI18N
                    elm.toString()));
        }
//        Set<Modifier> mods = elm.getModifiers();
//        if (!mods.contains(Modifier.PUBLIC) || mods.contains(Modifier.STATIC)) {
//            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_WrongModifiers", elm.getSimpleName().toString())); // NOI18N
//        }
        return null;
        
    }

    private Set<FileObject> getRelevantFiles() {
        final Set<FileObject> set = new HashSet<FileObject>();
        set.add(refactoring.getSourceType().getFileObject());
        return set;
    }
    
    private ClasspathInfo getClasspathInfo(Set<FileObject> a) {
        ClasspathInfo cpInfo;
        cpInfo = JavaRefactoringUtils.getClasspathInfoFor(a.toArray(new FileObject[0]));
        return cpInfo;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag bag) {
        RefactoringVisitor visitor = new ExtractSuperclassTransformer(refactoring, classHandle);
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(AbstractRefactoring.PREPARE, a.size());
        TransformTask transform = new TransformTask(visitor, refactoring.getSourceType());
        Problem problem = createAndAddElements(a, transform, bag, refactoring, getClasspathInfo(a));
        fireProgressListenerStop();
        return problem;
    }
    
    private static final class ExtractSuperclassTransformer extends RefactoringVisitor {
        private final ExtractSuperclassRefactoring refactoring;
        private final ElementHandle<TypeElement> sourceType;
        private List<Tree> members;
        private boolean makeAbstract;
        private List<Tree> members2Remove;
        
        private ExtractSuperclassTransformer(ExtractSuperclassRefactoring refactoring, ElementHandle<TypeElement> sourceType) {
            this.sourceType = sourceType;
            this.refactoring = refactoring;
        }
        
        @Override
        public Tree visitClass(ClassTree classTree, Element p) {
            TypeElement clazz = this.sourceType.resolve(workingCopy);
            assert clazz != null;
            Element current = workingCopy.getTrees().getElement(getCurrentPath());
            if(current == clazz) {
                GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
                makeAbstract = false;
                members = new LinkedList<Tree>();
                members2Remove = new LinkedList<Tree>();
                addConstructors(clazz);
                super.visitClass(classTree, p);

                List<Tree> implementsList = new ArrayList<Tree>();
                for (MemberInfo/*<ElementHandle<? extends Element>>*/ member : refactoring.getMembers()) {
                    if (member.getGroup() == MemberInfo.Group.IMPLEMENTS) {
                        TypeMirrorHandle handle = (TypeMirrorHandle) member.getElementHandle();
                        // XXX check if interface is not aready there; the templates might be changed by user :-(
                        TypeMirror implMirror = handle.resolve(workingCopy);
                        implementsList.add(make.Type(implMirror));
                        // XXX needs more granular check
                        makeAbstract |= true;
                    }
                }
                DeclaredType supType = (DeclaredType) clazz.getSuperclass();
                TypeElement supEl = (TypeElement) supType.asElement();
                Tree superClass = supEl.getSuperclass().getKind() == TypeKind.NONE
                        ? null
                        : make.Type(supType);
                makeAbstract |= supEl.getModifiers().contains(Modifier.ABSTRACT);
                ModifiersTree classModifiersTree = make.Modifiers(makeAbstract ? EnumSet.of(Modifier.PUBLIC, Modifier.ABSTRACT) : EnumSet.of(Modifier.PUBLIC));
                final List<? extends TypeMirror> typeParams = findUsedGenericTypes(clazz);
                List<TypeParameterTree> newTypeParams = new ArrayList<TypeParameterTree>(typeParams.size());
                for (TypeParameterElement typeParam : clazz.getTypeParameters()) {
                    TypeMirror origParam = typeParam.asType();
                    for (TypeMirror newParam : typeParams) {
                        if (workingCopy.getTypes().isSameType(origParam, newParam)) {
                            Tree t = workingCopy.getTrees().getTree(typeParam);
                            if (t.getKind() == Tree.Kind.TYPE_PARAMETER) {
                                TypeParameterTree typeParamTree = (TypeParameterTree) t;
                                if (!typeParamTree.getBounds().isEmpty()) {
                                    typeParamTree = (TypeParameterTree) genUtils.importFQNs(t);
                                }
                                newTypeParams.add(typeParamTree);
                            }
                        }
                    }
                }
                ClassTree newClassTree = make.Class(
                        classModifiersTree,
                        refactoring.getSuperClassName(),
                        newTypeParams,
                        superClass,
                        implementsList,
                        Collections.<Tree>emptyList());
                newClassTree = GeneratorUtilities.get(workingCopy).insertClassMembers(newClassTree, members);

                FileObject fileObject = refactoring.getSourceType().getFileObject();
                FileObject sourceRoot = ClassPath.getClassPath(fileObject, ClassPath.SOURCE).findOwnerRoot(fileObject);
                String relativePath = FileUtil.getRelativePath(sourceRoot, fileObject.getParent()) + "/" + refactoring.getSuperClassName() + ".java";
                CompilationUnitTree cu = JavaPluginUtils.createCompilationUnit(sourceRoot, relativePath, newClassTree, workingCopy, make);
                rewrite(null, cu);
                
                // fake interface since interface file does not exist yet
                Tree superClassTree;
                if (typeParams.isEmpty()) {
                    superClassTree = make.Identifier(refactoring.getSuperClassName());
                } else {
                    List<ExpressionTree> typeParamTrees = new ArrayList<ExpressionTree>(typeParams.size());
                    for (TypeMirror typeParam : typeParams) {
                        Tree t = make.Type(typeParam);
                        typeParamTrees.add((ExpressionTree) t);
                    }
                    superClassTree = make.ParameterizedType(
                            make.Identifier(refactoring.getSuperClassName()),
                            typeParamTrees
                            );
                }
                Set<Tree> interfaces2Remove = new HashSet<Tree>();

                interfaces2Remove.addAll(getImplements2Remove(workingCopy, refactoring.getMembers(), clazz));

                // filter out obsolete members
                List<Tree> newMembers = new ArrayList<Tree>();
                for (Tree tree : classTree.getMembers()) {
                    if (!members2Remove.contains(tree)) {
                        newMembers.add(tree);
                    }
                }
                // filter out obsolete implements trees
                List<Tree> newImpls = resolveImplements(classTree.getImplementsClause(), interfaces2Remove);

                ClassTree nc;
                nc = make.Class(
                        classTree.getModifiers(),
                        classTree.getSimpleName(),
                        classTree.getTypeParameters(),
                        superClassTree,
                        newImpls,
                        newMembers);

                rewrite(classTree, nc);
                return classTree;
            }
            return super.visitClass(classTree, p);
        }

        @Override
        public Tree visitVariable(VariableTree variableTree, Element p) {
            Element current = workingCopy.getTrees().getElement(getCurrentPath());
            if (current != null) {
                for (MemberInfo<ElementHandle<? extends Element>> memberInfo : refactoring.getMembers()) {
                    if (memberInfo.getGroup() == MemberInfo.Group.FIELD
                            && memberInfo.getElementHandle().resolve(workingCopy) == current) {
                        GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
                        members2Remove.add(variableTree);
                        VariableTree copy = genUtils.importComments(variableTree, workingCopy.getCompilationUnit());
                        copy = genUtils.importFQNs(copy);
                        ModifiersTree modifiers = copy.getModifiers();
                        if (modifiers.getFlags().contains(Modifier.PRIVATE)) {
                            modifiers = make.removeModifiersModifier(modifiers, Modifier.PRIVATE);
                            modifiers = make.addModifiersModifier(modifiers, Modifier.PROTECTED);
                            copy = make.Variable(modifiers, copy.getName(), copy.getType(), copy.getInitializer());
                            genUtils.copyComments(variableTree, copy, false);
                            genUtils.copyComments(variableTree, copy, true);
                        }
                        members.add(copy);
                        break;
                    }
                }
            }
            return variableTree;
        }

        @Override
        public Tree visitMethod(final MethodTree methodTree, Element p) {
            final Trees trees = workingCopy.getTrees();
            Element current = trees.getElement(getCurrentPath());
            if (current != null) {
                for (MemberInfo<ElementHandle<? extends Element>> memberInfo : refactoring.getMembers()) {
                    if (memberInfo.getGroup() == MemberInfo.Group.METHOD
                            && memberInfo.getElementHandle().resolve(workingCopy) == current) {
                        if(!memberInfo.isMakeAbstract()) {
                            members2Remove.add(methodTree);
                        }
                        GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
                        MethodTree newMethod = genUtils.importComments(methodTree, workingCopy.getCompilationUnit());
                        ModifiersTree modifiers = methodTree.getModifiers();
                        if (modifiers.getFlags().contains(Modifier.PRIVATE)) {
                            modifiers = make.removeModifiersModifier(modifiers, Modifier.PRIVATE);
                            modifiers = make.addModifiersModifier(modifiers, Modifier.PROTECTED);
                        }
                        newMethod = genUtils.importFQNs(newMethod);
                        modifiers = genUtils.importFQNs(modifiers);
                        final List<? extends TypeMirror> thrownTypes = ((ExecutableElement)current).getThrownTypes();
                        List<ExpressionTree> newThrownTypes = new ArrayList<ExpressionTree>(thrownTypes.size());
                        for (TypeMirror typeMirror : thrownTypes) {
                            newThrownTypes.add((ExpressionTree) make.Type(typeMirror)); // Necessary as this is not covered by importFQNs
                        }
                        if (memberInfo.isMakeAbstract() && !current.getModifiers().contains(Modifier.ABSTRACT)) {
                            newMethod = make.Method(
                                    RefactoringUtils.makeAbstract(make, modifiers),
                                    newMethod.getName(),
                                    newMethod.getReturnType(),
                                    newMethod.getTypeParameters(),
                                    newMethod.getParameters(),
                                    newThrownTypes,
                                    (BlockTree) null,
                                    null);
                        } else {
                            newMethod = make.Method(modifiers,
                                    newMethod.getName(),
                                    newMethod.getReturnType(),
                                    newMethod.getTypeParameters(),
                                    newMethod.getParameters(),
                                    newThrownTypes,
                                    newMethod.getBody(),
                                    (ExpressionTree) newMethod.getDefaultValue());
                        }
                        genUtils.copyComments(methodTree, newMethod, false);
                        genUtils.copyComments(methodTree, newMethod, true);
                        makeAbstract |= newMethod.getModifiers().getFlags().contains(Modifier.ABSTRACT);
                        members.add(newMethod);
                        break;
                    }
                }
            }
            return methodTree;
        }

        private List<Tree> getImplements2Remove(CompilationInfo javac,MemberInfo[] members, TypeElement clazz) {
            if (members == null || members.length == 0) {
                return Collections.<Tree>emptyList();
            }
            
            // resolve members to remove
            List<TypeMirror> memberTypes = new ArrayList<TypeMirror>(members.length);
            for (MemberInfo member : members) {
                if (member.getGroup() == MemberInfo.Group.IMPLEMENTS) {
                    TypeMirrorHandle handle = (TypeMirrorHandle) member.getElementHandle();
                    TypeMirror tm = handle.resolve(javac);
                    memberTypes.add(tm);
                }
            }
            
            ClassTree classTree = javac.getTrees().getTree(clazz);
            List<Tree> result = new ArrayList<Tree>();
            Types types = javac.getTypes();
            
            // map TypeMirror to Tree
            for (Tree tree : classTree.getImplementsClause()) {
                TreePath path = javac.getTrees().getPath(javac.getCompilationUnit(), tree);
                TypeMirror existingTM = javac.getTrees().getTypeMirror(path);
                
                for (TypeMirror tm : memberTypes) {
                    if (types.isSameType(tm, existingTM)) {
                        result.add(tree);
                        break;
                    }
                }
            }
            
            return result;
        }
        private static List<Tree> resolveImplements(List<? extends Tree> allImpls, Set<Tree> impls2Remove) {
            List<Tree> ret;
            if (allImpls == null) {
                ret = new ArrayList<Tree>(1);
            } else {
                ret = new ArrayList<Tree>(allImpls.size() + 1);
                ret.addAll(allImpls);
            }
            
            if (impls2Remove != null && !impls2Remove.isEmpty()) {
                ret.removeAll(impls2Remove);
            }
            return ret;
        }
        
        private List<TypeMirror> findUsedGenericTypes(TypeElement javaClass) {
            List<TypeMirror> typeArgs = JavaRefactoringUtils.elementsToTypes(javaClass.getTypeParameters());
            if (typeArgs.isEmpty()) {
                return typeArgs;
            }

            Types typeUtils = workingCopy.getTypes();
            Set<TypeMirror> used = Collections.newSetFromMap(new IdentityHashMap<TypeMirror, Boolean>());

            // check super class
            TypeMirror superClass = javaClass.getSuperclass();
            RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, superClass);

            MemberInfo[] members = refactoring.getMembers();
            for (int i = 0; i < members.length && !typeArgs.isEmpty(); i++) {
                if (members[i].getGroup() == MemberInfo.Group.METHOD) {
                // check methods
                    @SuppressWarnings("unchecked")
                    ElementHandle<ExecutableElement> handle = (ElementHandle<ExecutableElement>) members[i].getElementHandle();
                    ExecutableElement elm = handle.resolve(workingCopy);

                    RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, elm.getReturnType());

                    for (Iterator<? extends VariableElement> paramIter = elm.getParameters().iterator(); paramIter.hasNext() && !typeArgs.isEmpty();) {
                        VariableElement param = paramIter.next();
                        RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, param.asType());
                    }
                } else if (members[i].getGroup() == MemberInfo.Group.FIELD) {
                    if (members[i].getModifiers().contains(Modifier.STATIC)) {
                        // do not check since static fields cannot use type parameter of the enclosing class
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    ElementHandle<VariableElement> handle = (ElementHandle<VariableElement>) members[i].getElementHandle();
                    VariableElement elm = handle.resolve(workingCopy);
                    TypeMirror asType = elm.asType();
                    RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, asType);
                } else if (members[i].getGroup() == MemberInfo.Group.IMPLEMENTS) {
                    // check implements
                    TypeMirrorHandle handle = (TypeMirrorHandle) members[i].getElementHandle();
                    TypeMirror implemetz = handle.resolve(workingCopy);
                    RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, implemetz);
                }
                // do not check fields since static fields cannot use type parameter of the enclosing class
            }

            return RefactoringUtils.filterTypes(typeArgs, used);
        }
        
        // --- helper methods ----------------------------------
        
        /* in case there are constructors delegating to old superclass it is necessery to create delegates in new superclass */
        private void addConstructors(final TypeElement origClass) {
            final GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
            
            // cache of already resolved constructors
            final Set<Element> added = new HashSet<Element>();
            for (ExecutableElement constr : ElementFilter.constructorsIn(origClass.getEnclosedElements())) {
                if (workingCopy.getElementUtilities().isSynthetic(constr)) {
                    continue;
                }
                
                TreePath path = workingCopy.getTrees().getPath(constr);
                MethodTree mc = (MethodTree) (path != null? path.getLeaf(): null);
                if (mc != null) {
                    for (StatementTree stmt : mc.getBody().getStatements()) {
                        // search super(...); statement
                        if (stmt.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                            ExpressionStatementTree estmt = (ExpressionStatementTree) stmt;
                            boolean isSyntheticSuper = workingCopy.getTreeUtilities().isSynthetic(workingCopy.getTrees().getPath(path.getCompilationUnit(), estmt));
                            ExpressionTree expr = estmt.getExpression();
                            TreePath expath = workingCopy.getTrees().getPath(path.getCompilationUnit(), expr);
                            Element el = workingCopy.getTrees().getElement(expath);
                            if (el != null && el.getKind() == ElementKind.CONSTRUCTOR && added.add(el)) {
                                ExecutableElement superclassConstr = (ExecutableElement) el;
                                MethodInvocationTree invk = (MethodInvocationTree) expr;
                                // create constructor block with super call
                                BlockTree block = isSyntheticSuper
                                        ? make.Block(Collections.<StatementTree>emptyList(), false)
                                        : make.Block(Collections.<StatementTree>singletonList(
                                            make.ExpressionStatement(
                                                make.MethodInvocation(
                                                    Collections.<ExpressionTree>emptyList(),
                                                    invk.getMethodSelect(),
                                                    params2Arguments(make, superclassConstr.getParameters())
                                                ))), false);
                                // create constructor
                                MethodTree newConstr = make.Method(superclassConstr, block);
                                newConstr = removeAnnotations(make, newConstr);
                                newConstr = removeRuntimeExceptions(workingCopy, superclassConstr, make, newConstr);

                                newConstr = genUtils.importFQNs(newConstr);
                                members.add(newConstr);
                            }
                            
                        }
                        // take just first statement super(...)
                        break;
                    }
                }
            }
        }

        private static MethodTree removeAnnotations(final TreeMaker make, MethodTree newConstr) {
            return make.Method(make.Modifiers(newConstr.getModifiers().getFlags(), Collections.emptyList()), newConstr.getName(), newConstr.getReturnType(),
                    newConstr.getTypeParameters(), newConstr.getParameters(), newConstr.getThrows(), newConstr.getBody(), (ExpressionTree) newConstr.getDefaultValue());
        }

        private static MethodTree removeRuntimeExceptions(final WorkingCopy javac, ExecutableElement superclassConstr, final TreeMaker make, MethodTree newConstr) {
            int i = 0;
            TypeMirror rte = javac.getElements().getTypeElement("java.lang.RuntimeException").asType(); //NOI18N
            ArrayList<Integer> rtes = new ArrayList<Integer>();
            for (TypeMirror throwz : superclassConstr.getThrownTypes()) {
                if (javac.getTypes().isSubtype(throwz, rte)) {
                    rtes.add(i);
                }
                i++;
            }
            for (int j = rtes.size()-1; j >= 0; j--) {
                newConstr = make.removeMethodThrows(newConstr, rtes.get(j));
            }
            return newConstr;
        }

        
        private static List<? extends ExpressionTree> params2Arguments(TreeMaker make, List<? extends VariableElement> params) {
            if (params.isEmpty()) {
                return Collections.<ExpressionTree>emptyList();
            }
            List<ExpressionTree> args = new ArrayList<ExpressionTree>(params.size());
            for (VariableElement param : params) {
                args.add(make.Identifier(param.getSimpleName()));
            }
            return args;
        }        
    }
}
