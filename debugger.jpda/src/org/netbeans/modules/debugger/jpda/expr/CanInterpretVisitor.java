/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
import com.sun.source.util.TreePathScanner;

/**
 * Test of ability and suitability of an interpreter or compiler when evaluating.
 * 
 * @author Martin
 */
class CanInterpretVisitor extends TreePathScanner<Boolean, EvaluationContext> {

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
