/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.java.editor.base.semantic;

import org.netbeans.modules.java.editor.base.semantic.Utilities;

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
        this.usages = new HashSet<Token>();
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
}
