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
package org.netbeans.modules.java.metrics.hints;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.IntegerOption;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.UseOptions;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.metrics.hints.Bundle.*;

/**
 * Reports complex arithmetic exceptions. Simple use of {@link ExpressionVisitor},
 * see its documentation for details
 * 
 * @author sdedic
 */
@Hint(
    category = "metrics",
    displayName = "#DN_ComplexLogicalExpression",
    description = "#DESC_ComplexLogicalExpression",
    options = { Hint.Options.QUERY, Hint.Options.HEAVY },
    enabled = false
)
@NbBundle.Messages({
    "# {0} - number of operands",
    "TEXT_LogicalTooComplex=Logical expression is too complex: {0} operations"
})
public class ComplexLogicalExpression {
    static final int DEFAULT_LIMIT = 3;
    
    @IntegerOption(
        displayName = "#OPTNAME_ComplexLogicalLimit",
        tooltip = "#OPTDESC_ComplexLogicalLimit",
        minValue = 1,
        maxValue = 1000,
        step = 1
    )
    public static final String OPTION_LIMIT = "metrics.expression.logical.limit"; // NOI18N
    
    @TriggerTreeKind(Tree.Kind.METHOD)
    @UseOptions(ComplexLogicalExpression.OPTION_LIMIT)
    public static List<ErrorDescription> complexLogicalMethod(HintContext ctx) {
        return translate(ctx);
    }

    @TriggerTreeKind(Tree.Kind.VARIABLE)
    @UseOptions(ComplexLogicalExpression.OPTION_LIMIT)
    public static List<ErrorDescription> complexLogicalField(HintContext ctx) {
        Tree parentTree = ctx.getPath().getParentPath().getLeaf();
        if (!(parentTree.getKind() == Tree.Kind.CLASS || parentTree.getKind() == Tree.Kind.ENUM ||
            parentTree.getKind() == Tree.Kind.INTERFACE)) {
            return null;
        }
        ExpressionVisitor v = new ExpressionVisitor(ctx.getInfo(), true,
            ctx.getPreferences().getInt(OPTION_LIMIT, DEFAULT_LIMIT)
        );
        return translate(ctx);
    }
    
    private static List<ErrorDescription> translate(HintContext ctx) {
        ExpressionVisitor v = new ExpressionVisitor(ctx.getInfo(), true,
            ctx.getPreferences().getInt(OPTION_LIMIT, DEFAULT_LIMIT)
        );
        v.scan(ctx.getPath(), null);
        List<TreePath> paths = v.getErrorPaths();
        if (paths.isEmpty()) {
            return null;
        }
        
        List<ErrorDescription> desc = new ArrayList<>(paths.size());
        for (TreePath tp : paths) {
            int count = v.getNodeOperands(tp);
            desc.add(ErrorDescriptionFactory.forTree(
                    ctx, tp, TEXT_LogicalTooComplex(count)
            ));
        }
        return desc;
    }
}
