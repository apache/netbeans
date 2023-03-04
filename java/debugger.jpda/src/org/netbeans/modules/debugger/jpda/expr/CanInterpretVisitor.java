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
package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.Mirror;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.WhileLoopTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;

/**
 * Test of ability and suitability of an interpreter or compiler when evaluating.
 * 
 * @author Martin
 */
class CanInterpretVisitor extends ErrorAwareTreePathScanner<Boolean, EvaluationContext> {

    @Override
    public Boolean reduce(Boolean r1, Boolean r2) {
        if (r1 == null) {
            return r2;
        }
        if (r2 == null) {
            return r1;
        }
        return r1 && r2;
    }
    
    @Override
    public Boolean visitDoWhileLoop(DoWhileLoopTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitForLoop(ForLoopTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitLambdaExpression(LambdaExpressionTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitMemberReference(MemberReferenceTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }
    
    @Override
    public Boolean visitNewClass(NewClassTree node, EvaluationContext p) {
        if (node.getClassBody() != null) {
            return Boolean.FALSE;
        }
        return null;
    }

    @Override
    public Boolean visitWhileLoop(WhileLoopTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitOther(Tree node, EvaluationContext p) {
        return Boolean.FALSE;
    }
    

    // Unsupported Jigsaw modules visitors:

    @Override
    public Boolean visitModule(ModuleTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitExports(ExportsTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitProvides(ProvidesTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitRequires(RequiresTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visitUses(UsesTree node, EvaluationContext p) {
        return Boolean.FALSE;
    }

}
