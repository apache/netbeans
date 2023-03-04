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

package org.netbeans.modules.java.hints.bugs;

import com.sun.source.util.TreePath;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.UseOptions;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class EqualsHint {

    private static final boolean ERASURE_PREFS_DEFAULT = true; // NOI18N
    @BooleanOption(displayName = "#LBL_org.netbeans.modules.java.hints.bugs.EqualsHint.ERASURE_PREFS_KEY", tooltip = "#TP_org.netbeans.modules.java.hints.bugs.EqualsHint.ERASURE_PREFS_KEY", defaultValue=ERASURE_PREFS_DEFAULT)
    private static final String ERASURE_PREFS_KEY = "eguals-hint-erasure"; // NOI18N
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.EqualsHint.arrayEquals", description = "#DESC_org.netbeans.modules.java.hints.bugs.EqualsHint.arrayEquals", category="bugs", suppressWarnings="ArrayEquals")
    @TriggerPatterns({
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @ConstraintVariableType(variable="$obj", type="java.lang.Object"),
                            @ConstraintVariableType(variable="$arr", type="java.lang.Object[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @ConstraintVariableType(variable="$obj", type="java.lang.Object"),
                            @ConstraintVariableType(variable="$arr", type="boolean[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @ConstraintVariableType(variable="$obj", type="java.lang.Object"),
                            @ConstraintVariableType(variable="$arr", type="byte[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @ConstraintVariableType(variable="$obj", type="java.lang.Object"),
                            @ConstraintVariableType(variable="$arr", type="short[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @ConstraintVariableType(variable="$obj", type="java.lang.Object"),
                            @ConstraintVariableType(variable="$arr", type="char[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @ConstraintVariableType(variable="$obj", type="java.lang.Object"),
                            @ConstraintVariableType(variable="$arr", type="int[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @ConstraintVariableType(variable="$obj", type="java.lang.Object"),
                            @ConstraintVariableType(variable="$arr", type="long[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @ConstraintVariableType(variable="$obj", type="java.lang.Object"),
                            @ConstraintVariableType(variable="$arr", type="float[]")
                        }),
        @TriggerPattern(value="$obj.equals($arr)",
                        constraints={
                            @ConstraintVariableType(variable="$obj", type="java.lang.Object"),
                            @ConstraintVariableType(variable="$arr", type="double[]")
                        })
    })
    public static ErrorDescription arrayEquals(HintContext ctx) {
        //XXX: this check should not be needed:
        TreePath arr = ctx.getVariables().get("$arr");
        TypeMirror tm = ctx.getInfo().getTrees().getTypeMirror(arr);

        if (tm == null || tm.getKind() != TypeKind.ARRAY) {
            return null;
        }
        //XXX end

        String fixArraysDisplayName = NbBundle.getMessage(EqualsHint.class, "FIX_ReplaceWithArraysEquals");
        Fix arrays = JavaFixUtilities.rewriteFix(ctx, fixArraysDisplayName, ctx.getPath(), "java.util.Arrays.equals($obj, $arr)");
        String fixInstanceDisplayName = NbBundle.getMessage(EqualsHint.class, "FIX_ReplaceWithInstanceEquals");
        Fix instance = JavaFixUtilities.rewriteFix(ctx, fixInstanceDisplayName, ctx.getPath(), "$obj == $arr");
        String displayName = NbBundle.getMessage(EqualsHint.class, "ERR_ARRAY_EQUALS");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, arrays, instance);
    }
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.EqualsHint", description = "#DESC_org.netbeans.modules.java.hints.EqualsHint", id="org.netbeans.modules.java.hints.EqualsHint", category="bugs", suppressWarnings={"IncompatibleEquals", "", "EqualsBetweenInconvertibleTypes"}, options=Options.QUERY)
    @UseOptions(ERASURE_PREFS_KEY)
    @TriggerPattern(value="$this.equals($par)",
                    constraints={
                        @ConstraintVariableType(variable="$this", type="java.lang.Object"),
                        @ConstraintVariableType(variable="$par", type="java.lang.Object")
                    })
    public static ErrorDescription incompatibleEquals(HintContext ctx) {
        TreePath ths = ctx.getVariables().get("$this");
        TreePath par = ctx.getVariables().get("$par");
        TypeMirror thsType;

        if (ths != null) {
            thsType = ctx.getInfo().getTrees().getTypeMirror(ths);
        } else {
            TreePath cls = ctx.getPath();

            while (cls != null && !TreeUtilities.CLASS_TREE_KINDS.contains(cls.getLeaf().getKind())) {
                cls = cls.getParentPath();
            }

            if (cls == null) {
                return null;
            }

            thsType = ctx.getInfo().getTrees().getTypeMirror(cls);
        }
        if (thsType == null || thsType.getKind() != TypeKind.DECLARED) {
            return null;
        }
        
        TypeMirror parType = ctx.getInfo().getTrees().getTypeMirror(par);
        if (parType == null || parType.getKind() != TypeKind.DECLARED) {
            return null;
        }
        if (ctx.getPreferences().getBoolean(ERASURE_PREFS_KEY, ERASURE_PREFS_DEFAULT)) {
            Types types = ctx.getInfo().getTypes();
            thsType = types.erasure(thsType);
            parType = types.erasure(parType);
        }
        boolean castable = ctx.getInfo().getTypeUtilities().isCastable(thsType, parType) || ctx.getInfo().getTypeUtilities().isCastable(parType, thsType);

        if (castable) {
            return null;
        }

        String displayName = NbBundle.getMessage(EqualsHint.class, "ERR_INCOMPATIBLE_EQUALS"); // NOI18N
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

}
