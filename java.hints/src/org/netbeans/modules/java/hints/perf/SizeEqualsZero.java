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
import org.netbeans.spi.java.hints.JavaFix;
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
    })
    public static ErrorDescription sizeEqualsZero(HintContext ctx) {
        return sizeEqualsZeroHint(ctx, false);
    }

    @TriggerPatterns({
        @TriggerPattern(value="$subj.size() != 0", 
            constraints = @ConstraintVariableType(type = "java.util.Collection", variable = "$subj")),
        @TriggerPattern(value="$subj.size() != 0", 
            constraints = @ConstraintVariableType(type = "java.util.Map", variable = "$subj")),
    })
    public static ErrorDescription sizeNotEqualsZero(HintContext ctx) {
        if (!ctx.getPreferences().getBoolean(CHECK_NOT_EQUALS, CHECK_NOT_EQUALS_DEFAULT)) {
            return null;
        }
        return sizeEqualsZeroHint(ctx, true);
    }

    public static ErrorDescription sizeEqualsZeroHint(HintContext ctx, boolean not) {
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

        String fixDisplayName = NbBundle.getMessage(SizeEqualsZero.class, not ? "FIX_UseIsEmptyNeg" : "FIX_UseIsEmpty");
        Fix f = JavaFixUtilities.rewriteFix(ctx, fixDisplayName, ctx.getPath(), not ? "!$subj.isEmpty()" : "$subj.isEmpty()");
        String displayName = NbBundle.getMessage(SizeEqualsZero.class, not ? "ERR_SizeEqualsZeroNeg" : "ERR_SizeEqualsZero");
        return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), displayName, f);
    }

}
