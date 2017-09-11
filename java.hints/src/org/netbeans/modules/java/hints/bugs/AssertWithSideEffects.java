/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.modules.java.hints.SideEffectVisitor;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.Deque;
import java.util.LinkedList;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
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
