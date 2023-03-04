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

import com.sun.source.util.TreePath;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.jdk.ThrowableInitCause", description = "#DESC_org.netbeans.modules.java.hints.jdk.ThrowableInitCause", category="general", suppressWarnings="ThrowableInitCause")
public class ThrowableInitCause {

    public static final boolean STRICT_DEFAULT = false;
    @BooleanOption(displayName = "#LBL_org.netbeans.modules.java.hints.jdk.ThrowableInitCause.STRICT_KEY", tooltip = "#TP_org.netbeans.modules.java.hints.jdk.ThrowableInitCause.STRICT_KEY", defaultValue=STRICT_DEFAULT)
    public static final String STRICT_KEY = "strict";

    @TriggerPatterns({
        @TriggerPattern(value="($exc) new $exc($str).initCause($del)",
                        constraints={@ConstraintVariableType(variable="$str", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$del", type="java.lang.Throwable")}),
        @TriggerPattern(value="($exc) new $exc().initCause($del)",
                        constraints={@ConstraintVariableType(variable="$del", type="java.lang.Throwable")})
    })
    public static ErrorDescription expression(HintContext ctx) {
        return initCause(ctx, false);
    }

    @TriggerPatterns({
        @TriggerPattern(value="$exc $excVar = new $exc($str); $excVar.initCause($del); throw $excVar;",
                        constraints={@ConstraintVariableType(variable="$str", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$del", type="java.lang.Throwable")}),
        @TriggerPattern(value="final $exc $excVar = new $exc($str); $excVar.initCause($del); throw $excVar;",
                        constraints={@ConstraintVariableType(variable="$str", type="java.lang.String"),
                                     @ConstraintVariableType(variable="$del", type="java.lang.Throwable")}),
        @TriggerPattern(value="$exc $excVar = new $exc(); $excVar.initCause($del); throw $excVar;",
                        constraints={@ConstraintVariableType(variable="$del", type="java.lang.Throwable")}),
        @TriggerPattern(value="final $exc $excVar = new $exc(); $excVar.initCause($del); throw $excVar;",
                        constraints={@ConstraintVariableType(variable="$del", type="java.lang.Throwable")})
    })
    public static ErrorDescription variable(HintContext ctx) {
        return initCause(ctx, true);
    }

    private static ErrorDescription initCause(HintContext ctx, boolean toThrow) {
        TypeElement throwable = ctx.getInfo().getElements().getTypeElement("java.lang.Throwable");

        if (throwable == null) return null;

        TreePath exc = ctx.getVariables().get("$exc");
        TypeMirror excType = ctx.getInfo().getTrees().getTypeMirror(exc);
        Types t = ctx.getInfo().getTypes();

        if (!t.isSubtype(t.erasure(excType), t.erasure(throwable.asType()))) {
            return null;
        }

        Element el = t.asElement(excType);

        if (el == null || el.getKind() != ElementKind.CLASS) {
            //should not happen
            return null;
        }

        List<TypeMirror> constrParams = new LinkedList<TypeMirror>();
        TreePath str = ctx.getVariables().get("$str");
        String target;

        if (   (str != null && (   MatcherUtilities.matches(ctx, str, "$del.toString()")
                                || (    MatcherUtilities.matches(ctx, str, "$del.getMessage()")
                                    && !ctx.getPreferences().getBoolean(STRICT_KEY, STRICT_DEFAULT))
                                || (    MatcherUtilities.matches(ctx, str, "$del.getLocalizedMessage()")
                                    && !ctx.getPreferences().getBoolean(STRICT_KEY, STRICT_DEFAULT)))
            || (str == null && !ctx.getPreferences().getBoolean(STRICT_KEY, STRICT_DEFAULT)))) {
            target = "new $exc($del)";
        } else {
            TypeElement jlString = ctx.getInfo().getElements().getTypeElement("java.lang.String");

            if (jlString == null) return null;

            constrParams.add(jlString.asType());

            if (str != null) {
                target = "new $exc($str, $del)";
            } else {
                target = "new $exc(null, $del)"; //TODO: might lead to incompilable code (for overloaded constructors)
            }
        }

        if (toThrow) {
            target = "throw " + target + ";";
        }

        TreePath del = ctx.getVariables().get("$del");
        TypeMirror delType = ctx.getInfo().getTrees().getTypeMirror(del);

        constrParams.add(delType);

        if (!findConstructor(el, t, constrParams)) return null;

        String fixDisplayName = NbBundle.getMessage(ThrowableInitCause.class, "FIX_ThrowableInitCause");
        String displayName = NbBundle.getMessage(ThrowableInitCause.class, "ERR_ThrowableInitCause");
        TreePath toUnderline = ctx.getVariables().get("$excVar");

        if (toUnderline == null) {
            toUnderline = ctx.getPath();
        }

        return ErrorDescriptionFactory.forTree(ctx, toUnderline, displayName, JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), target));
    }

    private static boolean findConstructor(Element el, Types t, List<TypeMirror> paramTypes) {
        boolean found = false;
        OUTER: for (ExecutableElement ee : ElementFilter.constructorsIn(el.getEnclosedElements())) {
            if (ee.isVarArgs() || ee.getParameters().size() != paramTypes.size()) {
                continue;
            }

            Iterator<? extends VariableElement> p = ee.getParameters().iterator();
            Iterator<TypeMirror> expectedType = paramTypes.iterator();

            while (p.hasNext() && expectedType.hasNext()) {
                if (!t.isAssignable(expectedType.next(), p.next().asType())) {
                    continue OUTER;
                }
            }

            found = true;
            break;
        }

        return found;
    }

}
