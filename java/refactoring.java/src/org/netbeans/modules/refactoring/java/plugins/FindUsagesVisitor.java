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
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.WhereUsedElement;
import org.netbeans.modules.refactoring.java.spi.JavaWhereUsedFilters;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class FindUsagesVisitor extends ErrorAwareTreePathScanner<Tree, Element> {

    private Collection<TreePath> usages = new ArrayList<>();
    private List<WhereUsedElement> elements = new ArrayList<>();
    protected CompilationController workingCopy;
    private boolean findInComments = false;
    private final boolean isSearchOverloadedMethods;
    private final boolean fromTestRoot;
    private final boolean fromPlatform;
    private final boolean fromDependency;
    private final AtomicBoolean inImport;
    private boolean usagesInComments;
    private final AtomicBoolean isCancelled;
    private List<ExecutableElement> methods;

    public FindUsagesVisitor(CompilationController workingCopy, AtomicBoolean isCancelled) {
        this(workingCopy, isCancelled, false, false);
    }
    
    public FindUsagesVisitor(CompilationController workingCopy, AtomicBoolean isCancelled, boolean findInComments, boolean isSearchOverloadedMethods) {
        this(workingCopy, isCancelled, findInComments, isSearchOverloadedMethods, RefactoringUtils.isFromTestRoot(workingCopy.getFileObject(), workingCopy.getClasspathInfo().getClassPath(PathKind.SOURCE)), false, false, new AtomicBoolean());
    }

    public FindUsagesVisitor(CompilationController workingCopy, AtomicBoolean isCancelled, boolean findInComments, boolean isSearchOverloadedMethods, boolean fromTestRoot, boolean fromPlatform, boolean fromDependency, AtomicBoolean inImport) {
        try {
            setWorkingCopy(workingCopy);
        } catch (ToPhaseException ex) {
            Exceptions.printStackTrace(ex);
        }
        this.findInComments = findInComments;
        this.isSearchOverloadedMethods = isSearchOverloadedMethods;
        this.fromTestRoot = fromTestRoot;
        this.fromPlatform = fromPlatform;
        this.fromDependency = fromDependency;
        this.inImport = inImport;
        this.isCancelled = isCancelled;
        this.methods = new LinkedList<>();
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        if (findInComments) {
            String originalName;
            if(p.getKind() == ElementKind.CONSTRUCTOR) {
                originalName = p.getEnclosingElement().getSimpleName().toString();
            } else {
                originalName = p.getSimpleName().toString();
            }
            TokenSequence<JavaTokenId> ts = workingCopy.getTokenHierarchy().tokenSequence(JavaTokenId.language());

            while (ts.moveNext()) {
                if(isCancelled.get()) {
                    return null;
                }
                Token t = ts.token();

                if (t.id() == JavaTokenId.BLOCK_COMMENT || t.id() == JavaTokenId.LINE_COMMENT || t.id() == JavaTokenId.JAVADOC_COMMENT) {
                    Scanner tokenizer = new Scanner(t.text().toString());
                    tokenizer.useDelimiter("[^a-zA-Z0-9_]"); //NOI18N
                    while (tokenizer.hasNext()) {
                        String current = tokenizer.next();
                        if (current.equals(originalName)) {
                            WhereUsedElement comment = WhereUsedElement.create(ts.offset() + tokenizer.match().start(),
                                                                               ts.offset() + tokenizer.match().end(), workingCopy, fromTestRoot, fromPlatform, fromDependency);
                            elements.add(comment);
                            usagesInComments = true;
                        }
                    }
                }
            }
        }
        if(RefactoringUtils.isExecutableElement(p)) {
            ExecutableElement method = (ExecutableElement) p;
            methods.add(method);
            TypeElement enclosingTypeElement = workingCopy.getElementUtilities().enclosingTypeElement(method);
            if(isSearchOverloadedMethods) {
                for (Element overloaded : enclosingTypeElement.getEnclosedElements()) {
                    if(method != overloaded &&
                            method.getKind() == overloaded.getKind() &&
                            ((ExecutableElement)overloaded).getSimpleName().contentEquals(method.getSimpleName())) {
                        methods.add((ExecutableElement)overloaded);
                    }
                }
            }
        }
        return super.visitCompilationUnit(node, p);
    }
    
    private void addIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if(isCancelled.get()) {
            return;
        }
        if (JavaPluginUtils.isSyntheticPath(workingCopy, path)) {
            if (ElementKind.CONSTRUCTOR != elementToFind.getKind()
                    || tree.getKind() != Tree.Kind.IDENTIFIER
                    || !"super".contentEquals(((IdentifierTree) tree).getName())) { // NOI18N
                // do not skip synthetic usages of constructor
                return;
            }
        }
        Trees trees = workingCopy.getTrees();
        Element el = trees.getElement(path);
        if (el == null) {
            path = path.getParentPath();
            if (path != null && path.getLeaf().getKind() == Kind.IMPORT) {
                ImportTree impTree = (ImportTree) path.getLeaf();
                if (!impTree.isStatic()) {
                    return;
                }
                Tree idTree = impTree.getQualifiedIdentifier();
                if (idTree.getKind() != Kind.MEMBER_SELECT) {
                    return;
                }
                final Name id = ((MemberSelectTree) idTree).getIdentifier();
                if (id.contentEquals("*")) {
                    return;
                }
                Tree classTree = ((MemberSelectTree) idTree).getExpression();
                path = trees.getPath(workingCopy.getCompilationUnit(), classTree);
                el = trees.getElement(path);
                if (el == null) {
                    return;
                }
                Iterator iter = workingCopy.getElementUtilities().getMembers(el.asType(), new ElementUtilities.ElementAcceptor() {
                    @Override
                    public boolean accept(Element e, TypeMirror type) {
                        return id.equals(e.getSimpleName());
                    }
                }).iterator();
                if (iter.hasNext()) {
                    el = (Element) iter.next();
                }
                if (iter.hasNext()) {
                    return;
                }
            } else {
                return;
            }
        }
        if (elementToFind != null && elementToFind.getKind() == ElementKind.METHOD && el.getKind() == ElementKind.METHOD) {
            for (ExecutableElement executableElement : methods) {
                if (el.equals(executableElement) 
                        || workingCopy.getElements().overrides((ExecutableElement) el,
                        executableElement, (TypeElement) elementToFind.getEnclosingElement())) {
                    addUsage(path);
                }
            }
        } else if (el.equals(elementToFind)) {
            final ElementKind kind = elementToFind.getKind();
            if(kind.isField() || kind == ElementKind.LOCAL_VARIABLE || kind == ElementKind.RESOURCE_VARIABLE || kind == ElementKind.PARAMETER) {
                JavaWhereUsedFilters.ReadWrite access;
                Element collectionElement = workingCopy.getElementUtilities().findElement("java.util.Collection"); //NOI18N
                Element mapElement = workingCopy.getElementUtilities().findElement("java.util.Map"); //NOI18N
                if(collectionElement != null &&
                        workingCopy.getTypes().isSubtype(
                                workingCopy.getTypes().erasure(el.asType()),
                                workingCopy.getTypes().erasure(collectionElement.asType()))) {
                    access = analyzeCollectionAccess(path);
                } else if(mapElement != null &&
                        workingCopy.getTypes().isSubtype(
                                workingCopy.getTypes().erasure(el.asType()),
                                workingCopy.getTypes().erasure(mapElement.asType()))) {
                    access = analyzeCollectionAccess(path);
                } else {
                    access = analyzeVarAccess(path, elementToFind, tree);
                }
                addUsage(path, access);
            } else {
                addUsage(path);
            }
        }
    }
    
    private Set<String> writeMethods = new HashSet<>(Arrays.asList(
            "add", "addAll", "putAll", "remove", "removeAll", "retainAll",
            "removeIf", "clear"));
    private Set<String> readMethods = new HashSet<>(Arrays.asList(
            "get", "getOrDefault", "first", "last", "firstKey", "lastKey",
            "contains", "containsKey", "containsValue", "containsAll", "size",
            "isEmpty", "indexOf"));
    private Set<String> readWriteMethods = new HashSet<>(Arrays.asList(
            "sort", "set", "put", "putIfAbsent", "replace"));
    private JavaWhereUsedFilters.ReadWrite analyzeCollectionAccess(TreePath path) {
        JavaWhereUsedFilters.ReadWrite result = null;
        TreePath parentPath = path.getParentPath();
        Tree parentTree = parentPath.getLeaf();
        Kind parentKind = parentTree.getKind();
        if(parentKind == Kind.MEMBER_SELECT) {
            Element member = workingCopy.getTrees().getElement(parentPath);
            if(member != null && member.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) member;
                if (writeMethods.contains(method.getSimpleName().toString())) {
                    result = JavaWhereUsedFilters.ReadWrite.WRITE;
                } else if (readMethods.contains(method.getSimpleName().toString())) {
                    result = JavaWhereUsedFilters.ReadWrite.READ;
                } else if (readWriteMethods.contains(method.getSimpleName().toString())) {
                    result = JavaWhereUsedFilters.ReadWrite.READ_WRITE;
                }
            }
        }
        return result;
    }

    private JavaWhereUsedFilters.ReadWrite analyzeVarAccess(TreePath path, Element elementToFind, Tree tree) {
        JavaWhereUsedFilters.ReadWrite access = JavaWhereUsedFilters.ReadWrite.READ;
        TreePath parentPath = path.getParentPath();
        Tree parentTree = parentPath.getLeaf();
        Kind parentKind = parentTree.getKind();
        if(elementToFind.asType().getKind() == TypeKind.ARRAY &&
                parentKind == Kind.ARRAY_ACCESS) {
            tree = parentPath.getLeaf();
            parentPath = parentPath.getParentPath();
            parentTree = parentPath.getLeaf();
            parentKind = parentTree.getKind();
        }
        switch(parentKind) {
            case ARRAY_ACCESS:
            case MEMBER_SELECT:
                // TODO: Check usages of arrays for writing
                break;
                
            case POSTFIX_INCREMENT:
            case POSTFIX_DECREMENT:
            case PREFIX_INCREMENT:
            case PREFIX_DECREMENT:
                access = JavaWhereUsedFilters.ReadWrite.READ_WRITE;
                break;
                
            case ASSIGNMENT: {
                AssignmentTree assignmentTree = (AssignmentTree) parentTree;
                ExpressionTree left = assignmentTree.getVariable();
                if (left.equals(tree)) {
                    access = JavaWhereUsedFilters.ReadWrite.WRITE;
                }
                break;
            }
            case MULTIPLY_ASSIGNMENT:
            case DIVIDE_ASSIGNMENT:
            case REMAINDER_ASSIGNMENT:
            case PLUS_ASSIGNMENT:
            case MINUS_ASSIGNMENT:
            case LEFT_SHIFT_ASSIGNMENT:
            case RIGHT_SHIFT_ASSIGNMENT:
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
            case AND_ASSIGNMENT:
            case XOR_ASSIGNMENT:
            case OR_ASSIGNMENT: {
                CompoundAssignmentTree compoundAssignmentTree = (CompoundAssignmentTree) parentTree;
                ExpressionTree left = compoundAssignmentTree.getVariable();
                if (left.equals(tree)) {
                    access = JavaWhereUsedFilters.ReadWrite.READ_WRITE;
                }
                break;
            }
        }
        return access;
    }
    
    /**
     *
     * @param workingCopy
     * @throws org.netbeans.modules.refactoring.java.spi.ToPhaseException
     */
    public final void setWorkingCopy(CompilationController workingCopy) throws ToPhaseException {
        this.workingCopy = workingCopy;
        try {
            if (this.workingCopy.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                throw new ToPhaseException();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    protected void addUsage(TreePath tp, JavaWhereUsedFilters.ReadWrite access) {
        assert tp != null;
        elements.add(WhereUsedElement.create(workingCopy, tp, access, fromTestRoot, fromPlatform, fromDependency, inImport));
        usages.add(tp);
    }

    public boolean isInImport() {
        return inImport.get();
    }

    protected void addUsage(TreePath tp) {
        assert tp != null;
        elements.add(WhereUsedElement.create(workingCopy, tp, fromTestRoot, fromPlatform, fromDependency, inImport));
        usages.add(tp);
    }
    
    public Collection<WhereUsedElement> getElements() {
        if(findInComments) { // the elements need to be sorted. Comments are searched for the whole file at once.
            elements.sort(new Comparator<WhereUsedElement>() {

                @Override
                public int compare(WhereUsedElement o1, WhereUsedElement o2) {
                    return o1.getPosition().getBegin().getOffset() - o2.getPosition().getBegin().getOffset();
                }
            });
        }
        return elements;
    }
    
    public Collection<TreePath> getUsages() {
        return usages;
    }

    @Override
    public Tree visitMemberReference(MemberReferenceTree node, Element p) {
        if(isCancelled.get()) {
            return null;
        }
        addIfMatch(getCurrentPath(), node, p);
        return super.visitMemberReference(node, p);
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        if(isCancelled.get()) {
            return null;
        }
        addIfMatch(getCurrentPath(), node, p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        if(isCancelled.get()) {
            return null;
        }
        addIfMatch(getCurrentPath(), node, p);
        return super.visitMemberSelect(node, p);
    }

    @Override
    public Tree visitNewClass(NewClassTree node, Element p) {
        if(isCancelled.get()) {
            return null;
        }
        Trees trees = workingCopy.getTrees();
        ClassTree classTree = ((NewClassTree) node).getClassBody();
        if (classTree != null && p.getKind() == ElementKind.CONSTRUCTOR) {
            for (Tree t : classTree.getMembers()) {
                Element elem = workingCopy.getTrees().getElement(TreePath.getPath(workingCopy.getCompilationUnit(), t));
                if ((elem != null) && (elem.getKind() == ElementKind.CONSTRUCTOR)) {
                    TreePath superCall = trees.getPath(workingCopy.getCompilationUnit(), ((ExpressionStatementTree) ((MethodTree) t).getBody().getStatements().get(0)).getExpression());
                    Element superCallElement = trees.getElement(superCall);
                    if (superCallElement != null && superCallElement.equals(p) && !workingCopy.getTreeUtilities().isSynthetic(superCall)) {
                        addUsage(superCall);
                    }
                }
            }
        } else {
            addIfMatch(getCurrentPath(), node, p);
        }
        return super.visitNewClass(node, p);
    }

    public boolean usagesInComments() {
        return usagesInComments;
    }
}
