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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.Map;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;
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
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.ShiftOutOfRange", description = "#DESC_org.netbeans.modules.java.hints.ShiftOutOfRange", category="bitwise_operations", suppressWarnings="ShiftOutOfRange", options=Options.QUERY)
public class ShiftOutOfRange {

    @TriggerPatterns ({
        @TriggerPattern (value="$v >> $c"),
        @TriggerPattern (value="$v >>> $c"),
        @TriggerPattern (value="$v << $c")
    })
    public static ErrorDescription checkShiftOutOfRange (HintContext ctx) {
        TreePath treePath = ctx.getPath ();
        Map<String,TreePath> variables = ctx.getVariables ();
        TreePath tree = variables.get ("$c");
        Long value = IncompatibleMask.getConstant (tree, ctx);
        if (value == null) return null;
        tree = variables.get ("$v");
        if (!(tree.getLeaf() instanceof ExpressionTree)) return null;
        CompilationInfo compilationInfo = ctx.getInfo ();
        Trees trees = compilationInfo.getTrees ();
        TreePath identifierTreePath = tree;
        TypeMirror typeMirror = trees.getTypeMirror (identifierTreePath);
        if (!Utilities.isValidType(typeMirror)) {
            return null;
        }
        if (typeMirror.toString ().equals ("int")) {
            if (value < 0 || value > 31)
                return ErrorDescriptionFactory.forName (
                        ctx,
                        treePath,
                        NbBundle.getMessage (ShiftOutOfRange.class, "MSG_ShiftOutOfRange_int")
                );
        } else
        if (typeMirror.toString ().equals ("long")) {
            if (value < 0 || value > 63)
                return ErrorDescriptionFactory.forName (
                        ctx,
                        treePath,
                        NbBundle.getMessage (ShiftOutOfRange.class, "MSG_ShiftOutOfRange_long")
                );
        }
        return null;
    }
}
