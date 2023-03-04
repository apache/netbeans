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

package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author Jan Becicka
 */
public class LocalVarScanner extends ErrorAwareTreePathScanner<Boolean, Element> {

    private CompilationInfo info;
    private String newName;
    boolean result = false;
    public LocalVarScanner(CompilationInfo workingCopy, String newName) {
        this.info = workingCopy;
        this.newName = newName;
    }

    @Override
    public Boolean visitLambdaExpression(LambdaExpressionTree node, Element p) {
        return null;
    }

    @Override
    public Boolean visitClass(ClassTree node, Element p) {
        return null;
    }

    @Override
    public Boolean visitBlock(BlockTree node, Element p) {
        return null;
    }

    @Override
    public Boolean visitForLoop(ForLoopTree node, Element p) {
        return null;
    }

    @Override
    public Boolean visitIf(IfTree node, Element p) {
        return null;
    }

    @Override
    public Boolean visitWhileLoop(WhileLoopTree node, Element p) {
        return null;
    }

    @Override
    public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, Element p) {
        return null;
    }

    @Override
    public Boolean visitSwitch(SwitchTree node, Element p) {
        return null;
    }

    @Override
    public Boolean visitDoWhileLoop(DoWhileLoopTree node, Element p) {
        return null;
    }

    @Override
    public Boolean visitTry(TryTree node, Element p) {
        return null;
    }

    @Override
    public Boolean visitVariable(VariableTree variable, Element element) {
        if (newName!=null && variable.getName().toString().equals(newName)) {
            result= true;
        }
        return super.visitVariable(variable, element);
    }
    @Override
    public Boolean visitIdentifier(IdentifierTree node, Element p) {
        Element current = info.getTrees().getElement(getCurrentPath());
        if (newName==null) {
            if (current !=null && current.equals(p)) {
                result = true;
            }
        } else if (current != null && current.getKind() == ElementKind.FIELD && node.getName().toString().equals(newName)) {
            result = true;
        }
        return super.visitIdentifier(node, p);
    }
    
    public boolean hasRefernces() {
        return result;
    }
}
