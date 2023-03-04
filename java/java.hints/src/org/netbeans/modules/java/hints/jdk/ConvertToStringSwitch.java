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

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.java.hints.ArithmeticUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.suggestions.IfToSwitchSupport;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.IntegerOption;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch", description = "#DESC_org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch", category="rules15", suppressWarnings="ConvertToStringSwitch",
        minSourceVersion = "7")
@NbBundle.Messages({
    "# {0} - string literal value",
    "TEXT_ChainedIfContainsSameValues=The string value `{0}'' used in String comparison appears earlier in the chained if-else-if statement. This condition never evaluates to true",
    "TEXT_ConvertToSwitch=Convert to switch",
    "# initial label for breaking out of the innermost loop",
    "LABEL_OuterGeneratedLabelInitial=OUTER",
    "# template for generated label names, must form a valid Java identifiers",
    "# {0} - unique integer",
    "LABEL_OuterGeneratedLabel=OUTER_{0}"
})
public class ConvertToStringSwitch {

    static final boolean DEF_ALSO_EQ = true;
    static final int DEF_THRESHOLD = 3;
    
    @BooleanOption(displayName = "#LBL_org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch.KEY_ALSO_EQ", tooltip = "#TP_org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch.KEY_ALSO_EQ", defaultValue=DEF_ALSO_EQ)
    static final String KEY_ALSO_EQ = "also-equals";
    
    @IntegerOption(displayName = "#LBL_org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch.KEY_THRESHOLD", 
            tooltip = "#TP_org.netbeans.modules.java.hints.jdk.ConvertToStringSwitch.KEY_THRESHOLD", defaultValue = DEF_THRESHOLD,
            minValue = 2, step = 1)
    static final String KEY_THRESHOLD = "threshold";
    
    public static final boolean DEFAULT_OPTION_GENERATE_EMPTY_DEFAULT = true;

    @BooleanOption(displayName = "#OPT_ConvertIfToSwitch_EmptyDefault", tooltip = "#DESC_ConvertIfToSwitch_EmptyDefault",
            defaultValue = DEFAULT_OPTION_GENERATE_EMPTY_DEFAULT)
    public static final String OPTION_GENERATE_EMPTY_DEFAULT = "iftoswitch.generate.default"; // NOI18N
    
    private static final String[] INIT_PATTERNS = {
        "$c1.equals($c2)",
        "$c1.contentEquals($c2)",
        "$c1 == $c2"
    };

    private static final String[] PATTERNS = {
        "$var.equals($constant)",
        "$constant.equals($var)",
        "$var.contentEquals($constant)",
        "$constant.contentEquals($var)",
        "$var == $constant",
        "$constant == $var"
    };
    
    @TriggerPattern(value="if ($cond) $body; else $else;")
    public static List<ErrorDescription> hint(final HintContext ctx) {
        if (ctx.getPath().getParentPath().getLeaf().getKind() == Kind.IF) {
            return null;
        }

        final TypeElement jlString = ctx.getInfo().getElements().getTypeElement("java.lang.String"); // NOI18N

        if (jlString == null) {
            return null;
        }
        final Collection<String> initPatterns = new ArrayList<String>(INIT_PATTERNS.length);
        final boolean acceptEqEq = ctx.getPreferences().getBoolean(KEY_ALSO_EQ, DEF_ALSO_EQ);
        initPatterns.addAll(Arrays.asList(INIT_PATTERNS));
        
        IfToSwitchSupport eval = new IfToSwitchSupport(ctx) {
            private boolean [] varConst = new boolean[1];
            
            @Override
            protected Object evalConstant(TreePath path) {
                TypeMirror m = ci.getTrees().getTypeMirror(path);
                if (m.getKind() == TypeKind.NULL || ci.getTypes().asElement(m) == jlString) {
                    Object o = ArithmeticUtilities.compute(ci, path, true, true);
                    if (ArithmeticUtilities.isNull(o) || ArithmeticUtilities.isRealValue(o)) {
                        return o;
                    }
                }
                return null;
            }
            
            
            @Override
            protected TreePath matchesChainedItem(TreePath test, TreePath variable) {
                varConst[0] = false;
                TreePath p = isStringComparison(ctx, test, varConst, variable);
                if (p == null) {
                    return null;
                }
                if (varConst[0]) {
                    controlVariableNotNull();
                }
                return p;
            }

            @Override
            protected TreePath matches(TreePath test, boolean initial) {
                int cnt =  -1;
                for (String pat : initPatterns) {
                    cnt++;
                    if (MatcherUtilities.matches(ctx, test, pat, true)) {
                        TreePath c1 = ctx.getVariables().get("$c1");
                        TypeMirror m = ctx.getInfo().getTrees().getTypeMirror(c1);
                        boolean n = false;
                        if (!Utilities.isValidType(m) ||
                             (m.getKind() != TypeKind.NULL  &&
                             ctx.getInfo().getTypes().asElement(m) != jlString)) {
                            continue;
                        }
                        n |= m.getKind() == TypeKind.NULL;
                        TreePath c2 = ctx.getVariables().get("$c2");
                        m = ctx.getInfo().getTrees().getTypeMirror(c2);
                        if (!Utilities.isValidType(m) ||
                             (m.getKind() != TypeKind.NULL  &&
                             ctx.getInfo().getTypes().asElement(m) != jlString)) {
                            continue;
                        }
                        n |= m.getKind() == TypeKind.NULL;
                        if (cnt == 2 && !acceptEqEq && !n) {
                            // do not accept == if not explicitly permitted, and neither side is null
                            continue;
                        }
                        reportConstantAndLiteral(c1, c2);
                        return c1;
                    }
                }
                return null;
            }
            
        };
        if (!eval.process(ctx.getVariables().get("$cond"))) {
            return null;
        }
        int minBranches = ctx.getPreferences().getInt(KEY_THRESHOLD, DEF_THRESHOLD);
        if (eval.getNumberOfBranches() < minBranches) {
            return null;
        }
        
        if (eval.containsDuplicateConstants()) {
            List<ErrorDescription> descs = new ArrayList<>(eval.getDuplicateConstants().size());
            Set<Object> seen = new HashSet<>();
            for (Map.Entry<TreePath, Object> en : eval.getDuplicateConstants().entrySet()) {
                String lit = en.getValue().toString();
                // do not report a single value more than once; confusing.
                if (!seen.add(lit)) {
                    continue;
                }
                TreePath t = en.getKey();
                descs.add(ErrorDescriptionFactory.forTree(ctx, t, Bundle.TEXT_ChainedIfContainsSameValues(lit)));
            }
            return descs;
        }

        Fix convert = eval.createFix(NbBundle.getMessage(ConvertToStringSwitch.class, "FIX_ConvertToStringSwitch"),
                ctx.getPreferences().getBoolean(OPTION_GENERATE_EMPTY_DEFAULT, DEFAULT_OPTION_GENERATE_EMPTY_DEFAULT)).toEditorFix(); // NOI18N
        ErrorDescription ed = ErrorDescriptionFactory.forName(ctx,
                                                              ctx.getPath(),
                                                              Bundle.TEXT_ConvertToSwitch(),
                                                              convert);

        return Collections.singletonList(ed);
    }

    private static TreePath isStringComparison(HintContext ctx, TreePath tp, boolean[] varConst, TreePath var) {
        Tree leaf = tp.getLeaf();

        while (leaf.getKind() == Kind.PARENTHESIZED) {
            tp = new TreePath(tp, ((ParenthesizedTree) leaf).getExpression());
            leaf = tp.getLeaf();
        }

        Collection<String> patterns = new ArrayList<String>(PATTERNS.length);

        patterns.addAll(Arrays.asList(PATTERNS));

        ctx.getVariables().put("$var", var);
        
        boolean acceptEqEq = ctx.getPreferences().getBoolean(KEY_ALSO_EQ, DEF_ALSO_EQ);
        int i = -1;
        assert PATTERNS.length == 6; // the cycle counts with specific positions
        for (String patt : patterns) {
            ++i;
            ctx.getVariables().remove("$constant"); // NOI18N

            if (!MatcherUtilities.matches(ctx, tp, patt, true))
                continue;
            if (ctx.getVariables().get("$constant") == null ||
                ctx.getVariables().get("$var") == null) {
                continue;
            }
            if (i % 2 == 0 && i < 4) {
                varConst[0] = true;
            }
            TreePath constPath = ctx.getVariables().get("$constant"); // NOI18N
            if (i < 4 || acceptEqEq) {
                return constPath;
            }

            // equals is permitted when comparing with null, or option is set
            TypeMirror constType = ctx.getInfo().getTrees().getTypeMirror(constPath);
            if (constType != null && constType.getKind() == TypeKind.NULL) {
                return constPath;
            } 
        }

        return null;
    }

}
