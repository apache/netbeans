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

package org.netbeans.modules.java.hints.finalize;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.finalize.CallFinalize", description = "#DESC_org.netbeans.modules.java.hints.finalize.CallFinalize", category="finalization",suppressWarnings={"FinalizeCalledExplicitly"}, options=Options.QUERY)    //NOI18N
public class CallFinalize {

    @TriggerPatterns({
        @TriggerPattern(value="$ins.finalize()",    //NOI18N
            constraints={
                @ConstraintVariableType(variable="$ins",type="java.lang.Object")    //NOI18N
            })
        }
    )
    public static ErrorDescription hint(final HintContext ctx) {
        assert ctx != null;
        final TreePath ins = ctx.getVariables().get("$ins");    //NOI18N
        if (ins != null) {
            Tree target = ins.getLeaf();
            if (target.getKind() == Tree.Kind.IDENTIFIER && "super".contentEquals(((IdentifierTree)target).getName())) {    //NOI18N
                TreePath parent = ins.getParentPath();
                while (parent.getLeaf().getKind() != Tree.Kind.METHOD) {
                    parent = parent.getParentPath();
                }
                final MethodTree owner = (MethodTree) parent.getLeaf();
                if (Util.isFinalize(owner)) {
                    return null;
                }
            }
        }
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), NbBundle.getMessage(CallFinalize.class, "TXT_CallFinalize"));   //NOI18N
    }
}
