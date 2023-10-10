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

package org.netbeans.api.java.source;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConstantCaseLabelTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
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
class TranslateIdentifier extends ErrorAwareTreePathScanner<Void, Void>{
    
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
                
                if (   (parent != null && parent.getKind() == Kind.CONSTANT_CASE_LABEL && ((ConstantCaseLabelTree) parent).getConstantExpression() == node && element.getKind() == ElementKind.ENUM_CONSTANT)
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
