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

package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
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
        new TreePathScanner<Void, Void>() {
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
