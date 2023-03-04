/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
