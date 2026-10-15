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
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.DiffElement;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Plugin that implements the core functionality of Extract Interface refactoring.
 * <br>Extracts: <ul>
 * <li>implements interfaces</li>
 * <li>public nonstatic methods</li>
 * <li>public static final fields</li>
 * <li>XXX public static class/interface/enum/annotation type.<br><i>dangerous, it might contain
 *     elements that will be unaccessible from the new interface. Maybe reusing Move Class refactoring
 *     would be appropriate. Not implemented in 6.0 yet. Pre-6.0 implementation was not solved references at all.</i></li>
 * </ul>
 * XXX there should be option Copy/Move/AsIs javadoc.
 *
 * @author Martin Matula, Jan Pokorsky
 */
public final class ExtractInterfaceRefactoringPlugin extends JavaRefactoringPlugin {
    
    /** Reference to the parent refactoring instance */
    private final ExtractInterfaceRefactoring refactoring;
    
    private String pkgName;
    
    /** class for extracting interface */
    private ElementHandle<TypeElement> classHandle;
    
    /** Creates a new instance of ExtractInterfaceRefactoringPlugin
     * @param refactoring Parent refactoring instance.
     */
    ExtractInterfaceRefactoringPlugin(ExtractInterfaceRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem fastCheckParameters() {
        Problem result = null;
        
        String newName = refactoring.getInterfaceName();
        
        if (!Utilities.isJavaIdentifier(newName)) {
            result = createProblem(result, true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_InvalidIdentifier", newName)); // NOI18N
            return result;
        }
        
        FileObject primFile = refactoring.getSourceType().getFileObject();
        FileObject folder = primFile.getParent();
        FileObject[] children = folder.getChildren();
        for (FileObject child: children) {
            if (!child.isVirtual() && child.getName().equalsIgnoreCase(newName) && "java".equalsIgnoreCase(child.getExt())) { // NOI18N
                result = createProblem(result, true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ClassClash", newName, pkgName)); // NOI18N
                return result;
            }
        }

        return super.fastCheckParameters();
    }

    @Override
    protected Problem fastCheckParameters(CompilationController javac) throws IOException {
        Problem result = null;
        String newName = refactoring.getInterfaceName();
        TypeMirror parsedType = javac.getTreeUtilities().parseType(newName, classHandle.resolve(javac));
        if(parsedType != null && parsedType.getKind() != TypeKind.ERROR) {
            result = createProblem(result, true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ClassClash", newName, pkgName)); // NOI18N
            return result;
        }
        return super.fastCheckParameters(javac);
    }

    @Override
    public Problem prepare(RefactoringElementsBag bag) {
        FileObject primFile = refactoring.getSourceType().getFileObject();
        try {
            UpdateClassTask.create(bag, primFile, refactoring, classHandle);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }
    
    @Override
    protected JavaSource getJavaSource(Phase p) {
        return JavaSource.forFileObject(refactoring.getSourceType().getFileObject());
    }
    
    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        // fire operation start on the registered progress listeners (1 step)
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 1);
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
            if (sourceElm == null || (sourceElm.getKind() != ElementKind.CLASS && sourceElm.getKind() != ElementKind.INTERFACE && sourceElm.getKind() != ElementKind.ENUM)) {
                // fatal error -> return
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
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
    protected Problem checkParameters(CompilationController javac) throws IOException {
        if (refactoring.getMethods().isEmpty() && refactoring.getFields().isEmpty() && refactoring.getImplements().isEmpty()) {
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_MembersNotAvailable")); // NOI18N);
        }
        // check whether the selected members are public and non-static in case of methods, static in other cases
        // check whether all members belong to the source type
        // XXX check if method params and return type will be accessible after extraction; likely not fatal
        javac.toPhase(JavaSource.Phase.RESOLVED);
        
        TypeElement sourceType = (TypeElement) refactoring.getSourceType().resolveElement(javac);
        assert sourceType != null;
        
        Set<? extends Element> members = new HashSet<Element>(sourceType.getEnclosedElements());
        
        for (ElementHandle<ExecutableElement> elementHandle : refactoring.getMethods()) {
            ExecutableElement elm = elementHandle.resolve(javac);
            if (elm == null) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
            }
            if (javac.getElementUtilities().isSynthetic(elm) || elm.getKind() != ElementKind.METHOD) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_UnknownMember", // NOI18N
                        elm.toString()));
            }
            if (!members.contains(elm)) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_UnknownMember", // NOI18N
                        elm.toString()));
            }
            Set<Modifier> mods = elm.getModifiers();
            if (!mods.contains(Modifier.PUBLIC) || mods.contains(Modifier.STATIC)) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_WrongModifiers", elm.getSimpleName().toString())); // NOI18N
            }
        }
        
        for (ElementHandle<VariableElement> elementHandle : refactoring.getFields()) {
            VariableElement elm = elementHandle.resolve(javac);
            if (elm == null) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
            }
            if (javac.getElementUtilities().isSynthetic(elm) || elm.getKind() != ElementKind.FIELD) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_UnknownMember", // NOI18N
                        elm.toString()));
            }
            if (!members.contains(elm)) {
                return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_UnknownMember", // NOI18N
                        elm.toString()));
            }
            Set<Modifier> mods = elm.getModifiers();
            if (mods.contains(Modifier.PUBLIC) && mods.contains(Modifier.STATIC) && mods.contains(Modifier.FINAL)) {
                VariableTree tree = (VariableTree) javac.getTrees().getTree(elm);
                if (tree.getInitializer() != null) {
                    continue;
                }
            }
            return new Problem(true, NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "ERR_ExtractInterface_WrongModifiers", elm.getSimpleName().toString())); // NOI18N
        }
        
        // XXX check refactoring.getImplements()

        return null;
    }
    
    /**
     * Finds all type parameters of <code>javaClass</code> that are referenced by
     * any member that is going to be extract.
     * @param refactoring the refactoring containing members to extract
     * @param javac compilation info
     * @param javaClass java class declaring parameters to find
     * @return type parameters to extract
     */
    private static List<TypeMirror> findUsedGenericTypes(ExtractInterfaceRefactoring refactoring, CompilationInfo javac, TypeElement javaClass) {
        List<TypeMirror> typeArgs = JavaRefactoringUtils.elementsToTypes(javaClass.getTypeParameters());
        if (typeArgs.isEmpty()) {
            return typeArgs;
        }
        
        Types typeUtils = javac.getTypes();
        Set<TypeMirror> used = Collections.newSetFromMap(new IdentityHashMap<TypeMirror, Boolean>());
        // do not check fields since static fields cannot use type parameter of the enclosing class
        
        // check methods
        for (Iterator<ElementHandle<ExecutableElement>> methodIter = refactoring.getMethods().iterator(); methodIter.hasNext() && !typeArgs.isEmpty();) {
            ElementHandle<ExecutableElement> handle = methodIter.next();
            ExecutableElement elm = handle.resolve(javac);
            
            RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, elm.getReturnType());
            
            for (Iterator<? extends VariableElement> paramIter = elm.getParameters().iterator(); paramIter.hasNext() && !typeArgs.isEmpty();) {
                VariableElement param = paramIter.next();
                RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, param.asType());
            }
        }
        
        // check implements
        for (Iterator<TypeMirrorHandle<TypeMirror>> it = refactoring.getImplements().iterator(); it.hasNext() && !typeArgs.isEmpty();) {
            TypeMirrorHandle<TypeMirror> handle = it.next();
            TypeMirror implemetz = handle.resolve(javac);
            RefactoringUtils.findUsedGenericTypes(typeUtils, typeArgs, used, implemetz);
        }

        return RefactoringUtils.filterTypes(typeArgs, used);
    }

    // --- REFACTORING ELEMENTS ------------------------------------------------
    
    private static final class UpdateClassTask implements CancellableTask<WorkingCopy> {
        private final ExtractInterfaceRefactoring refactoring;
        private final ElementHandle<TypeElement> sourceType;
        
        private UpdateClassTask(ExtractInterfaceRefactoring refactoring, ElementHandle<TypeElement> sourceType) {
            this.sourceType = sourceType;
            this.refactoring = refactoring;
        }
        
        public static void create(RefactoringElementsBag bag, FileObject fo, ExtractInterfaceRefactoring refactoring, ElementHandle<TypeElement> sourceType) throws IOException {
            JavaSource js = JavaSource.forFileObject(fo);
            ModificationResult modification = js.runModificationTask(new UpdateClassTask(refactoring, sourceType));
            List<? extends ModificationResult.Difference> diffs = modification.getDifferences(fo);
            for (ModificationResult.Difference diff : diffs) {
                bag.add(refactoring, DiffElement.create(diff, fo, modification));
            }
            bag.registerTransaction(createTransaction(Collections.singletonList(modification)));
        }
        
        @Override
        public void cancel() {
        }

        @Override
        public void run(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            createCu(wc);
            TypeElement clazz = this.sourceType.resolve(wc);
            assert clazz != null;
            ClassTree classTree = wc.getTrees().getTree(clazz);
            TreeMaker maker = wc.getTreeMaker();
            // fake interface since interface file does not exist yet
            Tree interfaceTree;
            List<TypeMirror> typeParams = findUsedGenericTypes(refactoring, wc, clazz);
            if (typeParams.isEmpty()) {
                interfaceTree = maker.Identifier(refactoring.getInterfaceName());
            } else {
                List<ExpressionTree> typeParamTrees = new ArrayList<ExpressionTree>(typeParams.size());
                for (TypeMirror typeParam : typeParams) {
                    Tree t = maker.Type(typeParam);
                    typeParamTrees.add((ExpressionTree) t);
                }
                interfaceTree = maker.ParameterizedType(
                        maker.Identifier(refactoring.getInterfaceName()),
                        typeParamTrees
                        );
            }
            
            Set<Tree> members2Remove = new HashSet<Tree>();
            Set<Tree> interfaces2Remove = new HashSet<Tree>();
            
            members2Remove.addAll(getFields2Remove(wc, refactoring.getFields()));
            members2Remove.addAll(getMethods2Remove(wc, refactoring.getMethods(), clazz));
            interfaces2Remove.addAll(getImplements2Remove(wc, refactoring.getImplements(), clazz));
           
            for (ElementHandle el : refactoring.getMethods()) {
                ExecutableElement e = (ExecutableElement) el.resolve(wc);
                Tree tree = wc.getTrees().getTree(e);
                MethodTree mt = (MethodTree) tree;
                if (e.getAnnotation(Override.class)==null) {
                    TreeMaker make = wc.getTreeMaker();
                    AnnotationTree ann = make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList());
                    ModifiersTree modifiers = wc.getTreeMaker().addModifiersAnnotation(mt.getModifiers(), ann);
                    wc.rewrite(mt.getModifiers(), modifiers);
                }

            }

            // filter out obsolete members
            List<Tree> members2Add = new ArrayList<Tree>();
            for (Tree tree : classTree.getMembers()) {
                if (!members2Remove.contains(tree)) {
                    members2Add.add(tree);
                }
            }
            // filter out obsolete implements trees
            List<Tree> impls2Add = resolveImplements(classTree.getImplementsClause(), interfaces2Remove, interfaceTree);

            ClassTree nc;
            if (clazz.getKind() == ElementKind.CLASS) {
                nc = maker.Class(
                        classTree.getModifiers(),
                        classTree.getSimpleName(),
                        classTree.getTypeParameters(),
                        classTree.getExtendsClause(),
                        impls2Add,
                        classTree.getPermitsClause(),
                        members2Add);
            } else if (clazz.getKind() == ElementKind.INTERFACE) {
                nc = maker.Interface(
                        classTree.getModifiers(),
                        classTree.getSimpleName(),
                        classTree.getTypeParameters(),
                        impls2Add,
                        classTree.getPermitsClause(),
                        members2Add);
            } else if (clazz.getKind() == ElementKind.ENUM) {
                nc = maker.Enum(
                        classTree.getModifiers(),
                        classTree.getSimpleName(),
                        impls2Add,
                        members2Add);
            } else {
                throw new IllegalStateException(classTree.toString());
            }
            
            wc.rewrite(classTree, nc);
        }

        private List<Tree> getFields2Remove(CompilationInfo javac, List<ElementHandle<VariableElement>> members) {
            if (members.isEmpty()) {
                return Collections.<Tree>emptyList();
            }
            List<Tree> result = new ArrayList<Tree>(members.size());
            for (ElementHandle<VariableElement> handle : members) {
                VariableElement elm = handle.resolve(javac);
                assert elm != null;
                Tree t = javac.getTrees().getTree(elm);
                assert t != null;
                result.add(t);
            }

            return result;
        }
        
        private List<Tree> getMethods2Remove(CompilationInfo javac, List<ElementHandle<ExecutableElement>> members, TypeElement clazz) {
            if (members.isEmpty()) {
                return Collections.<Tree>emptyList();
            }
            boolean isInterface = clazz.getKind() == ElementKind.INTERFACE;
            List<Tree> result = new ArrayList<Tree>(members.size());
            for (ElementHandle<ExecutableElement> handle : members) {
                ExecutableElement elm = handle.resolve(javac);
                assert elm != null;
                
                
                if (isInterface || elm.getModifiers().contains(Modifier.ABSTRACT)) {
                    // it is interface method nor abstract method
                    Tree t = javac.getTrees().getTree(elm);
                    assert t != null;
                    result.add(t);
                }
            }

            return result;
        }
        
        private List<Tree> getImplements2Remove(CompilationInfo javac, List<TypeMirrorHandle<TypeMirror>> members, TypeElement clazz) {
            if (members.isEmpty()) {
                return Collections.<Tree>emptyList();
            }
            
            // resolve members to remove
            List<TypeMirror> memberTypes = new ArrayList<TypeMirror>(members.size());
            for (TypeMirrorHandle<TypeMirror> handle : members) {
                TypeMirror tm = handle.resolve(javac);
                memberTypes.add(tm);
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
        
        private static List<Tree> resolveImplements(List<? extends Tree> allImpls, Set<Tree> impls2Remove, Tree impl2Add) {
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
            ret.add(impl2Add);
            return ret;
        }
        
        public void createCu(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            TreeMaker make = wc.getTreeMaker();
            GeneratorUtilities genUtils = GeneratorUtilities.get(wc);            
            
            // add type parameters
            List<TypeMirror> typeParams = findUsedGenericTypes(refactoring, wc, sourceType.resolve(wc));
            List<TypeParameterTree> newTypeParams = new ArrayList<TypeParameterTree>(typeParams.size());
            // lets retrieve param type trees from origin class since it is
            // almost impossible to create them via TreeMaker
            TypeElement sourceTypeElm = sourceType.resolve(wc);
            for (TypeParameterElement typeParam : sourceTypeElm.getTypeParameters()) {
                TypeMirror origParam = typeParam.asType();
                for (TypeMirror newParam : typeParams) {
                    if (wc.getTypes().isSameType(origParam, newParam)) {
                        Tree t = wc.getTrees().getTree(typeParam);
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

            // add new fields
            List<Tree> members = new ArrayList<Tree>();
            for (ElementHandle<VariableElement> handle : refactoring.getFields()) {
                VariableElement memberElm = handle.resolve(wc);
                VariableTree tree = (VariableTree) wc.getTrees().getTree(memberElm);
                VariableTree newVarTree = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet(), tree.getModifiers().getAnnotations()),
                        tree.getName(),
                        tree.getType(),
                        tree.getInitializer());
                newVarTree = genUtils.importFQNs(newVarTree);
                tree = genUtils.importComments(tree,  wc.getTrees().getPath(memberElm).getCompilationUnit());
                genUtils.copyComments(tree, newVarTree, false);
                genUtils.copyComments(tree, newVarTree, true);
                members.add(newVarTree);
            }
            // add newmethods
            for (ElementHandle<ExecutableElement> handle : refactoring.getMethods()) {
                ExecutableElement memberElm = handle.resolve(wc);
                TreePath mpath = wc.getTrees().getPath(memberElm);
                MethodTree tree = wc.getTrees().getTree(memberElm);
                List<? extends AnnotationTree> annotations =
                        filterOutOverrideAnnotation(tree.getModifiers().getAnnotations(), wc, mpath);
                MethodTree newMethodTree = make.Method(
                        make.Modifiers(Collections.<Modifier>emptySet(), annotations),
                        tree.getName(),
                        tree.getReturnType(),
                        tree.getTypeParameters(),
                        tree.getParameters(),
                        tree.getThrows(),
                        (BlockTree) null,
                        null);
                newMethodTree = genUtils.importFQNs(newMethodTree);
                tree = genUtils.importComments(tree,  wc.getTrees().getPath(memberElm).getCompilationUnit());
                genUtils.copyComments(tree, newMethodTree, false);
                genUtils.copyComments(tree, newMethodTree, true);
                members.add(newMethodTree);
            }
            // add super interfaces
            List <Tree> extendsList = new ArrayList<Tree>();
            for (TypeMirrorHandle<? extends TypeMirror> handle : refactoring.getImplements()) {
                // XXX check if interface is not aready there; the templates might be changed by user :-(
                TypeMirror implMirror = handle.resolve(wc);
                extendsList.add(make.Type(implMirror));
            }
            // create new interface
            ClassTree newInterfaceTree = make.Interface(
                    make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                    refactoring.getInterfaceName(),
                    newTypeParams,
                    extendsList,
                    Collections.emptyList(),
                    Collections.<Tree>emptyList());
            
            newInterfaceTree = genUtils.insertClassMembers(newInterfaceTree, members);

            FileObject fileObject = refactoring.getSourceType().getFileObject();
            FileObject sourceRoot = ClassPath.getClassPath(fileObject, ClassPath.SOURCE).findOwnerRoot(fileObject);
            String relativePath = FileUtil.getRelativePath(sourceRoot, fileObject.getParent()) + "/" + refactoring.getInterfaceName() + ".java";

            CompilationUnitTree cu = JavaPluginUtils.createCompilationUnit(sourceRoot, relativePath, newInterfaceTree, wc, make);
            wc.rewrite(null, cu);
        }
        
        // --- helper methods ----------------------------------
        private static List<? extends AnnotationTree> filterOutOverrideAnnotation(List<? extends AnnotationTree> annotations, CompilationInfo javac, TreePath pathToMethod) {
            if (annotations.isEmpty()) {
                return annotations;
            }
            List<AnnotationTree> newAnnotations = new ArrayList<AnnotationTree>(annotations.size());
            TypeElement overrideAnn = javac.getElements().getTypeElement("java.lang.Override"); // NOI18N
            for (AnnotationTree annotationTree : annotations) {
                Element annotation = javac.getTrees().getElement(new TreePath(pathToMethod, annotationTree));
                if (annotation == null || annotation != overrideAnn) {
                    newAnnotations.add(annotationTree);
                }
            }
            return newAnnotations;
        }        
    }
    
}
