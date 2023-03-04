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
package org.netbeans.modules.java.source.pretty;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;

public class DanglingElseChecker extends Visitor {
    boolean foundDanglingElse;
    public boolean hasDanglingElse(JCTree t) {
	if(t==null) return false;
	foundDanglingElse = false;
	t.accept(this);
	return foundDanglingElse;
    }
    @Override
    public void visitTree(JCTree tree) {
    }
    @Override
    public void visitIf(JCIf tree) {
	if(tree.elsepart==null) foundDanglingElse = true;
	else tree.elsepart.accept(this);
    }
    @Override
    public void visitWhileLoop(JCWhileLoop tree) {
	tree.body.accept(this);
    }
    @Override
    public void visitDoLoop(JCDoWhileLoop tree) {
	tree.body.accept(this);
    }
    @Override
    public void visitForLoop(JCForLoop tree) {
	tree.body.accept(this);
    }
    @Override
    public void visitSynchronized(JCSynchronized tree) {
	tree.body.accept(this);
    }
    @Override
    public void visitLabelled(JCLabeledStatement tree) {
	tree.body.accept(this);
    }
    @Override
    public void visitBlock(JCBlock tree) {
	// Do dangling else checks on single statement blocks since
	// they often get eliminated and replaced by their constained statement
	if(!tree.stats.isEmpty() && tree.stats.tail.isEmpty())
	    tree.stats.head.accept(this);
    }
}
