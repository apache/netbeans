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

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.source.util.Trees;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties.Visibility;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Ralph Ruijs
 */
public class MoveMembersTransformer extends RefactoringVisitor {

    private static final int NOPOS = -2;
    private static final Set<Modifier> ALL_ACCESS_MODIFIERS = EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
    private Problem problem;
    private Collection<? extends TreePathHandle> allElements;
    private final Visibility visibility;
    private final HashMap<TreePathHandle, Boolean> usageOutsideOfPackage;
    private final HashMap<TreePathHandle, Boolean> usageOutsideOfType;
    private final TreePathHandle targetHandle;
    private final boolean delegate;
    private final boolean deprecate;
    private final boolean updateJavadoc;

    public MoveMembersTransformer(MoveRefactoring refactoring) {
        allElements = refactoring.getRefactoringSource().lookupAll(TreePathHandle.class);
        JavaMoveMembersProperties properties = refactoring.getContext().lookup(JavaMoveMembersProperties.class);
        properties = properties == null ? new JavaMoveMembersProperties(allElements.toArray(new TreePathHandle[0])) : properties;
        visibility = properties.getVisibility();
        usageOutsideOfPackage = new HashMap<>();
        usageOutsideOfType = new HashMap<>();
        for (TreePathHandle treePathHandle : allElements) {
            usageOutsideOfPackage.put(treePathHandle, Boolean.FALSE);
        }
        targetHandle = refactoring.getTarget().lookup(TreePathHandle.class);
        delegate = properties.isDelegate();
        deprecate = properties.isAddDeprecated();
        updateJavadoc = properties.isUpdateJavaDoc();
    }

    @Override
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        for (TreePathHandle element : allElements) {
            SourceUtils.forceSource(workingCopy, element.getFileObject());
        }
        super.setWorkingCopy(workingCopy);
    }

    public Problem getProblem() {
        return problem;
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element target) {
        if (changeIfMatch(getCurrentPath(), node, target)) {
            return node;
        }
        return super.visitMemberSelect(node, target);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element target) {
        if (changeIfMatch(getCurrentPath(), node, target)) {
            return node;
        }
        return super.visitIdentifier(node, target);
    }

    @Override
    public Tree visitMethodInvocation(MethodInvocationTree node, Element target) {
        if (changeIfMatch(getCurrentPath(), node, target)) {
            return node;
        }
        return super.visitMethodInvocation(node, target);
    }

    @Override
    public Tree visitMemberReference(MemberReferenceTree node, Element target) {
        if (changeIfMatch(getCurrentPath(), node, target)) {
            return node;
        }
        return super.visitMemberReference(node, target);
    }

    private boolean changeIfMatch(TreePath currentPath, Tree node, final Element target) throws IllegalArgumentException {
        Element el = workingCopy.getTrees().getElement(currentPath);
        if (el == null) {
            return false;
        }
        TreePathHandle elementBeingMoved = isElementBeingMoved(el);
        if (elementBeingMoved != null) {

            final FileObject folder = targetHandle.getFileObject().getParent();
            final CompilationUnitTree compilationUnit = currentPath.getCompilationUnit();
            checkForUsagesOutsideOfPackage(folder, compilationUnit, elementBeingMoved);
            checkForUsagesOutsideOfType(target, currentPath, elementBeingMoved);

            if (node instanceof MethodInvocationTree) {
                if (!delegate) {
                    changeMethodInvocation((ExecutableElement) el, (MethodInvocationTree) node, currentPath, target);
                }
            } else if (node instanceof IdentifierTree) {
                changeIdentifier(el, (IdentifierTree) node, currentPath, target);
            } else if (node instanceof MemberSelectTree) {
                changeMemberSelect(el, (MemberSelectTree) node, currentPath, target);
            } else if (node.getKind() == Tree.Kind.MEMBER_REFERENCE) {
                changeMemberRefer(el, (MemberReferenceTree) node, currentPath, target);

            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Tree visitClass(ClassTree node, Element target) {
        insertIfMatch(getCurrentPath(), node, target);
        return super.visitClass(node, target);
    }

    @Override
    public Tree visitVariable(VariableTree node, Element target) {
        if (removeIfMatch(getCurrentPath(), target)) {
            return node;
        } else {
            return super.visitVariable(node, target);
        }
    }

    @Override
    public Tree visitMethod(MethodTree node, Element target) {
        if (removeIfMatch(getCurrentPath(), target)) {
            return node;
        } else {
            return super.visitMethod(node, target);
        }
    }

    private void changeMemberRefer(Element el, final MemberReferenceTree node, TreePath currentPath, final Element target) {
        if (el.getModifiers().contains(Modifier.STATIC)) {
            Tree oldT = node.getQualifierExpression();
            Tree newT = make.QualIdent(make.setLabel(make.QualIdent(target), target.getSimpleName()).toString());
            rewrite(oldT, newT);
        } else {
            SourcePositions positions = workingCopy.getTrees().getSourcePositions();
            long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
            long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
            String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
            problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "WRN_NoAccessor", source))); //NOI18N
        }
    }

    private void changeMemberSelect(Element el, final MemberSelectTree node, TreePath currentPath, final Element target) {
        if (el.getModifiers().contains(Modifier.STATIC)) {
            ExpressionTree expression = node.getExpression();
            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, true);
            Element enclosingElement = workingCopy.getTrees().getElement(enclosingClassPath);
            if (target.equals(enclosingElement)) {
                IdentifierTree newIdt = make.Identifier(node.getIdentifier());
                rewrite(node, newIdt);
            } else {
                ExpressionTree newIdent = make.QualIdent(target);
                rewrite(expression, newIdent);
            }
        } else {
            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, false);
            Scope scope = workingCopy.getTrees().getScope(currentPath);
            Element enclosingElement = workingCopy.getTrees().getElement(enclosingClassPath);
            if (target.equals(enclosingElement)
                    && node.getKind() == Tree.Kind.MEMBER_SELECT
                    && !scope.getEnclosingMethod().getModifiers().contains(Modifier.STATIC)) {
                IdentifierTree newIdt = make.Identifier(((MemberSelectTree) node).getIdentifier());
                rewrite(node, newIdt);
            } else {
                Iterable<? extends Element> vars = workingCopy.getElementUtilities().getLocalMembersAndVars(scope, new ElementUtilities.ElementAcceptor() {

                    @Override
                    public boolean accept(Element e, TypeMirror type) { // Type will always be null
                        return workingCopy.getTypes().isSameType(e.asType(), target.asType()) && isElementBeingMoved(e) == null;
                    }
                });
                if (!vars.iterator().hasNext()) {
                    SourcePositions positions = workingCopy.getTrees().getSourcePositions();
                    long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
                    long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
                    String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
                    problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersRefactoringPlugin.class, "WRN_NoAccessor", source))); //NOI18N
                } else {
                    Element localVar = vars.iterator().next();
                    MemberSelectTree selectTree = (MemberSelectTree) node;

                    Tree it = selectTree.getExpression();
                    Tree newIt = make.Identifier(localVar);
                    if (it != null && newIt != null) {
                        rewrite(it, newIt);
                    }
                }
            }
        }
    }

    private void changeIdentifier(Element el, final IdentifierTree node, TreePath currentPath, final Element target) {
        if (el.getModifiers().contains(Modifier.STATIC)) {
            IdentifierTree it = (IdentifierTree) node;
            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, true);
            Element enclosingElement = workingCopy.getTrees().getElement(enclosingClassPath);
            if (enclosingElement == null || !enclosingElement.equals(target)) {
                ExpressionTree qualIdent = make.QualIdent(target);
                MemberSelectTree memberSelect = make.MemberSelect(qualIdent, it.getName().toString());
                rewrite(it, memberSelect);
            }
        } else {
            Scope scope = workingCopy.getTrees().getScope(currentPath);

            // TODO Maybe move to configuration
            Iterable<? extends Element> vars = workingCopy.getElementUtilities().getLocalMembersAndVars(scope, new ElementUtilities.ElementAcceptor() {

                @Override
                public boolean accept(Element e, TypeMirror type) { // Type will always be null
                    return workingCopy.getTypes().isSameType(e.asType(), target.asType());
                }
            });
            if (!vars.iterator().hasNext()) {
                SourcePositions positions = workingCopy.getTrees().getSourcePositions();
                    long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
                    long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
                    String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
                    problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_NoAccessor", source))); //NOI18N
            } else {
                Tree it;
                Tree newIt;
                Element localVar = vars.iterator().next();
                IdentifierTree variableTree = (IdentifierTree) node;
                it = node;
                newIt = make.setLabel(node, localVar.getSimpleName().toString() + "." + variableTree.getName().toString()); //NOI18N
                if (it != null && newIt != null) {
                    rewrite(it, newIt);
                }
            }
        }
    }

    /**
     * Changing a method invocation to refer to the new location.
     *
     * Steps: 1. Check if we need to remove a parameter from the invocation. 2.
     * Check if it is a Static method. 2.1 Change methodSelect 2.2 Translate
     * method arguments
     *
     * 3. Find Parameter or local var to use 3.1 Create problem if no accessor
     * 4. Check if it needs an argument for local accessors 4.1 Check if it van
     * be the memberselect
     *
     * 5. Create a new method invocation
     *
     * @param el
     * @param node
     * @param currentPath
     * @param target
     */
    private void changeMethodInvocation(final ExecutableElement el, final MethodInvocationTree node, final TreePath currentPath, final Element target) {
        rewrite(node, createMethodInvocationTree(el, node, currentPath, target, false));
    }

    private MethodInvocationTree createMethodInvocationTree(final ExecutableElement el, final MethodInvocationTree node, final TreePath currentPath, final Element target, boolean delegate) {
        TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, true);
        Element enclosingElement = workingCopy.getTrees().getElement(enclosingClassPath);

        final LinkedList<ExpressionTree> arguments = new LinkedList<>(node.getArguments());
        ExpressionTree newMethodSelect;

        if (el.getModifiers().contains(Modifier.STATIC)) {
            if (node.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                if (enclosingElement != null && enclosingElement.equals(target)) {
                    newMethodSelect = make.Identifier(((MemberSelectTree) node.getMethodSelect()).getIdentifier());
                } else {
                    newMethodSelect = make.MemberSelect(make.QualIdent(target), ((MemberSelectTree) node.getMethodSelect()).getIdentifier().toString());
                }
            } else { // if (methodSelect.getKind() == Tree.Kind.IDENTIFIER) {
                if (enclosingElement == null || !enclosingElement.equals(target)) {
                    newMethodSelect = make.MemberSelect(make.QualIdent(target), el);
                } else {
                    newMethodSelect = node.getMethodSelect();
                }
            }
        } else {
            final ExpressionTree selectExpression;
            int removedIndex = -1;
            List<? extends VariableElement> parameters = el.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                VariableElement variableElement = parameters.get(i);
                if (workingCopy.getTypes().isSameType(variableElement.asType(), target.asType())) {
                    removedIndex = i;
                    break;
                }
            }
            if (removedIndex != -1) {
                selectExpression = node.getArguments().get(removedIndex);
            } else {
                Scope scope = workingCopy.getTrees().getScope(currentPath);
                Iterable<? extends Element> vars = workingCopy.getElementUtilities().getLocalMembersAndVars(scope, new ElementUtilities.ElementAcceptor() {

                    @Override
                    public boolean accept(Element e, TypeMirror type) { // Type will always be null
                        return workingCopy.getTypes().isSameType(e.asType(), target.asType());
                    }
                });
                if (!vars.iterator().hasNext()) {
                    if(delegate) {
                        problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_NoAccessor", NbBundle.getMessage(MoveMembersTransformer.class, "TXT_DelegatingMethod"))));
                    } else {
                        SourcePositions positions = workingCopy.getTrees().getSourcePositions();
                        long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
                        long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
                        String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
                        problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_NoAccessor", source))); //NOI18N
                    }
                    selectExpression = null;
                } else {
                    Element localVar = vars.iterator().next();
                    selectExpression = make.Identifier(localVar);
                }
            }

            if (selectExpression == null) {
                newMethodSelect = node.getMethodSelect();
            } else {
                if (node.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                    MemberSelectTree selectTree = (MemberSelectTree) node.getMethodSelect();
                    boolean inStatic = false;
                    TreePath blockPath = currentPath;
                    while(blockPath != null) {
                        if(blockPath.getLeaf().getKind() == Tree.Kind.BLOCK) {
                            TreePath parentPath = blockPath.getParentPath();
                            if(parentPath != null && parentPath.getLeaf().getKind() == Tree.Kind.METHOD) {
                                MethodTree enclosingMethod = (MethodTree) parentPath.getLeaf();
                                if(enclosingMethod.getModifiers().getFlags().contains(Modifier.STATIC)) {
                                    inStatic = true;
                                }
                            } else if(parentPath != null && parentPath.getLeaf().getKind() == Tree.Kind.CLASS) {
                                inStatic = true;
                            }
                        }
                        blockPath = blockPath.getParentPath();
                    }
                    if (enclosingElement.equals(target)) {
                        if(inStatic) {
                            SourcePositions positions = workingCopy.getTrees().getSourcePositions();
                            long startPosition = positions.getStartPosition(workingCopy.getCompilationUnit(), node);
                            long lineNumber = workingCopy.getCompilationUnit().getLineMap().getLineNumber(startPosition);
                            String source = FileUtil.getFileDisplayName(workingCopy.getFileObject()) + ':' + lineNumber;
                            problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_NoAccessor", source))); //NOI18N
                            newMethodSelect = node.getMethodSelect();
                        } else {
                            newMethodSelect = make.Identifier(((MemberSelectTree) node.getMethodSelect()).getIdentifier());
                        }
                    } else {
                        newMethodSelect = make.MemberSelect(selectExpression, selectTree.getIdentifier());
                    }
                } else { // if (node.getMethodSelect().getKind() == Tree.Kind.IDENTIFIER) {
                    IdentifierTree variableTree = (IdentifierTree) node.getMethodSelect();
                    newMethodSelect = make.MemberSelect(selectExpression, variableTree.getName().toString());
                }
            }

            if (removedIndex != -1) {
                arguments.remove(removedIndex);
            }
            TypeMirror sourceType = workingCopy.getTrees().getTypeMirror(enclosingClassPath);
            ErrorAwareTreeScanner<Boolean, TypeMirror> needsArgumentScanner = new ErrorAwareTreeScanner<Boolean, TypeMirror>() {
                // Logic is a copy from #insertIfMatch

                @Override
                public Boolean visitMemberSelect(MemberSelectTree node, TypeMirror source) {
                    String isThis = node.getExpression().toString();
                    if (isThis.equals("this") || isThis.endsWith(".this")) { //NOI18N
                        TreePath thisPath = new TreePath(currentPath, node);
                        Element el = workingCopy.getTrees().getElement(thisPath);
                        if (el != null && isElementBeingMoved(el) != null) {
                            return false;
                        }
                    }
                    return super.visitMemberSelect(node, source);
                }

                @Override
                public Boolean visitIdentifier(IdentifierTree node, TypeMirror source) {
                    TreePath thisPath = new TreePath(currentPath, node);
                    Element el = workingCopy.getTrees().getElement(thisPath);
                    
                    if (el != null && isElementBeingMoved(el) == null) {
                        String isThis = node.toString();
                        // TODO: Check for super keyword. if super is used, but it is not overloaded, there is no problem. else warning.
                        if (isThis.equals("this") || isThis.endsWith(".this")) { //NOI18N
                            if (!el.getModifiers().contains(Modifier.STATIC)) {
                                return true;
                            }
                        } else {
                            if (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.FIELD || el.getKind() == ElementKind.ENUM) {
                                TypeElement elType = workingCopy.getElementUtilities().enclosingTypeElement(el);
                                if (elType != null && workingCopy.getTypes().isSubtype(source, elType.asType())) {
                                    if (!el.getModifiers().contains(Modifier.STATIC)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return super.visitIdentifier(node, source);
                }

                @Override
                public Boolean reduce(Boolean r1, Boolean r2) {
                    return (r1 == Boolean.TRUE || r2 == Boolean.TRUE);
                }
            };
            Boolean needsArgument = needsArgumentScanner.scan(workingCopy.getTrees().getTree(el).getBody(), sourceType);
            if (needsArgument == Boolean.TRUE) {
                ExpressionTree newArgument;
                if (enclosingElement.equals(target) && node.getMethodSelect().getKind() == Tree.Kind.MEMBER_SELECT) {
                    newArgument = ((MemberSelectTree) node.getMethodSelect()).getExpression();
                } else {
                    newArgument = workingCopy.getTreeUtilities().parseExpression("this", new SourcePositions[1]); //NOI18N
                }
                if (el.isVarArgs()) {
                    arguments.add(arguments.size() - 1, newArgument);
                } else {
                    arguments.add(newArgument);
                }
            }
        }
        
        List<ExpressionTree> typeArguments = new LinkedList<ExpressionTree>((List<? extends ExpressionTree>)node.getTypeArguments());
        Element returnType = workingCopy.getTypes().asElement(el.getReturnType());
        if(returnType != null && returnType.getKind() == ElementKind.TYPE_PARAMETER) {
            TypeParameterElement typeParameterElement = (TypeParameterElement) returnType;
            if(typeParameterElement.getGenericElement().getKind() != ElementKind.METHOD) {
                ExpressionTree methodSelect = node.getMethodSelect();
                if(methodSelect.getKind() == Tree.Kind.MEMBER_SELECT) {
                    VariableElement element = (VariableElement) workingCopy.getTrees().getElement(new TreePath(currentPath, ((MemberSelectTree)methodSelect).getExpression()));
                    if (element != null) {
                        TypeMirror asType = element.asType();
                        if(asType.getKind() == TypeKind.DECLARED) {
                            List<? extends TypeMirror> typeArguments1 = ((DeclaredType)asType).getTypeArguments();
                            for (TypeMirror typeMirror : typeArguments1) {
                                typeArguments.add((ExpressionTree)make.Type(typeMirror));
                            }
                        }
                    }
                } else {
                    ClassTree classTree = workingCopy.getTrees().getTree((TypeElement)enclosingElement);
                    for (TypeParameterTree typeParameterTree : classTree.getTypeParameters()) {
                        if(typeParameterTree.getName().contentEquals(el.getReturnType().toString())) {
                            typeArguments.add((ExpressionTree)make.Type(typeParameterTree.getName().toString()));
                        }
                    }
                }
            }
        }
        if(!typeArguments.isEmpty() &&
                newMethodSelect.getKind() == Tree.Kind.IDENTIFIER) {
            newMethodSelect = make.MemberSelect(make.Identifier("this"), ((IdentifierTree)newMethodSelect).getName());
        }
        return make.MethodInvocation(typeArguments, newMethodSelect, arguments);
    }

    private void checkForUsagesOutsideOfPackage(final FileObject folder, final CompilationUnitTree compilationUnit, TreePathHandle elementBeingMoved) {
        if (!RefactoringUtils.getPackageName(folder).equals(
                RefactoringUtils.getPackageName(compilationUnit))) {
            usageOutsideOfPackage.put(elementBeingMoved, Boolean.TRUE);
        }
    }
    
    private void checkForUsagesOutsideOfType(final Element target, TreePath currentPath, TreePathHandle elementBeingMoved) {
        final Types types = workingCopy.getTypes();
        TypeMirror targetType = target.asType();
        TreePath enclosingPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, false);
        Element enclosingEl = null;
        if(enclosingPath != null) {
            enclosingEl = workingCopy.getTrees().getElement(enclosingPath);
        }
        if(enclosingEl != null) {
            TypeMirror enclosingType = enclosingEl.asType();
            if(!(enclosedBy(targetType, enclosingType) || enclosedBy(enclosingType, targetType)) && !types.isSameType(enclosingType, targetType)) {
                usageOutsideOfType.put(elementBeingMoved, Boolean.TRUE);
            }
        } else {
            usageOutsideOfType.put(elementBeingMoved, Boolean.TRUE);
        }
    }

    private void insertIfMatch(TreePath currentPath, ClassTree node, final Element target) throws IllegalArgumentException {
        Element el = workingCopy.getTrees().getElement(currentPath);
        if (el == null) {
            return;
        }
        if (el.equals(target)) {
            ClassTree newClassTree = node;
            for (TreePathHandle tph : allElements) {

                final TreePath resolvedPath = tph.resolve(workingCopy);
                if (resolvedPath == null) {
                    // XXX - should report a problem ?
                    continue;
                }
                Tree member = resolvedPath.getLeaf();
                Tree newMember = null;
                Element resolvedElement = workingCopy.getTrees().getElement(resolvedPath);
                if (resolvedElement == null) {
                    continue;
                }
                final GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
                genUtils.importComments(member, resolvedPath.getCompilationUnit());
                // Make a new Method tree
                if (member.getKind() == Tree.Kind.METHOD) {

                    // Change Modifiers
                    final MethodTree methodTree = (MethodTree) member;
                    ExecutableElement method = (ExecutableElement) resolvedElement;
                    ModifiersTree modifiers = changeModifiers(genUtils.importFQNs(methodTree.getModifiers()), usageOutsideOfPackage.get(tph) == Boolean.TRUE, usageOutsideOfType.get(tph) == Boolean.TRUE);

                    // Find and remove a usable parameter
                    final List<? extends VariableTree> parameters = methodTree.getParameters();
                    LinkedList<VariableTree> newParameters;
                    VariableTree removedParameter = null;
                    boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
                    newParameters = new LinkedList<VariableTree>();
                    for (int i = 0; i < parameters.size(); i++) {
                        VariableTree variableTree = parameters.get(i);
                        TypeMirror type = workingCopy.getTrees().getTypeMirror(TreePath.getPath(resolvedPath, variableTree));
                        if (!isStatic && removedParameter == null && type != null && workingCopy.getTypes().isSameType(type, target.asType())) {
                            removedParameter = variableTree;
                        } else {
                            newParameters.add(genUtils.importFQNs(variableTree));
                        }
                    }
                    // Scan the body and fix references
                    BlockTree body = methodTree.getBody();
                    final TreePath bodyPath = new TreePath(resolvedPath, body);
                    final Trees trees = workingCopy.getTrees();
                    final Map<ExpressionTree, ExpressionTree> fqns = new HashMap<ExpressionTree, ExpressionTree>();
                    ErrorAwareTreeScanner<Void, Void> fqnScan = new ErrorAwareTreeScanner<Void, Void>() {

                        @Override
                        public Void visitIdentifier(IdentifierTree node, Void p) {
                            TreePath treePath = trees.getPath(bodyPath.getCompilationUnit(), node);
                            if(!JavaPluginUtils.isSyntheticPath(workingCopy, treePath)) {
                                // FIXME: path may skip some intermediate types which bring the identifier.
                                Element el = trees.getElement(treePath);
                                if (el != null) {
                                    fqns.put(node, make.Identifier(el));
                                }
                            }
                            return super.visitIdentifier(node, p);
                        }
                    };
                    fqnScan.scan(body, null);
                    body = (BlockTree) workingCopy.getTreeUtilities().translate(body, fqns);

                    // Remove the parameter and change it to the keyword this
                    final Map<ExpressionTree, ExpressionTree> original2Translated = new HashMap<ExpressionTree, ExpressionTree>();
                    // Add parameter and change local accessors
                    TreePath sourceClass = JavaRefactoringUtils.findEnclosingClass(workingCopy, resolvedPath, true, true, true, true, true);
                    TypeMirror sourceType = workingCopy.getTrees().getTypeMirror(sourceClass);
                    final String parameterName = getParameterName(sourceType, workingCopy.getTrees().getScope(bodyPath), workingCopy);
                    ErrorAwareTreeScanner<Boolean, TypeMirror> idScan = new ErrorAwareTreeScanner<Boolean, TypeMirror>() {

                        @Override
                        public Boolean visitMemberSelect(MemberSelectTree node, TypeMirror source) {
                            String isThis = node.getExpression().toString();
                            if (isThis.equals("this") || isThis.endsWith(".this")) { //NOI18N
                                TreePath currentPath = new TreePath(resolvedPath, node);
                                Element el = trees.getElement(currentPath);
                                if (el != null && isElementBeingMoved(el) != null) {
                                    return false;
                                }
                            } else {
                                TreePath currentPath = new TreePath(resolvedPath, node);
                                Element el = trees.getElement(currentPath);
                                if (el != null && isElementBeingMoved(el) != null &&
                                        el.getKind() != ElementKind.PACKAGE &&
                                        el.getModifiers().contains(Modifier.STATIC)) {
                                    ExpressionTree ident = make.Identifier(target);
                                    MemberSelectTree memberSelect = make.MemberSelect(ident, el);
                                    original2Translated.put(node, memberSelect);
                                }
                            }
                            return super.visitMemberSelect(node, source);
                        }

                        @Override
                        public Boolean visitIdentifier(IdentifierTree node, TypeMirror source) {
                            TreePath currentPath = new TreePath(resolvedPath, node);
                            Element el = trees.getElement(currentPath);

                            boolean result = false;

                            if (el != null && isElementBeingMoved(el) == null && el.getKind() != ElementKind.PACKAGE) {
                                TypeElement elType = workingCopy.getElementUtilities().enclosingTypeElement(el);
                                // TODO: Check for super keyword. if super is used, but it is not overloaded, there is no problem. else warning.
                                String isThis = node.toString();
                                if (isThis.equals("this") || isThis.endsWith(".this")) { //NOI18N
                                    // Check for static
                                    if (!el.getModifiers().contains(Modifier.STATIC)) {
                                        ExpressionTree newLabel = make.setLabel(node, parameterName);
                                        original2Translated.put(node, newLabel);
                                        result = true;
                                    } else {
                                        ExpressionTree ident = make.Identifier(elType);
                                        original2Translated.put(node, ident);
                                    }
                                } else {
                                    if (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.FIELD || el.getKind() == ElementKind.ENUM) {
                                        if (elType != null && workingCopy.getTypes().isSubtype(source, elType.asType())) {
                                            if (!el.getModifiers().contains(Modifier.STATIC)) {
                                                MemberSelectTree memberSelect = make.MemberSelect(workingCopy.getTreeUtilities().parseExpression(parameterName, new SourcePositions[1]), el);
                                                original2Translated.put(node, memberSelect);
                                                result = true;
                                            } else {
                                                ExpressionTree ident = make.Identifier(elType);
                                                MemberSelectTree memberSelect = make.MemberSelect(ident, el);
                                                original2Translated.put(node, memberSelect);
                                            }
                                        }
                                    }
                                }
                            }
                            return super.visitIdentifier(node, source) == Boolean.TRUE || result;
                        }

                        @Override
                        public Boolean reduce(Boolean r1, Boolean r2) {
                            return (r1 == Boolean.TRUE || r2 == Boolean.TRUE);
                        }
                    };
                    boolean addParameter = idScan.scan(body, sourceType) == Boolean.TRUE;

                    if (removedParameter != null) {
                        ErrorAwareTreeScanner<Void, Pair<Element, ExpressionTree>> idScan2 = new ErrorAwareTreeScanner<Void, Pair<Element, ExpressionTree>>() {

                            @Override
                            public Void visitIdentifier(IdentifierTree node, Pair<Element, ExpressionTree> p) {
                                TreePath currentPath = new TreePath(resolvedPath, node);
                                Element el = trees.getElement(currentPath);
                                if (p.first().equals(el)) {
                                    original2Translated.put(node, p.second());
                                }
                                return super.visitIdentifier(node, p);
                            }
                        };
                        TreePath path = new TreePath(resolvedPath, removedParameter);
                        Element element = trees.getElement(path);
                        if (element != null) {
                            final Pair<Element, ExpressionTree> pair = Pair.of(element, workingCopy.getTreeUtilities().parseExpression("this", new SourcePositions[1])); // NOI18N
                            idScan2.scan(body, pair);
                        }
                    }

                    body = (BlockTree) workingCopy.getTreeUtilities().translate(body, original2Translated);

                    if (addParameter) {
                        VariableTree vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), parameterName, make.QualIdent(sourceType.toString()), null);
                        if (method.isVarArgs()) {
                            newParameters.add(newParameters.size() - 1, vt);
                        } else {
                            newParameters.add(vt);
                        }
                    }

                    // Addimports
                    body = genUtils.importFQNs(body);
                    List<TypeParameterTree> typeParameters = new LinkedList<TypeParameterTree>(methodTree.getTypeParameters());
                    if(method.getReturnType().getKind() == TypeKind.TYPEVAR) {
                        Element element = workingCopy.getTypes().asElement(method.getReturnType());
                        if(element.getKind() == ElementKind.TYPE_PARAMETER) {
                            TypeParameterElement typeParameterElement = (TypeParameterElement) element;
                            if(typeParameterElement.getGenericElement().getKind() != ElementKind.METHOD) {
                                List<ExpressionTree> bounds = new LinkedList<ExpressionTree>();
                                for (TypeMirror typeMirror : typeParameterElement.getBounds()) {
                                    if(!typeMirror.toString().equals("java.lang.Object")) { //NOI18N
                                        bounds.add((ExpressionTree)make.Type(typeMirror));
                                    }
                                }
                                typeParameters.add(make.TypeParameter(typeParameterElement.getSimpleName(), bounds));
                            }
                        }
                    }
                    Tree returnType = methodTree.getReturnType();
                    if(returnType != null) {
                        final TreePath returnPath = new TreePath(resolvedPath, returnType);
                        Element returnTypeEl = trees.getElement(returnPath);
                        if(returnTypeEl != null && returnTypeEl.getKind() != ElementKind.TYPE_PARAMETER && isElementBeingMoved(returnTypeEl) == null) {
                            returnType = genUtils.importFQNs(returnType);
                        }
                    }
                    newMember = make.Method(modifiers, methodTree.getName(), returnType, typeParameters, newParameters, methodTree.getThrows(), body, (ExpressionTree) methodTree.getDefaultValue());

                    // Make a new Variable (Field) tree
                } else if (member.getKind() == Tree.Kind.VARIABLE) {
                    VariableTree field = (VariableTree) member;
                    ModifiersTree modifiers = changeModifiers(genUtils.importFQNs(field.getModifiers()), usageOutsideOfPackage.get(tph) == Boolean.TRUE, usageOutsideOfType.get(tph) == Boolean.TRUE);

                    // Scan the initializer and fix references
                    ExpressionTree initializer = field.getInitializer();
                    initializer = fixReferences(initializer, target, resolvedPath);
                    VariableTree importFQNs = genUtils.importFQNs(field);
                    newMember = make.Variable(modifiers, field.getName(), importFQNs.getType(), initializer);
                }

                // Insert the member and copy its comments
                if (newMember != null) {
                    genUtils.copyComments(member, newMember, true);
                    genUtils.copyComments(member, newMember, false);
                    if(newMember.getKind() == Tree.Kind.METHOD) {
                        if(updateJavadoc) {
                            MethodTree method = (MethodTree) newMember;
                            List<Comment> comments = workingCopy.getTreeUtilities().getComments(method, true);
                            Comment comment;
                            if(comments.isEmpty()) {
                                comment = generateJavadoc(method, target, false);
                            } else {
                                if(comments.get(0).isDocComment()) {
                                    make.removeComment(method, 0, true);
                                    comment = updateJavadoc(resolvedElement, target, false);
                                } else {
                                    comment = generateJavadoc(method, target, false);
                                }
                            }
                            make.addComment(newMember, comment, true);
                        }
                    }
                    newClassTree = genUtils.insertClassMember(newClassTree, newMember);
                }
            }
            rewrite(node, newClassTree);
        }
    }

    private boolean removeIfMatch(TreePath currentPath, Element target) throws IllegalArgumentException {
        Element el = workingCopy.getTrees().getElement(currentPath);
        if (el == null) {
            return false;
        }
        if (isElementBeingMoved(el) != null) {
            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, currentPath, true, true, true, true, true);
            ClassTree classTree = (ClassTree) enclosingClassPath.getLeaf();
            ClassTree newClassTree = classTree;
            for (TreePathHandle tph : allElements) {
                TreePath resolvedPath = tph.resolve(workingCopy);
                if (resolvedPath == null) {
                    // XXX: report missing target ?
                    continue;
                }
                Tree member = resolvedPath.getLeaf();
                if (delegate && member.getKind() == Tree.Kind.METHOD) {
                    MethodTree methodTree = (MethodTree) member;
                    int index = newClassTree.getMembers().indexOf(methodTree);
                    newClassTree = make.removeClassMember(newClassTree, methodTree);
                    ExecutableElement element = (ExecutableElement) workingCopy.getTrees().getElement(resolvedPath);
                    if (element == null) {
                        continue;
                    }
                    List<ExpressionTree> paramList = new ArrayList<ExpressionTree>();

                    for (VariableElement variableElement : element.getParameters()) {
                        IdentifierTree vt = make.Identifier(variableElement.getSimpleName().toString());
                        paramList.add(vt);
                    }

                    MethodInvocationTree methodInvocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                            make.Identifier(element),
                            paramList);
                    methodInvocation = createMethodInvocationTree(element, methodInvocation, currentPath, target, true);

                    TypeMirror methodReturnType = element.getReturnType();

                    final StatementTree statement;
                    final Types types = workingCopy.getTypes();
                    if (!types.isSameType(methodReturnType, types.getNoType(TypeKind.VOID))) {
                        statement = make.Return(methodInvocation);
                    } else {
                        statement = make.ExpressionStatement(methodInvocation);
                    }
                    ModifiersTree modifiers = methodTree.getModifiers();
                    if(deprecate) {
                        AnnotationTree annotation = make.Annotation(make.Identifier("Deprecated"), Collections.EMPTY_LIST); //NOI18N
                        modifiers = make.addModifiersAnnotation(modifiers, annotation);
                    }
                    MethodTree method = make.Method(modifiers, methodTree.getName(), methodTree.getReturnType(), methodTree.getTypeParameters(), methodTree.getParameters(), methodTree.getThrows(), make.Block(Collections.singletonList(statement), false), (ExpressionTree) methodTree.getDefaultValue());
                    GeneratorUtilities.get(workingCopy).importComments(member, resolvedPath.getCompilationUnit());
                    GeneratorUtilities.get(workingCopy).copyComments(member, method, true);
                    GeneratorUtilities.get(workingCopy).copyComments(member, method, false);
                    if(updateJavadoc) {
                        List<Comment> comments = workingCopy.getTreeUtilities().getComments(method, true);
                        Comment comment;
                        if(comments.isEmpty()) {
                            comment = generateJavadoc(method, target, deprecate);
                        } else {
                            if(comments.get(0).isDocComment()) {
                                make.removeComment(method, 0, true);
                                comment = updateJavadoc(element, target, deprecate);
                            } else {
                                comment = generateJavadoc(method, target, deprecate);
                            }
                        }
                        make.addComment(method, comment, true);
                    }
                    newClassTree = make.insertClassMember(newClassTree, index, method);
                } else {
                    newClassTree = make.removeClassMember(newClassTree, member);
                }
            }
            rewrite(classTree, newClassTree);
            return true;
        }
        return false;
    }
    
    private Comment updateJavadoc(Element method, Element targetElement, boolean addDeprecated) {
        DocCommentTree javadoc = workingCopy.getDocTrees().getDocCommentTree(method);
        
        List<DocTree> otherTags = new LinkedList<>();
        List<DocTree> returnTags = new LinkedList<>();
        List<DocTree> throwsTags = new LinkedList<>();
        List<DocTree> paramTags = new LinkedList<>();

        for (DocTree tag : javadoc.getBlockTags()) {
            switch (tag.getKind()) {
                case RETURN: returnTags.add(tag); break;
                case THROWS: throwsTags.add(tag); break;
                case PARAM: paramTags.add(tag); break;
                default: otherTags.add(tag);
            }
        }
        
        StringBuilder text = new StringBuilder(javadoc.getBody().stream().map(t -> t.toString()).collect(Collectors.joining(""))).append("\n\n"); // NOI18N
        text.append(tagsToString(paramTags));
        text.append(tagsToString(returnTags));
        text.append(tagsToString(throwsTags));
        text.append(tagsToString(otherTags));
        if(addDeprecated) {
            String target = targetElement.asType().toString() + "#" + method.getSimpleName(); // NOI18N
            text.append(org.openide.util.NbBundle.getMessage(MoveMembersTransformer.class, "TAG_Deprecated", target));
        }
        Comment comment = Comment.create(Comment.Style.JAVADOC, NOPOS, NOPOS, NOPOS, text.toString());
        return comment;
    }
    
    private String tagsToString(List<DocTree> tags) {
        StringBuilder sb = new StringBuilder();
        for (DocTree tag : tags) {
            sb.append(tag.toString()).append("\n"); // NOI18N
        }
        return sb.toString();
    }
    
    private Comment generateJavadoc(MethodTree current, Element targetElement, boolean addDeprecated) {
        Tree returnType = current.getReturnType();
        StringBuilder builder = new StringBuilder("\n"); // NOI18N
        for (VariableTree variableTree : current.getParameters()) {
            builder.append(String.format("@param %s the value of %s", variableTree.getName(), variableTree.getName())); // NOI18N
            builder.append("\n"); // NOI18N
        }
        boolean hasReturn = false;
        if (returnType != null && returnType.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
            if (((PrimitiveTypeTree) returnType).getPrimitiveTypeKind() != TypeKind.VOID) {
                hasReturn = true;
            }
        }
        if(hasReturn) {
            builder.append("@return the ").append(returnType).append("\n"); // NOI18N
        }
        for (ExpressionTree expressionTree : current.getThrows()) {
            builder.append("@throws ").append(expressionTree).append("\n"); // NOI18N
        }
        if(addDeprecated) {
            String target = targetElement.asType().toString() + "#" + current.getName(); // NOI18N
            builder.append(org.openide.util.NbBundle.getMessage(MoveMembersTransformer.class, "TAG_Deprecated", target));
        }
        Comment comment = Comment.create(
                Comment.Style.JAVADOC, NOPOS, NOPOS, NOPOS,
                builder.toString());
        return comment;
    }

    private TreePathHandle isElementBeingMoved(Element el) {
        for (TreePathHandle mh : allElements) {
            Element element = mh.resolveElement(workingCopy);
            if (element == null) {
                Logger.getLogger("org.netbeans.modules.refactoring.java").log(Level.INFO, "MoveMembersTransformer cannot resolve {0}", mh); //NOI18N
                continue;
            }
            if (element.equals(el)) {
                return mh;
            }
        }
        return null;
    }

    private ModifiersTree changeModifiers(ModifiersTree modifiersTree, boolean usageOutsideOfPackage, boolean usageOutsideOfType) {
        final Set<Modifier> flags = modifiersTree.getFlags();
        Set<Modifier> newModifiers = flags.isEmpty() ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(flags);
        switch (visibility) {
            case ESCALATE:
                if (usageOutsideOfPackage) {
                    if (!flags.contains(Modifier.PUBLIC)) { // TODO: if only subtype, change protected
                        newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                        newModifiers.add(Modifier.PUBLIC);
                    }
                } else {
                    if(usageOutsideOfType) {
                        if (flags.contains(Modifier.PRIVATE)) {
                            newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                        }
                    }
                }
                break;
            case ASIS:
            default:
                break;
            case PUBLIC:
                newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                newModifiers.add(Modifier.PUBLIC);
                break;
            case PROTECTED:
                newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                newModifiers.add(Modifier.PROTECTED);
                break;
            case DEFAULT:
                newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                break;
            case PRIVATE:
                newModifiers.removeAll(ALL_ACCESS_MODIFIERS);
                newModifiers.add(Modifier.PRIVATE);
                break;
        }
        ModifiersTree modifiers = make.Modifiers(newModifiers, modifiersTree.getAnnotations());
        return modifiers;
    }

    private <T extends Tree> T fixReferences(T body, Element target, final TreePath resolvedPath) {

        TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(workingCopy, resolvedPath, true, true, true, true, true);
        final TypeElement enclosingClass = (TypeElement) workingCopy.getTrees().getElement(enclosingClassPath);
        final Map<Tree, Tree> original2Translated = new HashMap<Tree, Tree>();
        
        // TODO Change this to something like that is used for method body; importFqns
        ErrorAwareTreeScanner<Void, Void> idScan = new ErrorAwareTreeScanner<Void, Void>() {
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                TreePath currentPath = new TreePath(resolvedPath, node);
                if (currentPath.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                    return super.visitIdentifier(node, p); // Already checked by visitMemberSelect
                }
                Element element = workingCopy.getTrees().getElement(currentPath);
                if (element != null && isElementBeingMoved(element) == null && element.getModifiers().contains(Modifier.STATIC)) {
                    Tree newTree = make.QualIdent(element);
                    original2Translated.put(node, newTree);
                }
                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                TreePath currentPath = new TreePath(resolvedPath, node);
                if (currentPath.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                    return super.visitMethodInvocation(node, p); // Already checked by visitMemberSelect
                }
                Element element = workingCopy.getTrees().getElement(currentPath);
                ExpressionTree methodSelect = node.getMethodSelect();
                if (element != null && isElementBeingMoved(element) == null) {
                    if (element.getModifiers().contains(Modifier.STATIC)) {
                        Tree newTree = make.QualIdent(element);
                        original2Translated.put(methodSelect, newTree);
                    } else {
                        problem = JavaPluginUtils.chainProblems(problem, new Problem(false, NbBundle.getMessage(MoveMembersTransformer.class, "WRN_InitNoAccess")));
                    }
                }
                return super.visitMethodInvocation(node, p);
            }

            @Override
            public Void visitMemberSelect(MemberSelectTree node, Void p) {
                Element element = workingCopy.getTrees().getElement(new TreePath(resolvedPath, node));
                if (element != null && isElementBeingMoved(element) == null && element.getModifiers().contains(Modifier.STATIC)) {
                    Tree newTree = make.QualIdent(element);
                    original2Translated.put(node, newTree);
                }
                return super.visitMemberSelect(node, p);
            }
        };
        idScan.scan(body, null);

        return (T) workingCopy.getTreeUtilities().translate(body, original2Translated);
    }

    private static String getParameterName(TypeMirror type, Scope scope, CompilationController info) {
        String name = JavaPluginUtils.getName(type);
        if (name == null) {
            name = JavaPluginUtils.DEFAULT_NAME;
        }

        return JavaPluginUtils.makeNameUnique(info, scope, name);
    }

    private boolean enclosedBy(TypeMirror t1, TypeMirror t2) {
        if(t1.getKind() == TypeKind.DECLARED) {
            if(workingCopy.getTypes().isSameType(t1, t2)) {
                return true;
            }
            DeclaredType dt = (DeclaredType) t1;
            TypeMirror enclosingType = dt.getEnclosingType();
            if(enclosingType.getKind() == TypeKind.NONE) {
                return false;
            } else {
                return enclosedBy(enclosingType, t2);
            }
        } else {
            return false;
        }
    }
}
