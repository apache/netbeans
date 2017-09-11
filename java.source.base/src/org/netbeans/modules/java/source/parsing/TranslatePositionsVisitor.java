/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import org.netbeans.lib.nbjavac.services.NBParserFactory.NBJavacParser.EndPosTableImpl;

/**
 * Helper visitor for partial reparse.
 * Updates tree positions by the given delta.
 * @author Tomas Zezula
 */
class TranslatePositionsVisitor extends TreeScanner<Void,Void> {

    private final MethodTree changedMethod;
    private final EndPosTable endPos;
    private final int delta;
    boolean active;
    boolean inMethod;

    public TranslatePositionsVisitor (final MethodTree changedMethod, final EndPosTable endPos, final int delta) {
        assert changedMethod != null;
        assert endPos != null;
        this.changedMethod = changedMethod;
        this.endPos = endPos;
        this.delta = delta;
    }


    @Override
    public Void scan(Tree node, Void p) {
        if (active && node != null) {
            if (((JCTree)node).pos >= 0) {                    
                ((JCTree)node).pos+=delta;
            }                
        }
        Void result = super.scan(node, p);            
        if (inMethod && node != null) {
            endPos.replaceTree((JCTree) node, null);//remove
        }
        if (active && node != null) {
            Integer pos = endPos.replaceTree((JCTree) node, null);//remove
            if (pos != null) {
                int newPos;
                if (pos < 0) {
                    newPos = pos;
                }
                else {
                    newPos = pos+delta;
                }
                ((EndPosTableImpl) endPos).storeEnd((JCTree)node,newPos);
            }                
        }
        return result;
    }

    @Override
    public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
        return scan (node.getTypeDecls(), p);
    }


    @Override
    public Void visitMethod(MethodTree node, Void p) {    
        if (active || inMethod) {
            scan(node.getModifiers(), p);
            scan(node.getReturnType(), p);
            scan(node.getTypeParameters(), p);
            scan(node.getParameters(), p);
            scan(node.getThrows(), p);
        }
        if (node == changedMethod) {
            inMethod = true;
        }
        if (active || inMethod) {
            scan(node.getBody(), p);
        }
        if (inMethod) {
            active = inMethod;
            inMethod = false;                
        }
        if (active || inMethod) {
            scan(node.getDefaultValue(), p);
        }
        return null;
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        JCVariableDecl varDecl = (JCVariableDecl) node;
        if (varDecl.sym != null && active && varDecl.sym.pos >= 0) {
            varDecl.sym.pos += delta;
        }
        return super.visitVariable(node, p);
    }
    
    
}
