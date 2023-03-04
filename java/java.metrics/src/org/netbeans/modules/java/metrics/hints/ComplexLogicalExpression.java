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
