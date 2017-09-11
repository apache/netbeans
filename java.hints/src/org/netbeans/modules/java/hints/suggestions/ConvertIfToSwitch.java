/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.IntegerOption;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.UseOptions;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class ConvertIfToSwitch {
    
    public static final int DEFAULT_OPTION_IF_SWITCH_BRANCH_THRESHOLD = 3;
    public static final boolean DEFAULT_OPTION_GENERATE_EMPTY_DEFAULT = true;
    
    @IntegerOption(displayName = "#OPT_ConvertIfToSwitch_Threshold", tooltip = "#DESC_ConvertIfToSwitch_Threshold",
            minValue = 3, defaultValue = DEFAULT_OPTION_IF_SWITCH_BRANCH_THRESHOLD, step = 1)
    public static final String OPTION_IF_SWITCH_BRANCH_THRESHOLD = "iftoswitch.branch.threshold"; // NOI18N

    @BooleanOption(displayName = "#OPT_ConvertIfToSwitch_EmptyDefault", tooltip = "#DESC_ConvertIfToSwitch_EmptyDefault",
            defaultValue = DEFAULT_OPTION_GENERATE_EMPTY_DEFAULT)
    public static final String OPTION_GENERATE_EMPTY_DEFAULT = "iftoswitch.generate.default"; // NOI18N
    
    
    @NbBundle.Messages({
        "# {0} - string literal value",
        "TEXT_ChainedIfContainsSameValues=The constant value `{0}'' used in comparison appears earlier in the chained if-else-if statement. This condition never evaluates to true",
        "FIX_ConvertIfsToSwitch=Convert ifs to switch statement"
    })
    @Hint(id="org.netbeans.modules.java.hints.suggestions.ConvertIfToSwitch", 
        displayName = "#DN_ConvertIfToSwitch", 
        description = "#DESC_ConvertIfToSwitch", 
        category="suggestions", 
        hintKind=org.netbeans.spi.java.hints.Hint.Kind.INSPECTION, severity=Severity.VERIFIER)
    @TriggerPatterns({
        @TriggerPattern(value = "if ($cond1) $body else if ($cond2) $stmt2; else $else"),
    })
    @UseOptions({ OPTION_IF_SWITCH_BRANCH_THRESHOLD, OPTION_GENERATE_EMPTY_DEFAULT })
    public static List<ErrorDescription> convertIfToSwitch(final HintContext ctx) {
        // ignore middle ifs:
        TreePath parent = ctx.getPath().getParentPath();
        if (parent.getLeaf().getKind() == Tree.Kind.IF) {
            return null;
        }
        IfToSwitchSupport eval = new IfToSwitchSupport(ctx) {
                boolean controlTypeChecked = false;
                
                @Override
                protected TypeMirror acceptArgType(TypeMirror controlType, TypeMirror argType) {
                    if (!controlTypeChecked) {
                        // See issue #257809; although all constants may be enum values, the control expression
                        // may be typed differently, i.e. an interface satisfied by those constants. In that case,
                        // generating switch would require type-check and casting. Better exclude the situation from hint.
                        if (!controlType.getKind().isPrimitive() &&
                            !Utilities.isPrimitiveWrapperType(argType)) {
                            // the contorl type must be an Enum; String is handled elsewhere
                            if (controlType.getKind() != TypeKind.DECLARED) {
                                return null;
                            }
                            Element el = ((DeclaredType)controlType).asElement();
                            if (el == null || el.getKind() != ElementKind.ENUM) {
                                return null;
                            }
                        }
                        controlTypeChecked = true;
                    }
                    return super.acceptArgType(controlType, argType);
                }
            
            
            protected TreePath matches(TreePath test, boolean initial) {
                for (String pat : ConvertIfToSwitch.PATTERNS_INIT) {
                    if (MatcherUtilities.matches(ctx, test, pat, true)) {
                        TreePath c1 = ctx.getVariables().get("$c1");
                        reportConstantAndLiteral(c1, ctx.getVariables().get("$c2"));
                        return c1;
                    }
                }
                return null;
            }

            protected TreePath matchesChainedItem(TreePath test, TreePath variable) {
                List<String> pats = new ArrayList<>(Arrays.asList(ConvertIfToSwitch.PATTERNS_ID));
                if (getVariableMirror().getKind() == TypeKind.DECLARED) {
                    // enum ?
                    pats.addAll(Arrays.asList(ConvertIfToSwitch.PATTERNS_EQ));
                }
                // hack:
                ctx.getVariables().put("$var", variable);
                ctx.getVariables().remove("$constant");
                ctx.getVariableNames().remove("$constant");
                for (String pat : pats) {
                    if (MatcherUtilities.matches(ctx, test, pat, true)) {
                        TreePath constPath = ctx.getVariables().get("$constant");
                        TreePath varPath = ctx.getVariables().get("$var$1");
                        if (varPath.getParentPath().getLeaf().getKind() == Tree.Kind.MEMBER_SELECT) {
                            controlVariableNotNull();
                        }
                        return constPath;
                    }
                }
                return null;
            }

        };
        if (!eval.process(ctx.getVariables().get("$cond1"))) {
            return null;
        }
        if (eval.containsDuplicateConstants()) {
            Map<TreePath, Object> duplicates = eval.getDuplicateConstants();
            List<ErrorDescription> descs = new ArrayList<>(duplicates.size());
            Set s = new HashSet();
            for (Map.Entry<TreePath, Object> en : duplicates.entrySet()) {
                Object lit = en.getValue();
                // do not report a single value more than once; confusing.
                if (!s.add(lit)) {
                    continue;
                }
                TreePath t = en.getKey();
                descs.add(ErrorDescriptionFactory.forTree(ctx, t, Bundle.TEXT_ChainedIfContainsSameValues(lit)));
            }
            return descs;
        }
        if (eval.getNumberOfBranches() < ctx.getPreferences().getInt(OPTION_IF_SWITCH_BRANCH_THRESHOLD, DEFAULT_OPTION_IF_SWITCH_BRANCH_THRESHOLD)) {
            return null;
        }
        Fix convert = eval.createFix(Bundle.FIX_ConvertIfsToSwitch(), ctx.getPreferences().getBoolean(OPTION_GENERATE_EMPTY_DEFAULT, DEFAULT_OPTION_GENERATE_EMPTY_DEFAULT)).toEditorFix();
        return Collections.singletonList(ErrorDescriptionFactory.forTree(ctx, ctx.getVariables().get("$body").getParentPath(),
                NbBundle.getMessage(ConvertIfToSwitch.class, "HINT_ConvertIfToSwitch"), convert));
    }
    
    private static final String[] PATTERNS_INIT = {
        "$c1.equals($c2)",
        "$c1 == $c2",
    };

    private static final String[] PATTERNS_EQ = {
        "$var.equals($constant)",
        "$constant.equals($var)",
        "$var.contentEquals($constant)",
        "$constant.contentEquals($var)"
    };
    
    private static final String[] PATTERNS_ID = {
        "$var == $constant",
        "$constant == $var",
    };


}
