/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
