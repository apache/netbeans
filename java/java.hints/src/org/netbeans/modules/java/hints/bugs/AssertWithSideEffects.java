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
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.modules.java.hints.SideEffectVisitor;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.StopProcessing;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.bugs.Bundle.*;

/**
 * Checks that the assert statement has no side-effects. Inspects only 1st level of the call
 * method chain, and DOES NOT attempt to lookup method overrides / implementations.
 * 
 * The implementation does not scan libraries, and does not cache results for invoked method's state - no API
 * is currently available to cache state during bulk inspection.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "TEXT_AssertWithSideEffects=Assert condition produces side effects"
})
@Hint(
        displayName = "#DN_AssertWithSideEffects",
        description = "#DESC_AssertWithSideEffects",
        category = "bugs",
        suppressWarnings = { "AssertWithSideEffects" },
        enabled = true
)
public final class AssertWithSideEffects {
    
    @TriggerTreeKind(Tree.Kind.ASSERT)
    public static ErrorDescription run(HintContext ctx) {
        CompilationInfo ci = ctx.getInfo();
        AssertTree at = (AssertTree)ctx.getPath().getLeaf();
        TreePath condPath = new TreePath(ctx.getPath(), at.getCondition());
        if (ci.getTreeUtilities().isCompileTimeConstantExpression(condPath)) {
            return null;
        }
        
        SideEffectVisitor visitor = new SideEffectVisitor(ctx);
        Tree culprit;
        try {
            visitor.scan(new TreePath(ctx.getPath(), at.getCondition()), null);
            return null;
        } catch (StopProcessing stop) {
            culprit = stop.getValue();
        }
        return ErrorDescriptionFactory.forTree(ctx, culprit, TEXT_AssertWithSideEffects());
    }
    
}
