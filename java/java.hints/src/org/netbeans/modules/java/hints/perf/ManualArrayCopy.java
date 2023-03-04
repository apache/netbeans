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

package org.netbeans.modules.java.hints.perf;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.ManualArrayCopy", description = "#DESC_org.netbeans.modules.java.hints.perf.ManualArrayCopy", category="performance", suppressWarnings={"ManualArrayToCollectionCopy", "", "ManualArrayToCollectionCopy"})
public class ManualArrayCopy {


    @TriggerPatterns({
        @TriggerPattern(value="for (int $i = $s; $i < $len; $i++) {\n"+
                              "    $tarr[$i] = $arr[$i];\n" +
                              "}"),
        @TriggerPattern(value="for (int $i = $s; $i < $len; $i++) {\n"+
                              "    $tarr[$o1 + $i] = $arr[$i];\n"+
                              "}"),
        @TriggerPattern(value="for (int $i = $s; $i < $len; $i++) {\n"+
                              "    $tarr[$i + $o2] = $arr[$i];\n"+
                              "}")
    })
    public static ErrorDescription arrayCopy(final HintContext ctx) {
        TypeMirror sourceType = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$arr"));
        TypeMirror targetType = ctx.getInfo().getTrees().getTypeMirror(ctx.getVariables().get("$tarr"));

        if (!Utilities.isValidType(sourceType) || !Utilities.isValidType(targetType) || !ctx.getInfo().getTypes().isSubtype(sourceType, targetType)) {
            return null;
        }

        String startSource;
        String startTarget;
        String length;
        boolean isSZero = MatcherUtilities.matches(ctx, ctx.getVariables().get("$s"), "0");
        
        Map<String, TreePath> innerVariables = new HashMap<>();
        TreePath base = ctx.getVariables().get("$arr");
        final Element i = ctx.getInfo().getTrees().getElement(ctx.getVariables().get("$i"));
        
        if (i != null) {
            final boolean[] used = new boolean[1];
            new ErrorAwareTreePathScanner<Void, Void>() {
                @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                    Element use = ctx.getInfo().getTrees().getElement(getCurrentPath());
                    
                    if (i.equals(use)) {
                        used[0] |= true;
                    }
                    
                    return super.visitIdentifier(node, p);
                }
            }.scan(base, null);
            
            if (used[0]) return null;
        }
        
        while (MatcherUtilities.matches(ctx, base, "$oarr[$innerIndex]", innerVariables, new HashMap<String, Collection<? extends TreePath>>(), new HashMap<String, String>())) {
            base = innerVariables.get("$oarr");
            
        }

        if (ctx.getVariables().containsKey("$o1")) {
            if (isSZero) {
                startSource = "0"; startTarget = "$o1"; length = "$len";
            } else {
                startSource = "$s"; startTarget = "$o1 + $s"; length = "$len - $s";
            }
        } else if (ctx.getVariables().containsKey("$o2")) {
            if (isSZero) {
                startSource = "0"; startTarget = "$o2"; length = "$len";
            } else {
                startSource = "$s"; startTarget = "$s + $o2"; length = "$len - $s";
            }
        } else {
            if (isSZero) {
                startSource = "0"; startTarget = "0"; length = "$len";
            } else {
                startSource = "$s"; startTarget = "$s"; length = "$len - $s";
            }
        }

        String fix = String.format("java.lang.System.arraycopy($arr, %s, $tarr, %s, %s);", startSource, startTarget, length);

        return compute(ctx, "manual-array-copy", fix);
    }

    @TriggerPatterns({
        @TriggerPattern(value="for (int $i = 0; $i < $arr.length; $i++) {\n"+
                              "    $coll.add($arr[$i]);\n"+
                              "}\n",
                        constraints={
                            @ConstraintVariableType(variable="$arr", type="java.lang.Object[]"),
                            @ConstraintVariableType(variable="$coll", type="java.util.Collection")
                        }),
        @TriggerPattern(value="for ($type $var : $arr) {\n"+
                              "    $coll.add($var);\n"+
                              "}\n",
                        constraints={
                            @ConstraintVariableType(variable="$arr", type="java.lang.Object[]"),
                            @ConstraintVariableType(variable="$coll", type="java.util.Collection")
                        })
    })
    public static ErrorDescription collection(HintContext ctx) {
        return compute(ctx, "manual-array-copy-coll", "$coll.addAll(java.util.Arrays.asList($arr));");
    }
    
    private static ErrorDescription compute(HintContext ctx, String key, String to) {
        String fixDisplayName = NbBundle.getMessage(ManualArrayCopy.class, "FIX_" + key);
        Fix fix = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), to);
        String displayName = NbBundle.getMessage(ManualArrayCopy.class, "ERR_" + key);

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, fix);
    }

}
