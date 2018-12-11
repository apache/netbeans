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

package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import org.netbeans.spi.java.hints.ConstraintVariableType;
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
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.Regexp", description = "#DESC_org.netbeans.modules.java.hints.bugs.Regexp", category="bugs", suppressWarnings={"MalformedRegexp", "", "MalformedRegex"}, options=Options.QUERY)
public class Regexp {

    @TriggerPatterns({
        @TriggerPattern(value="java.util.regex.Pattern.compile($pattern)",
                        constraints={
                            @ConstraintVariableType(variable="$pattern", type="java.lang.String")
                        }),
        @TriggerPattern(value="java.util.regex.Pattern.compile($pattern, $flags)",
                        constraints={
                            @ConstraintVariableType(variable="$pattern", type="java.lang.String"),
                            @ConstraintVariableType(variable="$flags", type="int")
                        }),
        @TriggerPattern(value="java.util.regex.Pattern.matches($pattern, $text)",
                        constraints={
                            @ConstraintVariableType(variable="$pattern", type="java.lang.String"),
                            @ConstraintVariableType(variable="$text", type="java.lang.CharSequence")
                        }),
        @TriggerPattern(value="$str.split($pattern)",
                        constraints={
                            @ConstraintVariableType(variable="$str", type="java.lang.String"),
                            @ConstraintVariableType(variable="$pattern", type="java.lang.String")
                        }),
        @TriggerPattern(value="$str.split($pattern, $limit)",
                        constraints={
                            @ConstraintVariableType(variable="$str", type="java.lang.String"),
                            @ConstraintVariableType(variable="$pattern", type="java.lang.String"),
                            @ConstraintVariableType(variable="$limit", type="int")
                        }),
        @TriggerPattern(value="$str.matches($pattern)",
                        constraints={
                            @ConstraintVariableType(variable="$str", type="java.lang.String"),
                            @ConstraintVariableType(variable="$pattern", type="java.lang.String")
                        }),
        @TriggerPattern(value="$str.replaceFirst($pattern, $repl)",
                        constraints={
                            @ConstraintVariableType(variable="$str", type="java.lang.String"),
                            @ConstraintVariableType(variable="$pattern", type="java.lang.String"),
                            @ConstraintVariableType(variable="$repl", type="java.lang.String")
                        }),
        @TriggerPattern(value="$str.replaceAll($pattern, $repl)",
                        constraints={
                            @ConstraintVariableType(variable="$str", type="java.lang.String"),
                            @ConstraintVariableType(variable="$pattern", type="java.lang.String"),
                            @ConstraintVariableType(variable="$repl", type="java.lang.String")
                        })
    })
    public static ErrorDescription hint(final HintContext ctx) {
        final StringBuilder regexp = new StringBuilder();
        final boolean[] accept = {true};
        TreePath pattern = ctx.getVariables().get("$pattern");
        new ErrorAwareTreePathScanner<Void, Void>() {
            @Override
            public Void visitLiteral(LiteralTree node, Void p) {
                if (node.getValue() instanceof String) {
                    regexp.append(node.getValue());
                    return null;
                }
                accept[0] = false;
                return null;
            }
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                Element el = ctx.getInfo().getTrees().getElement(getCurrentPath());

                if (el != null && el.getKind() == ElementKind.FIELD) {
                    VariableElement ve = (VariableElement) el;

                    if (ve.getConstantValue() instanceof String) {
                        regexp.append(ve.getConstantValue());
                        return null;
                    }
                }
                accept[0] = false;
                return null;
            }
            @Override
            public Void visitMemberSelect(MemberSelectTree node, Void p) {
                Element el = ctx.getInfo().getTrees().getElement(getCurrentPath());

                if (el != null && el.getKind() == ElementKind.FIELD) {
                    VariableElement ve = (VariableElement) el;

                    if (ve.getConstantValue() instanceof String) {
                        regexp.append(ve.getConstantValue());
                        return null;
                    }
                }
                accept[0] = false;
                return null;
            }
            @Override
            public Void visitBinary(BinaryTree node, Void p) {
                if (node.getKind() != Kind.PLUS) {
                    return super.visitBinary(node, p);
                }
                accept[0] = false;
                return null;
            }
            @Override
            public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                accept[0] = false;
                return null;
            }
        }.scan(pattern, null);

        if (!accept[0] || regexp.length() == 0) {
            return null;
        }

        try {
            Pattern.compile(regexp.toString());
            return null;
        } catch (PatternSyntaxException pse) {
            String displayName = NbBundle.getMessage(Regexp.class, "DN_RegExp", new Object[] {
                pse.getDescription(),
                pse.getMessage(),
                pse.getPattern(),
                pse.getIndex(),
            });
            return ErrorDescriptionFactory.forTree(ctx, pattern, displayName);
        }
    }
}
