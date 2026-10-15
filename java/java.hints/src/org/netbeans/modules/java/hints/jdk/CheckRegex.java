/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.List;
import javax.lang.model.element.Name;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_CheckRegex", description = "#DESC_CheckRegex", category = "general", severity = Severity.HINT)
@Messages({
    "DN_CheckRegex=Check Regular Expression",
    "DESC_CheckRegex=Check Regular Expression"
})
public class CheckRegex {

    @TriggerPatterns({
        @TriggerPattern(value = "java.util.regex.Pattern.compile($pattern)",
                constraints = {
                    @ConstraintVariableType(variable = "$pattern", type = "java.lang.String")
                }),
        @TriggerPattern(value = "java.util.regex.Pattern.compile($pattern, $flags)",
                constraints = {
                    @ConstraintVariableType(variable = "$pattern", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$flags", type = "int")
                }),
        @TriggerPattern(value = "java.util.regex.Pattern.matches($pattern, $text)",
                constraints = {
                    @ConstraintVariableType(variable = "$pattern", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$text", type = "java.lang.CharSequence")
                }),
        @TriggerPattern(value = "$str.split($pattern)",
                constraints = {
                    @ConstraintVariableType(variable = "$str", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$pattern", type = "java.lang.String")
                }),
        @TriggerPattern(value = "$str.split($pattern, $limit)",
                constraints = {
                    @ConstraintVariableType(variable = "$str", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$pattern", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$limit", type = "int")
                }),
        @TriggerPattern(value = "$str.matches($pattern)",
                constraints = {
                    @ConstraintVariableType(variable = "$str", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$pattern", type = "java.lang.String")
                }),
        @TriggerPattern(value = "$str.replaceFirst($pattern, $repl)",
                constraints = {
                    @ConstraintVariableType(variable = "$str", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$pattern", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$repl", type = "java.lang.String")
                }),
        @TriggerPattern(value = "$str.replaceAll($pattern, $repl)",
                constraints = {
                    @ConstraintVariableType(variable = "$str", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$pattern", type = "java.lang.String"),
                    @ConstraintVariableType(variable = "$repl", type = "java.lang.String")
                })
    })
    @CheckForNull
    @Messages({"ERR_CheckRegex=Check regular expression",
        "# {0} - invalidRegex",
        "ERR_InvalidRegex=Invalid regular expression: {0}"})
    public static ErrorDescription computeWarning(HintContext ctx) {

        String originalString = null;
        Tree leaf = ctx.getVariables().get("$pattern").getLeaf();  // NOI18N

        if (leaf.getKind() == Kind.STRING_LITERAL) {
            originalString = (String) ((LiteralTree) leaf).getValue();
        } else if (leaf.getKind() == Kind.IDENTIFIER) {
            originalString = identifierSearch(leaf, ctx);
        }

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_CheckRegex(), new FixImpl(ctx.getInfo(), ctx.getPath(), originalString).toEditorFix());
    }

    @CheckForNull
    public static String identifierSearch(Tree leaf, HintContext ctx) {
        String originalString = null;
        Name name = ((IdentifierTree) leaf).getName();
        TreePath tp = ctx.getPath().getParentPath();

        Tree statement = tp.getLeaf();
        BlockTree bt = null;
        while (originalString == null && tp != null) {
            if (tp.getLeaf() instanceof BlockTree blockTree) {
                originalString = identifierBlockSearch(tp.getLeaf(), name, statement, bt);
                bt = blockTree;
            } else if (tp.getLeaf() instanceof ClassTree ct) {
                originalString = identifierClassSearch(ct, name);
            }
            tp = tp.getParentPath();
        }
        return originalString;
    }

    @CheckForNull
    private static String identifierBlockSearch(Tree leaf, Name name, Tree statement, BlockTree blocktree) {
        String res = null;
        try {
            BlockTree bt = (BlockTree) leaf;
            List<? extends StatementTree> statements = bt.getStatements();
            for (int i = 0; i < statements.size(); i++) {
                StatementTree st = statements.get(i);
                if (st.equals(statement)) {
                    return res;
                }
                if (st.equals(blocktree)) {
                    return res;
                }
                if (st instanceof VariableTree vt) {
                    if (vt.getType().toString().equalsIgnoreCase("String") && vt.getName().equals(name)) {  // NOI18N
                        LiteralTree lt = (LiteralTree) vt.getInitializer();
                        res = (String) lt.getValue();
                    }
                } else if (st instanceof ExpressionStatementTree est && est.getExpression() instanceof AssignmentTree at) {
                    if (at.getVariable().toString().equals(name.toString())) {
                        res = (String) ((LiteralTree) at.getExpression()).getValue();
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return res;
    }

    @CheckForNull
    private static String identifierClassSearch(ClassTree ct, Name name) {
        String res = null;
        try {
            List<? extends Tree> members = ct.getMembers();
            for (int i = 0; i < members.size(); i++) {
                Tree t = members.get(i);
                if (t instanceof VariableTree vt) {
                    if (vt.getType().toString().equalsIgnoreCase("String") && vt.getName().equals(name)) {  // NOI18N
                        LiteralTree lt = (LiteralTree) vt.getInitializer();
                        res = lt != null ? (String) lt.getValue() : null;
                    }
                } else if (t instanceof ExpressionStatementTree est && est.getExpression() instanceof AssignmentTree at) {
                    if (at.getVariable().toString().equals(name.toString())) {
                        res = (String) ((LiteralTree) at.getExpression()).getValue();
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return res;
    }

    private static final class FixImpl extends JavaFix {

        private final String origString;

        private FixImpl(CompilationInfo info, TreePath path, String origString) {
            super(info, path);
            this.origString = origString;
        }

        @Override
        protected String getText() {
            return Bundle.DESC_CheckRegex();
        }

        @Override
        protected void performRewrite(TransformationContext tc) throws Exception {
            SwingUtilities.invokeLater(() -> {
                CheckRegexTopComponent win = CheckRegexTopComponent.findInstance();
                win.open();
                win.requestActive();
                win.setData(origString);
            });
        }

    }
}
