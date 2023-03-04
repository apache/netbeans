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

import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.java.hints.introduce.TreeUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ConstraintVariableType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.SizeEqualsZero", description = "#DESC_org.netbeans.modules.java.hints.perf.SizeEqualsZero", category="performance", suppressWarnings="SizeReplaceableByIsEmpty")
public class SizeEqualsZero {

    static final boolean CHECK_NOT_EQUALS_DEFAULT = true;
    
    @BooleanOption(displayName = "#LBL_org.netbeans.modules.java.hints.perf.SizeEqualsZero.CHECK_NOT_EQUALS", tooltip = "#TP_org.netbeans.modules.java.hints.perf.SizeEqualsZero.CHECK_NOT_EQUALS", defaultValue=CHECK_NOT_EQUALS_DEFAULT)
    public static final String CHECK_NOT_EQUALS = "check.not.equals";

    @TriggerPatterns({
        @TriggerPattern(value="$subj.size() == 0", 
            constraints = @ConstraintVariableType(type = "java.util.Collection", variable = "$subj")),
        @TriggerPattern(value="$subj.size() == 0", 
            constraints = @ConstraintVariableType(type = "java.util.Map", variable = "$subj")),
        @TriggerPattern(value="0 == $subj.size()",
            constraints = @ConstraintVariableType(type = "java.util.Collection", variable = "$subj")),
        @TriggerPattern(value="0 == $subj.size()",
            constraints = @ConstraintVariableType(type = "java.util.Map", variable = "$subj")),
    })
    public static ErrorDescription sizeEqualsZero(HintContext ctx) {
        return sizeEqualsZeroHint(ctx, "SizeEqualsZero", "$subj.isEmpty()"); // NOI18N
    }

    @TriggerPatterns({
        @TriggerPattern(value="$subj.size() != 0", 
            constraints = @ConstraintVariableType(type = "java.util.Collection", variable = "$subj")),
        @TriggerPattern(value="$subj.size() != 0", 
            constraints = @ConstraintVariableType(type = "java.util.Map", variable = "$subj")),
        @TriggerPattern(value="0 != $subj.size()",
            constraints = @ConstraintVariableType(type = "java.util.Collection", variable = "$subj")),
        @TriggerPattern(value="0 != $subj.size()",
            constraints = @ConstraintVariableType(type = "java.util.Map", variable = "$subj")),
    })
    public static ErrorDescription sizeNotEqualsZero(HintContext ctx) {
        if (!ctx.getPreferences().getBoolean(CHECK_NOT_EQUALS, CHECK_NOT_EQUALS_DEFAULT)) {
            return null;
        }
        return sizeEqualsZeroHint(ctx, "SizeEqualsZeroNeg", "!$subj.isEmpty()"); // NOI18N
    }

    @TriggerPatterns({
        @TriggerPattern(value="$subj.size() > 0", 
            constraints = @ConstraintVariableType(type = "java.util.Collection", variable = "$subj")),
        @TriggerPattern(value="$subj.size() > 0", 
            constraints = @ConstraintVariableType(type = "java.util.Map", variable = "$subj")),
        @TriggerPattern(value="0 < $subj.size()",
            constraints = @ConstraintVariableType(type = "java.util.Collection", variable = "$subj")),
        @TriggerPattern(value="0 < $subj.size()",
            constraints = @ConstraintVariableType(type = "java.util.Map", variable = "$subj")),
    })
    public static ErrorDescription sizeGreaterZero(HintContext ctx) {
        if (!ctx.getPreferences().getBoolean(CHECK_NOT_EQUALS, CHECK_NOT_EQUALS_DEFAULT)) {
            return null;
        }
        return sizeEqualsZeroHint(ctx, "SizeGreaterZeroNeg", "!$subj.isEmpty()"); // NOI18N
    }

    public static ErrorDescription sizeEqualsZeroHint(HintContext ctx, String keyPostfix, String to) {
        TreePath subj = ctx.getVariables().get("$subj");
        if (subj == null) {
            // assume implicit this
            subj = TreeUtils.findClass(ctx.getPath());
        }
        TypeMirror subjType = ctx.getInfo().getTrees().getTypeMirror(subj);

        if (subjType == null || subjType.getKind() != TypeKind.DECLARED) {
            return null;
        }
        
        Element el = ((DeclaredType) subjType).asElement();

        if (el == null || (!el.getKind().isClass() && !el.getKind().isInterface())) {
            return null;
        }

        Element isEmptyFound = null;

        
        for (ExecutableElement method : ElementFilter.methodsIn(ctx.getInfo().getElementUtilities().getMembers(subjType, null))) {
            if (method.getSimpleName().contentEquals("isEmpty") && method.getParameters().isEmpty() && method.getTypeParameters().isEmpty()) { // NOI18N
                isEmptyFound = method;
                break;
            }
        }

        if (isEmptyFound == null) {
            return null;
        }
        
        // #247190: check that the replacement is NOT done in the isEmpty method of the target type itself
        TreePath enclMethod = org.netbeans.modules.java.hints.errors.Utilities.findOwningExecutable(ctx, ctx.getPath(), false);
        if (enclMethod != null) {
            Element enclMethodEl = ctx.getInfo().getTrees().getElement(enclMethod);
            if (enclMethodEl == isEmptyFound) {
                return null;
            }
        }
        
        String subjName = subj.getLeaf().toString();
        String fixDisplayName = NbBundle.getMessage(SizeEqualsZero.class, "FIX_" + keyPostfix, subjName); // NOI18N
        String errDisplayName = NbBundle.getMessage(SizeEqualsZero.class, "ERR_" + keyPostfix, subjName); // NOI18N
        
        Fix fix = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), to);
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), errDisplayName, fix);
    }

}
