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
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.support.CancellableTreeScanner;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.InlineRefactoring;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import static org.netbeans.modules.refactoring.java.plugins.Bundle.*;

/**
 * Refactoring used to replace all references of an element with its body
 * or expression.
 * @author Ralph Ruijs
 */
public class InlineRefactoringPlugin extends JavaRefactoringPlugin {

    
    private static final List<Tree.Kind> unary = Arrays.asList(Tree.Kind.POSTFIX_INCREMENT, Tree.Kind.POSTFIX_DECREMENT, Tree.Kind.PREFIX_INCREMENT, Tree.Kind.PREFIX_DECREMENT);
    private final InlineRefactoring refactoring;
    private TreePathHandle treePathHandle;

    public InlineRefactoringPlugin(InlineRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }

    @Override
    protected JavaSource getJavaSource(Phase p) {
        if (treePathHandle == null) {
            return null;
        }
        switch (p) {
            case PRECHECK:
            case FASTCHECKPARAMETERS:
                return JavaSource.forFileObject(treePathHandle.getFileObject());
            case CHECKPARAMETERS:
                ClasspathInfo classpathInfo = getClasspathInfo(refactoring);
                JavaSource source = JavaSource.create(classpathInfo, treePathHandle.getFileObject());
                return source;
        }
        throw new IllegalStateException();
    }
    
    protected ClasspathInfo getClasspathInfo(Set<FileObject> a) {
        ClasspathInfo cpInfo;
        cpInfo = JavaRefactoringUtils.getClasspathInfoFor(a.toArray(new FileObject[0]));
        return cpInfo;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (treePathHandle == null) {
            return null;
        }
        RefactoringVisitor visitor;
        switch (refactoring.getType()) {
            case METHOD:
                visitor = new InlineMethodTransformer(treePathHandle);
                break;
            case CONSTANT:
            case TEMP:
                visitor = new InlineVariableTransformer(treePathHandle);
                break;
            default:
                return null;
        }

        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(AbstractRefactoring.PREPARE, a.size());
        TransformTask transform = new TransformTask(visitor, treePathHandle);
        Problem problem = createAndAddElements(a, transform, refactoringElements, refactoring, getClasspathInfo(a));
        fireProgressListenerStop();
        if (visitor instanceof InlineMethodTransformer) {
            InlineMethodTransformer imt = (InlineMethodTransformer) visitor;
            problem = problem != null ? problem : imt.getProblem();
        }
        return problem;
    }
    private Set<ElementHandle<ExecutableElement>> allMethods;

    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        JavaSource source = JavaSource.create(cpInfo, treePathHandle.getFileObject());

        try {
            source.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController info) throws Exception {
                    final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    Element el = treePathHandle.resolveElement(info);
                    ElementHandle<TypeElement> enclosingType;
                    if (el instanceof TypeElement) {
                        enclosingType = ElementHandle.create((TypeElement) el);
                    } else {
                        enclosingType = ElementHandle.create(info.getElementUtilities().enclosingTypeElement(el));
                    }
                    set.add(SourceUtils.getFile(enclosingType, info.getClasspathInfo()));
                    if (el.getModifiers().contains(Modifier.PRIVATE)) {
                        if (el.getKind() == ElementKind.METHOD) {
                            //add all references of overriding methods
                            allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                            allMethods.add(ElementHandle.create((ExecutableElement) el));
                        }
                    } else {
                        if (el.getKind().isField()) {
                            set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        } else if (el instanceof TypeElement) {
                            set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES, ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        } else if (el.getKind() == ElementKind.METHOD) {
                            //add all references of overriding methods
                            allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                            allMethods.add(ElementHandle.create((ExecutableElement) el));
                            for (ExecutableElement e : JavaRefactoringUtils.getOverridingMethods((ExecutableElement) el, info, cancelRequested)) {
                                addMethods(e, set, info, idx);
                            }
                            //add all references of overriden methods
                            for (ExecutableElement ov : JavaRefactoringUtils.getOverriddenMethods((ExecutableElement) el, info)) {
                                addMethods(ov, set, info, idx);
                                for (ExecutableElement e : JavaRefactoringUtils.getOverridingMethods(ov, info, cancelRequested)) {
                                    addMethods(e, set, info, idx);
                                }
                            }
                            set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE))); //?????
                        }
                    }
                }
            }, true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return set;
    }

    private void addMethods(ExecutableElement e, Set set, CompilationInfo info, ClassIndex idx) {
        ElementHandle<TypeElement> encl = ElementHandle.create(info.getElementUtilities().enclosingTypeElement(e));
        set.add(SourceUtils.getFile(ElementHandle.create(e), info.getClasspathInfo()));
        set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        allMethods.add(ElementHandle.create(e));
    }

    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        Problem problem = isElementAvail(treePathHandle, javac);
        return problem;
    }

    @Override
    @NbBundle.Messages({
        "ERR_InlineWrongType=Cannot refactor this type of element: {0}.",
        "ERR_InlineNoVarInitializer=Cannot find the variable initializer.",
        "ERR_InlineNullVarInitializer=Cannot inline null literal.",
        "ERR_InlineAssignedOnce=Variable is assigned to more than once.",
        "WRN_InlineChange=Unsafe -- the functionality of the program may be changed.",
        "ERR_InlineNotInIterator=Cannot refactor for loop iterator.",
        "ERR_InlineNotCompoundArrayInit=Array declarations without the \"new\" keyword are not supported.",
        "ERR_InlineMethodInInterface=Cannot inline method from interface.",
        "ERR_InlineMethodAbstract=Cannot inline abstract method.",
        "ERR_InlineMethodPolymorphic=Cannot inline polymorphic method.",
        "ERR_InlineMethodVoidReturn=Cannot inline method with return statements without return type.",
        "ERR_InlineMethodRecursion=Cannot inline recursive method."})
    protected Problem preCheck(CompilationController javac) throws IOException {
        Problem preCheckProblem = null;
        javac.toPhase(JavaSource.Phase.RESOLVED);
        Element element = treePathHandle.resolveElement(javac);
        preCheckProblem = isElementAvail(treePathHandle, javac);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }

        preCheckProblem = JavaPluginUtils.isSourceElement(element, javac);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }

        switch (element.getKind()) {
            case FIELD:
            case LOCAL_VARIABLE:
                Tree tree = javac.getTrees().getTree(element);
                if (tree.getKind() != Tree.Kind.VARIABLE) {
                    preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineWrongType(element.getKind().toString())); //NOI18N
                }
                VariableTree variableTree = (VariableTree) tree;

                // Inline a Variable needs the following preconditions:
                // - Not in Iterator
                // - Assigned to exactly once
                // - Not assigned to null
                // - Used at least once
                // - Not compound array initialization
                // ----------------------
                // Need to be initialized
                if (variableTree.getInitializer() == null) {
                    preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineNoVarInitializer()); //NOI18N
                    return preCheckProblem;
                }
                // Not assigned to null
                if (variableTree.getInitializer().getKind() == Tree.Kind.NULL_LITERAL) {
                    preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineNullVarInitializer()); //NOI18N
                    return preCheckProblem;
                }
                // Assigned to exactly once
                InlineUsageVisitor visitor = new InlineUsageVisitor(javac, element);
                TreePath elementPath = javac.getTrees().getPath(element);
                TreePath blockPath = elementPath.getParentPath();

                visitor.scan(blockPath.getLeaf(), blockPath);
                if (visitor.assignmentCount > 0) {
                    preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineAssignedOnce()); //NOI18N
                    return preCheckProblem;
                }
                // Possible change
                ExpressionTree initializer = variableTree.getInitializer();
                int skipFirstMethodInvocation = 0;
                if (initializer.getKind() == Tree.Kind.METHOD_INVOCATION) {
                    skipFirstMethodInvocation++;
                }
                ErrorAwareTreeScanner<Boolean, Boolean> scanner = new UnsafeTreeScanner(skipFirstMethodInvocation);
                Boolean isChanged = scanner.scan(initializer, false);
                if (isChanged != null && isChanged) {
                    preCheckProblem = createProblem(preCheckProblem, false, WRN_InlineChange()); //NOI18N
                }
                // Not in Iterator
                TreePath treePath = treePathHandle.resolve(javac);
                treePath = treePath.getParentPath();
                Tree loop = treePath.getLeaf();
                if (loop.getKind() == Tree.Kind.ENHANCED_FOR_LOOP || loop.getKind() == Tree.Kind.FOR_LOOP) {
                    preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineNotInIterator()); //NOI18N
                    return preCheckProblem;
                }
                // Not compound array initialization
                if (variableTree.getInitializer().getKind() == Tree.Kind.NEW_ARRAY) {
                    NewArrayTree newArrayTree = (NewArrayTree) variableTree.getInitializer();
                    if (newArrayTree.getType() == null || newArrayTree.getDimensions() == null) {
                        preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineNotCompoundArrayInit()); //NOI18N
                        return preCheckProblem;
                    }
                }
                break;
            case METHOD:
                // Method can not be in annotation or interface
                if(element.getEnclosingElement().getKind().isInterface()) {
                    preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineMethodInInterface()); //NOI18N
                    return preCheckProblem;
                }
                if(element.getModifiers().contains(Modifier.ABSTRACT)) {
                    preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineMethodAbstract()); //NOI18N
                    return preCheckProblem;
                }
                // Method can not be polymorphic
                Collection<ExecutableElement> overridenMethods = JavaRefactoringUtils.getOverriddenMethods((ExecutableElement) element, javac);
                Collection<ExecutableElement> overridingMethods = JavaRefactoringUtils.getOverridingMethods((ExecutableElement) element, javac,cancelRequested);
                if (overridenMethods.size() > 0 || overridingMethods.size() > 0) {
                    preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineMethodPolymorphic()); //NOI18N
                    return preCheckProblem;
                }

                TreePath methodPath = javac.getTrees().getPath(element);
                MethodTree methodTree = (MethodTree) methodPath.getLeaf();
                Tree returnType = methodTree.getReturnType();
                boolean hasReturn = true;
                if (returnType.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
                    if (((PrimitiveTypeTree) returnType).getPrimitiveTypeKind() == TypeKind.VOID) {
                        hasReturn = false;
                    }
                }
                InlineMethodVisitor methodVisitor = new InlineMethodVisitor(javac, element);
                methodVisitor.scan(methodPath.getLeaf(), methodPath);
                if (!hasReturn) {
                    // Method with returntype void cannot have a return statement
                    if (methodVisitor.nrOfReturnStatements > 0) {
                        preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineMethodVoidReturn()); //NOI18N
                        return preCheckProblem;
                    }
                }
                // Method can not be recursive
                if (methodVisitor.isRecursive) {
                    preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineMethodRecursion()); //NOI18N
                    return preCheckProblem;
                }
                break;
            default:
                preCheckProblem = createProblem(preCheckProblem, true, ERR_InlineWrongType(element.getKind().toString())); //NOI18N
        }
        return preCheckProblem;
    }

    private class InlineMethodVisitor extends CancellableTreeScanner<Tree, TreePath> {

        public int nrOfReturnStatements = 0;
        public boolean isRecursive = false;
//        private boolean accessorRightProblem = false;
        private boolean qualIdentProblem = false;
        private final CompilationController workingCopy;
        private final Modifier access;
        private final Element element;

        public InlineMethodVisitor(CompilationController workingCopy, Element element) {
            this.workingCopy = workingCopy;
            this.access = getAccessSpecifier(element.getModifiers());
            this.element = element;
        }

        @Override
        public Tree visitReturn(ReturnTree node, TreePath p) {
            nrOfReturnStatements++;
            return super.visitReturn(node, p);
        }

        @Override
        public Tree visitMethodInvocation(MethodInvocationTree node, TreePath p) {
            Element asElement = asElement(new TreePath(p, node));
            if (element.equals(asElement)) {
                isRecursive = true;
            } else if (asElement != null) {
                if (asElement.getKind() == ElementKind.FIELD
                        || asElement.getKind() == ElementKind.METHOD
                        || asElement.getKind() == ElementKind.CLASS) {
                    Modifier mod = getAccessSpecifier(asElement.getModifiers());
//                    accessorRightProblem = hasAccessorRightProblem(mod);
                    qualIdentProblem = hasQualIdentProblem(element, asElement);
                }
            }
            return super.visitMethodInvocation(node, p);
        }

        private boolean hasQualIdentProblem(Element p, Element asElement) throws IllegalArgumentException {
            boolean result = qualIdentProblem;
            ElementUtilities elementUtilities = workingCopy.getElementUtilities();
            TypeElement bodyEnclosingTypeElement = elementUtilities.enclosingTypeElement(p);
            TypeElement invocationEnclosingTypeElement = elementUtilities.enclosingTypeElement(asElement);
            if (bodyEnclosingTypeElement.equals(invocationEnclosingTypeElement) && access != Modifier.PRIVATE) {
                result = true;
            }
            return result;
        }

//        private boolean hasAccessorRightProblem(Modifier mod) {
//            boolean hasProblem = accessorRightProblem;
//            if (access != null) {
//                switch (access) {
//                    case PUBLIC:
//                        if (mod == null || Modifier.PROTECTED.equals(mod) || Modifier.PRIVATE.equals(mod)) {
//                            hasProblem = true;
//                        }
//                        break;
//                    case PROTECTED:
//                        if (mod == null || Modifier.PRIVATE.equals(mod)) {
//                            hasProblem = true;
//                        }
//                        break;
//                    case PRIVATE:
//                    default:
//                        break;
//                }
//            } else {
//                if (Modifier.PRIVATE.equals(mod)) {
//                    hasProblem = true;
//                }
//            }
//            return hasProblem;
//        }

        private Modifier getAccessSpecifier(Set<Modifier> modifiers) {
            Modifier mod = null;
            for (Modifier modifier : modifiers) {
                switch (modifier) {
                    case PUBLIC:
                    case PRIVATE:
                    case PROTECTED:
                        mod = modifier;
                }
            }
            return mod;
        }

        @Override
        public Tree visitIdentifier(IdentifierTree node, TreePath p) {
            Element asElement = asElement(new TreePath(p, node));
            if (!node.getName().contentEquals("this") &&
                    asElement != null && 
                    (asElement.getKind() == ElementKind.FIELD
                    || asElement.getKind() == ElementKind.METHOD
                    || asElement.getKind() == ElementKind.CLASS)) {
                Modifier mod = getAccessSpecifier(asElement.getModifiers());
//                accessorRightProblem = hasAccessorRightProblem(mod);
                qualIdentProblem = hasQualIdentProblem(element, asElement);
            }
            return super.visitIdentifier(node, p);
        }

        @Override
        public Tree visitNewClass(NewClassTree node, TreePath p) {
            Element asElement = asElement(new TreePath(p, node));
            if (asElement != null && (asElement.getKind() == ElementKind.FIELD
                    || asElement.getKind() == ElementKind.METHOD
                    || asElement.getKind() == ElementKind.CLASS)) {
                Modifier mod = getAccessSpecifier(asElement.getModifiers());
//                accessorRightProblem = hasAccessorRightProblem(mod);
                qualIdentProblem = hasQualIdentProblem(element, asElement);
            }
            return super.visitNewClass(node, p);
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree node, TreePath p) {
            Element asElement = asElement(new TreePath(p, node));
            if (asElement != null && (asElement.getKind() == ElementKind.FIELD
                    || asElement.getKind() == ElementKind.METHOD
                    || asElement.getKind() == ElementKind.CLASS)) {
                Modifier mod = getAccessSpecifier(asElement.getModifiers());
//                accessorRightProblem = hasAccessorRightProblem(mod);
                qualIdentProblem = hasQualIdentProblem(element, asElement);
            }
            return super.visitMemberSelect(node, p);
        }

        private Element asElement(TreePath treePath) {
            Trees treeUtil = workingCopy.getTrees();
            Element element = treeUtil.getElement(treePath);
            return element;
        }
    }

    private class InlineUsageVisitor extends CancellableTreeScanner<Tree, TreePath> {

        public int assignmentCount = 0;
        public int usageCount = 0;
        private final CompilationController workingCopy;
        private final Element element;

        public InlineUsageVisitor(CompilationController workingCopy, Element element) {
            this.workingCopy = workingCopy;
            this.element = element;
        }

        @Override
        public Tree visitVariable(VariableTree node, TreePath p) {
            if (element.equals(asElement(new TreePath(p, node)))) {
                usageCount++;
            }
            return super.visitVariable(node, p);
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree node, TreePath p) {
            if (element.equals(asElement(new TreePath(p, node)))) {
                usageCount++;
            }
            return super.visitMemberSelect(node, p);
        }

        @Override
        public Tree visitIdentifier(IdentifierTree node, TreePath p) {
            if (element.equals(asElement(new TreePath(p, node)))) {
                usageCount++;
            }
            return super.visitIdentifier(node, p);
        }

        @Override
        public Tree visitMethod(MethodTree node, TreePath p) {
            if (element.equals(asElement(new TreePath(p, node)))) {
                usageCount++;
            }
            return super.visitMethod(node, p);
        }

        @Override
        public Tree visitMethodInvocation(MethodInvocationTree node, TreePath p) {
            if (element.equals(asElement(new TreePath(p, node)))) {
                usageCount++;
            }
            return super.visitMethodInvocation(node, p);
        }

        @Override
        public Tree visitAssignment(AssignmentTree node, TreePath p) {
            if (element.equals(asElement(new TreePath(p, node.getVariable())))) {
                assignmentCount++;
            }
            return super.visitAssignment(node, p);
        }

        @Override
        public Tree visitCompoundAssignment(CompoundAssignmentTree node, TreePath p) {
            if (element.equals(asElement(new TreePath(p, node.getVariable())))) {
                assignmentCount++;
            }
            return super.visitCompoundAssignment(node, p);
        }

        @Override
        public Tree visitUnary(UnaryTree node, TreePath p) {
            if (element.equals(asElement(new TreePath(p, node.getExpression())))) {
                if(unary.contains(node.getKind())) {
                    assignmentCount++;
                }
            }
            return super.visitUnary(node, p);
        }

        private Element asElement(TreePath treePath) {
            Trees treeUtil = workingCopy.getTrees();
            return treeUtil.getElement(treePath);
        }
    }

    private static class UnsafeTreeScanner extends ErrorAwareTreeScanner<Boolean, Boolean> {

        private int skipFirstMethodInvocation;

        public UnsafeTreeScanner(int skipFirstMethodInvocation) {
            super();
            this.skipFirstMethodInvocation = skipFirstMethodInvocation;
        }

        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, Boolean p) {
            if (skipFirstMethodInvocation > 0) {
                skipFirstMethodInvocation--;
                return super.visitMethodInvocation(node, p);
            } else {
                return true;
            }
        }

        @Override
        public Boolean visitNewClass(NewClassTree node, Boolean p) {
            return true;
        }

        @Override
        public Boolean visitNewArray(NewArrayTree node, Boolean p) {
            return true;
        }

        @Override
        public Boolean visitAssignment(AssignmentTree node, Boolean p) {
            return true;
        }

        @Override
        public Boolean visitUnary(UnaryTree node, Boolean p) {
            return true;
        }

        @Override
        public Boolean reduce(Boolean left, Boolean right) {
            return (left == null ? false : left) || (right == null ? false : right);
        }
    }
}
