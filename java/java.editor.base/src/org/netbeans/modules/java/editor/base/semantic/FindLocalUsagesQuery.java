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
package org.netbeans.modules.java.editor.base.semantic;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.java.editor.base.javadoc.JavadocImports;

/**
 *
 * @author Jan Lahoda
 */
public class FindLocalUsagesQuery extends CancellableTreePathScanner<Void, Stack<Tree>> {
    
    private CompilationInfo info;
    private Set<Token> usages;
    private Element toFind;
    private Document doc;
    private boolean instantRename;
    
    public FindLocalUsagesQuery() {
        this(false);
    }

    public FindLocalUsagesQuery(boolean instantRename) {
        this.instantRename = instantRename;
    }
    
    public Set<Token> findUsages(Element element, CompilationInfo info, Document doc) {
        this.info = info;
        this.usages = new HashSet<>();
        this.toFind = element;
        this.doc = doc;
        
        scan(info.getCompilationUnit(), null);
        return usages;
    }

    private void handlePotentialVariable(TreePath tree) {
        Element el = info.getTrees().getElement(tree);
        
        if (toFind.equals(el)) {
            Token<JavaTokenId> t = Utilities.getToken(info, doc, tree);
            
            if (t != null)
                usages.add(t);
        }
    }
    
    private void handleJavadoc(TreePath el) {
        List<Token> tokens = JavadocImports.computeTokensOfReferencedElements(info, el, toFind);
        usages.addAll(tokens);
    }
    
    @Override
    public Void visitIdentifier(IdentifierTree tree, Stack<Tree> d) {
        handlePotentialVariable(getCurrentPath());
        super.visitIdentifier(tree, d);
        return null;
    }

    @Override
    public Void visitMemberReference(MemberReferenceTree node, Stack<Tree> p) {
        handlePotentialVariable(getCurrentPath());
        super.visitMemberReference(node, p);
        return null;
    }
    
    @Override
    public Void visitMethod(MethodTree tree, Stack<Tree> d) {
        handlePotentialVariable(getCurrentPath());
        handleJavadoc(getCurrentPath());
        super.visitMethod(tree, d);
        return null;
    }
    
    @Override
    public Void visitMemberSelect(MemberSelectTree node, Stack<Tree> p) {
        handlePotentialVariable(getCurrentPath());
        super.visitMemberSelect(node, p);
        return null;
    }
    
    @Override
    public Void visitVariable(VariableTree tree, Stack<Tree> d) {
        handlePotentialVariable(getCurrentPath());
        Element el = info.getTrees().getElement(getCurrentPath());
        if (el != null && el.getKind().isField()) {
            handleJavadoc(getCurrentPath());
        }
        super.visitVariable(tree, d);
        return null;
    }

    @Override
    public Void visitClass(ClassTree tree, Stack<Tree> d) {
        handlePotentialVariable(getCurrentPath());
        handleJavadoc(getCurrentPath());
        super.visitClass(tree, d);
        return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameterTree node, Stack<Tree> p) {
        handlePotentialVariable(getCurrentPath());
        super.visitTypeParameter(node, p);
        return null;
    }

    @Override
    public Void visitNewClass(NewClassTree node, Stack<Tree> p) {
        if (instantRename) {
            return super.visitNewClass(node, p);
        }
        
        Element el = info.getTrees().getElement(getCurrentPath());

        if (toFind.equals(el) && node.getIdentifier() != null) {
            Token<JavaTokenId> t = Utilities.getToken(info, doc, new TreePath(getCurrentPath(), node.getIdentifier()));
            
            if (t != null)
                usages.add(t);

            return null;
        }

        if (el != null && toFind.equals(el.getEnclosingElement())) {
            return null;
        }
        
        return super.visitNewClass(node, p);
    }

    @Override
    public Void visitImport(ImportTree node, Stack<Tree> p) {
        if (node.isStatic() && toFind.getModifiers().contains(Modifier.STATIC)) {
            Tree qualIdent = node.getQualifiedIdentifier();
            if (qualIdent.getKind() == Kind.MEMBER_SELECT) {
                MemberSelectTree mst = (MemberSelectTree) qualIdent;
                if (toFind.getSimpleName().contentEquals(mst.getIdentifier())) {
                    Element el = info.getTrees().getElement(new TreePath(getCurrentPath(), mst.getExpression()));
                    if (el != null && el.equals(toFind.getEnclosingElement())) {
                        Token<JavaTokenId> t = Utilities.getToken(info, doc, new TreePath(getCurrentPath(), mst));
                        if (t != null)
                            usages.add(t);
                    }
                }
            }
        }
        return super.visitImport(node, p);
    }

    @Override
    public Void scan(Tree tree, Stack<Tree> p) {
        if (tree != null && "BINDING_PATTERN".equals(tree.getKind().name())) {
            handlePotentialVariable(new TreePath(getCurrentPath(), tree));
        }
        return super.scan(tree, p);
    }
    
    
}
