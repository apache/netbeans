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

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.tree.*;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.source.util.Trees;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.lexer.JavaTokenId;
import static org.netbeans.api.java.lexer.JavaTokenId.*;
import org.netbeans.api.java.source.DocTreePathHandle;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;

/**
 *
 * @author Jan Becicka
 */
public class RenameTransformer extends RefactoringVisitor {

    private final Set<ElementHandle<ExecutableElement>> allMethods;
    private final TreePathHandle handle;
    private final DocTreePathHandle docHandle;
    private final String newName;
    private final boolean renameInComments;
    private final RenameRefactoring refactoring;
    private Iterable<? extends Element> shadowed;
    private Map<ImportTree, ImportTree> imports;
    private List<ImportTree> newImports;

    public RenameTransformer(TreePathHandle handle, DocTreePathHandle docHandle, RenameRefactoring refactoring, Set<ElementHandle<ExecutableElement>> am, boolean renameInComments) {
        super(true);
        this.handle = handle;
        this.docHandle = docHandle;
        this.refactoring = refactoring;
        this.newName = refactoring.getNewName();
        this.allMethods = am;
        this.renameInComments = renameInComments;
    }

    @Override
    public Tree scan(Tree tree, Element p) {
        if(p == null && handle == null) {
            p = docHandle != null? ((DocTrees)workingCopy.getTrees()).getElement(docHandle.resolve(workingCopy))
                                                       : handle.resolveElement(workingCopy);
        }
        return super.scan(tree, p);
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
        genUtils.importComments(node, node);
        if (renameInComments) {
            if (p.getKind() == ElementKind.PARAMETER) {
                renameParameterInMethodComments(p);

            } else {
                String originalName = getOldSimpleName(p);
                if (originalName!=null) {
                    TokenSequence<JavaTokenId> ts = workingCopy.getTokenHierarchy().tokenSequence(JavaTokenId.language());

                    while (ts.moveNext()) {
                        Token<JavaTokenId> t = ts.token();

                        if (isComment(t)) {
                            rewriteAllInComment(t.text().toString(), ts.offset(), originalName);
                        }
                    }
                }
            }
        }
        imports = new HashMap<>();
        newImports = new LinkedList<>();
        Tree value = super.visitCompilationUnit(node, p);
        if(!imports.isEmpty() || !newImports.isEmpty()) {
            CompilationUnitTree newNode = node;
            for (Map.Entry<ImportTree, ImportTree> entry : imports.entrySet()) {
                newNode = make.removeCompUnitImport(newNode, entry.getKey());
                newNode = make.addCompUnitImport(newNode, entry.getValue());
            }
            for (ImportTree newImport : newImports) {
                newNode = make.addCompUnitImport(newNode, newImport);
            }
            rewrite(node, newNode);
        }
        return value;
    }

    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        renameShadowIfMatch(getCurrentPath(), node, p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        return super.visitMemberSelect(node, p);
    }

    @Override
    public Tree visitMemberReference(MemberReferenceTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        return super.visitMemberReference(node, p);
    }

    @Override
    public Tree visitLabeledStatement(LabeledStatementTree tree, Element p) {
        if(handle != null && handle.getKind() == Tree.Kind.LABELED_STATEMENT && tree == handle.resolve(workingCopy).getLeaf()) {
            LabeledStatementTree newTree = make.LabeledStatement(newName, tree.getStatement());
            rewrite(tree, newTree);
        }
        return super.visitLabeledStatement(tree, p);
    }

    @Override
    public Tree visitContinue(ContinueTree tree, Element p) {
        if(handle != null && handle.getKind() == Tree.Kind.LABELED_STATEMENT) {
            StatementTree target = workingCopy.getTreeUtilities().getBreakContinueTarget(getCurrentPath());
            if(target == handle.resolve(workingCopy).getLeaf()) {
                ContinueTree newTree = make.Continue(newName);
                rewrite(tree, newTree);
            }
        }
        return super.visitContinue(tree, p);
    }

    @Override
    public Tree visitBreak(BreakTree tree, Element p) {
        if(handle != null && handle.getKind() == Tree.Kind.LABELED_STATEMENT) {
            Tree target = workingCopy.getTreeUtilities().getBreakContinueTargetTree(getCurrentPath());
            if(target == handle.resolve(workingCopy).getLeaf()) {
                BreakTree newTree = make.Break(newName);
                rewrite(tree, newTree);
            }
        }
        return super.visitBreak(tree, p);
    }
    
    private String getOldSimpleName(Element p) {
        if (p!=null) {
            return p.getSimpleName().toString();
        }
        for (ElementHandle<ExecutableElement> mh : allMethods) {
            ExecutableElement baseMethod = mh.resolve(workingCopy);
            if (baseMethod == null) {
                continue;
            }
            return baseMethod.getSimpleName().toString();
        }
        return null;
    }
    
    private void renameUsageIfMatch(final TreePath path, Tree tree, Element elementToFind) {
        if (JavaPluginUtils.isSyntheticPath(workingCopy, path) || (handle != null && handle.getKind() == Tree.Kind.LABELED_STATEMENT)) {
            return;
        }
        TreePath elementPath = path;
        Trees trees = workingCopy.getTrees();
        Element el = workingCopy.getTrees().getElement(elementPath);
        
        if (el == null) {
            elementPath = elementPath.getParentPath();
            if (elementPath != null && elementPath.getLeaf().getKind() == Tree.Kind.IMPORT) {
                ImportTree impTree = (ImportTree)elementPath.getLeaf();
                if (!impTree.isStatic()) {
                    return;
                }
                Tree idTree = impTree.getQualifiedIdentifier();
                if (idTree.getKind() != Tree.Kind.MEMBER_SELECT) {
                    return;
                }
                final Name id = ((MemberSelectTree) idTree).getIdentifier();
                if (id == null || id.contentEquals("*") || !id.contentEquals(elementToFind.getSimpleName())) { // NOI18N
                    // skip import static java.lang.Math.*
                    return;
                }
                Tree classTree = ((MemberSelectTree) idTree).getExpression();
                elementPath = trees.getPath(workingCopy.getCompilationUnit(), classTree);
                el = trees.getElement(elementPath);
                if (el == null) {
                    return;
                }
                Iterator iter = workingCopy.getElementUtilities().getMembers(el.asType(),new ElementUtilities.ElementAcceptor() {
                    @Override
                    public boolean accept(Element e, TypeMirror type) {
                        return id.equals(e.getSimpleName());
                    }
                }).iterator();
                if (iter.hasNext()) {
                    el = (Element) iter.next();
                    if(iter.hasNext()) {
                        if(el.equals(elementToFind) || isMethodMatch(el)) {
                            newImports.add(make.Import(make.QualIdent((Element) iter.next()), true));
                        } else {
                            newImports.add(make.Import(make.QualIdent(el), true));
                            do {
                                el = (Element) iter.next();
                            } while (iter.hasNext() && el != null && !(el.equals(elementToFind) || isMethodMatch(el)));
                            if(el == null) {
                                return;
                            }
                        }
                    }
                }
            } else {
                return;
            }
        }
        
        if (el.equals(elementToFind) || isMethodMatch(el)) {
            String useThis = null;
            String useSuper = null;

            if (elementToFind!=null && elementToFind.getKind().isField() && tree.getKind() == Tree.Kind.IDENTIFIER) {
                Scope scope = workingCopy.getTrees().getScope(elementPath);
                for (Element ele : scope.getLocalElements()) {
                    if ((ele.getKind() == ElementKind.LOCAL_VARIABLE || ele.getKind() == ElementKind.PARAMETER) 
                            && ele.getSimpleName().toString().equals(newName)) {
                        if (elementToFind.getModifiers().contains(Modifier.STATIC)) {
                            useThis = elementToFind.getEnclosingElement().getSimpleName().toString() + ".";
                        } else {
                            Types types = workingCopy.getTypes();
                            if (types.isSubtype(scope.getEnclosingClass().asType(), elementToFind.getEnclosingElement().asType())) {
                                useThis = "this."; // NOI18N
                            } else {
                                useThis = elementToFind.getEnclosingElement().getSimpleName() + ".this."; // NOI18N
                            }
                        }
                        break;
                    }
                }
            }
            if (elementToFind!=null && elementToFind.getKind().isField() || elementToFind.getKind() == ElementKind.METHOD) {
                Scope scope = workingCopy.getTrees().getScope(elementPath);
                TypeElement enclosingTypeElement = scope.getEnclosingClass();
                TypeMirror superclass = enclosingTypeElement==null ? null:enclosingTypeElement.getSuperclass();
                Types types = workingCopy.getTypes();
                
                if(superclass!=null && !types.isSameType(types.getNoType(TypeKind.NONE), superclass) &&
                    types.isSubtype(superclass, elementToFind.getEnclosingElement().asType())) {
                    if(elementToFind.getKind().isField()) {
                        for (Element ele : enclosingTypeElement.getEnclosedElements()) {
                            if (ele.getKind().isField() && ele.getSimpleName().toString().equals(newName)) {
                                if (tree.getKind() == Tree.Kind.MEMBER_SELECT) {
                                    String isSuper = ((MemberSelectTree) tree).getExpression().toString();
                                    if (isSuper.equals("super") || isSuper.endsWith(".super")) { // NOI18N
                                        break;
                                    }
                                }
                                if (types.isSubtype(enclosingTypeElement.asType(), elementToFind.getEnclosingElement().asType())) {
                                    useSuper = "super."; // NOI18N
                                } else {
                                    useSuper = elementToFind.getEnclosingElement().getSimpleName() + ".super."; // NOI18N
                                }
                                break;
                            }
                        }
                    } else if(elementToFind.getKind() == ElementKind.METHOD) {
                        ElementUtilities utils = workingCopy.getElementUtilities();
                        if(utils.alreadyDefinedIn((CharSequence) newName, (ExecutableType) elementToFind.asType(), (TypeElement) enclosingTypeElement)) {
                            boolean isSuper = false;
                            if (tree.getKind() == Tree.Kind.MEMBER_SELECT) {
                                String superString = ((MemberSelectTree) tree).getExpression().toString();
                                if (superString.equals("super") || superString.endsWith(".super")) { // NOI18N
                                    isSuper = true;
                                }
                            }
                            if(!isSuper) {
                                if (types.isSubtype(enclosingTypeElement.asType(), elementToFind.getEnclosingElement().asType())) {
                                    useSuper = "super."; // NOI18N
                                } else {
                                    useSuper = elementToFind.getEnclosingElement().getSimpleName() + ".super."; // NOI18N
                                }
                            }
                        }
                    }
                }
            }
            Tree nju = null;
            if (useThis!=null) {
                nju = make.setLabel(tree, useThis + newName);
            } else if (useSuper !=null) {
                nju = make.setLabel(tree, useSuper + newName);
            } else if(elementToFind.getKind().isClass()) {
                boolean duplicate = duplicateDeclaration();
                final TreePath parentPath = path.getParentPath();
                if(parentPath != null && parentPath.getLeaf().getKind() == Tree.Kind.IMPORT) {
                    ImportTree importTree = (ImportTree) parentPath.getLeaf();
                    if(duplicate) {
                        nju = make.removeCompUnitImport(workingCopy.getCompilationUnit(), importTree);
                        tree = workingCopy.getCompilationUnit();
                    } else {
                        nju = make.setLabel(tree, newName);
                    }                } else {
                    if(duplicate) {
                        nju = make.QualIdent(make.setLabel(make.QualIdent(elementToFind), newName).toString());
                    } else {
                        nju = make.setLabel(tree, newName);
                    }
                }
            } else {
                final TreePath parentPath = path.getParentPath();
                if(parentPath != null && parentPath.getLeaf().getKind() == Tree.Kind.IMPORT) {
                    ImportTree importTree = (ImportTree) parentPath.getLeaf();
                    imports.put(importTree, make.Import(make.setLabel(tree, newName), importTree.isStatic()));
                } else {
                    nju = make.setLabel(tree, newName);
                }
            }
            if(nju != null) {
                rewrite(tree, nju);
            }
        }
    }
    
    private void renameShadowIfMatch(final TreePath path, Tree tree, Element elementToFind) {
        if (shadowed == null || JavaPluginUtils.isSyntheticPath(workingCopy, path) || (handle != null && handle.getKind() == Tree.Kind.LABELED_STATEMENT)) {
            return;
        }
        TreePath elementPath = path;
        Element el = workingCopy.getTrees().getElement(elementPath);
        if (el == null) {
            // fail fast
            return;
        }
        for (Element shadow : shadowed) {
            if (shadow.equals(el)) {
                if (elementToFind.getModifiers().contains(Modifier.STATIC)) {
                    rewrite(tree, make.MemberSelect(make.QualIdent(el.getEnclosingElement()), shadow));
                } else {
                    rewrite(tree, make.MemberSelect(make.MemberSelect(make.QualIdent(el.getEnclosingElement()), "this"), shadow));
                }
                break;
            }
        }
    }

    @Override
    public Tree visitMethod(MethodTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitMethod(tree, p);
    }

    @Override
    public Tree visitClass(ClassTree tree, final Element p) {
        final TreePath currentPath = getCurrentPath();
        renameDeclIfMatch(getCurrentPath(), tree, p);
        Element el = workingCopy.getTrees().getElement(currentPath);
        if (el != null && el.getEnclosedElements().contains(p)) {
            Trees trees = workingCopy.getTrees();
            Scope scope = trees.getScope(trees.getPath(p));
            shadowed = workingCopy.getElementUtilities().getLocalMembersAndVars(scope, new ElementUtilities.ElementAcceptor() {

                @Override
                public boolean accept(Element element, TypeMirror type) {
                    return !element.equals(p) && element.getKind() == p.getKind() && element.getSimpleName().contentEquals(newName);
                }
            });
        }
        Tree value = super.visitClass(tree, p);
        shadowed = null;
        return value;
    }

    @Override
    public Tree visitVariable(VariableTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitVariable(tree, p);
    }

    @Override
    public Tree visitTypeParameter(TypeParameterTree arg0, Element arg1) {
        renameDeclIfMatch(getCurrentPath(), arg0, arg1);
        return super.visitTypeParameter(arg0, arg1);
    }
    
    private void renameDeclIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (JavaPluginUtils.isSyntheticPath(workingCopy, path) || (handle != null && handle.getKind() == Tree.Kind.LABELED_STATEMENT)) {
            return;
        }
        Element el = workingCopy.getTrees().getElement(path);
        if (el==null) {
            return;
        }
        if (el.equals(elementToFind) || isMethodMatch(el)) {
            Tree nju = make.setLabel(tree, newName);
            rewrite(tree, nju);
            return;
        }
    }
    
    private boolean isMethodMatch(Element method) {
        if (method.getKind() == ElementKind.METHOD && allMethods !=null) {
            for (ElementHandle<ExecutableElement> mh: allMethods) {
                ExecutableElement baseMethod =  mh.resolve(workingCopy);
                if (baseMethod==null) {
                    Logger.getLogger("org.netbeans.modules.refactoring.java").info("RenameTransformer cannot resolve " + mh);
                    continue;
                }
                if (baseMethod.equals(method) || workingCopy.getElements().overrides((ExecutableElement)method, baseMethod, workingCopy.getElementUtilities().enclosingTypeElement(baseMethod))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public DocTree visitReference(ReferenceTree node, Element elementToFind) {
        DocTreePath currentDocPath = getCurrentDocPath();
        DocTrees trees = workingCopy.getDocTrees();
        Element el = trees.getElement(currentDocPath);
        ExpressionTree classReference = workingCopy.getTreeUtilities().getReferenceClass(currentDocPath);
        if((el == null || !(el.equals(elementToFind) || isMethodMatch(el))) && classReference != null) {
            el = trees.getElement(new TreePath(getCurrentPath(), classReference));
        }
        if (el != null && (el.equals(elementToFind) || isMethodMatch(el))) {
            ReferenceTree newRef;
            Name memberName = workingCopy.getTreeUtilities().getReferenceName(currentDocPath);
            List<? extends Tree> methodParameters = workingCopy.getTreeUtilities().getReferenceParameters(currentDocPath);
            if(el.getKind().isClass() || el.getKind().isInterface()) {
                newRef = make.Reference(make.setLabel(classReference, newName), memberName, methodParameters);
            } else {
                newRef = make.Reference(classReference, newName, methodParameters);
            }
            rewrite(currentDocPath.getTreePath().getLeaf(), node, newRef);
        }
        return super.visitReference(node, elementToFind);
    }

    @Override
    public DocTree visitText(TextTree node, Element p) {
        if(renameInComments && refactoring.getContext().lookup(RenamePropertyRefactoringPlugin.class) == null) {
            DocTreePath currentDocPath = getCurrentDocPath();
            if(p.getKind() == ElementKind.PARAMETER) {
                VariableElement var = (VariableElement) p;
                Element method = workingCopy.getTrees().getElement(currentDocPath.getTreePath());
                if(!var.getEnclosingElement().equals(method)) {
                    return super.visitText(node, p);
                }
            }
            String originalName = getOldSimpleName(p);
            if(node.getBody().contains(originalName)) {
                StringBuilder text = new StringBuilder(node.getBody());
                for (int index = text.indexOf(originalName); index != -1; index = text.indexOf(originalName, index + 1)) {
                    if (index > 0 && Character.isJavaIdentifierPart(text.charAt(index - 1))) {
                        continue;
                    }
                    if ((index + originalName.length() < text.length()) && Character.isJavaIdentifierPart(text.charAt(index + originalName.length()))) {
                        continue;
                    }
                    text.delete(index, index + originalName.length());
                    text.insert(index, newName);
                }
                if(!node.getBody().contentEquals(text)) {
                    TextTree newText = make.Text(text.toString());
                    rewrite(currentDocPath.getTreePath().getLeaf(), node, newText);
                }
            }
        }
        return super.visitText(node, p);
    }

    @Override
    public DocTree visitIdentifier(com.sun.source.doctree.IdentifierTree node, Element elementToFind) {
        DocTreePath currentDocPath = getCurrentDocPath();
        DocTrees trees = workingCopy.getDocTrees();
        Element el = trees.getElement(currentDocPath);
        if (el != null && (el.equals(elementToFind))) {
            com.sun.source.doctree.IdentifierTree newIdent = make.DocIdentifier(newName);
            rewrite(currentDocPath.getTreePath().getLeaf(), node, newIdent);
        }
        return super.visitIdentifier(node, elementToFind);
    }
    
    /**
     * Renames the method (or constructor) parameter in comments. This method
     * considers comments before and inside the method declaration, and within
     * the method body.
     *
     * @param parameter the method or constructor parameter {@link Element}
     */
    private void renameParameterInMethodComments(final Element parameter) {
        if(refactoring.getContext().lookup(RenamePropertyRefactoringPlugin.class) != null) {
            return; // XXX: Hack, do not update comments twice
        }
        final Tree method = workingCopy.getTrees().getPath(parameter).getParentPath().getLeaf();

        final String originalName = getOldSimpleName(parameter);
        final int methodStart = (int) workingCopy.getTrees().getSourcePositions()
                .getStartPosition(workingCopy.getCompilationUnit(), method);
        final TokenSequence<JavaTokenId> tokenSequence = workingCopy.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        //renaming in comments before the method/constructor
        tokenSequence.move(methodStart);
        while (tokenSequence.movePrevious()) {
            final Token<JavaTokenId> token = tokenSequence.token();
            if (isComment(token)) {
                rewriteAllInComment(token.text().toString(), tokenSequence.offset(), originalName);
            } else if (token.id() != WHITESPACE) {
                break;
            }            
        }

        //renaming in comments within the method/constructor declaration and body
        final int methodEnd = (int) workingCopy.getTrees().getSourcePositions()
                .getEndPosition(workingCopy.getCompilationUnit(), method);

        tokenSequence.move(methodStart);
        while (tokenSequence.moveNext() && tokenSequence.offset() < methodEnd) {
            final Token<JavaTokenId> token = tokenSequence.token();
            if (isComment(token)) {
                rewriteAllInComment(token.text().toString(), tokenSequence.offset(), originalName);
            }
        }
    }

    /**
     * Checks if {@code token} represents a comment.
     * 
     * @param token the {@link Token} to check
     * @return {@code true} if {@code token} represents a line comment, block
     *          comment; {@code false} otherwise or javadoc.
     */
    private boolean isComment(final Token<JavaTokenId> token) {
        switch (token.id()) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
                return true;
            case JAVADOC_COMMENT:
            default:
                return false;
        }
    }

    /**
     * Changes all occurrences of {@code originalName} to the new name in the comment {@code text}.
     *
     * @param text the text of the comment token
     * @param offset the offset of the comment token
     * @param originalName the old name to change
     */
    private void rewriteAllInComment(final String text, final int offset, final String originalName) {
        for (int index = text.indexOf(originalName); index != -1; index = text.indexOf(originalName, index + 1)) {
            if (index > 0 && Character.isJavaIdentifierPart(text.charAt(index - 1))) {
                continue;
            }
            if ((index + originalName.length() < text.length()) && Character.isJavaIdentifierPart(text.charAt(index + originalName.length()))) {
                continue;
            }
            //at least do not rename html start and end tags.
            if (text.charAt(index-1) == '<' || text.charAt(index-1) == '/') {
                continue;
            }
            workingCopy.rewriteInComment(offset + index, originalName.length(), newName);
        }
    }

    private boolean duplicateDeclaration() {
        ErrorAwareTreeScanner<Boolean, String> duplicateIds = new ErrorAwareTreeScanner<Boolean, String>() {
            @Override public Boolean visitClass(ClassTree node, String p) {
                if(node.getSimpleName().contentEquals(p)) {
                    return Boolean.TRUE;
                }
                return super.visitClass(node, p);
            }
            @Override
            public Boolean reduce(Boolean r1, Boolean r2) {
                return r1 != null ? r1 : r2;
            }
        };
        return Boolean.TRUE == duplicateIds.scan(workingCopy.getCompilationUnit(), newName);
    }
}
