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
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.SourcePositions;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
class FindMethodRegionsVisitor extends SimpleTreeVisitor<Void,Void> implements Runnable {
        
        private final Document doc;
        private final SourcePositions pos;
        private final AtomicBoolean canceled;
        private CompilationUnitTree cu;
        private final List<Pair<DocPositionRegion,MethodTree>> posRegions = new LinkedList<Pair<DocPositionRegion, MethodTree>>();
        
        public FindMethodRegionsVisitor (final Document doc, final SourcePositions pos, final AtomicBoolean canceled, CompilationUnitTree unit) {
            assert doc != null;
            assert pos != null;
            assert canceled != null;
            this.doc = doc;
            this.pos = pos;
            this.canceled = canceled;
            this.cu = unit;
        }
        
        public void run() {
            visit(cu, null);
        }

        public List<Pair<DocPositionRegion,MethodTree>> getResult () {
            //todo: threading, user of returned value should do the check
            if (canceled.get()) {
                posRegions.clear();
            }
            return posRegions;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
            for (Tree t : node.getTypeDecls()) {
                visit (t,p);
            }
            return null;
        }

        @Override
        public Void visitClass(ClassTree node, Void p) {
            for (Tree t : node.getMembers()) {
                visit(t, p);
            }
            return null;
        }

        @Override
        public Void visitMethod(MethodTree node, Void p) {            
            assert cu != null;
            if (!canceled.get()) {
                int startPos = (int) pos.getStartPosition(cu, node.getBody());
                int endPos = (int) pos.getEndPosition(cu, node.getBody());
                if (startPos >=0) {
                    try {
                        posRegions.add(Pair.<DocPositionRegion,MethodTree>of(new DocPositionRegion(doc,startPos,endPos),node));
                    } catch (BadLocationException e) {
                        posRegions.clear();
                    }
                }
            }            
            return null;
        }
                        
    }
