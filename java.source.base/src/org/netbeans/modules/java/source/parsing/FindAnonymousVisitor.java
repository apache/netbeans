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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.lib.nbjavac.services.NBTreeMaker.IndexedClassDecl;

/**
 * Partial reparse helper visitor.
 * Finds anonymous and local classes in given method tree.
 * @author Tomas Zezula
 */
class FindAnonymousVisitor extends TreeScanner<Void,Void> {

    private static enum Mode {COLLECT, CHECK};

    int firstInner = -1;
    int noInner;
    boolean hasLocalClass;
    final Set<Tree> docOwners = new HashSet<Tree>();
    private Mode mode = Mode.COLLECT;            
    
    public final void reset () {
        this.firstInner = -1;
        this.noInner = 0;
        this.hasLocalClass = false;
        this.mode = Mode.CHECK;
    }

    @Override
    public Void visitClass(ClassTree node, Void p) {
        if (firstInner == -1) {
            firstInner = ((IndexedClassDecl)node).index;
        }
        if (node.getSimpleName().length() != 0) {
            hasLocalClass = true;
        }
        noInner++;
        handleDoc(node);
        return super.visitClass(node, p);
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        handleDoc(node);
        return super.visitMethod(node, p);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        handleDoc(node);
        return super.visitVariable(node, p);
    }

    private void handleDoc (final Tree tree) {
        if (mode == Mode.COLLECT) {
            docOwners.add(tree);
        }
    }

}
