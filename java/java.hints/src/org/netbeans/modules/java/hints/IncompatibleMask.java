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

package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.text.MessageFormat;
import java.util.Map;

import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Jancura
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.IncompatibleMask", description = "#DESC_org.netbeans.modules.java.hints.IncompatibleMask", category="bitwise_operations", suppressWarnings="IncompatibleBitwiseMaskOperation", options=Options.QUERY)
public class IncompatibleMask {

    @TriggerPatterns ({
        @TriggerPattern (value="($a & $b) == $c"),
        @TriggerPattern (value="$c == ($a & $b)")
    })
    public static ErrorDescription checkIncompatibleMask1 (HintContext ctx) {
        TreePath treePath = ctx.getPath ();
        Map<String,TreePath> variables = ctx.getVariables ();
        TreePath tree = variables.get ("$a");
        Long v1 = getConstant (tree, ctx);
        if (v1 == null) {
            tree = variables.get ("$b");
            v1 = getConstant (tree, ctx);
        }
        if (v1 == null)
            return null;
        tree = variables.get ("$c");
        Long v2 = getConstant (tree, ctx);
        if (v2 == null)
            return null;

        if ((~v1 & v2) > 0)
            return ErrorDescriptionFactory.forName (
                ctx,
                treePath,
                MessageFormat.format (
                    NbBundle.getMessage (IncompatibleMask.class, "MSG_IncompatibleMask"),
                    treePath.getLeaf ().toString ()
                )
            );
        return null;
    }

    @TriggerPatterns ({
        @TriggerPattern (value="($a | $b) == $c"),
        @TriggerPattern (value="$c == ($a | $b)")
    })
    public static ErrorDescription checkIncompatibleMask2 (HintContext ctx) {
        TreePath treePath = ctx.getPath ();
        Map<String,TreePath> variables = ctx.getVariables ();
        TreePath tree = variables.get ("$a");
        Long v1 = getConstant (tree, ctx);
        if (v1 == null) {
            tree = variables.get ("$b");
            v1 = getConstant (tree, ctx);
        }
        if (v1 == null)
            return null;
        tree = variables.get ("$c");
        Long v2 = getConstant (tree, ctx);
        if (v2 == null)
            return null;

        if ((v1 & v2) != v1)
            return ErrorDescriptionFactory.forName (
                ctx,
                treePath,
                MessageFormat.format (
                    NbBundle.getMessage (IncompatibleMask.class, "MSG_IncompatibleMask"),
                    treePath.getLeaf ().toString ()
                )
            );
        return null;
    }

    static Long getConstant (
        TreePath                tp,
        HintContext             ctx
    ) {
        Number value = ArithmeticUtilities.compute(ctx.getInfo(), tp, true);

        if (value instanceof Integer)
            return Long.valueOf(value.longValue());
        if (value instanceof Long)
            return (Long) value;
        return null;
    }
}
