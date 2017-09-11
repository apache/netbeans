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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.source;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.code.Symbol;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Replaces identifiers representing all used types with the new ones - imports
 * for them will be solved throughout new commit phase.
 * 
 * This is provided because of refactoring, which wants to take tree from
 * one compilation unit and add it to another one and wants to have all
 * types resolved.
 *
 * @author Pavel Flaska
 */
class TranslateIdentifier extends TreePathScanner<Void, Void>{
    
    public static <T extends Tree> T importFQNs(WorkingCopy copy, T tree) {
        if (tree == null) return null;
        
        TranslateIdentifier ti = new TranslateIdentifier(copy);
        
        //XXX: the TreePath constructed below below depends on javac internals (that elements are attributes of a tree, not a tree path):
        ti.scan(tree.getKind() == Kind.COMPILATION_UNIT ? new TreePath((CompilationUnitTree) tree) : new TreePath(new TreePath(copy.getCompilationUnit()), tree), null);
        
        return (T) copy.getTreeUtilities().translate(tree, ti.translateMap);
    }
    
    private final Map<Tree, Tree> translateMap = new HashMap<Tree, Tree>();
    private final @NonNull CompilationInfo info;
    private final @NonNull TreeMaker make;
    
    private TranslateIdentifier(@NonNull final WorkingCopy copy) {
        this.info = copy;
        this.make = copy.getTreeMaker();
    }

    public Void visitIdentifier(IdentifierTree node, Void p) {
        TreePath path = getCurrentPath();
        Element element = info.getTrees().getElement(path);
        
        if (element != null && element.asType().getKind() != TypeKind.ERROR) {
            // solve the imports only when declared type!!!
            if (element.getKind().isClass() || element.getKind().isInterface()
                    || (element.getKind().isField() && ((Symbol) element).isStatic())) {
                Tree parent = path.getParentPath() != null ? path.getParentPath().getLeaf() : null;
                
                if (   (parent != null && parent.getKind() == Kind.CASE && ((CaseTree) parent).getExpression() == node && element.getKind() == ElementKind.ENUM_CONSTANT)
                    || (path.getCompilationUnit() != null && ((Symbol) element).enclClass() != null && path.getCompilationUnit().getSourceFile() == ((Symbol) element).enclClass().sourcefile)) {
                    translateMap.put(node, make.Identifier(element.getSimpleName()));
                } else {
                    translateMap.put(node, make.QualIdent(element));
                }
            } 
        }
        
        return null;
    }
    
    public Void visitMemberSelect(MemberSelectTree node, Void p) {
        TypeElement e = info.getElements().getTypeElement(node.toString());
        if (e != null) {
            translateMap.put(node, make.QualIdent(e));
            return null;
        } else {
            return super.visitMemberSelect(node, p);
        }
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        if (info.getTreeUtilities().isSynthetic(/*should not be used:*/getCurrentPath().getCompilationUnit(), node)) return null;
        return super.visitMethod(node, p);
    }

}
