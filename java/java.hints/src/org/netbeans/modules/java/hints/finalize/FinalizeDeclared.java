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

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.finalize.FinalizeDeclared", description = "#DESC_org.netbeans.modules.java.hints.finalize.FinalizeDeclared", category="finalization",suppressWarnings={"FinalizeDeclaration"}, options=Options.QUERY) //NOI18N
public class FinalizeDeclared {

    @TriggerTreeKind(Tree.Kind.METHOD)
    public static ErrorDescription hint(final HintContext ctx) {
        assert ctx != null;
        final TreePath tp = ctx.getPath();
        final MethodTree tree = (MethodTree) tp.getLeaf();
        if (Util.isFinalize(tree)) {
            if (!Util.isInObject(ctx, tp)) {
                return ErrorDescriptionFactory.forName(ctx, tp, NbBundle.getMessage(FinalizeDeclared.class, "TXT_FinalizeDeclared"));
            }
        }
        return null;
    }
}
