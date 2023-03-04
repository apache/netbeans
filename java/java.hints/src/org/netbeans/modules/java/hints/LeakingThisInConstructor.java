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

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;

/**
 *
 * @author David Strupl
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.LeakingThisInConstructor", description = "#DESC_org.netbeans.modules.java.hints.LeakingThisInConstructor", category="initialization", suppressWarnings={"LeakingThisInConstructor", "", "ThisEscapedInObjectConstruction"}, options=Options.QUERY)
public class LeakingThisInConstructor {
    private static final String THIS_KEYWORD = "this"; // NOI18N
    private static final String SUPER_KEYWORD = "super"; // NOI18N
    public LeakingThisInConstructor() {
    }

    @TriggerTreeKind(Tree.Kind.IDENTIFIER)
    public static ErrorDescription hint(HintContext ctx) {
        IdentifierTree it = (IdentifierTree) ctx.getPath().getLeaf();
        CompilationInfo info = ctx.getInfo();
        if (!Utilities.isInConstructor(ctx)) {
            return null;
        }

        Element e = info.getTrees().getElement(ctx.getPath());
        if (e == null || !e.getSimpleName().contentEquals(THIS_KEYWORD)) {
            return null;
        }

        if (ctx.getPath().getParentPath().getLeaf().getKind() != Tree.Kind.METHOD_INVOCATION) {
            return null;
        }

        return ErrorDescriptionFactory.forName(ctx, it,
                NbBundle.getMessage(
                    LeakingThisInConstructor.class,
                    "MSG_org.netbeans.modules.java.hints.LeakingThisInConstructor"));
    }

    @TriggerPattern(value="$v=$this") // NOI18N
    public static ErrorDescription hintOnAssignment(HintContext ctx) {
        Map<String,TreePath> variables = ctx.getVariables ();
        TreePath thisPath = variables.get ("$this"); // NOI18N
        if (   thisPath.getLeaf().getKind() != Kind.IDENTIFIER
            || !((IdentifierTree) thisPath.getLeaf()).getName().contentEquals(THIS_KEYWORD)) {
            return null;
        }
        if (!Utilities.isInConstructor(ctx)) {
            return null;
        }
        TreePath storePath = variables.get("$v");
        Tree t = storePath.getLeaf();
        if (t.getKind() == Tree.Kind.MEMBER_SELECT) {
            t = ((MemberSelectTree)t).getExpression();
            while (t != null && t.getKind() == Tree.Kind.PARENTHESIZED) {
                t = ((ParenthesizedTree)t).getExpression();
            }
            if (t == null) {
                return null;
            } else if (t.getKind() == Tree.Kind.IDENTIFIER) {
                IdentifierTree it = (IdentifierTree)t;
                if (it.getName().contentEquals(THIS_KEYWORD) ||
                    it.getName().contentEquals(SUPER_KEYWORD)) {
                    return null;
                }
            }
        } else {
            return null;
        }
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(),
                NbBundle.getMessage(
                    LeakingThisInConstructor.class,
                    "MSG_org.netbeans.modules.java.hints.LeakingThisInConstructor"));
    }

}
